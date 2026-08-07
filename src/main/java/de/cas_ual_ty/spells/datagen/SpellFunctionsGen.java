package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellFunctions;
import de.cas_ual_ty.spells.spell.SpellFunction;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.control.ConditionalDeactivationAction;
import de.cas_ual_ty.spells.spell.action.cooldown.GetCooldownAction;
import de.cas_ual_ty.spells.spell.action.cooldown.SetCooldownAction;
import de.cas_ual_ty.spells.spell.action.item.PlayerHasItemsAction;
import de.cas_ual_ty.spells.spell.action.item.TryConsumePlayerItemsAction;
import de.cas_ual_ty.spells.spell.action.mana.BurnManaAction;
import de.cas_ual_ty.spells.spell.action.mana.HasManaAction;
import de.cas_ual_ty.spells.spell.action.mana.TryBurnManaAction;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

import static de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups.OWNER;
import static de.cas_ual_ty.spells.spell.context.BuiltinVariables.MANA_COST;

/**
 * Reusable "pay a cost" functions covering every non-empty combination of the mod's 3 cost types (mana, items,
 * cooldown). Each function is entered/exited on its own internal "active" activation - the caller maps whichever
 * activation is currently active to "active" (and back) via {@code call_function}'s activations map. If every
 * relevant cost can be paid, "active" stays active for the whole run and the costs are actually deducted; if any
 * one of them can't be paid, "active" gets deactivated partway through and nothing further in the function runs -
 * including any not-yet-reached consume step - so a failed combined check never partially consumes one cost
 * while failing on another.
 * <p>
 * Item costs specifically also respect the {@code item_costs} config toggle (see {@code SpellsConfig#GLOBAL_ITEM_COSTS}):
 * when it's off, the effective item amount required/consumed is substituted down to {@code 0} (both
 * {@code PlayerHasItemsAction} and {@code TryConsumePlayerItemsAction} treat "need/consume 0 items" as an
 * unconditional no-op pass, verified against their own matching logic), rather than skipping either action
 * outright - this mod's control flow only supports jumping backward to an already-visited label, so a
 * conditional skip-ahead is not available and isn't needed here anyway.
 * <p>
 * Mana and cooldown costs both declare real parameters ({@code mana_cost}, {@link #DURATION}) with fixed literal
 * defaults ({@link #DEFAULT_MANA_COST}, {@link #DEFAULT_COOLDOWN_DURATION}). A {@link de.cas_ual_ty.spells.spell.variable.CtxVar}
 * parameter default can only be a fixed literal, not a reference to another variable, and is applied
 * unconditionally after the caller's rename-in - so mapping a custom value into either name has no effect, the
 * declared default always wins. The mana parameter deliberately reuses the ambient {@code mana_cost} builtin's
 * own name rather than a separate one; since the default always wins regardless, this doesn't let a caller pass
 * the spell's real mana cost through either, but it does mean calling one of these functions leaves
 * {@code mana_cost} at {@link #DEFAULT_MANA_COST} for the rest of the host spell's run, not just inside the
 * function - see {@link #manaAmountDefault()}.
 */
public class SpellFunctionsGen
{
    public static final String PAY = "active";

    public static final CtxVarType<Integer> INT = CtxVarTypes.INT.get();
    public static final CtxVarType<Double> DOUBLE = CtxVarTypes.DOUBLE.get();
    public static final CtxVarType<Boolean> BOOLEAN = CtxVarTypes.BOOLEAN.get();
    public static final CtxVarType<CompoundTag> TAG = CtxVarTypes.TAG.get();
    public static final CtxVarType<String> STRING = CtxVarTypes.STRING.get();

    public static final DynamicCtxVar<Boolean> TRUE = BOOLEAN.immediate(true);

    // parameter names - what the caller's `variables`/`activations` input maps translate into internally
    public static final String ITEM = "item";
    public static final String ITEM_AMOUNT = "item_amount";
    public static final String ITEM_TAG = "item_tag";
    public static final String MUST_BE_IN_HAND = "must_be_in_hand";
    public static final String SLOT = "slot";
    public static final String DURATION = "duration";

    // default cost parameters - declared on the SpellFunction itself, applied after any caller rename-in
    public static final double DEFAULT_MANA_COST = 5.0;
    public static final int DEFAULT_COOLDOWN_DURATION = 100; // 5 seconds

    protected String modId;
    protected final BootstrapContext<SpellFunction> context;

    public SpellFunctionsGen(String modId, BootstrapContext<SpellFunction> context)
    {
        this.modId = modId;
        this.context = context;
        addSpellFunctions();
    }

    public void addFunction(String key, List<CtxVar<?>> parameters, List<SpellAction> actions)
    {
        context.register(ResourceKey.create(SpellFunctions.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(modId, key)), new SpellFunction(parameters, actions));
    }

    public void addFunction(String key, List<SpellAction> actions)
    {
        addFunction(key, new LinkedList<>(), actions);
    }

    public void addSpellFunctions()
    {
        addFunction("check_mana_cost", manaAmountDefault(), checkManaCost());
        addFunction("check_item_cost", checkItemCost());
        addFunction("check_cooldown_cost", cooldownDurationDefault(), checkCooldownCost());
        addFunction("check_mana_and_item_cost", manaAmountDefault(), checkManaAndItemCost());
        addFunction("check_mana_and_cooldown_cost", combine(manaAmountDefault(), cooldownDurationDefault()), checkManaAndCooldownCost());
        addFunction("check_item_and_cooldown_cost", cooldownDurationDefault(), checkItemAndCooldownCost());
        addFunction("check_mana_and_item_and_cooldown_cost", combine(manaAmountDefault(), cooldownDurationDefault()), checkManaAndItemAndCooldownCost());
    }

    // ----- mana only -----

    protected List<SpellAction> checkManaCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(TryBurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        return actions;
    }

    // ----- items only -----

    protected List<SpellAction> checkItemCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(TryConsumePlayerItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- cooldown only -----

    protected List<SpellAction> checkCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        addCooldownCheck(actions);
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SLOT), INT.reference(DURATION)));
        return actions;
    }

    // ----- mana + items -----

    protected List<SpellAction> checkManaAndItemCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        actions.add(PlayerHasItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(BurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        actions.add(TryConsumePlayerItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- mana + cooldown -----

    protected List<SpellAction> checkManaAndCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        addCooldownCheck(actions);
        actions.add(BurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SLOT), INT.reference(DURATION)));
        return actions;
    }

    // ----- items + cooldown -----

    protected List<SpellAction> checkItemAndCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        addCooldownCheck(actions);
        actions.add(PlayerHasItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SLOT), INT.reference(DURATION)));
        actions.add(TryConsumePlayerItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- mana + items + cooldown -----

    protected List<SpellAction> checkManaAndItemAndCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        addCooldownCheck(actions);
        actions.add(PlayerHasItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(BurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SLOT), INT.reference(DURATION)));
        actions.add(TryConsumePlayerItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- shared building blocks -----

    /**
     * Appends a check for whether the {@code slot} cooldown has run out, deactivating {@link #PAY} if not.
     * Does not consume anything - pair with a {@code SetCooldownAction} once every other cost has also
     * been confirmed.
     */
    protected void addCooldownCheck(List<SpellAction> actions)
    {
        actions.add(GetCooldownAction.make(PAY, OWNER, INT.reference(SLOT), "cooldown_remaining"));
        actions.add(ConditionalDeactivationAction.make(PAY, Compiler.compileString(" cooldown_remaining <= 0 ", BOOLEAN)));
    }

    /**
     * Default {@link #DURATION} of {@link #DEFAULT_COOLDOWN_DURATION} ticks, declared as a {@link SpellFunction}
     * parameter. Since parameter defaults unconditionally overwrite whatever the caller's rename-in already
     * placed under the same internal name, mapping a custom value into {@link #DURATION} has no effect on these
     * functions - the default always wins.
     */
    protected List<CtxVar<?>> cooldownDurationDefault()
    {
        List<CtxVar<?>> parameters = new LinkedList<>();
        parameters.add(new CtxVar<>(INT, DURATION, DEFAULT_COOLDOWN_DURATION));
        return parameters;
    }

    /**
     * Default {@code mana_cost} of {@link #DEFAULT_MANA_COST}, declared as a {@link SpellFunction} parameter
     * reusing the same name as the ambient {@code mana_cost} builtin (deliberately - so a caller wanting the
     * function to use the spell's real mana cost never needs a variables mapping just to pass it through, it's
     * already sitting under that name). Because parameter defaults are applied unconditionally, this default
     * overwrites {@code mana_cost} on every call regardless of what was already there - the real ambient value
     * included - so the function always spends exactly {@link #DEFAULT_MANA_COST} unless a caller edits this
     * default directly; no variables-map trick can preserve or override it, same as {@link #cooldownDurationDefault()}.
     * Because the name is shared and the context isn't isolated, {@code mana_cost} is left at
     * {@link #DEFAULT_MANA_COST} for the rest of the host spell's run after the call too, not just inside it.
     */
    protected List<CtxVar<?>> manaAmountDefault()
    {
        List<CtxVar<?>> parameters = new LinkedList<>();
        parameters.add(new CtxVar<>(DOUBLE, MANA_COST.name, DEFAULT_MANA_COST));
        return parameters;
    }

    @SafeVarargs
    protected final List<CtxVar<?>> combine(List<CtxVar<?>>... parameterLists)
    {
        List<CtxVar<?>> parameters = new LinkedList<>();
        for(List<CtxVar<?>> parameterList : parameterLists)
        {
            parameters.addAll(parameterList);
        }
        return parameters;
    }

    /**
     * The item amount to actually require/consume: the real {@link #ITEM_AMOUNT} parameter while item costs
     * are globally enabled, or {@code 0} (an unconditional no-op for both the check and the consume side)
     * while they're disabled.
     */
    protected DynamicCtxVar<Integer> effectiveItemAmount()
    {
        return Compiler.compileString(" item_costs() ? " + ITEM_AMOUNT + " : 0 ", INT);
    }

    public String getName()
    {
        return "Spells & Shields Spell Function Files";
    }
}

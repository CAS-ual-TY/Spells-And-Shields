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
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

import static de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups.OWNER;

/**
 * Reusable "pay a cost" functions covering every non-empty combination of the mod's 3 cost types (mana, items,
 * cooldown). Each function is entered/exited on its own internal "pay" activation - the caller maps whichever
 * activation is currently active to "pay" (and back) via {@code call_function}'s activations map. If every
 * relevant cost can be paid, "pay" stays active for the whole run and the costs are actually deducted; if any
 * one of them can't be paid, "pay" gets deactivated partway through and nothing further in the function runs -
 * including any not-yet-reached consume step - so a failed combined check never partially consumes one cost
 * while failing on another.
 * <p>
 * Item costs specifically also respect the {@code item_costs} config toggle (see {@code SpellsConfig#GLOBAL_ITEM_COSTS}):
 * when it's off, the effective item amount required/consumed is substituted down to {@code 0} (both
 * {@code PlayerHasItemsAction} and {@code TryConsumePlayerItemsAction} treat "need/consume 0 items" as an
 * unconditional no-op pass, verified against their own matching logic), rather than skipping either action
 * outright - this mod's control flow only supports jumping backward to an already-visited label, so a
 * conditional skip-ahead is not available and isn't needed here anyway.
 */
public class SpellFunctionsGen
{
    public static final String PAY = "pay";

    public static final CtxVarType<Integer> INT = CtxVarTypes.INT.get();
    public static final CtxVarType<Double> DOUBLE = CtxVarTypes.DOUBLE.get();
    public static final CtxVarType<Boolean> BOOLEAN = CtxVarTypes.BOOLEAN.get();
    public static final CtxVarType<CompoundTag> TAG = CtxVarTypes.TAG.get();
    public static final CtxVarType<String> STRING = CtxVarTypes.STRING.get();

    public static final DynamicCtxVar<Boolean> TRUE = BOOLEAN.immediate(true);

    // parameter names - what the caller's `variables`/`activations` input maps translate into internally
    public static final String MANA_AMOUNT = "mana_amount";
    public static final String ITEM = "item";
    public static final String ITEM_AMOUNT = "item_amount";
    public static final String ITEM_TAG = "item_tag";
    public static final String MUST_BE_IN_HAND = "must_be_in_hand";
    public static final String SLOT = "slot";
    public static final String DURATION = "duration";

    protected String modId;
    protected final BootstrapContext<SpellFunction> context;

    public SpellFunctionsGen(String modId, BootstrapContext<SpellFunction> context)
    {
        this.modId = modId;
        this.context = context;
        addSpellFunctions();
    }

    public void addFunction(String key, List<SpellAction> actions)
    {
        context.register(ResourceKey.create(SpellFunctions.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(modId, key)), new SpellFunction(actions));
    }

    public void addSpellFunctions()
    {
        addFunction("check_mana_cost", checkManaCost());
        addFunction("check_item_cost", checkItemCost());
        addFunction("check_cooldown_cost", checkCooldownCost());
        addFunction("check_mana_and_item_cost", checkManaAndItemCost());
        addFunction("check_mana_and_cooldown_cost", checkManaAndCooldownCost());
        addFunction("check_item_and_cooldown_cost", checkItemAndCooldownCost());
        addFunction("check_mana_and_item_and_cooldown_cost", checkManaAndItemAndCooldownCost());
    }

    // ----- mana only -----

    protected List<SpellAction> checkManaCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(TryBurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_AMOUNT)));
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
        actions.add(HasManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_AMOUNT)));
        actions.add(PlayerHasItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(BurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_AMOUNT)));
        actions.add(TryConsumePlayerItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- mana + cooldown -----

    protected List<SpellAction> checkManaAndCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_AMOUNT)));
        addCooldownCheck(actions);
        actions.add(BurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_AMOUNT)));
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
        actions.add(HasManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_AMOUNT)));
        addCooldownCheck(actions);
        actions.add(PlayerHasItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(BurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_AMOUNT)));
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

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
import static de.cas_ual_ty.spells.spell.context.BuiltinVariables.SPELL_SLOT;

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
 * Every declared parameter's default is applied via {@code initCtxVarIfAbsent} (see
 * {@link de.cas_ual_ty.spells.spell.context.SpellContext#runNestedActions}) - only where nothing is already
 * present under that name - so a caller's explicit {@code call_function} {@code parameters} override, or an
 * already-populated ambient value, always takes precedence over the default. Mana costs declare a parameter
 * under the ambient {@code mana_cost} builtin's own name: for a normal spell call, {@code mana_cost} is already
 * sitting there before the function ever runs, so {@link #DEFAULT_MANA_COST} never actually fires and the
 * function just uses the spell's real cost automatically, no override needed. The cooldown slot similarly
 * reuses the ambient {@code spell_slot} builtin directly, with no declared parameter/default at all. Item costs
 * ({@link #ITEM}/{@link #ITEM_AMOUNT}/{@link #MUST_BE_IN_HAND}) and {@link #DURATION} (cooldown length) have no
 * ambient builtin to fall back to, so their defaults ({@link #DEFAULT_ITEM}, {@link #DEFAULT_ITEM_AMOUNT},
 * {@link #DEFAULT_MUST_BE_IN_HAND}, {@link #DEFAULT_COOLDOWN_DURATION}) always apply unless a caller overrides
 * them - {@link #ITEM} in particular is expected to always be overridden by real spells.
 * <p>
 * Besides the atomic check-and-pay combinations above, {@link #addSpellFunctions()} also registers single-step
 * check-only ({@code has_*_cost}) and consume-only ({@code burn_mana_cost}/{@code consume_item_cost}/
 * {@code set_cooldown_cost}) functions for spells that check a cost up front (on their own "active"-mapped
 * activation) but only actually spend it later, on a different activation (eg. a projectile that only burns
 * mana/consumes its reagent once it actually hits something) - the combined functions above pay everything
 * atomically in one call and don't fit that split-timing pattern.
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
    public static final String DURATION = "duration";

    // default cost parameters - declared on the SpellFunction itself, applied after any caller rename-in
    public static final double DEFAULT_MANA_COST = 5.0;
    public static final int DEFAULT_COOLDOWN_DURATION = 100; // 5 seconds
    public static final String DEFAULT_ITEM = "minecraft:lapis_lazuli";
    public static final int DEFAULT_ITEM_AMOUNT = 1;
    public static final boolean DEFAULT_MUST_BE_IN_HAND = true;

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
        addFunction("check_item_cost", itemCostDefault(), checkItemCost());
        addFunction("check_cooldown_cost", cooldownDurationDefault(), checkCooldownCost());
        addFunction("check_mana_and_item_cost", combine(manaAmountDefault(), itemCostDefault()), checkManaAndItemCost());
        addFunction("check_mana_and_cooldown_cost", combine(manaAmountDefault(), cooldownDurationDefault()), checkManaAndCooldownCost());
        addFunction("check_item_and_cooldown_cost", combine(itemCostDefault(), cooldownDurationDefault()), checkItemAndCooldownCost());
        addFunction("check_mana_and_item_and_cooldown_cost", combine(manaAmountDefault(), itemCostDefault(), cooldownDurationDefault()), checkManaAndItemAndCooldownCost());

        // single-step check-only / consume-only functions, for spells that check a cost up front but only
        // actually spend it later on a different activation
        addFunction("has_mana_cost", manaAmountDefault(), hasManaCost());
        addFunction("burn_mana_cost", manaAmountDefault(), burnManaCost());
        addFunction("has_item_cost", itemCostDefault(), hasItemCost());
        addFunction("consume_item_cost", itemCostDefault(), consumeItemCost());
        addFunction("has_cooldown_cost", hasCooldownCost());
        addFunction("set_cooldown_cost", cooldownDurationDefault(), setCooldownCost());
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
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
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
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
        return actions;
    }

    // ----- items + cooldown -----

    protected List<SpellAction> checkItemAndCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        addCooldownCheck(actions);
        actions.add(PlayerHasItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
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
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
        actions.add(TryConsumePlayerItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- single-step: mana only -----

    protected List<SpellAction> hasManaCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        return actions;
    }

    protected List<SpellAction> burnManaCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(BurnManaAction.make(PAY, OWNER, DOUBLE.reference(MANA_COST.name)));
        return actions;
    }

    // ----- single-step: items only -----

    /**
     * Unlike {@link #effectiveItemAmount()}-based checks elsewhere in this class, this always requires the full
     * {@link #ITEM_AMOUNT} regardless of the {@code item_costs} toggle - matching the established convention in
     * existing spells, where the item must physically be in hand either way and only the later consume step
     * respects the toggle. See {@link #consumeItemCost()}.
     */
    protected List<SpellAction> hasItemCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(PlayerHasItemsAction.make(PAY, OWNER, STRING.reference(ITEM), INT.reference(ITEM_AMOUNT), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        return actions;
    }

    protected List<SpellAction> consumeItemCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(TryConsumePlayerItemsAction.make(PAY, OWNER, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- single-step: cooldown only -----

    protected List<SpellAction> hasCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        addCooldownCheck(actions);
        return actions;
    }

    protected List<SpellAction> setCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(SetCooldownAction.make(PAY, OWNER, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
        return actions;
    }

    // ----- shared building blocks -----

    /**
     * Appends a check for whether the ambient {@code spell_slot}'s cooldown has run out, deactivating
     * {@link #PAY} if not. Does not consume anything - pair with a {@code SetCooldownAction} once every other
     * cost has also been confirmed.
     */
    protected void addCooldownCheck(List<SpellAction> actions)
    {
        actions.add(GetCooldownAction.make(PAY, OWNER, INT.reference(SPELL_SLOT.name), "cooldown_remaining"));
        actions.add(ConditionalDeactivationAction.make(PAY, Compiler.compileString(" cooldown_remaining <= 0 ", BOOLEAN)));
    }

    /**
     * Fallback {@link #DURATION} of {@link #DEFAULT_COOLDOWN_DURATION} ticks, declared as a {@link SpellFunction}
     * parameter - applied only if nothing is already present under that name (see
     * {@link de.cas_ual_ty.spells.spell.context.SpellContext#runNestedActions}), so a caller mapping a custom
     * value into {@link #DURATION} via {@code variables} takes precedence over this default automatically.
     */
    protected List<CtxVar<?>> cooldownDurationDefault()
    {
        List<CtxVar<?>> parameters = new LinkedList<>();
        parameters.add(new CtxVar<>(INT, DURATION, DEFAULT_COOLDOWN_DURATION));
        return parameters;
    }

    /**
     * Fallback {@code mana_cost} of {@link #DEFAULT_MANA_COST}, declared as a {@link SpellFunction} parameter
     * reusing the same name as the ambient {@code mana_cost} builtin (deliberately - the whole point is that a
     * normal spell call never needs to touch this at all). Since it's only applied if {@code mana_cost} isn't
     * already present, and a running spell's context already has the real value sitting there before the
     * function ever runs, this default is dead weight for the common case and only matters as a safety net for
     * some hypothetical call site with no ambient {@code mana_cost} at all.
     */
    protected List<CtxVar<?>> manaAmountDefault()
    {
        List<CtxVar<?>> parameters = new LinkedList<>();
        parameters.add(new CtxVar<>(DOUBLE, MANA_COST.name, DEFAULT_MANA_COST));
        return parameters;
    }

    /**
     * Fallback {@link #ITEM}/{@link #ITEM_AMOUNT}/{@link #MUST_BE_IN_HAND}/{@link #ITEM_TAG} of
     * {@link #DEFAULT_ITEM} (1, in hand, no tag), declared as {@link SpellFunction} parameters - same if-absent
     * semantics as {@link #cooldownDurationDefault()}. Unlike {@code mana_cost}/{@code spell_slot}, none of these
     * have an ambient builtin to fall back to (every spell's required item is entirely spell-specific), so this
     * default is just a sensible placeholder - real spells are expected to always override {@link #ITEM} at
     * minimum via {@code call_function}'s own {@code parameters} list. {@link #ITEM_TAG} defaults to an empty tag
     * rather than being left undeclared, so it actually shows up as a parameter - both {@code PlayerHasItemsAction}
     * and {@code TryConsumePlayerItemsAction} treat an empty tag the same as no tag filter at all.
     */
    protected List<CtxVar<?>> itemCostDefault()
    {
        List<CtxVar<?>> parameters = new LinkedList<>();
        parameters.add(new CtxVar<>(STRING, ITEM, DEFAULT_ITEM));
        parameters.add(new CtxVar<>(INT, ITEM_AMOUNT, DEFAULT_ITEM_AMOUNT));
        parameters.add(new CtxVar<>(BOOLEAN, MUST_BE_IN_HAND, DEFAULT_MUST_BE_IN_HAND));
        parameters.add(new CtxVar<>(TAG, ITEM_TAG, new CompoundTag()));
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

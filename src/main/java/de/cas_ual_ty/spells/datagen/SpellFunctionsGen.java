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
import java.util.Map;

import static de.cas_ual_ty.spells.spell.context.BuiltinEvents.ACTIVE;
import static de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups.OWNER;
import static de.cas_ual_ty.spells.spell.context.BuiltinVariables.MANA_COST;
import static de.cas_ual_ty.spells.spell.context.BuiltinVariables.SPELL_SLOT;

/**
 * Reusable "pay a cost" functions covering every non-empty combination of the mod's 3 cost types (mana, items,
 * cooldown). Each function is entered/exited on its own internal {@link #ACTIVE_INTERNAL} activation, working against its
 * own internal {@link #OWNER_INTERNAL} target group - both purely internal names (underscore-prefixed) with no
 * meaning to any host, translated via {@link SpellFunction#getDefaultActivations()}/{@link SpellFunction#getDefaultTargets()}
 * ({@link #DEFAULT_ACTIVATIONS}/{@link #DEFAULT_TARGETS}, attached to every function by {@link #addFunction}) -
 * a caller who supplies no {@code activations}/{@code targets} map of their own gets these defaults instead,
 * which map the conventional host names ({@code active}, {@code owner}) onto the function's internal ones. If
 * every relevant cost can be paid, {@link #ACTIVE_INTERNAL} stays active for the whole run and the costs are actually
 * deducted; if any one of them can't be paid, {@link #ACTIVE_INTERNAL} gets deactivated partway through and nothing further
 * in the function runs - including any not-yet-reached consume step - so a failed combined check never partially
 * consumes one cost while failing on another.
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
 * already-populated (ambient or rename-in'd) value, always takes precedence over the default. Mana costs work
 * against {@link #MANA_COST_INTERNAL}, a purely internal name like {@link #ACTIVE_INTERNAL}/{@link #OWNER_INTERNAL} -
 * a caller who leaves {@code variables} empty gets {@link #DEFAULT_VARIABLES_MANA} instead, translating the
 * ambient {@code mana_cost} builtin onto it, so a normal spell call never needs to map or override anything.
 * There's deliberately no declared parameter/default for it at all (unlike {@link #DURATION}/{@link #ITEM} etc.
 * below) - the mapping alone is enough, since a running spell always has the real {@code mana_cost} to map in.
 * The cooldown slot, similarly, is hardcoded straight into the relevant actions as the ambient {@code spell_slot}
 * builtin, with no internal name, no default mapping, and no parameter at all - there's no reason to expose it
 * as one, nothing ever needs to override it. Item costs ({@link #ITEM}/{@link #ITEM_AMOUNT}/{@link #MUST_BE_IN_HAND})
 * and {@link #DURATION} (cooldown length) have no ambient builtin to fall back to, so their defaults
 * ({@link #DEFAULT_ITEM}, {@link #DEFAULT_ITEM_AMOUNT}, {@link #DEFAULT_MUST_BE_IN_HAND},
 * {@link #DEFAULT_COOLDOWN_DURATION}) always apply unless a caller overrides them - {@link #ITEM} in particular
 * is expected to always be overridden by real spells.
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
    public static final String ACTIVE_INTERNAL = "_active";
    public static final String OWNER_INTERNAL = "_owner";
    public static final String MANA_COST_INTERNAL = "_mana_cost";

    // attached to every function via addFunction() - a caller who leaves its own activations/targets map empty
    // falls back to these, translating the conventional host names onto this class's internal ones
    public static final Map<String, String> DEFAULT_ACTIVATIONS = Map.of(ACTIVE.toString(), ACTIVE_INTERNAL);
    public static final Map<String, String> DEFAULT_TARGETS = Map.of(OWNER.toString(), OWNER_INTERNAL);
    // only attached to functions that actually use mana - see addFunction(key, parameters, defaultVariables, actions)
    public static final Map<String, String> DEFAULT_VARIABLES_MANA = Map.of(MANA_COST.name, MANA_COST_INTERNAL);

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

    public void addFunction(ResourceLocation key, List<CtxVar<?>> parameters, Map<String, String> defaultVariables, List<SpellAction> actions)
    {
        context.register(ResourceKey.create(SpellFunctions.REGISTRY_KEY, key), new SpellFunction(parameters, actions, DEFAULT_ACTIVATIONS, defaultVariables, DEFAULT_TARGETS));
    }

    public void addFunction(ResourceLocation key, List<CtxVar<?>> parameters, List<SpellAction> actions)
    {
        addFunction(key, parameters, Map.of(), actions);
    }

    public void addFunction(ResourceLocation key, Map<String, String> defaultVariables, List<SpellAction> actions)
    {
        addFunction(key, new LinkedList<>(), defaultVariables, actions);
    }

    public void addFunction(ResourceLocation key, List<SpellAction> actions)
    {
        addFunction(key, new LinkedList<>(), actions);
    }

    public void addSpellFunctions()
    {
        addFunction(SpellFunctions.CHECK_MANA_COST, DEFAULT_VARIABLES_MANA, checkManaCost());
        addFunction(SpellFunctions.CHECK_ITEM_COST, itemCostDefault(), checkItemCost());
        addFunction(SpellFunctions.CHECK_COOLDOWN_COST, cooldownDurationDefault(), checkCooldownCost());
        addFunction(SpellFunctions.CHECK_MANA_AND_ITEM_COST, itemCostDefault(), DEFAULT_VARIABLES_MANA, checkManaAndItemCost());
        addFunction(SpellFunctions.CHECK_MANA_AND_COOLDOWN_COST, cooldownDurationDefault(), DEFAULT_VARIABLES_MANA, checkManaAndCooldownCost());
        addFunction(SpellFunctions.CHECK_ITEM_AND_COOLDOWN_COST, combine(itemCostDefault(), cooldownDurationDefault()), checkItemAndCooldownCost());
        addFunction(SpellFunctions.CHECK_MANA_AND_ITEM_AND_COOLDOWN_COST, combine(itemCostDefault(), cooldownDurationDefault()), DEFAULT_VARIABLES_MANA, checkManaAndItemAndCooldownCost());

        // single-step check-only / consume-only functions, for spells that check a cost up front but only
        // actually spend it later on a different activation
        addFunction(SpellFunctions.HAS_MANA_COST, DEFAULT_VARIABLES_MANA, hasManaCost());
        addFunction(SpellFunctions.BURN_MANA_COST, DEFAULT_VARIABLES_MANA, burnManaCost());
        addFunction(SpellFunctions.HAS_ITEM_COST, itemCostDefault(), hasItemCost());
        addFunction(SpellFunctions.CONSUME_ITEM_COST, itemCostDefault(), consumeItemCost());
        addFunction(SpellFunctions.HAS_COOLDOWN_COST, hasCooldownCost());
        addFunction(SpellFunctions.SET_COOLDOWN_COST, cooldownDurationDefault(), setCooldownCost());
    }

    // ----- mana only -----

    protected List<SpellAction> checkManaCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(TryBurnManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
        return actions;
    }

    // ----- items only -----

    protected List<SpellAction> checkItemCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(TryConsumePlayerItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- cooldown only -----

    protected List<SpellAction> checkCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        addCooldownCheck(actions);
        actions.add(SetCooldownAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
        return actions;
    }

    // ----- mana + items -----

    protected List<SpellAction> checkManaAndItemCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
        actions.add(PlayerHasItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(BurnManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
        actions.add(TryConsumePlayerItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- mana + cooldown -----

    protected List<SpellAction> checkManaAndCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
        addCooldownCheck(actions);
        actions.add(BurnManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
        actions.add(SetCooldownAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
        return actions;
    }

    // ----- items + cooldown -----

    protected List<SpellAction> checkItemAndCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        addCooldownCheck(actions);
        actions.add(PlayerHasItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(SetCooldownAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
        actions.add(TryConsumePlayerItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- mana + items + cooldown -----

    protected List<SpellAction> checkManaAndItemAndCooldownCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
        addCooldownCheck(actions);
        actions.add(PlayerHasItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        actions.add(BurnManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
        actions.add(SetCooldownAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
        actions.add(TryConsumePlayerItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
        return actions;
    }

    // ----- single-step: mana only -----

    protected List<SpellAction> hasManaCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(HasManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
        return actions;
    }

    protected List<SpellAction> burnManaCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(BurnManaAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, DOUBLE.reference(MANA_COST_INTERNAL)));
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
        actions.add(PlayerHasItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), INT.reference(ITEM_AMOUNT), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND), TRUE));
        return actions;
    }

    protected List<SpellAction> consumeItemCost()
    {
        List<SpellAction> actions = new LinkedList<>();
        actions.add(TryConsumePlayerItemsAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, STRING.reference(ITEM), effectiveItemAmount(), TAG.reference(ITEM_TAG), BOOLEAN.reference(MUST_BE_IN_HAND)));
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
        actions.add(SetCooldownAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, INT.reference(SPELL_SLOT.name), INT.reference(DURATION)));
        return actions;
    }

    // ----- shared building blocks -----

    /**
     * Appends a check for whether the ambient {@code spell_slot}'s cooldown has run out, deactivating
     * {@link #ACTIVE_INTERNAL} if not. Does not consume anything - pair with a {@code SetCooldownAction} once every other
     * cost has also been confirmed.
     */
    protected void addCooldownCheck(List<SpellAction> actions)
    {
        actions.add(GetCooldownAction.make(ACTIVE_INTERNAL, OWNER_INTERNAL, INT.reference(SPELL_SLOT.name), "_cooldown_remaining"));
        actions.add(ConditionalDeactivationAction.make(ACTIVE_INTERNAL, Compiler.compileString(" _cooldown_remaining <= 0 ", BOOLEAN)));
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

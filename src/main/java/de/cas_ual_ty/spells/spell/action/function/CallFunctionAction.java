package de.cas_ual_ty.spells.spell.action.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.SpellsCodecs;
import de.cas_ual_ty.spells.spell.SpellFunction;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import net.minecraft.core.Holder;

import java.util.*;

/**
 * Calls a {@link SpellFunction}: runs its actions as a contained sub-run of the current {@link SpellContext},
 * translated in and back out through the three name maps below. On entry, for every {@code (host, internal)}
 * pair, whatever the host has under {@code host} is renamed to {@code internal} so the function's own actions
 * (written purely in terms of their own internal names) can find it; on exit the same maps are walked in
 * reverse, moving whatever is left under each {@code internal} name back to its {@code host} name. Names not
 * listed in a map are left alone on both sides - the function's own internal-only names never touch the host's
 * namespace, and the host's other state is never visible inside the function.
 * <p>
 * {@link #parameters} let the caller override specific declared {@link SpellFunction#getParameters()} defaults
 * by name (in the function's own internal namespace, post-mapping) - the same "caller's value wins if present,
 * function's own default stays otherwise" pattern {@code SpellNode}'s optional parameter list already uses over
 * a {@code Spell}'s base parameters. Unlisted parameter names keep the function's own declared default (or the
 * value already present under that name, per {@link SpellContext#runNestedActions}).
 * <p>
 * Each of {@link #activations}/{@link #variables}/{@link #targets} falls back independently to the function's
 * own {@link SpellFunction#getDefaultActivations()}/{@link SpellFunction#getDefaultVariables()}/
 * {@link SpellFunction#getDefaultTargets()} whenever the caller left that particular map empty - a function can
 * declare a sensible default translation (eg. {@code owner -> _owner}) for names it conventionally expects to
 * correspond with the host, without every caller needing to spell it out.
 */
public class CallFunctionAction extends SpellAction
{
    public static Codec<CallFunctionAction> makeCodec(SpellActionType<CallFunctionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                SpellsCodecs.SPELL_FUNCTION.fieldOf("function").forGetter(CallFunctionAction::getFunction),
                Codec.lazyInitialized(() -> SpellsCodecs.STRING_MAP).optionalFieldOf("activations").xmap(o -> o.orElse(new HashMap<>()), m -> m.isEmpty() ? Optional.empty() : Optional.of(m)).forGetter(CallFunctionAction::getActivations),
                Codec.lazyInitialized(() -> SpellsCodecs.STRING_MAP).optionalFieldOf("variables").xmap(o -> o.orElse(new HashMap<>()), m -> m.isEmpty() ? Optional.empty() : Optional.of(m)).forGetter(CallFunctionAction::getVariables),
                Codec.lazyInitialized(() -> SpellsCodecs.STRING_MAP).optionalFieldOf("targets").xmap(o -> o.orElse(new HashMap<>()), m -> m.isEmpty() ? Optional.empty() : Optional.of(m)).forGetter(CallFunctionAction::getTargets),
                Codec.lazyInitialized(() -> SpellsCodecs.CTX_VAR).listOf().optionalFieldOf("parameters").xmap(o -> o.orElse(new LinkedList<>()), p -> p.isEmpty() ? Optional.empty() : Optional.of(p)).forGetter(CallFunctionAction::getParameters)
        ).apply(instance, (activation, function, activations, variables, targets, parameters) -> new CallFunctionAction(type, activation, function, activations, variables, targets, parameters)));
    }

    public static CallFunctionAction make(Object activation, Holder<SpellFunction> function, Map<String, String> activations, Map<String, String> variables, Map<String, String> targets, List<CtxVar<?>> parameters)
    {
        return new CallFunctionAction(SpellActionTypes.CALL_FUNCTION.get(), activation.toString(), function, activations, variables, targets, parameters);
    }

    public static CallFunctionAction make(Object activation, Holder<SpellFunction> function, Map<String, String> activations, Map<String, String> variables, Map<String, String> targets)
    {
        return make(activation, function, activations, variables, targets, new LinkedList<>());
    }

    protected Holder<SpellFunction> function;
    protected Map<String, String> activations;
    protected Map<String, String> variables;
    protected Map<String, String> targets;
    protected List<CtxVar<?>> parameters;

    public CallFunctionAction(SpellActionType<?> type)
    {
        super(type);
    }

    public CallFunctionAction(SpellActionType<?> type, String activation, Holder<SpellFunction> function, Map<String, String> activations, Map<String, String> variables, Map<String, String> targets, List<CtxVar<?>> parameters)
    {
        super(type, activation);
        this.function = function;
        this.activations = activations;
        this.variables = variables;
        this.targets = targets;
        this.parameters = parameters;
    }

    public Holder<SpellFunction> getFunction()
    {
        return function;
    }

    public Map<String, String> getActivations()
    {
        return activations;
    }

    public Map<String, String> getVariables()
    {
        return variables;
    }

    public Map<String, String> getTargets()
    {
        return targets;
    }

    public List<CtxVar<?>> getParameters()
    {
        return parameters;
    }

    @Override
    protected void wasActivated(SpellContext ctx)
    {
        SpellFunction fn = function.value();
        Map<String, String> activations = this.activations.isEmpty() ? fn.getDefaultActivations() : this.activations;
        Map<String, String> variables = this.variables.isEmpty() ? fn.getDefaultVariables() : this.variables;
        Map<String, String> targets = this.targets.isEmpty() ? fn.getDefaultTargets() : this.targets;

        activations.forEach((host, internal) -> ctx.renameActivation(host, internal));
        variables.forEach((host, internal) -> ctx.renameCtxVar(host, internal));
        targets.forEach((host, internal) -> ctx.renameTargetGroup(host, internal));

        // if the nest limit was already exhausted, runNestedActions runs nothing - renaming back
        // immediately below still correctly undoes the rename-in either way, so nothing is left
        // stranded under an internal name
        ctx.runNestedActions(fn, parameters);

        activations.forEach((host, internal) -> ctx.renameActivation(internal, host));
        variables.forEach((host, internal) -> ctx.renameCtxVar(internal, host));
        targets.forEach((host, internal) -> ctx.renameTargetGroup(internal, host));
    }
}

package de.cas_ual_ty.spells.spell.action.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.SpellsCodecs;
import de.cas_ual_ty.spells.spell.SpellFunction;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import net.minecraft.core.Holder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Calls a {@link SpellFunction}: runs its actions as a contained sub-run of the current {@link SpellContext},
 * translated in and back out through the three name maps below. On entry, for every {@code (host, internal)}
 * pair, whatever the host has under {@code host} is renamed to {@code internal} so the function's own actions
 * (written purely in terms of their own internal names) can find it; on exit the same maps are walked in
 * reverse, moving whatever is left under each {@code internal} name back to its {@code host} name. Names not
 * listed in a map are left alone on both sides - the function's own internal-only names never touch the host's
 * namespace, and the host's other state is never visible inside the function.
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
                Codec.lazyInitialized(() -> SpellsCodecs.STRING_MAP).optionalFieldOf("targets").xmap(o -> o.orElse(new HashMap<>()), m -> m.isEmpty() ? Optional.empty() : Optional.of(m)).forGetter(CallFunctionAction::getTargets)
        ).apply(instance, (activation, function, activations, variables, targets) -> new CallFunctionAction(type, activation, function, activations, variables, targets)));
    }

    public static CallFunctionAction make(Object activation, Holder<SpellFunction> function, Map<String, String> activations, Map<String, String> variables, Map<String, String> targets)
    {
        return new CallFunctionAction(SpellActionTypes.CALL_FUNCTION.get(), activation.toString(), function, activations, variables, targets);
    }

    protected Holder<SpellFunction> function;
    protected Map<String, String> activations;
    protected Map<String, String> variables;
    protected Map<String, String> targets;

    public CallFunctionAction(SpellActionType<?> type)
    {
        super(type);
    }

    public CallFunctionAction(SpellActionType<?> type, String activation, Holder<SpellFunction> function, Map<String, String> activations, Map<String, String> variables, Map<String, String> targets)
    {
        super(type, activation);
        this.function = function;
        this.activations = activations;
        this.variables = variables;
        this.targets = targets;
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

    @Override
    protected void wasActivated(SpellContext ctx)
    {
        activations.forEach((host, internal) -> ctx.renameActivation(host, internal));
        variables.forEach((host, internal) -> ctx.renameCtxVar(host, internal));
        targets.forEach((host, internal) -> ctx.renameTargetGroup(host, internal));

        // if the nest limit was already exhausted, runNestedActions runs nothing - renaming back
        // immediately below still correctly undoes the rename-in either way, so nothing is left
        // stranded under an internal name
        ctx.runNestedActions(function.value());

        activations.forEach((host, internal) -> ctx.renameActivation(internal, host));
        variables.forEach((host, internal) -> ctx.renameCtxVar(internal, host));
        targets.forEach((host, internal) -> ctx.renameTargetGroup(internal, host));
    }
}

package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.ReferencedCtxVar;

import java.util.Map;

/**
 * Client-side compiled form of one {@code CustomParticleInitEntry} - a named ctx var evaluated once per particle
 * at spawn ({@code index} of that particle, {@code age} 0) and then kept on that particle
 * ({@link CustomParticleInstance#initVars}) for every later per-tick formula evaluation to reference by name.
 */
public class CustomParticleInitVar<T>
{
    public final CtxVarType<T> type;
    public final String name;
    public final ReferencedCtxVar<T> expr;

    public CustomParticleInitVar(CtxVarType<T> type, String name, ReferencedCtxVar<T> expr)
    {
        this.type = type;
        this.name = name;
        this.expr = expr;
    }

    /**
     * Evaluates {@link #expr} against {@code context}'s current state - including whatever earlier entries in
     * the same {@code initialize} list have already pushed into it via THIS method, so later entries can
     * reference earlier ones - with the given spawn-time {@code index}/{@code totalIndex}/{@code age}. On
     * success, pushes the result into {@code context} under {@link #name} (so {@code initial_position} and
     * subsequent entries can reference it too) AND stores it into {@code target}, the new particle's own
     * permanent map.
     */
    public void evaluateAndStore(CustomParticleContext context, int index, int totalIndex, int age, Map<String, CtxVar<?>> target)
    {
        context.evaluate(expr, index, totalIndex, age).ifPresent(value ->
        {
            context.capture(type, name, value);
            target.put(name, new CtxVar<>(type, name, value));
        });
    }
}

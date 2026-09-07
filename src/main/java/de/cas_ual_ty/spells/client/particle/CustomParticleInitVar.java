package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.ReferencedCtxVar;

import java.util.Map;

/**
 * Client-side compiled form of one {@code CustomParticleInitEntry} - evaluated once at spawn, kept on the
 * particle ({@link CustomParticleInstance#initVars}) for later per-tick formulas to reference by name.
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

    /** Pushes the result into {@code context} (so later entries can reference it) and into {@code target}. */
    public void evaluateAndStore(CustomParticleContext context, int index, int totalIndex, int age, Map<String, CtxVar<?>> target)
    {
        context.evaluate(expr, index, totalIndex, age).ifPresent(value ->
        {
            context.capture(type, name, value);
            target.put(name, new CtxVar<>(type, name, value));
        });
    }
}

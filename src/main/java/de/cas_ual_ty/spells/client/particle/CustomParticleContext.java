package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Per-emitter evaluation context for the particle DSL functions (position/motion/color). Wraps a bare
 * {@link SpellContext} purely as a vehicle for the existing {@link de.cas_ual_ty.spells.spell.compiler.Compiler}
 * expression pipeline - no spell is ever run through it, and nothing here touches {@code owner}/{@code spell},
 * only the context variable map that {@link SpellContext#getCtxVar(CtxVarType, String)}/
 * {@link SpellContext#setCtxVar} already provide.
 * <p>
 * These formulas are compiled via {@link de.cas_ual_ty.spells.spell.compiler.Compiler#compileString} directly
 * (see {@code CustomParticleEmitterClientAction}), NOT through a {@link DynamicCtxVar} JSON field codec - so
 * variable names below are referenced bare (eg. {@code age * 6}), never wrapped in {@code <<...>>}. That wrapper
 * is purely the outer JSON-field convention for "this whole field is a reference, not a literal" and means
 * nothing to the compiler's own grammar; using it inside a formula string is a silent parse failure (the
 * expression permanently evaluates to empty, no error visible in-game - only a logged compiler error).
 * <p>
 * Three tiers of variable here, by how often they change: {@link #capture} is called once per emitter, right
 * after it's spawned client-side, with values already resolved server-side at cast time (see
 * {@code CustomParticleEmitterInstance}) - per the mod's rule that particles never get live access to
 * server-only spell state, everything captured this way is frozen from that point on ({@link #MAX_AGE_NAME} -
 * the emitter's {@code duration} - is captured this way too, since it never changes once spawned). {@code age}/
 * {@code max_age} are both INT (tick counts) - a fade formula like {@code age / max_age} needs an explicit
 * {@code to_double(...)} cast on one side, since INT/INT division truncates via its own overload rather than
 * promoting to DOUBLE.
 * {@link #setFrameVars} is called once per emitter per client tick (not per particle) for values that DO keep
 * changing but are the same for every particle in the emitter that tick - currently just
 * {@link #SRC_MOTION_NAME}, the attached entity's current {@code getDeltaMovement()}. {@link #INDEX_NAME}/
 * {@link #AGE_NAME} are per-particle, overwritten right before every {@link #evaluate}.
 */
public class CustomParticleContext
{
    public static final String INDEX_NAME = "index";
    public static final String AGE_NAME = "age";
    public static final String MAX_AGE_NAME = "max_age";
    public static final String SRC_MOTION_NAME = "src_motion";

    private final SpellContext ctx;

    public CustomParticleContext(Level level)
    {
        this.ctx = new SpellContext(level, null, null);
    }

    public <T> void capture(CtxVarType<T> type, String name, T value)
    {
        ctx.setCtxVar(type, name, value);
    }

    public void setFrameVars(Vec3 srcMotion)
    {
        ctx.setCtxVar(CtxVarTypes.VEC3.get(), SRC_MOTION_NAME, srcMotion);
    }

    public <T> Optional<T> evaluate(DynamicCtxVar<T> expression, int index, int age)
    {
        ctx.setCtxVar(CtxVarTypes.INT.get(), INDEX_NAME, index);
        ctx.setCtxVar(CtxVarTypes.INT.get(), AGE_NAME, age);
        return expression.getValue(ctx);
    }
}

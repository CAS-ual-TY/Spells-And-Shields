package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
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
 * the emitter's {@code total_lifetime} - is captured this way too, since it never changes once spawned). {@code age}/
 * {@code max_age} are both INT (tick counts) - a fade formula like {@code age / max_age} needs an explicit
 * {@code to_double(...)} cast on one side, since INT/INT division truncates via its own overload rather than
 * promoting to DOUBLE.
 * {@link #setFrameVars} is called once per emitter per client tick (not per particle) for values that DO keep
 * changing but are the same for every particle in the emitter that tick - {@link #SOURCE_MOTION_NAME} (the
 * attached entity's current {@code getDeltaMovement()}), {@link #SOURCE_YAW_NAME}/{@link #SOURCE_PITCH_NAME}
 * (its current {@code getViewYRot}/{@code getViewXRot}, degrees) - all zero if unattached.
 * {@link #INDEX_NAME}/{@link #TOTAL_INDEX_NAME}/{@link #AGE_NAME} are per-particle, overwritten right before
 * every {@link #evaluate}. {@code index} resets to 0 for every batch ({@code 0..count-1}, see
 * {@code CustomParticleEmitterInstance#spawnBatch()}); {@code total_index} is the ever-growing counter across
 * every batch a repeating emitter has spawned, for formulas that need a value that never repeats.
 * {@code source_position} is deliberately NOT exposed - particle offsets are already relative to the anchor
 * (see {@code CustomParticleRenderer#resolveWorldPosition}), so formulas never need the source's absolute
 * world position.
 * <p>
 * A fourth tier - per-PARTICLE, not per-emitter or per-tick - comes from {@code CustomParticleInitEntry}: named
 * ctx vars evaluated once per particle at spawn and then kept on that specific particle
 * ({@link CustomParticleInstance#initVars}), pushed in by {@link #evaluate(DynamicCtxVar, CustomParticleInstance)}
 * for every later per-tick evaluation of that particle's own formulas.
 */
public class CustomParticleContext
{
    public static final String INDEX_NAME = "index";
    public static final String TOTAL_INDEX_NAME = "total_index";
    public static final String AGE_NAME = "age";
    public static final String MAX_AGE_NAME = "max_age";
    /**
     * This specific particle's own {@link CustomParticleInstance#maxAge} ({@code -1} if it has no individual
     * limit) - distinct from {@link #MAX_AGE_NAME}, which is the whole EMITTER's total_lifetime/period, not per-particle.
     */
    public static final String PARTICLE_MAX_AGE_NAME = "particle_max_age";
    public static final String SOURCE_MOTION_NAME = "source_motion";
    public static final String SOURCE_YAW_NAME = "source_yaw";
    public static final String SOURCE_PITCH_NAME = "source_pitch";

    private final SpellContext ctx;

    public CustomParticleContext(Level level)
    {
        this.ctx = new SpellContext(level, null, null);
    }

    public <T> void capture(CtxVarType<T> type, String name, T value)
    {
        ctx.setCtxVar(type, name, value);
    }

    public void setFrameVars(Vec3 sourceMotion, float sourceYaw, float sourcePitch)
    {
        ctx.setCtxVar(CtxVarTypes.VEC3.get(), SOURCE_MOTION_NAME, sourceMotion);
        ctx.setCtxVar(CtxVarTypes.DOUBLE.get(), SOURCE_YAW_NAME, (double) sourceYaw);
        ctx.setCtxVar(CtxVarTypes.DOUBLE.get(), SOURCE_PITCH_NAME, (double) sourcePitch);
    }

    public <T> Optional<T> evaluate(DynamicCtxVar<T> expression, int index, int totalIndex, int age)
    {
        ctx.setCtxVar(CtxVarTypes.INT.get(), INDEX_NAME, index);
        ctx.setCtxVar(CtxVarTypes.INT.get(), TOTAL_INDEX_NAME, totalIndex);
        ctx.setCtxVar(CtxVarTypes.INT.get(), AGE_NAME, age);
        return expression.getValue(ctx);
    }

    /**
     * Same as {@link #evaluate(DynamicCtxVar, int, int, int)}, but also pushes {@code particle}'s own
     * {@link CustomParticleInstance#initVars} in first, so per-tick formulas (position/motion/color/alpha) can
     * reference whatever that particle's {@code initialize} entries computed for it at spawn. Spawn-time
     * evaluation itself (initial_position, the initialize entries) happens BEFORE the particle object exists,
     * so that still goes through the plain {@code (index, totalIndex, age)} overload - see
     * {@link CustomParticleEmitterInstance#spawnBatch()}.
     */
    public <T> Optional<T> evaluate(DynamicCtxVar<T> expression, CustomParticleInstance particle)
    {
        ctx.setCtxVar(CtxVarTypes.INT.get(), INDEX_NAME, particle.index);
        ctx.setCtxVar(CtxVarTypes.INT.get(), TOTAL_INDEX_NAME, particle.totalIndex);
        ctx.setCtxVar(CtxVarTypes.INT.get(), AGE_NAME, particle.age);
        ctx.setCtxVar(CtxVarTypes.INT.get(), PARTICLE_MAX_AGE_NAME, particle.maxAge);

        for(CtxVar<?> var : particle.initVars.values())
        {
            setVar(var);
        }

        return expression.getValue(ctx);
    }

    private <T> void setVar(CtxVar<T> var)
    {
        ctx.setCtxVar(var.getType(), var.getName(), var.getValue());
    }
}

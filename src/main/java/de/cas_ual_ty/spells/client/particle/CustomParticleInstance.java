package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.spell.variable.CtxVar;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * One particle's live, per-tick state within a {@link CustomParticleEmitterInstance}. {@link #prevPosition} is
 * kept alongside {@link #position} purely for render-time partial-tick interpolation (same idea as vanilla
 * {@code Particle}'s own xo/yo/zo) - it's the position as of the END of the previous tick, updated once per
 * tick right before {@link #position} changes again.
 * <p>
 * {@link #spawnPosition} is THIS particle's own anchor for whichever attachment axis is
 * {@link CustomParticleAttachMode#ABSOLUTE} - captured fresh by {@link CustomParticleEmitterInstance#spawnBatch()}
 * at the moment THIS particle (or its batch) was spawned, not shared emitter-wide. This is what makes a repeating
 * ({@code period > 0}) ABSOLUTE-attached emitter read as a proper trail following the source around - each new
 * batch anchors to wherever the source currently is, rather than every batch reusing the very first batch's
 * spawn point. (Rotation has no equivalent spawn-time anchor - RELATIVE rotation always uses the entity's full
 * CURRENT yaw/pitch, see {@code CustomParticleRenderer#resolveWorldPosition}.)
 * <p>
 * {@link #initVars} holds this particle's OWN copy of whatever the emitter's {@code CustomParticleInitEntry}
 * list computed for it at spawn (see {@code CustomParticleInitVar#evaluateAndStore}) - permanent, per-particle
 * state that every later per-tick formula can reference by name (see
 * {@code CustomParticleContext#evaluate(DynamicCtxVar, CustomParticleInstance)}), distinct from the emitter-wide
 * captured constants or the always-changing {@code index}/{@code age}.
 */
public class CustomParticleInstance
{
    /**
     * Local slot within THIS particle's own batch - {@code 0..count-1}, reset to 0 every time
     * {@link CustomParticleEmitterInstance#spawnBatch()} runs. See {@link #totalIndex} for a value that never
     * repeats across batches.
     */
    public final int index;
    /**
     * Ever-growing across every batch a repeating emitter has spawned - never resets, unlike {@link #index}.
     */
    public final int totalIndex;

    public Vec3 position;
    public Vec3 prevPosition;
    public Vec3 motion;

    public double red;
    public double green;
    public double blue;
    public double alpha;

    public int age;

    public final Vec3 spawnPosition;

    /**
     * This particle's own individual lifetime, from {@code CustomParticleEmitterInstance#particleLifetimeExpr}
     * evaluated once at spawn - {@code -1} means unlimited (this particle lives until the whole emitter expires
     * via its own {@code total_lifetime}, same as before this field existed). Enforced in {@code CustomParticleManager}.
     */
    public final int maxAge;

    public final Map<String, CtxVar<?>> initVars;

    public CustomParticleInstance(int index, int totalIndex, Vec3 position, Vec3 spawnPosition, int maxAge, Map<String, CtxVar<?>> initVars)
    {
        this.index = index;
        this.totalIndex = totalIndex;
        this.position = position;
        this.prevPosition = position;
        this.motion = Vec3.ZERO;
        this.red = 1.0;
        this.green = 1.0;
        this.blue = 1.0;
        this.alpha = 1.0;
        this.age = 0;
        this.spawnPosition = spawnPosition;
        this.maxAge = maxAge;
        this.initVars = initVars;
    }
}

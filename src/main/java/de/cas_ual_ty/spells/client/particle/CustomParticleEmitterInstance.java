package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.spell.variable.ReferencedCtxVar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * One active emitter's live client-side state - the particles themselves, plus how they're anchored to
 * {@link #attachedTo}. Ticking (evaluating each particle's position/motion/color) and rendering both happen
 * elsewhere - this class only holds state, except for {@link #spawnBatch()}, which both the initial spawn (see
 * {@code CustomParticleEmitterClientAction#execute}) and every repeat spawn (see {@code CustomParticleManager})
 * call, so there's exactly one place that turns {@link #count}/{@link #initialPositionExpr} into new
 * {@link CustomParticleInstance}s.
 * <p>
 * {@link #spawnPosition}/{@link #spawnYaw} are captured once, at construction, and never change afterward -
 * they're the anchor for whichever attachment axis is {@link CustomParticleAttachMode#ABSOLUTE}. Both modes
 * absolute: the entity is irrelevant after spawn, everything is relative to these. Both relative: these are
 * unused, {@link #attachedTo}'s own live transform is used instead. Mixed (eg. position absolute, rotation
 * relative): the particle stays anchored at {@link #spawnPosition} but its own local offset still rotates by
 * however far {@link #attachedTo}'s yaw has turned since {@link #spawnYaw} - "stays put but reorients."
 * <p>
 * {@link #period} is ticks between repeat spawns of a fresh {@link #count}-particle batch - {@code <= 0} means
 * one-shot (spawn once, particles live until the whole emitter expires via {@link #duration}). When repeating,
 * each individual particle instead gets removed once its OWN age reaches {@link #period} (see
 * {@code CustomParticleManager}), so batches overlap/replace each other into a continuous trail rather than
 * piling up forever.
 */
public class CustomParticleEmitterInstance
{
    public final Level level;
    @Nullable
    public final Entity attachedTo;
    public final CustomParticleAttachMode positionAttachMode;
    public final CustomParticleAttachMode rotationAttachMode;
    public final List<CustomParticleInstance> particles;
    public final int count;
    public final int duration;
    public final int period;
    public final Vec3 spawnPosition;
    public final float spawnYaw;
    /**
     * Holds this emitter's captured constants (baked server-side at cast time, see {@link CustomParticleContext})
     * plus {@code index}/{@code age}, and evaluates the position/motion/color DSL expressions against them. One
     * instance per emitter, reused every tick for every particle - not per-particle, since captured values never
     * change and only {@code index}/{@code age} need overwriting between evaluations.
     */
    public final CustomParticleContext context;

    /**
     * Set right after construction, once the raw DSL strings sent over the network are compiled - see
     * {@code CustomParticleEmitterClientAction#execute}. Exactly one of {@link #motionExpr}/{@link #positionExpr}
     * is non-null, depending on which mode spawned this emitter ({@code CustomParticleEmitterMotionAction} vs
     * {@code CustomParticleEmitterPositionAction}); {@link #colorExpr}/{@link #alphaExpr} are always present.
     * {@link #initialPositionExpr} is motion-mode-only (may be null there too, meaning "spawn at the anchor").
     */
    @Nullable
    public ReferencedCtxVar<Vec3> motionExpr;
    @Nullable
    public ReferencedCtxVar<Vec3> positionExpr;
    public ReferencedCtxVar<Vec3> colorExpr;
    public ReferencedCtxVar<Double> alphaExpr;
    @Nullable
    public ReferencedCtxVar<Vec3> initialPositionExpr;

    public int age;
    /**
     * Running counter handed out to each newly spawned particle as its {@code index}, across every batch - NOT
     * reset per batch, so DSL formulas referencing {@code index} keep seeing fresh, distinct values instead of
     * every repeat batch overlapping the previous one's indices.
     */
    public int nextIndex;

    public CustomParticleEmitterInstance(Level level, @Nullable Entity attachedTo, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<CustomParticleInstance> particles, int count, int duration, int period, Vec3 spawnPosition, float spawnYaw)
    {
        this.level = level;
        this.attachedTo = attachedTo;
        this.positionAttachMode = positionAttachMode;
        this.rotationAttachMode = rotationAttachMode;
        this.particles = particles;
        this.count = count;
        this.duration = duration;
        this.period = period;
        this.spawnPosition = spawnPosition;
        this.spawnYaw = spawnYaw;
        this.context = new CustomParticleContext(level);
        this.age = 0;
        this.nextIndex = 0;
    }

    /**
     * Spawns one fresh batch of {@link #count} particles, evaluating {@link #initialPositionExpr} once per
     * particle (with that particle's own freshly-assigned {@code index}, {@code age} 0) if present, else
     * spawning all of them at the anchor. Called once up front for the initial batch, then again every
     * {@link #period} ticks for repeat batches (see {@code CustomParticleManager}) - requires
     * {@link #initialPositionExpr} (if used) to already be set.
     */
    public void spawnBatch()
    {
        for(int i = 0; i < count; i++)
        {
            int index = nextIndex++;
            Vec3 startPosition = initialPositionExpr == null ? Vec3.ZERO : context.evaluate(initialPositionExpr, index, 0).orElse(Vec3.ZERO);
            particles.add(new CustomParticleInstance(index, startPosition));
        }
    }

    public boolean isExpired()
    {
        return age >= duration;
    }

    /**
     * Whether {@link #attachedTo} is actually needed for rendering (either axis is
     * {@link CustomParticleAttachMode#RELATIVE}) - if it dies/unloads while still needed, the emitter can't
     * correctly resolve anymore and gets dropped (see {@code CustomParticleManager}); if neither axis needs it,
     * the entity going away doesn't matter, the emitter keeps going against {@link #spawnPosition}/
     * {@link #spawnYaw}.
     */
    public boolean needsAttachedEntity()
    {
        return positionAttachMode == CustomParticleAttachMode.RELATIVE || rotationAttachMode == CustomParticleAttachMode.RELATIVE;
    }
}

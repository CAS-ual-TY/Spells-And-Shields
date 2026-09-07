package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.ReferencedCtxVar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One active emitter's live client-side state - the particles themselves, plus how they're anchored to
 * {@link #attachedTo}. Ticking (evaluating each particle's position/motion/color) and rendering both happen
 * elsewhere - this class only holds state, except for {@link #spawnBatch()}, which both the initial spawn (see
 * {@code CustomParticleEmitterClientAction#execute}) and every repeat spawn (see {@code CustomParticleManager})
 * call, so there's exactly one place that turns {@link #count}/{@link #initialPositionExpr} into new
 * {@link CustomParticleInstance}s.
 * <p>
 * {@link #spawnPosition}/{@link #spawnYaw}/{@link #spawnPitch} are this emitter's OWN construction-time capture,
 * used only as the seed/fallback {@link #spawnBatch()} falls back to when {@link #attachedTo} is null (only
 * possible when neither attachment axis is RELATIVE - see {@link #needsAttachedEntity()} - so every batch would
 * sample the same fixed anchor regardless). The anchor actually used for the ABSOLUTE axis at render time is
 * each particle's OWN {@link CustomParticleInstance#spawnPosition}/{@link CustomParticleInstance#spawnYaw}/
 * {@link CustomParticleInstance#spawnPitch}, captured fresh per BATCH by {@link #spawnBatch()} - not shared
 * emitter-wide - so a repeating ({@code period > 0}) ABSOLUTE-attached emitter reads as a trail (each batch
 * anchors to wherever the source was when IT spawned) rather than every batch piling onto the very first
 * batch's spawn point. Both modes absolute: the entity is irrelevant after spawn, everything is relative to
 * that batch's own captured anchor. Both relative: these are unused, {@link #attachedTo}'s own live transform
 * is used instead. Mixed (eg. position absolute, rotation relative): the particle stays anchored at its own
 * spawn position but its local offset still rotates (both yaw AND pitch, see
 * {@code CustomParticleRenderer#resolveWorldPosition}) by however far {@link #attachedTo}'s orientation has
 * turned since ITS batch's own spawn yaw/pitch - "stays put but reorients."
 * <p>
 * {@link #period} is ticks between repeat spawns of a fresh {@link #count}-particle batch - {@code <= 0} means
 * one-shot. Every particle, from every batch, lives until the whole emitter expires via {@link #totalLifetime} -
 * {@link #period} does NOT force-remove individual particles early (see {@code CustomParticleManager}); a pulse
 * that should visually fade out needs its own {@code alpha} formula for that.
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
    public final int totalLifetime;
    public final int period;
    /**
     * Ticks to wait before the first batch spawns - {@code <= 0} means spawn immediately (the original behavior,
     * still handled by {@code CustomParticleEmitterClientAction#execute} calling {@link #spawnBatch()} directly).
     * When {@code > 0}, {@code execute} skips that immediate call and {@code CustomParticleManager} spawns the
     * first batch once {@code age} reaches this, then (if {@link #period} {@code > 0}) every {@link #period}
     * ticks after that - {@code delay, delay + period, delay + 2*period, ...} rather than
     * {@code 0, period, 2*period, ...}.
     */
    public final int delay;
    public final Vec3 spawnPosition;
    public final float spawnYaw;
    public final float spawnPitch;
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
    /**
     * Raw DSL formula (INT), evaluated once per particle at spawn just like {@link #initialPositionExpr} - the
     * result becomes that particle's own {@link CustomParticleInstance#maxAge}. Null means no per-particle limit
     * (every particle lives until this emitter itself expires via {@link #totalLifetime}, same as before this
     * field existed) - see {@code CustomParticleManager} for where {@code maxAge} actually gets enforced.
     */
    @Nullable
    public ReferencedCtxVar<Integer> particleLifetimeExpr;

    /**
     * Compiled {@code CustomParticleInitEntry} list, in declaration order (later entries may reference earlier
     * ones - see {@code CustomParticleInitVar#evaluateAndStore}). Set right after construction alongside the
     * expr fields above. Evaluated once per particle, fresh, every time {@link #spawnBatch()} creates one.
     */
    public List<CustomParticleInitVar<?>> initVars = List.of();

    public int age;
    /**
     * Running counter handed out to each newly spawned particle as its {@code totalIndex} - NOT reset per batch,
     * unlike the particle's own {@code index} (always {@code 0..count-1} within its batch, see
     * {@link #spawnBatch()}) - so DSL formulas that need a value that never repeats across batches (bare
     * {@code total_index}) still can.
     */
    public int nextIndex;

    public CustomParticleEmitterInstance(Level level, @Nullable Entity attachedTo, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<CustomParticleInstance> particles, int count, int totalLifetime, int period, int delay, Vec3 spawnPosition, float spawnYaw, float spawnPitch)
    {
        this.level = level;
        this.attachedTo = attachedTo;
        this.positionAttachMode = positionAttachMode;
        this.rotationAttachMode = rotationAttachMode;
        this.particles = particles;
        this.count = count;
        this.totalLifetime = totalLifetime;
        this.period = period;
        this.delay = delay;
        this.spawnPosition = spawnPosition;
        this.spawnYaw = spawnYaw;
        this.spawnPitch = spawnPitch;
        this.context = new CustomParticleContext(level);
        this.age = 0;
        this.nextIndex = 0;
    }

    /**
     * Spawns one fresh batch of {@link #count} particles, evaluating {@link #initialPositionExpr} once per
     * particle (with that particle's own freshly-assigned {@code index}/{@code totalIndex}, {@code age} 0) if
     * present, else spawning all of them at the anchor. Called once up front for the initial batch, then again
     * every {@link #period} ticks for repeat batches (see {@code CustomParticleManager}) - requires
     * {@link #initialPositionExpr} (if used) to already be set.
     * <p>
     * Samples {@link #attachedTo}'s CURRENT position/yaw/pitch fresh, right now, for this batch's particles'
     * own {@link CustomParticleInstance#spawnPosition}/{@link CustomParticleInstance#spawnYaw}/
     * {@link CustomParticleInstance#spawnPitch} - falls back to this emitter's own {@link #spawnPosition}/
     * {@link #spawnYaw}/{@link #spawnPitch} when unattached (only possible when neither attachment axis is
     * RELATIVE, see {@link #needsAttachedEntity()}, in which case every batch would sample the same fixed
     * anchor anyway).
     */
    public void spawnBatch()
    {
        Vec3 batchPosition = attachedTo != null ? attachedTo.position() : spawnPosition;
        float batchYaw = attachedTo != null ? attachedTo.getViewYRot(1.0F) : spawnYaw;
        float batchPitch = attachedTo != null ? attachedTo.getViewXRot(1.0F) : spawnPitch;

        for(int index = 0; index < count; index++)
        {
            int totalIndex = nextIndex++;

            Map<String, CtxVar<?>> particleInitVars = new HashMap<>();

            for(CustomParticleInitVar<?> initVar : initVars)
            {
                initVar.evaluateAndStore(context, index, totalIndex, 0, particleInitVars);
            }

            Vec3 startPosition = initialPositionExpr == null ? Vec3.ZERO : context.evaluate(initialPositionExpr, index, totalIndex, 0).orElse(Vec3.ZERO);
            int maxAge = particleLifetimeExpr == null ? -1 : context.evaluate(particleLifetimeExpr, index, totalIndex, 0).orElse(-1);
            CustomParticleInstance particle = new CustomParticleInstance(index, totalIndex, startPosition, batchPosition, batchYaw, batchPitch, maxAge, particleInitVars);

            // Evaluate color/alpha once right away too - otherwise a fresh particle renders at
            // CustomParticleInstance's white/opaque constructor defaults for the one tick between spawning here
            // and CustomParticleManager's next per-tick evaluation, which is most visible on whichever particle
            // happens to be the last one ever spawned (eg. the emitter's source dying right after).
            context.evaluate(colorExpr, particle).ifPresent(color ->
            {
                particle.red = color.x();
                particle.green = color.y();
                particle.blue = color.z();
            });
            context.evaluate(alphaExpr, particle).ifPresent(alpha -> particle.alpha = alpha);

            particles.add(particle);
        }
    }

    public boolean isExpired()
    {
        return age >= totalLifetime;
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

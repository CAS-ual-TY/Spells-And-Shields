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
 * One active emitter's live client-side state. {@link #spawnBatch()} is the only place that turns
 * {@link #count}/{@link #initialPositionExpr} into new {@link CustomParticleInstance}s - called for the initial
 * spawn and every repeat ({@link #period}).
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
    /** Ticks before the first batch spawns; {@code <= 0} spawns immediately. */
    public final int delay;
    public final Vec3 spawnPosition;
    public final CustomParticleContext context;

    /** Exactly one of {@link #motionExpr}/{@link #positionExpr} is non-null, depending on emitter mode. */
    @Nullable
    public ReferencedCtxVar<Vec3> motionExpr;
    @Nullable
    public ReferencedCtxVar<Vec3> positionExpr;
    public ReferencedCtxVar<Vec3> colorExpr;
    public ReferencedCtxVar<Double> alphaExpr;
    @Nullable
    public ReferencedCtxVar<Vec3> initialPositionExpr;
    @Nullable
    public ReferencedCtxVar<Integer> particleLifetimeExpr;

    public List<CustomParticleInitVar<?>> initVars = List.of();

    public int age;
    /** Ever-growing across batches, unlike the particle's own {@code index} which resets per batch. */
    public int nextIndex;

    public CustomParticleEmitterInstance(Level level, @Nullable Entity attachedTo, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<CustomParticleInstance> particles, int count, int totalLifetime, int period, int delay, Vec3 spawnPosition)
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
        this.context = new CustomParticleContext(level);
        this.age = 0;
        this.nextIndex = 0;
    }

    public void spawnBatch()
    {
        Vec3 batchPosition = attachedTo != null ? attachedTo.position() : spawnPosition;

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
            CustomParticleInstance particle = new CustomParticleInstance(index, totalIndex, startPosition, batchPosition, maxAge, particleInitVars);

            // evaluate once now too, else it renders white/opaque for one tick before CustomParticleManager's first tick
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

    public boolean needsAttachedEntity()
    {
        return positionAttachMode == CustomParticleAttachMode.RELATIVE || rotationAttachMode == CustomParticleAttachMode.RELATIVE;
    }
}

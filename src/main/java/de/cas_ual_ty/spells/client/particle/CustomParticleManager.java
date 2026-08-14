package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.SpellsAndShields;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Iterator;

/**
 * Advances every active {@link CustomParticleEmitterInstance} once per client tick - ages the emitter, evaluates
 * that emitter's DSL expressions (see {@link CustomParticleContext}) fresh against each particle's own
 * {@code index}/{@code age} and writes the results into the particle (snapshotting the previous position first,
 * for render-time interpolation - see {@link CustomParticleRenderer}), and drops emitters that expired or whose
 * attachment died/unloaded while still {@linkplain CustomParticleEmitterInstance#needsAttachedEntity() needed}.
 * <p>
 * Exactly one of {@link CustomParticleEmitterInstance#motionExpr}/{@link CustomParticleEmitterInstance#positionExpr}
 * is set per emitter (see {@code CustomParticleEmitterClientAction}) - motion mode re-evaluates a velocity every
 * tick and integrates it into position, position mode recomputes the position directly with no integration.
 * <p>
 * When {@link CustomParticleEmitterInstance#period} is {@code > 0}, individual particles are dropped once their
 * own age reaches it (before that, one-shot particles only ever go away with the whole emitter), and a fresh
 * {@link CustomParticleEmitterInstance#spawnBatch()} fires every {@code period} ticks - old and new batches
 * overlap/replace each other into a continuous trail rather than piling up for the emitter's whole
 * {@link CustomParticleEmitterInstance#duration}.
 */
@EventBusSubscriber(modid = SpellsAndShields.MOD_ID, value = Dist.CLIENT)
public class CustomParticleManager
{
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        Iterator<CustomParticleEmitterInstance> iterator = CustomParticleRenderer.ACTIVE.iterator();

        while(iterator.hasNext())
        {
            CustomParticleEmitterInstance emitter = iterator.next();

            if(emitter.isExpired() || (emitter.needsAttachedEntity() && (emitter.attachedTo == null || !emitter.attachedTo.isAlive())))
            {
                iterator.remove();
                continue;
            }

            emitter.age++;
            emitter.context.setFrameVars(emitter.attachedTo != null ? emitter.attachedTo.getDeltaMovement() : Vec3.ZERO);

            Iterator<CustomParticleInstance> particleIterator = emitter.particles.iterator();

            while(particleIterator.hasNext())
            {
                CustomParticleInstance particle = particleIterator.next();

                if(emitter.period > 0 && particle.age >= emitter.period)
                {
                    particleIterator.remove();
                    continue;
                }

                particle.prevPosition = particle.position;

                if(emitter.motionExpr != null)
                {
                    particle.motion = emitter.context.evaluate(emitter.motionExpr, particle.index, particle.age).orElse(Vec3.ZERO);
                    particle.position = particle.position.add(particle.motion);
                }
                else if(emitter.positionExpr != null)
                {
                    particle.position = emitter.context.evaluate(emitter.positionExpr, particle.index, particle.age).orElse(particle.position);
                }

                emitter.context.evaluate(emitter.colorExpr, particle.index, particle.age).ifPresent(color ->
                {
                    particle.red = color.x();
                    particle.green = color.y();
                    particle.blue = color.z();
                });
                emitter.context.evaluate(emitter.alphaExpr, particle.index, particle.age).ifPresent(alpha -> particle.alpha = alpha);

                particle.age++;
            }

            if(emitter.period > 0 && emitter.age % emitter.period == 0)
            {
                emitter.spawnBatch();
            }
        }
    }
}

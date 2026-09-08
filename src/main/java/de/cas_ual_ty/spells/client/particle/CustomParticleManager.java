package de.cas_ual_ty.spells.client.particle;

import de.cas_ual_ty.spells.SpellsAndShields;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Iterator;

/**
 * Advances every active {@link CustomParticleEmitterInstance} once per client tick - evaluates DSL expressions,
 * ages/removes particles and emitters, spawns repeat batches.
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
            emitter.context.setFrameVars(
                    emitter.attachedTo != null ? emitter.attachedTo.getDeltaMovement() : Vec3.ZERO,
                    emitter.attachedTo != null ? emitter.attachedTo.getViewYRot(1.0F) : 0.0F,
                    emitter.attachedTo != null ? emitter.attachedTo.getViewXRot(1.0F) : 0.0F
            );

            Iterator<CustomParticleInstance> particleIterator = emitter.particles.iterator();

            while(particleIterator.hasNext())
            {
                CustomParticleInstance particle = particleIterator.next();
                particle.prevPosition = particle.position;
                emitter.context.beginParticle(particle);

                if(emitter.motionExpr != null)
                {
                    particle.motion = emitter.context.evaluate(emitter.motionExpr).orElse(Vec3.ZERO);
                    particle.position = particle.position.add(particle.motion);
                }
                else if(emitter.positionExpr != null)
                {
                    particle.position = emitter.context.evaluate(emitter.positionExpr).orElse(particle.position);
                }

                emitter.context.evaluate(emitter.colorExpr).ifPresent(color ->
                {
                    particle.red = color.x();
                    particle.green = color.y();
                    particle.blue = color.z();
                });
                emitter.context.evaluate(emitter.alphaExpr).ifPresent(alpha -> particle.alpha = alpha);

                particle.age++;

                if(particle.maxAge >= 0 && particle.age >= particle.maxAge)
                {
                    particleIterator.remove();
                }
            }

            if(emitter.delay > 0 && emitter.age == emitter.delay)
            {
                emitter.spawnBatch();
            }
            else if(emitter.period > 0 && emitter.age > emitter.delay && (emitter.age - emitter.delay) % emitter.period == 0)
            {
                emitter.spawnBatch();
            }
        }
    }
}

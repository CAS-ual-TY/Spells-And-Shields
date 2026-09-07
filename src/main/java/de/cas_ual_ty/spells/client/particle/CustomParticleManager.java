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
 * {@code index}/{@code age}/{@code initVars} (see {@link CustomParticleContext#evaluate(de.cas_ual_ty.spells.spell.variable.DynamicCtxVar, CustomParticleInstance)})
 * and writes the results into the particle (snapshotting the previous position first, for render-time
 * interpolation - see {@link CustomParticleRenderer}), and drops emitters that expired or whose attachment
 * died/unloaded while still {@linkplain CustomParticleEmitterInstance#needsAttachedEntity() needed}.
 * <p>
 * Exactly one of {@link CustomParticleEmitterInstance#motionExpr}/{@link CustomParticleEmitterInstance#positionExpr}
 * is set per emitter (see {@code CustomParticleEmitterClientAction}) - motion mode re-evaluates a velocity every
 * tick and integrates it into position, position mode recomputes the position directly with no integration.
 * <p>
 * When {@link CustomParticleEmitterInstance#period} is {@code > 0}, a fresh {@link CustomParticleEmitterInstance#spawnBatch()}
 * fires every {@code period} ticks, offset by {@link CustomParticleEmitterInstance#delay} if set - individual
 * particles are NOT force-removed once they age past {@code period}, they simply keep ticking (and rendering,
 * however the formulas leave them) until either the whole emitter expires via
 * {@link CustomParticleEmitterInstance#totalLifetime}, or - if {@code particle_lifetime} was set on the action that
 * spawned them - their OWN individual {@link CustomParticleInstance#maxAge} is reached, in which case only that
 * one particle is removed. A pulse that should visually disappear without either of those needs its own
 * {@code alpha} formula to fade it out (eg. {@code 1.0 - age / to_double(max_age)}, where {@code max_age} is
 * captured as {@code period} for repeating emitters) - that already makes it invisible at the same moment a
 * hard removal would have, with no visible difference, while still allowing an emitter that wants its particles
 * to persist (eg. a trail that should stay planted, not fade) to just use a constant alpha instead.
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

                if(emitter.motionExpr != null)
                {
                    particle.motion = emitter.context.evaluate(emitter.motionExpr, particle).orElse(Vec3.ZERO);
                    particle.position = particle.position.add(particle.motion);
                }
                else if(emitter.positionExpr != null)
                {
                    particle.position = emitter.context.evaluate(emitter.positionExpr, particle).orElse(particle.position);
                }

                emitter.context.evaluate(emitter.colorExpr, particle).ifPresent(color ->
                {
                    particle.red = color.x();
                    particle.green = color.y();
                    particle.blue = color.z();
                });
                emitter.context.evaluate(emitter.alphaExpr, particle).ifPresent(alpha -> particle.alpha = alpha);

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

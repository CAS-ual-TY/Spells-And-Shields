package de.cas_ual_ty.spells.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import de.cas_ual_ty.spells.SpellsAndShields;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws every active {@link CustomParticleEmitterInstance}'s particles as simple camera-facing colored quads, at
 * {@link RenderLevelStageEvent.Stage#AFTER_PARTICLES} (translucent-safe, after vanilla particles/entities). No
 * texture yet - flat-colored squares, same {@link RenderType}/vertex-building technique vanilla's own
 * {@code SingleQuadParticle} uses (raw camera-relative coordinates fed straight to the buffer, not
 * {@code PoseStack} transforms - vanilla particles bypass the pose stack entirely for position, so this does
 * too, for the same proven-correct billboard math).
 * <p>
 * {@link #ACTIVE} is a placeholder list living here for now - ticking/spawning/expiring emitters into it is a
 * separate concern (the manager), not yet built.
 */
@EventBusSubscriber(modid = SpellsAndShields.MOD_ID, value = Dist.CLIENT)
public class CustomParticleRenderer
{
    public static final List<CustomParticleEmitterInstance> ACTIVE = new ArrayList<>();

    private static final RenderType RENDER_TYPE = RenderType.create(
            SpellsAndShields.MOD_ID + ":custom_particle",
            com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR,
            com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event)
    {
        if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty())
        {
            return;
        }

        Camera camera = event.getCamera();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 cameraPos = camera.getPosition();
        Quaternionf billboard = camera.rotation();

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RENDER_TYPE);

        for(CustomParticleEmitterInstance emitter : ACTIVE)
        {
            for(CustomParticleInstance particle : emitter.particles)
            {
                renderParticle(buffer, emitter, particle, billboard, cameraPos, partialTick);
            }
        }

        bufferSource.endBatch(RENDER_TYPE);
    }

    private static void renderParticle(VertexConsumer buffer, CustomParticleEmitterInstance emitter, CustomParticleInstance particle, Quaternionf billboard, Vec3 cameraPos, float partialTick)
    {
        Vec3 worldPos = resolveWorldPosition(emitter, particle, partialTick);

        float x = (float) (worldPos.x() - cameraPos.x());
        float y = (float) (worldPos.y() - cameraPos.y());
        float z = (float) (worldPos.z() - cameraPos.z());

        float size = 0.1F;
        float r = (float) particle.red;
        float g = (float) particle.green;
        float b = (float) particle.blue;
        float a = (float) particle.alpha;

        addVertex(buffer, billboard, x, y, z, 1.0F, -1.0F, size, r, g, b, a);
        addVertex(buffer, billboard, x, y, z, 1.0F, 1.0F, size, r, g, b, a);
        addVertex(buffer, billboard, x, y, z, -1.0F, 1.0F, size, r, g, b, a);
        addVertex(buffer, billboard, x, y, z, -1.0F, -1.0F, size, r, g, b, a);
    }

    /**
     * Resolves a particle's interpolated local offset (its own {@code position}, spawn-local - not world space)
     * against its emitter's attachment. Position and rotation attachment resolve independently:
     * <ul>
     * <li>position ABSOLUTE: anchor is THIS PARTICLE's own {@code spawnPosition} (captured per-batch, see
     * {@link CustomParticleEmitterInstance#spawnBatch()}), never moves after that batch spawned.</li>
     * <li>position RELATIVE: anchor is {@link Entity#getPosition(float)} of {@code emitter.attachedTo}.</li>
     * <li>rotation ABSOLUTE: local offset stays unrotated.</li>
     * <li>rotation RELATIVE: local offset rotates by the entity's current yaw AND pitch - by how far each has
     * turned since THIS PARTICLE's own {@code spawnYaw}/{@code spawnPitch} if position is ABSOLUTE ("stays put
     * but reorients"), or by the entity's full current yaw/pitch if position is also RELATIVE (rigid
     * child-style attachment). Composed the same way vanilla does for local-offset-to-world transforms (eg.
     * {@code Player#getRopeHoldPosition}): pitch first via {@code xRot}, then yaw via {@code yRot}, both
     * negated-degrees-to-radians.</li>
     * </ul>
     */
    private static Vec3 resolveWorldPosition(CustomParticleEmitterInstance emitter, CustomParticleInstance particle, float partialTick)
    {
        Vec3 localOffset = new Vec3(
                Mth.lerp(partialTick, particle.prevPosition.x(), particle.position.x()),
                Mth.lerp(partialTick, particle.prevPosition.y(), particle.position.y()),
                Mth.lerp(partialTick, particle.prevPosition.z(), particle.position.z())
        );

        Vec3 anchor = particle.spawnPosition;
        float yawAngle = 0.0F;
        float pitchAngle = 0.0F;

        if(emitter.needsAttachedEntity())
        {
            Entity attachedTo = emitter.attachedTo;
            float entityYaw = attachedTo.getViewYRot(partialTick);
            float entityPitch = attachedTo.getViewXRot(partialTick);

            if(emitter.positionAttachMode == CustomParticleAttachMode.RELATIVE)
            {
                anchor = attachedTo.getPosition(partialTick);

                if(emitter.rotationAttachMode == CustomParticleAttachMode.RELATIVE)
                {
                    yawAngle = entityYaw;
                    pitchAngle = entityPitch;
                }
            }
            else if(emitter.rotationAttachMode == CustomParticleAttachMode.RELATIVE)
            {
                yawAngle = entityYaw - particle.spawnYaw;
                pitchAngle = entityPitch - particle.spawnPitch;
            }
        }

        if(pitchAngle != 0.0F || yawAngle != 0.0F)
        {
            localOffset = localOffset.xRot((float) -Math.toRadians(pitchAngle)).yRot((float) -Math.toRadians(yawAngle));
        }

        return anchor.add(localOffset);
    }

    private static void addVertex(VertexConsumer buffer, Quaternionf billboard, float x, float y, float z, float xOffset, float yOffset, float size, float r, float g, float b, float a)
    {
        Vector3f vertex = new Vector3f(xOffset, yOffset, 0.0F).rotate(billboard).mul(size).add(x, y, z);
        buffer.addVertex(vertex.x(), vertex.y(), vertex.z()).setColor(r, g, b, a);
    }
}

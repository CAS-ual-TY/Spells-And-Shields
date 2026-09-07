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
 * Draws every active {@link CustomParticleEmitterInstance}'s particles as camera-facing colored quads, at
 * {@link RenderLevelStageEvent.Stage#AFTER_PARTICLES}.
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
     * Position ABSOLUTE anchors to the particle's own spawnPosition; RELATIVE anchors to the entity's live
     * position. Rotation RELATIVE always uses the entity's full current yaw/pitch (never frozen at spawn),
     * composed pitch-then-yaw like vanilla's {@code Player#getRopeHoldPosition}.
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
            }

            if(emitter.rotationAttachMode == CustomParticleAttachMode.RELATIVE)
            {
                yawAngle = entityYaw;
                pitchAngle = entityPitch;
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

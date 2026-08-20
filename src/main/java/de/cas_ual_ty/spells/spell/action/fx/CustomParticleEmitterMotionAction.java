package de.cas_ual_ty.spells.spell.action.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.client.particle.CustomParticleAttachMode;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * Spawns a {@code CustomParticleEmitter} in "motion" mode - {@link #initialPosition} is evaluated once per
 * particle at spawn ({@code index} only, {@code age} is 0), {@link #motion} is re-evaluated every tick
 * client-side and integrated into position. See {@link CustomParticleEmitterPositionAction} for the other mode
 * (position recomputed fresh every tick, no integration) and {@link CustomParticleEmitterActionBase} for the
 * fields shared between both, including {@code period} (repeat spawns) and {@code initialize} (per-particle
 * one-time ctx vars).
 */
public class CustomParticleEmitterMotionAction extends CustomParticleEmitterActionBase
{
    public static Codec<CustomParticleEmitterMotionAction> makeCodec(SpellActionType<CustomParticleEmitterMotionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                countCodec(),
                durationCodec(),
                periodCodec(),
                positionAttachModeCodec(),
                rotationAttachModeCodec(),
                capturedVariablesCodec(),
                Codec.STRING.fieldOf("initial_position").forGetter(CustomParticleEmitterMotionAction::getInitialPosition),
                Codec.STRING.fieldOf("motion").forGetter(CustomParticleEmitterMotionAction::getMotion),
                colorCodec(),
                alphaCodec(),
                initializeCodec()
        ).apply(instance, (activation, multiTargets, count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, initialPosition, motion, color, alpha, initialize) ->
                new CustomParticleEmitterMotionAction(type, activation, multiTargets, count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, initialPosition, motion, color, alpha, initialize)));
    }

    public static CustomParticleEmitterMotionAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> period, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String initialPosition, String motion, String color, String alpha, List<CustomParticleInitEntry> initialize)
    {
        return new CustomParticleEmitterMotionAction(SpellActionTypes.CUSTOM_PARTICLE_EMITTER_MOTION.get(), activation.toString(), multiTargets.toString(), count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, initialPosition, motion, color, alpha, initialize);
    }

    public static CustomParticleEmitterMotionAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> period, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String initialPosition, String motion, String color, String alpha)
    {
        return make(activation, multiTargets, count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, initialPosition, motion, color, alpha, List.of());
    }

    protected String initialPosition;
    protected String motion;

    public CustomParticleEmitterMotionAction(SpellActionType<?> type)
    {
        super(type);
    }

    public CustomParticleEmitterMotionAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> period, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String initialPosition, String motion, String color, String alpha, List<CustomParticleInitEntry> initialize)
    {
        super(type, activation, multiTargets, count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, color, alpha, initialize);
        this.initialPosition = initialPosition;
        this.motion = motion;
    }

    public String getInitialPosition()
    {
        return initialPosition;
    }

    public String getMotion()
    {
        return motion;
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget entityTarget)
    {
        Entity entity = entityTarget.getEntity();

        count.getValue(ctx).ifPresent(countValue -> duration.getValue(ctx).ifPresent(durationValue -> period.getValue(ctx).ifPresent(periodValue ->
        {
            List<CtxVar<?>> captured = captureVariables(ctx);
            sendClientAction(entity, new CustomParticleEmitterClientAction(entity.getId(), positionAttachMode, rotationAttachMode, countValue, durationValue, periodValue, initialPosition, motion, "", color, alpha, captured, initialize));
        })));
    }
}

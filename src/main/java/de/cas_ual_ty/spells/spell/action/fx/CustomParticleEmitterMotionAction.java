package de.cas_ual_ty.spells.spell.action.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.client.particle.CustomParticleAttachMode;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
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
 * "Motion" mode - {@link #initialPosition} evaluated once at spawn, {@link #motion} re-evaluated every tick and
 * integrated into position. See {@link CustomParticleEmitterPositionAction} for the other mode.
 */
public class CustomParticleEmitterMotionAction extends CustomParticleEmitterActionBase
{
    public static Codec<CustomParticleEmitterMotionAction> makeCodec(SpellActionType<CustomParticleEmitterMotionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                countCodec(),
                totalLifetimeCodec(),
                periodCodec(),
                delayCodec(),
                positionAttachModeCodec(),
                rotationAttachModeCodec(),
                capturedVariablesCodec(),
                Codec.STRING.fieldOf("p1/initial_position").forGetter(CustomParticleEmitterMotionAction::getInitialPosition),
                Codec.STRING.fieldOf("p2/motion").forGetter(CustomParticleEmitterMotionAction::getMotion),
                colorCodec("p3/color"),
                alphaCodec("p4/alpha"),
                particleLifetimeCodec("p5/particle_lifetime"),
                initializeCodec()
        ).apply(instance, (activation, multiTargets, count, totalLifetime, period, delay, positionAttachMode, rotationAttachMode, capturedVariables, initialPosition, motion, color, alpha, particleLifetime, initialize) ->
                new CustomParticleEmitterMotionAction(type, activation, multiTargets, count, totalLifetime, period, delay, positionAttachMode, rotationAttachMode, capturedVariables, initialPosition, motion, color, alpha, particleLifetime, initialize)));
    }

    public static CustomParticleEmitterMotionAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> totalLifetime, DynamicCtxVar<Integer> period, DynamicCtxVar<Integer> delay, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String initialPosition, String motion, String color, String alpha, String particleLifetime, List<CustomParticleInitEntry> initialize)
    {
        return new CustomParticleEmitterMotionAction(SpellActionTypes.CUSTOM_PARTICLE_EMITTER_MOTION.get(), activation.toString(), multiTargets.toString(), count, totalLifetime, period, delay, positionAttachMode, rotationAttachMode, capturedVariables, initialPosition, motion, color, alpha, particleLifetime, initialize);
    }

    public static CustomParticleEmitterMotionAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> totalLifetime, DynamicCtxVar<Integer> period, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String initialPosition, String motion, String color, String alpha)
    {
        return make(activation, multiTargets, count, totalLifetime, period, CtxVarTypes.INT.get().immediate(0), positionAttachMode, rotationAttachMode, capturedVariables, initialPosition, motion, color, alpha, "", List.of());
    }

    protected String initialPosition;
    protected String motion;

    public CustomParticleEmitterMotionAction(SpellActionType<?> type)
    {
        super(type);
    }

    public CustomParticleEmitterMotionAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> totalLifetime, DynamicCtxVar<Integer> period, DynamicCtxVar<Integer> delay, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String initialPosition, String motion, String color, String alpha, String particleLifetime, List<CustomParticleInitEntry> initialize)
    {
        super(type, activation, multiTargets, count, totalLifetime, period, delay, positionAttachMode, rotationAttachMode, capturedVariables, color, alpha, particleLifetime, initialize);
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

        count.getValue(ctx).ifPresent(countValue -> totalLifetime.getValue(ctx).ifPresent(totalLifetimeValue -> period.getValue(ctx).ifPresent(periodValue -> delay.getValue(ctx).ifPresent(delayValue ->
        {
            List<CtxVar<?>> captured = captureVariables(ctx);
            sendClientAction(entity, new CustomParticleEmitterClientAction(entity.getId(), positionAttachMode, rotationAttachMode, countValue, totalLifetimeValue, periodValue, delayValue, initialPosition, motion, "", color, alpha, particleLifetime, captured, initialize));
        }))));
    }
}

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
 * "Position" mode - {@link #position} re-evaluated every tick, replacing the particle's position outright
 * rather than integrating a velocity. See {@link CustomParticleEmitterMotionAction} for the other mode.
 */
public class CustomParticleEmitterPositionAction extends CustomParticleEmitterActionBase
{
    public static Codec<CustomParticleEmitterPositionAction> makeCodec(SpellActionType<CustomParticleEmitterPositionAction> type)
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
                Codec.STRING.fieldOf("p1/position").forGetter(CustomParticleEmitterPositionAction::getPosition),
                colorCodec("p2/color"),
                alphaCodec("p3/alpha"),
                particleLifetimeCodec("p4/particle_lifetime"),
                initializeCodec()
        ).apply(instance, (activation, multiTargets, count, totalLifetime, period, delay, positionAttachMode, rotationAttachMode, capturedVariables, position, color, alpha, particleLifetime, initialize) ->
                new CustomParticleEmitterPositionAction(type, activation, multiTargets, count, totalLifetime, period, delay, positionAttachMode, rotationAttachMode, capturedVariables, position, color, alpha, particleLifetime, initialize)));
    }

    public static CustomParticleEmitterPositionAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> totalLifetime, DynamicCtxVar<Integer> period, DynamicCtxVar<Integer> delay, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String position, String color, String alpha, String particleLifetime, List<CustomParticleInitEntry> initialize)
    {
        return new CustomParticleEmitterPositionAction(SpellActionTypes.CUSTOM_PARTICLE_EMITTER_POSITION.get(), activation.toString(), multiTargets.toString(), count, totalLifetime, period, delay, positionAttachMode, rotationAttachMode, capturedVariables, position, color, alpha, particleLifetime, initialize);
    }

    public static CustomParticleEmitterPositionAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> totalLifetime, DynamicCtxVar<Integer> period, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String position, String color, String alpha)
    {
        return make(activation, multiTargets, count, totalLifetime, period, CtxVarTypes.INT.get().immediate(0), positionAttachMode, rotationAttachMode, capturedVariables, position, color, alpha, "", List.of());
    }

    protected String position;

    public CustomParticleEmitterPositionAction(SpellActionType<?> type)
    {
        super(type);
    }

    public CustomParticleEmitterPositionAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> totalLifetime, DynamicCtxVar<Integer> period, DynamicCtxVar<Integer> delay, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String position, String color, String alpha, String particleLifetime, List<CustomParticleInitEntry> initialize)
    {
        super(type, activation, multiTargets, count, totalLifetime, period, delay, positionAttachMode, rotationAttachMode, capturedVariables, color, alpha, particleLifetime, initialize);
        this.position = position;
    }

    public String getPosition()
    {
        return position;
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget entityTarget)
    {
        Entity entity = entityTarget.getEntity();

        count.getValue(ctx).ifPresent(countValue -> totalLifetime.getValue(ctx).ifPresent(totalLifetimeValue -> period.getValue(ctx).ifPresent(periodValue -> delay.getValue(ctx).ifPresent(delayValue ->
        {
            List<CtxVar<?>> captured = captureVariables(ctx);
            sendClientAction(entity, new CustomParticleEmitterClientAction(entity.getId(), positionAttachMode, rotationAttachMode, countValue, totalLifetimeValue, periodValue, delayValue, "", "", position, color, alpha, particleLifetime, captured, initialize));
        }))));
    }
}

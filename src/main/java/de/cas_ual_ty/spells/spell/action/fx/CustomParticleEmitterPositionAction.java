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
 * Spawns a {@code CustomParticleEmitter} in "position" mode - {@link #position} is re-evaluated directly every
 * tick client-side (against that particle's own {@code index}/{@code age}), replacing the particle's position
 * outright rather than integrating a velocity. See {@link CustomParticleEmitterMotionAction} for the other mode
 * (initial position once at spawn + per-tick motion integrated) and {@link CustomParticleEmitterActionBase} for
 * the fields shared between both, including {@code period} (repeat spawns).
 */
public class CustomParticleEmitterPositionAction extends CustomParticleEmitterActionBase
{
    public static Codec<CustomParticleEmitterPositionAction> makeCodec(SpellActionType<CustomParticleEmitterPositionAction> type)
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
                Codec.STRING.fieldOf("position").forGetter(CustomParticleEmitterPositionAction::getPosition),
                colorCodec(),
                alphaCodec()
        ).apply(instance, (activation, multiTargets, count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, position, color, alpha) ->
                new CustomParticleEmitterPositionAction(type, activation, multiTargets, count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, position, color, alpha)));
    }

    public static CustomParticleEmitterPositionAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> period, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String position, String color, String alpha)
    {
        return new CustomParticleEmitterPositionAction(SpellActionTypes.CUSTOM_PARTICLE_EMITTER_POSITION.get(), activation.toString(), multiTargets.toString(), count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, position, color, alpha);
    }

    protected String position;

    public CustomParticleEmitterPositionAction(SpellActionType<?> type)
    {
        super(type);
    }

    public CustomParticleEmitterPositionAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> count, DynamicCtxVar<Integer> duration, DynamicCtxVar<Integer> period, CustomParticleAttachMode positionAttachMode, CustomParticleAttachMode rotationAttachMode, List<String> capturedVariables, String position, String color, String alpha)
    {
        super(type, activation, multiTargets, count, duration, period, positionAttachMode, rotationAttachMode, capturedVariables, color, alpha);
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

        count.getValue(ctx).ifPresent(countValue -> duration.getValue(ctx).ifPresent(durationValue -> period.getValue(ctx).ifPresent(periodValue ->
        {
            List<CtxVar<?>> captured = captureVariables(ctx);
            sendClientAction(entity, new CustomParticleEmitterClientAction(entity.getId(), positionAttachMode, rotationAttachMode, countValue, durationValue, periodValue, "", "", position, color, alpha, captured));
        })));
    }
}

package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;

public class ResetFallDistanceAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<ResetFallDistanceAction> makeCodec(SpellActionType<ResetFallDistanceAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec()
        ).apply(instance, (activation, targets) -> new ResetFallDistanceAction(type, activation, targets)));
    }
    
    public ResetFallDistanceAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ResetFallDistanceAction(SpellActionType<?> type, String activation, String targets)
    {
        super(type, activation, targets);
    }
    
    public ResetFallDistanceAction(SpellActionType<?> type, BuiltinActivations activation, BuiltinTargetGroups targets)
    {
        this(type, activation.activation, targets.targetGroup);
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        target.getLivingEntity().fallDistance = 0;
    }
}

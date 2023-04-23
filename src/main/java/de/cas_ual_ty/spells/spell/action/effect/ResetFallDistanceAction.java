package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;

public class ResetFallDistanceAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<ResetFallDistanceAction> makeCodec(SpellActionType<ResetFallDistanceAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec()
        ).apply(instance, (activation, multiTargets) -> new ResetFallDistanceAction(type, activation, multiTargets)));
    }
    
    public static ResetFallDistanceAction make(Object activation, Object multiTargets)
    {
        return new ResetFallDistanceAction(SpellActionTypes.RESET_FALL_DISTANCE.get(), activation.toString(), multiTargets.toString());
    }
    
    public ResetFallDistanceAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ResetFallDistanceAction(SpellActionType<?> type, String activation, String multiTargets)
    {
        super(type, activation, multiTargets);
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

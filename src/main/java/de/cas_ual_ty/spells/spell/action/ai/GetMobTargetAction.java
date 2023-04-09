package de.cas_ual_ty.spells.spell.action.ai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.Mob;

public class GetMobTargetAction extends GetTargetAttributeAction<LivingEntityTarget>
{
    public static Codec<GetMobTargetAction> makeCodec(SpellActionType<GetMobTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("target")).forGetter(GetMobTargetAction::getTarget),
                Codec.STRING.fieldOf(ParamNames.var("has_target")).forGetter(GetMobTargetAction::getHasTarget)
        ).apply(instance, (activation, source, target, hasTarget) -> new GetMobTargetAction(type, activation, source, target, hasTarget)));
    }
    
    public static GetMobTargetAction make(String activation, String source, String target, String hasTarget)
    {
        return new GetMobTargetAction(SpellActionTypes.GET_MOB_TARGET.get(), activation, source, target, hasTarget);
    }
    
    protected String target;
    protected String hasTarget;
    
    public GetMobTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetMobTargetAction(SpellActionType<?> type, String activation, String source, String target, String hasTarget)
    {
        super(type, activation, source);
        this.target = target;
        this.hasTarget = hasTarget;
        
        if(!target.isEmpty())
        {
            addTargetAttribute(e -> e.getLivingEntity() instanceof Mob mob ? Target.of(mob.getTarget()) : null, target);
        }
        
        if(!hasTarget.isEmpty())
        {
            addVariableAttribute(e -> e.getLivingEntity() instanceof Mob mob ? (mob.getTarget() != null) : false, CtxVarTypes.BOOLEAN.get(), hasTarget);
        }
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public String getTarget()
    {
        return target;
    }
    
    public String getHasTarget()
    {
        return hasTarget;
    }
}

package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetEntityPositionDirectionMotionAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityPositionDirectionMotionAction> makeCodec(SpellActionType<GetEntityPositionDirectionMotionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("position")).forGetter(GetEntityPositionDirectionMotionAction::getPosition),
                Codec.STRING.fieldOf(ParamNames.var("direction")).forGetter(GetEntityPositionDirectionMotionAction::getDirection),
                Codec.STRING.fieldOf(ParamNames.var("motion")).forGetter(GetEntityPositionDirectionMotionAction::getMotion)
        ).apply(instance, (activation, source, position, direction, motion) -> new GetEntityPositionDirectionMotionAction(type, activation, source, position, direction, motion)));
    }
    
    public static GetEntityPositionDirectionMotionAction make(String activation, String source, String position, String direction, String motion)
    {
        return new GetEntityPositionDirectionMotionAction(SpellActionTypes.GET_ENTITY_POSITION_DIRECTION_MOTION.get(), activation, source, position, direction, motion);
    }
    
    protected String position;
    protected String direction;
    protected String motion;
    
    public GetEntityPositionDirectionMotionAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetEntityPositionDirectionMotionAction(SpellActionType<?> type, String activation, String source, String position, String direction, String motion)
    {
        super(type, activation, source);
        this.position = position;
        this.direction = direction;
        this.motion = motion;
        
        if(!position.isEmpty())
        {
            addTargetAttribute(e -> Target.of(e.getLevel(), e.getEntity().position()), position);
        }
        
        if(!direction.isEmpty())
        {
            addVariableAttribute(e -> e.getEntity().getLookAngle().normalize(), CtxVarTypes.VEC3.get(), direction);
        }
        
        if(!motion.isEmpty())
        {
            addVariableAttribute(e -> e.getEntity().getDeltaMovement(), CtxVarTypes.VEC3.get(), motion);
        }
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    public String getPosition()
    {
        return position;
    }
    
    public String getDirection()
    {
        return direction;
    }
    
    public String getMotion()
    {
        return motion;
    }
}

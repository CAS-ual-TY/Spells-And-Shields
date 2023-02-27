package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;

import java.util.LinkedList;
import java.util.List;

public class GetEntityPositionDirectionAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityPositionDirectionAction> makeCodec(SpellActionType<GetEntityPositionDirectionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("position")).forGetter(GetEntityPositionDirectionAction::getPosition),
                Codec.STRING.fieldOf(ParamNames.var("direction")).forGetter(GetEntityPositionDirectionAction::getDirection)
        ).apply(instance, (activation, target, position, direction) -> new GetEntityPositionDirectionAction(type, activation, target, position, direction)));
    }
    
    public static GetEntityPositionDirectionAction make(String activation, String target, String position, String direction)
    {
        return new GetEntityPositionDirectionAction(SpellActionTypes.GET_POSITION_DIRECTION.get(), activation, target, position, direction);
    }
    
    protected String position;
    protected String direction;
    
    protected List<TargetAttribute<EntityTarget, ?>> targetAttributes;
    protected List<VariableAttribute<EntityTarget, ?>> variableAttributes;
    
    public GetEntityPositionDirectionAction(SpellActionType<?> type)
    {
        super(type);
        targetAttributes = new LinkedList<>();
        variableAttributes = new LinkedList<>();
    }
    
    public GetEntityPositionDirectionAction(SpellActionType<?> type, String activation, String target, String position, String direction)
    {
        super(type, activation, target);
        this.position = position;
        this.direction = direction;
        
        targetAttributes = new LinkedList<>();
        variableAttributes = new LinkedList<>();
        
        if(!position.isEmpty())
        {
            targetAttributes.add(new TargetAttribute<>(e -> Target.of(e.getLevel(), e.getEntity().position()), position));
        }
        
        if(!direction.isEmpty())
        {
            variableAttributes.add(new VariableAttribute<>(e -> e.getEntity().getLookAngle().normalize(), CtxVarTypes.VEC3.get(), direction));
        }
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    @Override
    public List<TargetAttribute<EntityTarget, ?>> getTargetAttributes()
    {
        return targetAttributes;
    }
    
    @Override
    public List<VariableAttribute<EntityTarget, ?>> getVariableAttributes()
    {
        return variableAttributes;
    }
    
    public String getPosition()
    {
        return position;
    }
    
    public String getDirection()
    {
        return direction;
    }
}

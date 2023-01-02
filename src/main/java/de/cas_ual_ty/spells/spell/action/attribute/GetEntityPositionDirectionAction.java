package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.util.ParamNames;

import java.util.LinkedList;
import java.util.List;

public class GetEntityPositionDirectionAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityPositionDirectionAction> makeCodec(SpellActionType<GetEntityPositionDirectionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                Codec.STRING.fieldOf(ParamNames.var("position")).forGetter(GetEntityPositionDirectionAction::getPosition),
                Codec.STRING.fieldOf(ParamNames.var("direction")).forGetter(GetEntityPositionDirectionAction::getDirection)
        ).apply(instance, (activation, targets, position, direction) -> new GetEntityPositionDirectionAction(type, activation, targets, position, direction)));
    }
    
    protected String position;
    protected String direction;
    
    protected List<TargetAttribute<EntityTarget, ?>> attributes;
    
    public GetEntityPositionDirectionAction(SpellActionType<?> type)
    {
        super(type);
        attributes = new LinkedList<>();
    }
    
    public GetEntityPositionDirectionAction(SpellActionType<?> type, String activation, String targets, String position, String direction)
    {
        super(type, activation, targets);
        this.position = position;
        this.direction = direction;
        
        attributes = new LinkedList<>();
        
        if(!position.isEmpty())
        {
            attributes.add(new TargetAttribute<>(e -> e.getEntity().position(), CtxVarTypes.VEC3.get(), position));
        }
        
        if(!direction.isEmpty())
        {
            attributes.add(new TargetAttribute<>(e -> e.getEntity().getLookAngle().normalize(), CtxVarTypes.VEC3.get(), direction));
        }
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    @Override
    public List<TargetAttribute<EntityTarget, ?>> getAttributes()
    {
        return attributes;
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

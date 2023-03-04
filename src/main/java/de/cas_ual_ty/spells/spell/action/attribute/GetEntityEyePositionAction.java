package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetEntityEyePositionAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityEyePositionAction> makeCodec(SpellActionType<GetEntityEyePositionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("eye_position")).forGetter(GetEntityEyePositionAction::getEyePosition)
        ).apply(instance, (activation, target, eyePosition) -> new GetEntityEyePositionAction(type, activation, target, eyePosition)));
    }
    
    public static GetEntityEyePositionAction make(String activation, String target, String eyePosition)
    {
        return new GetEntityEyePositionAction(SpellActionTypes.GET_ENTITY_EYE_POSITION.get(), activation, target, eyePosition);
    }
    
    protected String eyePosition;
    
    public GetEntityEyePositionAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetEntityEyePositionAction(SpellActionType<?> type, String activation, String target, String eyePosition)
    {
        super(type, activation, target);
        this.eyePosition = eyePosition;
        
        if(!eyePosition.isEmpty())
        {
            addTargetAttribute(e -> Target.of(e.getLevel(), e.getEntity().getEyePosition()), eyePosition);
        }
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    public String getEyePosition()
    {
        return eyePosition;
    }
}

package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetPositionAction extends GetTargetAttributeAction<PositionTarget>
{
    public static Codec<GetPositionAction> makeCodec(SpellActionType<GetPositionAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("position")).forGetter(GetPositionAction::getPosition)
        ).apply(instance, (activation, source, position) -> new GetPositionAction(type, activation, source, position)));
    }
    
    public static GetPositionAction make(String activation, String source, String position)
    {
        return new GetPositionAction(SpellActionTypes.GET_POSITION.get(), activation, source, position);
    }
    
    protected String position;
    
    public GetPositionAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetPositionAction(SpellActionType<?> type, String activation, String source, String position)
    {
        super(type, activation, source);
        this.position = position;
        
        if(!position.isEmpty())
        {
            addVariableAttribute(PositionTarget::getPosition, CtxVarTypes.VEC3.get(), position);
        }
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    public String getPosition()
    {
        return position;
    }
}

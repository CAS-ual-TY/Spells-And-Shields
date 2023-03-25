package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetIsCreativeAction extends GetTargetAttributeAction<PlayerTarget>
{
    public static Codec<GetIsCreativeAction> makeCodec(SpellActionType<GetIsCreativeAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("is_creative")).forGetter(GetIsCreativeAction::getIsCreative)
        ).apply(instance, (activation, source, isCreative) -> new GetIsCreativeAction(type, activation, source, isCreative)));
    }
    
    public static GetIsCreativeAction make(String activation, String source, String isCreative)
    {
        return new GetIsCreativeAction(SpellActionTypes.GET_IS_CREATIVE.get(), activation, source, isCreative);
    }
    
    protected String isCreative;
    
    public GetIsCreativeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetIsCreativeAction(SpellActionType<?> type, String activation, String source, String isCreative)
    {
        super(type, activation, source);
        this.isCreative = isCreative;
        
        if(!isCreative.isEmpty())
        {
            addVariableAttribute(p -> p.getPlayer().isCreative(), CtxVarTypes.BOOLEAN.get(), isCreative);
        }
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    public String getIsCreative()
    {
        return isCreative;
    }
}

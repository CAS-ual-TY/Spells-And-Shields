package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetItemTagAction extends GetTargetAttributeAction<ItemTarget>
{
    public static Codec<GetItemTagAction> makeCodec(SpellActionType<GetItemTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("compound_tag")).forGetter(GetItemTagAction::getCompoundTag)
        ).apply(instance, (activation, target, compoundTag) -> new GetItemTagAction(type, activation, target, compoundTag)));
    }
    
    public static GetItemTagAction make(String activation, String target, String compoundTag)
    {
        return new GetItemTagAction(SpellActionTypes.GET_ITEM_TAG.get(), activation, target, compoundTag);
    }
    
    protected String compoundTag;
    
    public GetItemTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetItemTagAction(SpellActionType<?> type, String activation, String target, String compoundTag)
    {
        super(type, activation, target);
        this.compoundTag = compoundTag;
        
        if(!compoundTag.isEmpty())
        {
            addVariableAttribute(i -> i.getItem().getOrCreateTag(), CtxVarTypes.COMPOUND_TAG.get(), compoundTag);
        }
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
    
    public String getCompoundTag()
    {
        return compoundTag;
    }
}
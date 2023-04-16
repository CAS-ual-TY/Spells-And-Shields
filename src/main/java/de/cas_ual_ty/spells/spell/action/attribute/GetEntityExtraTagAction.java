package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ExtraTagHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetEntityExtraTagAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityExtraTagAction> makeCodec(SpellActionType<GetEntityExtraTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("compound_tag")).forGetter(GetEntityExtraTagAction::getCompoundTag)
        ).apply(instance, (activation, source, compoundTag) -> new GetEntityExtraTagAction(type, activation, source, compoundTag)));
    }
    
    public static GetEntityExtraTagAction make(String activation, String source, String compoundTag)
    {
        return new GetEntityExtraTagAction(SpellActionTypes.GET_ENTITY_EXTRA_TAG.get(), activation, source, compoundTag);
    }
    
    protected String compoundTag;
    
    public GetEntityExtraTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetEntityExtraTagAction(SpellActionType<?> type, String activation, String source, String compoundTag)
    {
        super(type, activation, source);
        this.compoundTag = compoundTag;
        
        if(!compoundTag.isEmpty())
        {
            addVariableAttribute(e -> ExtraTagHolder.getHolder(e.getEntity()).lazyMap(ExtraTagHolder::getExtraTag).orElse(null), CtxVarTypes.TAG.get(), compoundTag);
        }
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    public String getCompoundTag()
    {
        return compoundTag;
    }
}

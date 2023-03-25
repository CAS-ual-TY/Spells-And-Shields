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
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.nbt.CompoundTag;

public class GetEntityTagAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityTagAction> makeCodec(SpellActionType<GetEntityTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("compound_tag")).forGetter(GetEntityTagAction::getCompoundTag)
        ).apply(instance, (activation, source, compoundTag) -> new GetEntityTagAction(type, activation, source, compoundTag)));
    }
    
    public static GetEntityTagAction make(String activation, String source, String compoundTag)
    {
        return new GetEntityTagAction(SpellActionTypes.GET_ENTITY_TAG.get(), activation, source, compoundTag);
    }
    
    protected String compoundTag;
    
    public GetEntityTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetEntityTagAction(SpellActionType<?> type, String activation, String source, String compoundTag)
    {
        super(type, activation, source);
        this.compoundTag = compoundTag;
        
        if(!compoundTag.isEmpty())
        {
            addVariableAttribute(e -> e.getEntity().saveWithoutId(new CompoundTag()), CtxVarTypes.COMPOUND_TAG.get(), compoundTag);
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

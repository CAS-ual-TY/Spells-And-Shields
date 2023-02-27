package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.nbt.CompoundTag;

import java.util.LinkedList;
import java.util.List;

public class GetEntityTagAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityTagAction> makeCodec(SpellActionType<GetEntityTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("compound_tag")).forGetter(GetEntityTagAction::getCompoundTag)
        ).apply(instance, (activation, target, compoundTag) -> new GetEntityTagAction(type, activation, target, compoundTag)));
    }
    
    public static GetEntityTagAction make(String activation, String target, String compoundTag)
    {
        return new GetEntityTagAction(SpellActionTypes.GET_POSITION_DIRECTION.get(), activation, target, compoundTag);
    }
    
    protected String compoundTag;
    
    protected List<TargetAttribute<EntityTarget, ?>> targetAttributes;
    protected List<VariableAttribute<EntityTarget, ?>> variableAttributes;
    
    public GetEntityTagAction(SpellActionType<?> type)
    {
        super(type);
        targetAttributes = new LinkedList<>();
        variableAttributes = new LinkedList<>();
    }
    
    public GetEntityTagAction(SpellActionType<?> type, String activation, String target, String compoundTag)
    {
        super(type, activation, target);
        this.compoundTag = compoundTag;
        
        targetAttributes = new LinkedList<>();
        variableAttributes = new LinkedList<>();
        
        if(!compoundTag.isEmpty())
        {
            variableAttributes.add(new VariableAttribute<>(e -> e.getEntity().saveWithoutId(new CompoundTag()), CtxVarTypes.COMPOUND_TAG.get(), compoundTag));
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
    
    public String getCompoundTag()
    {
        return compoundTag;
    }
}

package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraftforge.registries.ForgeRegistries;

public class GetItemAttributesAction extends GetTargetAttributeAction<ItemTarget>
{
    public static Codec<GetItemAttributesAction> makeCodec(SpellActionType<GetItemAttributesAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("item")).forGetter(GetItemAttributesAction::getItem),
                Codec.STRING.fieldOf(ParamNames.var("amount")).forGetter(GetItemAttributesAction::getAmount),
                Codec.STRING.fieldOf(ParamNames.var("damage")).forGetter(GetItemAttributesAction::getDamage),
                Codec.STRING.fieldOf(ParamNames.var("compound_tag")).forGetter(GetItemAttributesAction::getCompoundTag)
        ).apply(instance, (activation, source, item, amount, damage, compoundTag) -> new GetItemAttributesAction(type, activation, source, item, amount, damage, compoundTag)));
    }
    
    public static GetItemAttributesAction make(String activation, String source, String item, String amount, String damage, String compoundTag)
    {
        return new GetItemAttributesAction(SpellActionTypes.GET_ITEM_ATTRIBUTES.get(), activation, source, item, amount, damage, compoundTag);
    }
    
    protected String item;
    protected String amount;
    protected String damage;
    protected String compoundTag;
    
    public GetItemAttributesAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetItemAttributesAction(SpellActionType<?> type, String activation, String source, String item, String amount, String damage, String compoundTag)
    {
        super(type, activation, source);
        this.item = item;
        this.amount = amount;
        this.damage = damage;
        this.compoundTag = compoundTag;
        
        if(!item.isEmpty())
        {
            addVariableAttribute(i -> ForgeRegistries.ITEMS.getKey(i.getItem().getItem()).toString(), CtxVarTypes.STRING.get(), item);
        }
        
        if(!amount.isEmpty())
        {
            addVariableAttribute(i -> i.getItem().getCount(), CtxVarTypes.INT.get(), amount);
        }
        
        if(!damage.isEmpty())
        {
            addVariableAttribute(i -> i.getItem().getDamageValue(), CtxVarTypes.INT.get(), damage);
        }
        
        if(!compoundTag.isEmpty())
        {
            addVariableAttribute(i -> i.getItem().getOrCreateTag(), CtxVarTypes.TAG.get(), compoundTag);
        }
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
    
    public String getItem()
    {
        return item;
    }
    
    public String getAmount()
    {
        return amount;
    }
    
    public String getDamage()
    {
        return damage;
    }
    
    public String getCompoundTag()
    {
        return compoundTag;
    }
}

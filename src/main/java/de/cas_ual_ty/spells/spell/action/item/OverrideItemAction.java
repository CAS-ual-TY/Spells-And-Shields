package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class OverrideItemAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<OverrideItemAction> makeCodec(SpellActionType<OverrideItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(OverrideItemAction::getAmount),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("damage")).forGetter(OverrideItemAction::getDamage),
                CtxVarTypes.COMPOUND_TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("tag")).forGetter(OverrideItemAction::getTag),
                ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(OverrideItemAction::getItem)
        ).apply(instance, (activation, target, amount, damage, tag, item) -> new OverrideItemAction(type, activation, target, amount, damage, tag, item)));
    }
    
    public static OverrideItemAction make(String activation, String target, DynamicCtxVar<Integer> amount, DynamicCtxVar<Integer> damage, DynamicCtxVar<CompoundTag> tag, Item item)
    {
        return new OverrideItemAction(SpellActionTypes.OVERRIDE_ITEM.get(), activation, target, amount, damage, tag, item);
    }
    
    protected DynamicCtxVar<Integer> amount;
    protected DynamicCtxVar<Integer> damage;
    protected DynamicCtxVar<CompoundTag> tag;
    protected Item item;
    
    public OverrideItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public OverrideItemAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Integer> amount, DynamicCtxVar<Integer> damage, DynamicCtxVar<CompoundTag> tag, Item item)
    {
        super(type, activation, targets);
        this.amount = amount;
        this.damage = damage;
        this.tag = tag;
        this.item = item;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    public DynamicCtxVar<Integer> getDamage()
    {
        return damage;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    public Item getItem()
    {
        return item;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        ItemStack newStack = new ItemStack(item);
        
        amount.getValue(ctx).ifPresent(amount ->
        {
            if(amount >= 0)
            {
                newStack.setCount(amount);
            }
        });
        
        if(newStack.isEmpty())
        {
            return;
        }
        
        damage.getValue(ctx).ifPresent(damage ->
        {
            if(damage >= 0)
            {
                newStack.setDamageValue(damage);
            }
        });
        
        tag.getValue(ctx).ifPresent(newStack::setTag);
        
        itemTarget.modify(newStack);
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
}

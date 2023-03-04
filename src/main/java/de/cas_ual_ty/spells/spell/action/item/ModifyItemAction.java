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
import net.minecraft.world.item.ItemStack;

public class ModifyItemAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<ModifyItemAction> makeCodec(SpellActionType<ModifyItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(ModifyItemAction::getAmount),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("damage")).forGetter(ModifyItemAction::getDamage),
                CtxVarTypes.COMPOUND_TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("tag")).forGetter(ModifyItemAction::getTag)
        ).apply(instance, (activation, target, amount, damage, tag) -> new ModifyItemAction(type, activation, target, amount, damage, tag)));
    }
    
    public static ModifyItemAction make(String activation, String target, DynamicCtxVar<Integer> amount, DynamicCtxVar<Integer> damage, DynamicCtxVar<CompoundTag> tag)
    {
        return new ModifyItemAction(SpellActionTypes.MODIFY_ITEM.get(), activation, target, amount, damage, tag);
    }
    
    protected DynamicCtxVar<Integer> amount;
    protected DynamicCtxVar<Integer> damage;
    protected DynamicCtxVar<CompoundTag> tag;
    
    public ModifyItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ModifyItemAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Integer> amount, DynamicCtxVar<Integer> damage, DynamicCtxVar<CompoundTag> tag)
    {
        super(type, activation, targets);
        this.amount = amount;
        this.damage = damage;
        this.tag = tag;
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
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        ItemStack itemStack = itemTarget.getItem();
        
        amount.getValue(ctx).ifPresent(amount ->
        {
            if(amount >= 0)
            {
                itemStack.setCount(amount);
            }
        });
        
        damage.getValue(ctx).ifPresent(damage ->
        {
            if(damage >= 0)
            {
                itemStack.setDamageValue(damage);
            }
        });
        
        tag.getValue(ctx).ifPresent(itemStack::setTag);
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
}

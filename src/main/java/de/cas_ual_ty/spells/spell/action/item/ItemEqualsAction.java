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
import net.minecraft.world.item.ItemStack;

public class ItemEqualsAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<ItemEqualsAction> makeCodec(SpellActionType<ItemEqualsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                ItemStack.CODEC.fieldOf("item").forGetter(ItemEqualsAction::getItem),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("ignore_tag")).forGetter(ItemEqualsAction::getIgnoreTag),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("minimum_count")).forGetter(ItemEqualsAction::getMinimumCount),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("minimum_durability")).forGetter(ItemEqualsAction::getMinimumDurability)
        ).apply(instance, (activation, singleTarget, item, ignoreTag, minimumCount, minimumDurability) -> new ItemEqualsAction(type, activation, singleTarget, item, ignoreTag, minimumCount, minimumDurability)));
    }
    
    public static ItemEqualsAction make(Object activation, Object singleTarget, ItemStack item, DynamicCtxVar<Boolean> ignoreTag, DynamicCtxVar<Integer> minimumCount, DynamicCtxVar<Integer> minimumDurability)
    {
        return new ItemEqualsAction(SpellActionTypes.ITEM_EQUALS.get(), activation.toString(), singleTarget.toString(), item, ignoreTag, minimumCount, minimumDurability);
    }
    
    protected ItemStack item;
    protected DynamicCtxVar<Boolean> ignoreTag;
    protected DynamicCtxVar<Integer> minimumCount;
    protected DynamicCtxVar<Integer> minimumDurability;
    
    public ItemEqualsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ItemEqualsAction(SpellActionType<?> type, String activation, String singleTarget, ItemStack item, DynamicCtxVar<Boolean> ignoreTag, DynamicCtxVar<Integer> minimumCount, DynamicCtxVar<Integer> minimumDurability)
    {
        super(type, activation, singleTarget);
        this.item = item;
        this.ignoreTag = ignoreTag;
        this.minimumCount = minimumCount;
        this.minimumDurability = minimumDurability;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    public DynamicCtxVar<Boolean> getIgnoreTag()
    {
        return ignoreTag;
    }
    
    public DynamicCtxVar<Integer> getMinimumCount()
    {
        return minimumCount;
    }
    
    public DynamicCtxVar<Integer> getMinimumDurability()
    {
        return minimumDurability;
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        minimumCount.getValue(ctx).ifPresent(minimumCount ->
        {
            minimumDurability.getValue(ctx).ifPresent(minimumDurability ->
            {
                ignoreTag.getValue(ctx).ifPresent(ignoreTag ->
                {
                    if(!itemTarget.getItem().is(item.getItem()))
                    {
                        ctx.deactivate(activation);
                        return;
                    }
                    
                    if(minimumCount >= 0 && itemTarget.getItem().getCount() < minimumCount)
                    {
                        ctx.deactivate(activation);
                        return;
                    }
                    
                    if(minimumDurability >= 0 && itemTarget.getItem().getMaxDamage() - itemTarget.getItem().getDamageValue() < minimumDurability)
                    {
                        ctx.deactivate(activation);
                        return;
                    }
                    
                    if(!ignoreTag && !ItemStack.tagMatches(item, itemTarget.getItem()))
                    {
                        ctx.deactivate(activation);
                    }
                });
            });
        });
    }
}

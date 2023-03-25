package de.cas_ual_ty.spells.spell.action.target.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.FilterTargetsAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;

public class ItemFilterAction extends FilterTargetsAction
{
    public static Codec<ItemFilterAction> makeCodec(SpellActionType<ItemFilterAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec(),
                ItemStack.CODEC.fieldOf("item").forGetter(ItemFilterAction::getItem),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("ignore_tag")).forGetter(ItemFilterAction::getIgnoreTag),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("minimum_count")).forGetter(ItemFilterAction::getMinimumCount),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("minimum_durability")).forGetter(ItemFilterAction::getMinimumDurability)
        ).apply(instance, (activation, dst, src, item, ignoreTag, minimumCount, minimumDurability) -> new ItemFilterAction(type, activation, dst, src, item, ignoreTag, minimumCount, minimumDurability)));
    }
    
    public static ItemFilterAction make(String activation, String dst, String src, ItemStack item, DynamicCtxVar<Boolean> ignoreTag, DynamicCtxVar<Integer> minimumCount, DynamicCtxVar<Integer> minimumDurability)
    {
        return new ItemFilterAction(SpellActionTypes.ITEM_FILTER.get(), activation, dst, src, item, ignoreTag, minimumCount, minimumDurability);
    }
    
    protected ItemStack item;
    protected DynamicCtxVar<Boolean> ignoreTag;
    protected DynamicCtxVar<Integer> minimumCount;
    protected DynamicCtxVar<Integer> minimumDurability;
    
    public ItemFilterAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ItemFilterAction(SpellActionType<?> type, String activation, String dst, String src, ItemStack item, DynamicCtxVar<Boolean> ignoreTag, DynamicCtxVar<Integer> minimumCount, DynamicCtxVar<Integer> minimumDurability)
    {
        super(type, activation, dst, src);
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
    protected boolean acceptTarget(SpellContext ctx, Target target)
    {
        AtomicBoolean ret = new AtomicBoolean(false);
        
        minimumCount.getValue(ctx).ifPresent(minimumCount ->
        {
            minimumDurability.getValue(ctx).ifPresent(minimumDurability ->
            {
                ignoreTag.getValue(ctx).ifPresent(ignoreTag ->
                {
                    TargetTypes.ITEM.get().ifType(target, t ->
                    {
                        if(!t.getItem().is(this.item.getItem()))
                        {
                            return;
                        }
                        
                        if(t.getItem().getCount() <= minimumCount)
                        {
                            return;
                        }
                        
                        if(t.getItem().getMaxDamage() - t.getItem().getDamageValue() <= minimumDurability)
                        {
                            return;
                        }
                        
                        if(!ignoreTag && !ItemStack.tagMatches(item, t.getItem()))
                        {
                            return;
                        }
                        
                        ret.set(true);
                    });
                });
            });
        });
        
        return ret.get();
    }
}

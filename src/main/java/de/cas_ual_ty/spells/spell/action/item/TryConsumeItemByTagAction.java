package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TryConsumeItemByTagAction extends AffectTypeAction<PlayerTarget>
{
    public static Codec<TryConsumeItemByTagAction> makeCodec(SpellActionType<TryConsumeItemByTagAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("must_be_in_hand")).forGetter(TryConsumeItemByTagAction::getMustBeInHand),
                TagKey.codec(BuiltInRegistries.ITEM.key()).fieldOf("item_tag").forGetter(TryConsumeItemByTagAction::getItemTag),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("count")).forGetter(TryConsumeItemByTagAction::getCount)
        ).apply(instance, (activation, target, mustBeInHand, itemTag, count) -> new TryConsumeItemByTagAction(type, activation, target, mustBeInHand, itemTag, count)));
    }
    
    public static TryConsumeItemByTagAction make(Object activation, Object targets, DynamicCtxVar<Boolean> mustBeInHand, TagKey<Item> itemTag, DynamicCtxVar<Integer> count)
    {
        return new TryConsumeItemByTagAction(SpellActionTypes.TRY_CONSUME_ITEM_BY_TAG.get(), activation.toString(), targets.toString(), mustBeInHand, itemTag, count);
    }
    
    protected DynamicCtxVar<Boolean> mustBeInHand;
    protected TagKey<Item> itemTag;
    protected DynamicCtxVar<Integer> count;
    
    public TryConsumeItemByTagAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TryConsumeItemByTagAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Boolean> mustBeInHand, TagKey<Item> itemTag, DynamicCtxVar<Integer> count)
    {
        super(type, activation, targets);
        this.mustBeInHand = mustBeInHand;
        this.itemTag = itemTag;
        this.count = count;
    }
    
    public DynamicCtxVar<Boolean> getMustBeInHand()
    {
        return mustBeInHand;
    }
    
    public TagKey<Item> getItemTag()
    {
        return itemTag;
    }
    
    public DynamicCtxVar<Integer> getCount()
    {
        return count;
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        mustBeInHand.getValue(ctx).ifPresent(mustBeInHand ->
        {
            count.getValue(ctx).ifPresent(count ->
            {
                Player player = playerTarget.getPlayer();
                
                if(mustBeInHand)
                {
                    for(ItemStack itemStack : player.getHandSlots())
                    {
                        if(itemStack.is(itemTag) && itemStack.getCount() >= count)
                        {
                            if(!player.isCreative())
                            {
                                itemStack.shrink(count);
                            }
                            
                            return;
                        }
                    }
                    
                    ctx.deactivate(activation);
                }
                else
                {
                    if(player.isCreative())
                    {
                        return;
                    }
                    
                    for(ItemStack itemStack : player.getInventory().items)
                    {
                        if(itemStack.is(itemTag) && itemStack.getCount() >= count)
                        {
                            itemStack.shrink(count);
                            return;
                        }
                    }
                    
                    ItemStack itemStack = player.getOffhandItem();
                    if(itemStack.is(itemTag) && itemStack.getCount() >= count)
                    {
                        itemStack.shrink(count);
                        return;
                    }
                    
                    ctx.deactivate(activation);
                }
            });
        });
    }
}

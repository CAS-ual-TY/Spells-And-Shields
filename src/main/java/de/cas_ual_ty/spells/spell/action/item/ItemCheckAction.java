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
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemCheckAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<ItemCheckAction> makeCodec(SpellActionType<ItemCheckAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("must_be_in_hand")).forGetter(ItemCheckAction::getMustBeInHand),
                ItemStack.CODEC.fieldOf("item").forGetter(ItemCheckAction::getItem)
        ).apply(instance, (activation, singleTarget, mustBeInHand, item) -> new ItemCheckAction(type, activation, singleTarget, mustBeInHand, item)));
    }
    
    public static ItemCheckAction make(Object activation, Object singleTarget, DynamicCtxVar<Boolean> mustBeInHand, ItemStack item)
    {
        return new ItemCheckAction(SpellActionTypes.ITEM_CHECK.get(), activation.toString(), singleTarget.toString(), mustBeInHand, item);
    }
    
    protected DynamicCtxVar<Boolean> mustBeInHand;
    protected ItemStack item;
    
    public ItemCheckAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ItemCheckAction(SpellActionType<?> type, String activation, String singleTarget, DynamicCtxVar<Boolean> mustBeInHand, ItemStack item)
    {
        super(type, activation, singleTarget);
        this.mustBeInHand = mustBeInHand;
        this.item = item;
    }
    
    public DynamicCtxVar<Boolean> getMustBeInHand()
    {
        return mustBeInHand;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        mustBeInHand.getValue(ctx).ifPresent(mustBeInHand ->
        {
            Player player = playerTarget.getPlayer();
            
            if(mustBeInHand)
            {
                for(ItemStack itemStack : player.getHandSlots())
                {
                    if(itemStack.is(item.getItem()) && itemStack.getCount() >= this.item.getCount() && itemStack.areShareTagsEqual(item))
                    {
                        if(!player.isCreative())
                        {
                            itemStack.shrink(item.getCount());
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
                    if(itemStack.is(item.getItem()) && itemStack.getCount() >= this.item.getCount() && itemStack.areShareTagsEqual(item))
                    {
                        itemStack.shrink(item.getCount());
                        return;
                    }
                }
                
                ItemStack itemStack = player.getOffhandItem();
                if(itemStack.is(item.getItem()) && itemStack.getCount() >= this.item.getCount() && itemStack.areShareTagsEqual(item))
                {
                    itemStack.shrink(item.getCount());
                    return;
                }
                
                ctx.deactivate(activation);
            }
        });
    }
}

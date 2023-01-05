package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SimpleItemCheckAction extends AffectTypeAction<PlayerTarget>
{
    public static Codec<SimpleItemCheckAction> makeCodec(SpellActionType<SimpleItemCheckAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetCodec(),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf("must_be_in_hand").forGetter(SimpleItemCheckAction::getMustBeInHand),
                ItemStack.CODEC.fieldOf("item").forGetter(SimpleItemCheckAction::getItem)
        ).apply(instance, (activation, target, mustBeInHand, item) -> new SimpleItemCheckAction(type, activation, target, mustBeInHand, item)));
    }
    
    public static SimpleItemCheckAction make(String activation, String targets, DynamicCtxVar<Boolean> mustBeInHand, ItemStack item)
    {
        return new SimpleItemCheckAction(SpellActionTypes.SIMPLE_ITEM_CHECK.get(), activation, targets, mustBeInHand, item);
    }
    
    protected DynamicCtxVar<Boolean> mustBeInHand;
    protected ItemStack item;
    
    public SimpleItemCheckAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SimpleItemCheckAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Boolean> mustBeInHand, ItemStack item)
    {
        super(type, activation, targets);
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
    public void affectTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        Player player = playerTarget.getPlayer();
        
        if(player.isCreative())
        {
            return;
        }
        
        for(ItemStack itemStack : player.getHandSlots())
        {
            if(itemStack.is(item.getItem()) && itemStack.getCount() >= this.item.getCount() && itemStack.areShareTagsEqual(item))
            {
                itemStack.shrink(item.getCount());
                return;
            }
        }
        
        ctx.deactivate(activation);
    }
}

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
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FindItemAction extends AffectTypeAction<PlayerTarget>
{
    public static Codec<FindItemAction> makeCodec(SpellActionType<FindItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("must_be_in_hand")).forGetter(FindItemAction::getMustBeInHand),
                ItemStack.CODEC.fieldOf("item").forGetter(FindItemAction::getItem),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("found_items")).forGetter(FindItemAction::getFoundItems)
        ).apply(instance, (activation, target, mustBeInHand, item, foundItems) -> new FindItemAction(type, activation, target, mustBeInHand, item, foundItems)));
    }
    
    public static FindItemAction make(String activation, String multiTargets, DynamicCtxVar<Boolean> mustBeInHand, ItemStack item, String foundItems)
    {
        return new FindItemAction(SpellActionTypes.FIND_ITEM.get(), activation, multiTargets, mustBeInHand, item, foundItems);
    }
    
    protected DynamicCtxVar<Boolean> mustBeInHand;
    protected ItemStack item;
    protected String foundItems;
    
    public FindItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public FindItemAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Boolean> mustBeInHand, ItemStack item, String foundItems)
    {
        super(type, activation, multiTargets);
        this.mustBeInHand = mustBeInHand;
        this.item = item;
        this.foundItems = foundItems;
    }
    
    public DynamicCtxVar<Boolean> getMustBeInHand()
    {
        return mustBeInHand;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    public String getFoundItems()
    {
        return foundItems;
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
            Player player = playerTarget.getPlayer();
            TargetGroup foundItems = ctx.getOrCreateTargetGroup(this.foundItems);
            
            if(mustBeInHand)
            {
                for(EquipmentSlot slot : EquipmentSlot.values())
                {
                    if(slot.getType() != EquipmentSlot.Type.HAND)
                    {
                        continue;
                    }
                    
                    ItemStack itemStack = player.getItemBySlot(slot);
                    
                    if(itemStack.is(item.getItem()) && itemStack.getCount() >= this.item.getCount() && itemStack.areShareTagsEqual(item))
                    {
                        foundItems.addTargets(ItemTarget.of(ctx.level, itemStack, newItem -> player.setItemSlot(slot, newItem), player.isCreative()));
                    }
                }
            }
            else
            {
                for(int i = 0; i < player.getInventory().getContainerSize(); i++)
                {
                    ItemStack itemStack = player.getInventory().getItem(i);
                    
                    if(itemStack.is(item.getItem()) && itemStack.getCount() >= this.item.getCount() && itemStack.areShareTagsEqual(item))
                    {
                        final int finalI = i;
                        foundItems.addTargets(ItemTarget.of(ctx.level, itemStack, newItem -> player.getInventory().setItem(finalI, newItem), player.isCreative()));
                    }
                }
                
                for(EquipmentSlot slot : EquipmentSlot.values())
                {
                    if(slot == EquipmentSlot.MAINHAND)
                    {
                        continue;
                    }
                    
                    ItemStack itemStack = player.getItemBySlot(slot);
                    
                    if(itemStack.is(item.getItem()) && itemStack.getCount() >= this.item.getCount() && itemStack.areShareTagsEqual(item))
                    {
                        foundItems.addTargets(ItemTarget.of(ctx.level, itemStack, newItem -> player.setItemSlot(slot, newItem), player.isCreative()));
                    }
                }
            }
        });
    }
}

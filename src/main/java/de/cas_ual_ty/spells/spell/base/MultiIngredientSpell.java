package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class MultiIngredientSpell extends Spell
{
    public static final String KEY_REQUIRED_HAND = "spell.ingredients.hand";
    public static final String KEY_REQUIRED_INVENTORY = "spell.ingredients.inventory";
    
    public MultiIngredientSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public void perform(ManaHolder manaHolder, Optional<List<ItemStack>> handIngredients, Optional<List<ItemStack>> inventoryIngredients)
    {
        perform(manaHolder);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
    }
    
    @Override
    public boolean activate(ManaHolder manaHolder)
    {
        if(this.canActivate(manaHolder))
        {
            Optional<List<ItemStack>> handIngredients = this.hasHandIngredients(manaHolder);
            Optional<List<ItemStack>> inventoryIngredients = this.hasInventoryIngredients(manaHolder);
            
            if(manaHolder.getPlayer() instanceof Player player)
            {
                if((handIngredients.isPresent() && inventoryIngredients.isPresent()) || player.isCreative())
                {
                    this.perform(manaHolder, handIngredients, inventoryIngredients);
                    
                    if(!player.level.isClientSide && !player.isCreative())
                    {
                        this.burnMana(manaHolder);
                        this.consumeItemStacks(manaHolder, handIngredients.get(), inventoryIngredients.get());
                    }
                    
                    return true;
                }
            }
            else
            {
                this.perform(manaHolder, handIngredients, inventoryIngredients);
                
                if(!manaHolder.getPlayer().level.isClientSide)
                {
                    this.consumeItemStacks(manaHolder, List.of(), List.of());
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    public List<ItemStack> findHandIngredients(ManaHolder manaHolder)
    {
        List<ItemStack> foundList = new LinkedList<>();
        
        for(ItemStack required : getRequiredHandIngredients())
        {
            for(ItemStack toTest : manaHolder.getPlayer().getHandSlots())
            {
                if(this.checkHandIngredient(manaHolder, required, toTest))
                {
                    foundList.add(toTest);
                    break;
                }
            }
        }
        
        return foundList;
    }
    
    public List<ItemStack> findInventoryIngredients(ManaHolder manaHolder)
    {
        List<ItemStack> foundList = new LinkedList<>();
        
        if(manaHolder.getPlayer() instanceof Player player)
        {
            for(ItemStack required : getRequiredInventoryIngredients())
            {
                for(ItemStack toTest : player.getInventory().items)
                {
                    if(toTest != player.getMainHandItem() && this.checkInventoryIngredient(manaHolder, required, toTest))
                    {
                        foundList.add(toTest);
                        break;
                    }
                }
            }
        }
        
        return foundList;
    }
    
    public Optional<List<ItemStack>> hasHandIngredients(ManaHolder manaHolder)
    {
        List<ItemStack> handIngredients = findHandIngredients(manaHolder);
        return getRequiredHandIngredients().size() == handIngredients.size() ? Optional.of(handIngredients) : Optional.empty();
    }
    
    public Optional<List<ItemStack>> hasInventoryIngredients(ManaHolder manaHolder)
    {
        if(manaHolder.getPlayer() instanceof Player)
        {
            List<ItemStack> inventoryIngredients = findInventoryIngredients(manaHolder);
            return getRequiredInventoryIngredients().size() == inventoryIngredients.size() ? Optional.of(inventoryIngredients) : Optional.empty();
        }
        else
        {
            return SpellsUtil.EMPTY_ITEMSTACK_LIST_OPTIONAL;
        }
    }
    
    public abstract List<ItemStack> getRequiredHandIngredients();
    
    public abstract List<ItemStack> getRequiredInventoryIngredients();
    
    public boolean checkHandIngredient(ManaHolder manaHolder, ItemStack required, ItemStack toTest)
    {
        return required.isEmpty() || (toTest.getItem() == required.getItem() && toTest.getCount() >= required.getCount());
    }
    
    public boolean checkInventoryIngredient(ManaHolder manaHolder, ItemStack required, ItemStack toTest)
    {
        return required.isEmpty() || (toTest.getItem() == required.getItem() && toTest.getCount() >= required.getCount());
    }
    
    public abstract void consumeItemStacks(ManaHolder manaHolder, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients);
    
    @Override
    public void addSpellDesc(List<Component> list)
    {
        super.addSpellDesc(list);
        
        List<ItemStack> handIngredients = this.getRequiredHandIngredients();
        List<ItemStack> inventoryIngredients = this.getRequiredInventoryIngredients();
        
        if(!handIngredients.isEmpty())
        {
            list.add(Component.empty());
            list.add(Component.translatable(KEY_REQUIRED_HAND).withStyle(ChatFormatting.BLUE));
            handIngredients.stream().map(itemStack -> Component.literal(" ").append(itemStack.getHoverName()).withStyle(ChatFormatting.YELLOW)).forEach(list::add);
        }
        
        if(!inventoryIngredients.isEmpty())
        {
            list.add(Component.empty());
            list.add(Component.translatable(KEY_REQUIRED_INVENTORY).withStyle(ChatFormatting.BLUE));
            inventoryIngredients.stream().map(itemStack -> Component.literal(" ").append(itemStack.getHoverName()).withStyle(ChatFormatting.YELLOW)).forEach(list::add);
        }
    }
}

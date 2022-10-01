package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class MultiIngredientSpell extends Spell
{
    public static final String KEY_REQUIRED_HAND = "spell.ingredients.hand";
    public static final String KEY_REQUIRED_INVENTORY = "spell.ingredients.inventory";
    public static final String KEY_INGREDIENT = "spell.ingredients.ingredient";
    public static final String KEY_INGREDIENT_MULTIPLE = "spell.ingredients.ingredient.multiple";
    
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
            Optional<List<ItemStack>> handIngredients = this.findHandIngredients(manaHolder);
            Optional<List<ItemStack>> inventoryIngredients = this.findInventoryIngredients(manaHolder);
            
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
    
    public Optional<List<ItemStack>> findHandIngredients(ManaHolder manaHolder)
    {
        List<ItemStack> foundList = new LinkedList<>();
        
        if(manaHolder.getPlayer() instanceof Player player)
        {
            InteractionHand prevHand = null;
            List<ItemStack> requiredList = getRequiredHandIngredients();
            
            for(ItemStack required : requiredList)
            {
                System.out.println("REQUIRED " + required.getItem() + " / " + required.getCount());
                
                if(required.isEmpty())
                {
                    continue;
                }
                
                int count = required.getCount();
                System.out.println("    count " + count);
                
                for(InteractionHand hand : InteractionHand.values())
                {
                    System.out.println("    HAND");
                    System.out.println("        hand" + hand);
                    
                    if(hand == prevHand)
                    {
                        continue;
                    }
                    
                    ItemStack toTest = player.getItemInHand(hand);
                    System.out.println("        toTest" + toTest.getCount() + " / " + toTest.getCount());
                    System.out.println("        if" + toTest.getCount() + " / " + toTest.getCount());
                    
                    if(ItemStack.isSameItemSameTags(toTest, required))
                    {
                        prevHand = hand;
                        foundList.add(toTest);
                        count -= toTest.getCount();
                    }
                }
                
                if(count > 0)
                {
                    return Optional.empty();
                }
            }
        }
        
        return Optional.of(foundList);
    }
    
    public Optional<List<ItemStack>> findInventoryIngredients(ManaHolder manaHolder)
    {
        List<ItemStack> foundList = new LinkedList<>();
        
        if(manaHolder.getPlayer() instanceof Player player)
        {
            for(ItemStack required : getRequiredInventoryIngredients())
            {
                if(required.isEmpty())
                {
                    continue;
                }
                
                int count = required.getCount();
                
                for(ItemStack toTest : player.getInventory().items)
                {
                    if(toTest != player.getMainHandItem() && ItemStack.isSameItemSameTags(toTest, required))
                    {
                        foundList.add(toTest);
                        count -= toTest.getCount();
                    }
                }
                
                if(count > 0)
                {
                    return Optional.empty();
                }
            }
        }
        
        return Optional.of(foundList);
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
            handIngredients.stream().map(itemStack ->
            {
                if(itemStack.getCount() == 1)
                {
                    return Component.translatable(KEY_INGREDIENT, itemStack.getHoverName());
                }
                else
                {
                    return Component.translatable(KEY_INGREDIENT_MULTIPLE, itemStack.getCount(), itemStack.getHoverName());
                }
            }).map(component -> Component.literal(" ").append(component).withStyle(ChatFormatting.YELLOW)).forEach(list::add);
        }
        
        if(!inventoryIngredients.isEmpty())
        {
            list.add(Component.empty());
            list.add(Component.translatable(KEY_REQUIRED_INVENTORY).withStyle(ChatFormatting.BLUE));
            inventoryIngredients.stream().map(itemStack ->
            {
                if(itemStack.getCount() == 1)
                {
                    return Component.translatable(KEY_INGREDIENT, itemStack.getHoverName());
                }
                else
                {
                    return Component.translatable(KEY_INGREDIENT_MULTIPLE, itemStack.getCount(), itemStack.getHoverName());
                }
            }).map(component -> Component.literal(" ").append(component).withStyle(ChatFormatting.YELLOW)).forEach(list::add);
        }
    }
}

package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public abstract class HandIngredientSpell extends Spell
{
    public HandIngredientSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public void perform(ManaHolder manaHolder, ItemStack itemStack)
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
            Optional<ItemStack> ingredient = this.hasIngredient(manaHolder);
            
            ingredient.ifPresent(itemStack ->
            {
                this.perform(manaHolder, itemStack);
                
                if(!manaHolder.getPlayer().level.isClientSide && (!(manaHolder.getPlayer() instanceof Player player) || !player.isCreative()))
                {
                    this.burnMana(manaHolder);
                    this.consumeItemStack(manaHolder, itemStack);
                }
            });
        }
        
        return false;
    }
    
    public Optional<ItemStack> hasIngredient(ManaHolder manaHolder)
    {
        for(ItemStack itemStack : manaHolder.getPlayer().getHandSlots())
        {
            if(this.checkHandIngredient(manaHolder, itemStack))
            {
                return Optional.of(itemStack);
            }
        }
        
        return Optional.empty();
    }
    
    public abstract boolean checkHandIngredient(ManaHolder manaHolder, ItemStack itemStack);
    
    public boolean checkInventoryIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return checkHandIngredient(manaHolder, itemStack);
    }
    
    public abstract void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack);
}

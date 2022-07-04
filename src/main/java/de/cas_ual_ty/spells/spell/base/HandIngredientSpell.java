package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.world.item.ItemStack;

public abstract class HandIngredientSpell extends IngredientSpell
{
    public HandIngredientSpell(float manaCost, ItemStack ingredient)
    {
        super(manaCost, ingredient);
    }
    
    @Override
    public ItemStack hasIngredient(ManaHolder manaHolder)
    {
        for(ItemStack itemStack : manaHolder.getPlayer().getHandSlots())
        {
            if(this.isItemStackIngredient(manaHolder, itemStack))
            {
                return itemStack;
            }
        }
        
        return ItemStack.EMPTY;
    }
}

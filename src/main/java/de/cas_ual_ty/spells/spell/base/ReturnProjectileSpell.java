package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IReturnProjectileSpell;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class ReturnProjectileSpell extends BaseIngredientsSpell implements IReturnProjectileSpell
{
    public ReturnProjectileSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        super(manaCost, handIngredients, inventoryIngredients);
    }
    
    public ReturnProjectileSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public ReturnProjectileSpell(float manaCost, ItemStack handIngredient)
    {
        super(manaCost, handIngredient);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        IReturnProjectileSpell.super.shootStraight(manaHolder);
    }
}

package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WaterLeapSpell extends LeapSpell
{
    public WaterLeapSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, double speed)
    {
        super(manaCost, handIngredients, inventoryIngredients, speed);
    }
    
    public WaterLeapSpell(float manaCost, double speed)
    {
        super(manaCost, speed);
    }
    
    public WaterLeapSpell(float manaCost)
    {
        super(manaCost, 4D);
    }
    
    @Override
    public boolean canActivate(ManaHolder manaHolder)
    {
        return manaHolder.getPlayer().isEyeInFluid(FluidTags.WATER);
    }
}

package de.cas_ual_ty.spells.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MaxManaEnchantment extends ManaArmorEnchantment
{
    public MaxManaEnchantment(Rarity rarity, EquipmentSlot... slots)
    {
        super(rarity, slots);
    }
    
    @Override
    public int getMinCost(int level)
    {
        return 20 + (level - 1) * 5;
    }
    
    @Override
    public int getMaxCost(int level)
    {
        return this.getMinCost(level) + 5;
    }
    
    @Override
    public int getMaxLevel()
    {
        return 2;
    }
    
    public double getAttributeIncrease(int level)
    {
        return level == 0 ? 0 : (3D + (level - 1) * 2D);
    }
}
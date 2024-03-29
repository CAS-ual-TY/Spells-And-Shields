package de.cas_ual_ty.spells.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ManaRegenEnchantment extends ManaArmorEnchantment
{
    public ManaRegenEnchantment(Rarity rarity, EquipmentSlot... slots)
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
        return getMinCost(level) + 5;
    }
    
    @Override
    public int getMaxLevel()
    {
        return 2;
    }
    
    public double getAttributeIncrease(int level, EquipmentSlot s)
    {
        if(s == EquipmentSlot.CHEST || s == EquipmentSlot.LEGS)
        {
            return level == 0 ? 0 : (0.2D + (level - 1) * 0.1D);
        }
        else
        {
            return level == 0 ? 0 : (0.1D + (level - 1) * 0.1D);
        }
    }
}

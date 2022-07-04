package de.cas_ual_ty.spells.enchantment;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

public class MagicProtectionEnchantment extends Enchantment
{
    public static final int MIN_COST = 4;
    public static final int LEVEL_COST = 5;
    
    public MagicProtectionEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots)
    {
        super(rarity, EnchantmentCategory.ARMOR, equipmentSlots);
    }
    
    @Override
    public int getMinCost(int level)
    {
        return MIN_COST + (level - 1) * LEVEL_COST;
    }
    
    @Override
    public int getMaxCost(int level)
    {
        return getMinCost(level) + LEVEL_COST;
    }
    
    @Override
    public int getMaxLevel()
    {
        return 4;
    }
    
    @Override
    public int getDamageProtection(int level, DamageSource damageSource)
    {
        if(damageSource.isBypassInvul())
        {
            return 0;
        }
        else if(damageSource.isMagic())
        {
            return level * 2;
        }
        else
        {
            return 0;
        }
    }
    
    @Override
    public boolean checkCompatibility(Enchantment enchantment)
    {
        if(enchantment instanceof ProtectionEnchantment protectionEnchantment)
        {
            return protectionEnchantment.type == ProtectionEnchantment.Type.FALL;
        }
        else
        {
            return super.checkCompatibility(enchantment);
        }
    }
}

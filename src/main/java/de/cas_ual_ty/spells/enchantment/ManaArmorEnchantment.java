package de.cas_ual_ty.spells.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public abstract class ManaArmorEnchantment extends Enchantment
{
    protected ManaArmorEnchantment(Rarity rarity, EquipmentSlot[] slots)
    {
        super(rarity, EnchantmentCategory.ARMOR, slots);
    }
    
    @Override
    protected boolean checkCompatibility(Enchantment enchantment)
    {
        return !(enchantment instanceof ManaArmorEnchantment);
    }
}
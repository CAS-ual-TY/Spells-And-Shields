package de.cas_ual_ty.spells.enchantment;

import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;

public class ManaShieldEnchantment extends Enchantment
{
    public static final int MIN_COST = 4;
    public static final int LEVEL_COST = 5;
    
    public ManaShieldEnchantment(Rarity rarity, EquipmentSlot... equipmentSlots)
    {
        super(rarity, BuiltInRegisters.SHIELD_ENCHANTMENT_CATEGORY, equipmentSlots);
    }
    
    @Override
    public int getMinCost(int level)
    {
        return 5 + (level - 1) * 8;
    }
    
    @Override
    public int getMaxCost(int level)
    {
        return super.getMinCost(level) + 50;
    }
    
    @Override
    public int getMaxLevel()
    {
        return 3;
    }
    
    @Override
    public boolean canEnchant(ItemStack itemStack)
    {
        return itemStack.getItem() instanceof ShieldItem && super.canEnchant(itemStack);
    }
    
}

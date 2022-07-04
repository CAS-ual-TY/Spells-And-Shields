package de.cas_ual_ty.spells.capability;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

public interface IManaHolder extends INBTSerializable<ListTag>
{
    void setMana(float mana);
    
    float getMana();
    
    void setExtraMana(float extraMana);
    
    float getExtraMana();
    
    void replenish(float amount);
    
    void burn(float amount);
    
    LivingEntity getPlayer();
}

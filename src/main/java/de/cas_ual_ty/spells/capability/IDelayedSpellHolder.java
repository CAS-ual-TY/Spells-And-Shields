package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public interface IDelayedSpellHolder extends INBTSerializable<ListTag>
{
    void addDelayedSpell(SpellInstance spell, UUID uuid, String activation, int tickTime, CompoundTag tag);
    
    boolean hasDelayedSpell(UUID uuid);
    
    boolean removeDelayedSpell(UUID uuid, boolean forceActivate);
    
    void tick();
    
    Entity getHolder();
}

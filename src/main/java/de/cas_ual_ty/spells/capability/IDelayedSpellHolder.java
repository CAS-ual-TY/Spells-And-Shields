package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public interface IDelayedSpellHolder extends INBTSerializable<ListTag>
{
    void addDelayedSpell(SpellNodeId spell, UUID uuid, String activation, int tickTime);
    
    default void addDelayedSpell(SpellNodeId spell, String activation, int tickTime)
    {
        addDelayedSpell(spell, null, activation, tickTime);
    }
    
    boolean hasDelayedSpell(UUID uuid);
    
    boolean removeDelayedSpell(UUID uuid, boolean forceActivate);
    
    void tick();
    
    Entity getHolder();
}

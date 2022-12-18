package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public interface ISpellHolder extends INBTSerializable<ListTag>
{
    int getSlots();
    
    @Nullable
    SpellInstance getSpell(int slot);
    
    void setSpell(int slot, @Nullable SpellInstance spell);
    
    default void removeSpell(int slot)
    {
        setSpell(slot, null);
    }
    
    Player getPlayer();
    
    void sendSync();
}

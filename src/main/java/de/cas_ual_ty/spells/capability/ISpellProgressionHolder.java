package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.Spell;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;

public interface ISpellProgressionHolder extends INBTSerializable<ListTag>
{
    boolean isSpellAvailable(Spell spell);
    
    SpellStatus getSpellStatus(Spell spell);
    
    void setSpellStatus(Spell spell, SpellStatus spellStatus);
    
    Map<Spell, SpellStatus> getProgression();
    
    Player getPlayer();
}

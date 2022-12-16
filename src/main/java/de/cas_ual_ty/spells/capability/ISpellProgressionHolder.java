package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.NewSpell;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;

public interface ISpellProgressionHolder extends INBTSerializable<ListTag>
{
    boolean isSpellAvailable(NewSpell spell);
    
    SpellStatus getSpellStatus(NewSpell spell);
    
    void setSpellStatus(NewSpell spell, SpellStatus spellStatus);
    
    Map<NewSpell, SpellStatus> getProgression();
    
    Player getPlayer();
}

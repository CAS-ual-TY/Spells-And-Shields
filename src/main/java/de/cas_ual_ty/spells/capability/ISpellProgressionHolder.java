package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.base.ISpell;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;

public interface ISpellProgressionHolder extends INBTSerializable<ListTag>
{
    boolean isSpellAvailable(ISpell spell);
    
    SpellStatus getSpellStatus(ISpell spell);
    
    void setSpellStatus(ISpell spell, SpellStatus spellStatus);
    
    Map<ISpell, SpellStatus> getProgression();
    
    Player getPlayer();
}

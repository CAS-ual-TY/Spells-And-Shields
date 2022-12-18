package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;

public interface ISpellProgressionHolder extends INBTSerializable<ListTag>
{
    boolean isSpellAvailable(SpellNodeId spell);
    
    SpellStatus getSpellStatus(SpellNodeId spell);
    
    void setSpellStatus(SpellNodeId spell, SpellStatus spellStatus);
    
    Map<SpellNodeId, SpellStatus> getProgression();
    
    Player getPlayer();
}

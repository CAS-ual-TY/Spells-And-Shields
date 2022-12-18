package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;
import java.util.Map;

public class SpellProgressionHolder implements ISpellProgressionHolder
{
    public static final String KEY_SPELL = "spell";
    public static final String KEY_SPELL_STATUS = "spell_status";
    
    protected final Player player;
    protected final HashMap<SpellNodeId, SpellStatus> progression;
    
    public SpellProgressionHolder(Player player)
    {
        this.player = player;
        this.progression = new HashMap<>();
    }
    
    @Override
    public boolean isSpellAvailable(SpellNodeId spell)
    {
        return getSpellStatus(spell) == SpellStatus.LEARNED;
    }
    
    @Override
    public SpellStatus getSpellStatus(SpellNodeId spell)
    {
        return progression.getOrDefault(spell, SpellStatus.LOCKED);
    }
    
    @Override
    public void setSpellStatus(SpellNodeId spell, SpellStatus spellStatus)
    {
        progression.put(spell, spellStatus);
    }
    
    @Override
    public ListTag serializeNBT()
    {
        Registry<Spell> registry = Spells.getRegistry(player.getLevel());
        
        ListTag list = new ListTag();
        
        for(Map.Entry<SpellNodeId, SpellStatus> entry : progression.entrySet())
        {
            CompoundTag tag = new CompoundTag();
            entry.getKey().toNbt(tag);
            tag.putByte(KEY_SPELL_STATUS, (byte) entry.getValue().ordinal());
            list.add(tag);
        }
        
        return list;
    }
    
    @Override
    public void deserializeNBT(ListTag nbt)
    {
        progression.clear();
        
        if(nbt.getElementType() != Tag.TAG_COMPOUND)
        {
            return;
        }
        
        Registry<Spell> registry = Spells.getRegistry(player.getLevel());
        
        for(int i = 0; i < nbt.size(); ++i)
        {
            CompoundTag tag = nbt.getCompound(i);
            
            if(tag.contains(KEY_SPELL) && tag.contains(KEY_SPELL_STATUS) && tag.get(KEY_SPELL).getId() == Tag.TAG_STRING && tag.get(KEY_SPELL_STATUS).getId() == Tag.TAG_BYTE)
            {
                SpellNodeId spellNodeId = SpellNodeId.fromNbt(tag);
                byte ordinal = tag.getByte(KEY_SPELL_STATUS);
                
                if(spellNodeId != null && ordinal >= 0 && ordinal < SpellStatus.values().length)
                {
                    progression.put(spellNodeId, SpellStatus.values()[ordinal]);
                }
            }
        }
    }
    
    @Override
    public HashMap<SpellNodeId, SpellStatus> getProgression()
    {
        return progression;
    }
    
    @Override
    public Player getPlayer()
    {
        return player;
    }
    
    public static LazyOptional<SpellProgressionHolder> getSpellProgressionHolder(Player player)
    {
        return player.getCapability(SpellsCapabilities.SPELL_PROGRESSION_CAPABILITY).cast();
    }
}

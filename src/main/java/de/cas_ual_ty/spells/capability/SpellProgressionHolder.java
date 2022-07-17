package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.base.ISpell;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;
import java.util.Map;

public class SpellProgressionHolder implements ISpellProgressionHolder
{
    public static final String KEY_SPELL = "spell";
    public static final String KEY_SPELL_STATUS = "spell_status";
    
    protected final Player player;
    protected final HashMap<ISpell, SpellStatus> progression;
    
    public SpellProgressionHolder(Player player)
    {
        this.player = player;
        this.progression = new HashMap<>();
    }
    
    @Override
    public boolean isSpellAvailable(ISpell spell)
    {
        return getSpellStatus(spell) == SpellStatus.LEARNED;
    }
    
    @Override
    public SpellStatus getSpellStatus(ISpell spell)
    {
        return progression.getOrDefault(spell, SpellStatus.LOCKED);
    }
    
    @Override
    public void setSpellStatus(ISpell spell, SpellStatus spellStatus)
    {
        progression.put(spell, spellStatus);
    }
    
    @Override
    public ListTag serializeNBT()
    {
        ListTag list = new ListTag();
        
        for(Map.Entry<ISpell, SpellStatus> entry : progression.entrySet())
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("spell", SpellsUtil.getSpellKey(entry.getKey()).toString());
            tag.putByte("spell_status", (byte) entry.getValue().ordinal());
            list.add(tag);
        }
        
        return list;
    }
    
    @Override
    public void deserializeNBT(ListTag nbt)
    {
        for(int i = 0; i < nbt.size(); ++i)
        {
            if(nbt.get(i).getId() != Tag.TAG_COMPOUND)
            {
                continue;
            }
            
            CompoundTag tag = nbt.getCompound(i);
            progression.clear();
            
            if(tag.contains(KEY_SPELL) && tag.contains(KEY_SPELL_STATUS) && tag.get(KEY_SPELL).getId() == Tag.TAG_STRING && tag.get(KEY_SPELL_STATUS).getId() == Tag.TAG_BYTE)
            {
                ISpell spell = SpellsUtil.getSpell(new ResourceLocation(tag.getString(KEY_SPELL)));
                byte ordinal = tag.getByte(KEY_SPELL_STATUS);
                
                if(spell != null && ordinal >= 0 && ordinal < SpellStatus.values().length)
                {
                    progression.put(spell, SpellStatus.values()[ordinal]);
                }
            }
        }
    }
    
    @Override
    public HashMap<ISpell, SpellStatus> getProgression()
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

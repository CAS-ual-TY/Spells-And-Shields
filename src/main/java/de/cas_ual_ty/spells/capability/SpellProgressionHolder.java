package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpellProgressionHolder implements INBTSerializable<ListTag>
{
    public static final String KEY_SPELL_STATUS = "spell_status";

    protected Player player;
    protected final HashMap<SpellNodeId, SpellStatus> progression;

    public SpellProgressionHolder()
    {
        player = null;
        progression = new HashMap<>();
    }

    public SpellProgressionHolder(Player player)
    {
        this.player = player;
        progression = new HashMap<>();
    }

    public void initPlayer(Player player)
    {
        if(this.player == null)
        {
            this.player = player;
        }
    }

    public boolean isSpellAvailable(SpellNodeId spell)
    {
        return getSpellStatus(spell) == SpellStatus.LEARNED;
    }

    public SpellStatus getSpellStatus(SpellNodeId spell)
    {
        return progression.getOrDefault(spell, SpellStatus.LOCKED);
    }

    public void setSpellStatus(SpellNodeId spell, SpellStatus spellStatus)
    {
        progression.put(spell, spellStatus);
    }

    @Override
    public ListTag serializeNBT()
    {
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

        for(int i = 0; i < nbt.size(); ++i)
        {
            CompoundTag tag = nbt.getCompound(i);

            if(tag.contains(KEY_SPELL_STATUS) && tag.get(KEY_SPELL_STATUS).getId() == Tag.TAG_BYTE)
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

    public HashMap<SpellNodeId, SpellStatus> getProgression()
    {
        return progression;
    }

    public Player getPlayer()
    {
        return player;
    }

    public static Optional<SpellProgressionHolder> getSpellProgressionHolder(Player player)
    {
        SpellProgressionHolder holder = player.getData(SpellsCapabilities.SPELL_PROGRESSION_HOLDER.get());
        holder.initPlayer(player);
        return Optional.of(holder);
    }
}

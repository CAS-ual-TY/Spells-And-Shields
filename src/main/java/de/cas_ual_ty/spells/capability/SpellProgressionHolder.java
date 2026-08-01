package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.spelltree.FullSpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpellProgressionHolder implements INBTSerializable<ListTag>
{
    public static final String KEY_SPELL_STATUS = "spell_status";

    protected Player player;
    protected final HashMap<FullSpellNodeId, SpellStatus> progression;

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

    public boolean isSpellAvailable(FullSpellNodeId spell)
    {
        return getSpellStatus(spell) == SpellStatus.LEARNED;
    }

    public SpellStatus getSpellStatus(FullSpellNodeId spell)
    {
        return progression.getOrDefault(spell, SpellStatus.LOCKED);
    }

    public void setSpellStatus(FullSpellNodeId spell, SpellStatus spellStatus)
    {
        progression.put(spell, spellStatus);
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider)
    {
        ListTag list = new ListTag();

        for(Map.Entry<FullSpellNodeId, SpellStatus> entry : progression.entrySet())
        {
            CompoundTag tag = new CompoundTag();
            entry.getKey().toNbt(tag);
            tag.putByte(KEY_SPELL_STATUS, (byte) entry.getValue().ordinal());
            list.add(tag);
        }

        return list;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt)
    {
        progression.clear();

        if(nbt.getElementType() != Tag.TAG_COMPOUND)
        {
            return;
        }

        // null while attachment framework hasn't attached us to a player yet - skip validation in that case,
        // rather than crash. In practice this always runs with a player already set.
        Registry<SpellTree> registry = player != null ? SpellTrees.getRegistry(player.level()) : null;

        for(int i = 0; i < nbt.size(); ++i)
        {
            CompoundTag tag = nbt.getCompound(i);

            if(tag.contains(KEY_SPELL_STATUS) && tag.get(KEY_SPELL_STATUS).getId() == Tag.TAG_BYTE)
            {
                FullSpellNodeId fullSpellNodeId = FullSpellNodeId.fromNbt(tag);
                byte ordinal = tag.getByte(KEY_SPELL_STATUS);

                if(fullSpellNodeId != null && ordinal >= 0 && ordinal < SpellStatus.values().length
                        && (registry == null || fullSpellNodeId.isValid(registry)))
                {
                    progression.put(fullSpellNodeId, SpellStatus.values()[ordinal]);
                }
            }
        }
    }

    public HashMap<FullSpellNodeId, SpellStatus> getProgression()
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

    public static void registerEvents(IEventBus modEventBus)
    {
        NeoForge.EVENT_BUS.addListener(SpellProgressionHolder::onDatapackSync);
    }

    private static void onDatapackSync(OnDatapackSyncEvent event)
    {
        // event.getPlayer() == null means this fired for a /reload, broadcast to everyone -
        // that's the only case this is for. On an ordinary join it's the specific joining
        // player, whose data has already gone through the normal (already-safe) load path
        // by the time this event can fire, so there is nothing new to prune here.
        if(event.getPlayer() == null)
        {
            event.getRelevantPlayers().forEach(SpellProgressionHolder::pruneStaleReferences);
        }
    }

    private static void pruneStaleReferences(ServerPlayer player)
    {
        // round-trip both holders through NBT: their deserializeNBT already drops whatever no
        // longer resolves (spell/tree/node gone) against the currently-loaded registries - this
        // is the exact same safeguard that already applies to every ordinary load, just re-run
        // here for a player who stayed connected through a live /reload.
        RegistryAccess registryAccess = player.level().registryAccess();

        SpellProgressionHolder.getSpellProgressionHolder(player).ifPresent(progressionHolder ->
                progressionHolder.deserializeNBT(registryAccess, progressionHolder.serializeNBT(registryAccess)));
    }
}

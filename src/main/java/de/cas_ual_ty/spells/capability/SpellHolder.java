package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.network.SpellsSyncMessage;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.context.BuiltinEvents;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

public class SpellHolder implements INBTSerializable<ListTag>
{
    public static final int SPELL_SLOTS = 5;
    public static final String EMPTY_SLOT = "";

    protected final SpellInstance[] slots;
    protected Player player;
    private ListTag pendingNBT;

    public SpellHolder()
    {
        slots = new SpellInstance[SPELL_SLOTS];
        player = null;
        pendingNBT = null;
    }

    public SpellHolder(Player player)
    {
        this();
        this.player = player;
    }

    public void initPlayer(Player player)
    {
        if(this.player == null)
        {
            this.player = player;
            if(pendingNBT != null)
            {
                ListTag tag = pendingNBT;
                pendingNBT = null;
                deserializeNBT(null, tag);
            }
        }
    }

    public int getSlots()
    {
        return SPELL_SLOTS;
    }

    public SpellInstance getSpell(int slot)
    {
        return slots[slot];
    }

    public void setSpell(int slot, @Nullable SpellInstance spell)
    {
        if(!player.level().isClientSide)
        {
            if(slots[slot] != null)
            {
                slots[slot].run(player.level(), player, BuiltinEvents.ON_UNEQUIP.activation, ctx -> ctx.setCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.SPELL_SLOT.name, slot));
            }
            if(spell != null)
            {
                spell.run(player.level(), player, BuiltinEvents.ON_EQUIP.activation, ctx -> ctx.setCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.SPELL_SLOT.name, slot));
            }
        }
        slots[slot] = spell;
    }

    public Player getPlayer()
    {
        return player;
    }

    public void activateAll(String activation)
    {
        for(int i = 0; i < getSlots(); i++)
        {
            SpellInstance s = getSpell(i);
            if(s != null)
            {
                final int slot = i;
                s.run(player.level(), player, activation, ctx -> ctx.setCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.SPELL_SLOT.name, slot));
            }
        }
    }

    public int getAmountSpellEquipped(Spell spell)
    {
        int amount = 0;
        for(int i = 0; i < SPELL_SLOTS; ++i)
        {
            SpellInstance spellInstance = getSpell(i);
            if(spellInstance != null && spellInstance.getSpell().value() == spell)
            {
                amount++;
            }
        }
        return amount;
    }

    public void removeSpell(int slot)
    {
        setSpell(slot, null);
    }

    public void clear()
    {
        for(int i = 0; i < SPELL_SLOTS; i++)
        {
            removeSpell(i);
        }
    }

    public SpellsSyncMessage makeSyncMessage()
    {
        Registry<Spell> registry = Spells.getRegistry(player.level());
        return new SpellsSyncMessage(
                player.getId(),
                Arrays.stream(slots).map(s -> s != null ? s.getSpell().unwrap().map(ResourceKey::location, registry::getKey) : null).toArray(ResourceLocation[]::new),
                Arrays.stream(slots).map(s -> s != null ? s.getNodeId() : null).toArray(SpellNodeId[]::new)
        );
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider)
    {
        if(player == null)
        {
            return pendingNBT != null ? pendingNBT : new ListTag();
        }

        Registry<Spell> spellRegistry = Spells.getRegistry(player.level());

        ListTag list = new ListTag();
        for(int i = 0; i < SPELL_SLOTS; ++i)
        {
            CompoundTag tag = new CompoundTag();
            if(slots[i] != null)
            {
                slots[i].toNbt(tag, spellRegistry);
            }
            list.add(i, tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag tag)
    {
        if(player == null)
        {
            pendingNBT = tag;
            return;
        }

        Registry<SpellTree> spellTreeRegistry = SpellTrees.getRegistry(player.level());
        Registry<Spell> spellRegistry = Spells.getRegistry(player.level());

        if(tag.getElementType() != Tag.TAG_COMPOUND)
        {
            return;
        }

        for(int i = 0; i < SPELL_SLOTS && i < tag.size(); ++i)
        {
            slots[i] = SpellInstance.fromNbt(tag.getCompound(i), spellTreeRegistry, spellRegistry);
        }
    }

    public void sendSync()
    {
        if(player instanceof ServerPlayer serverPlayer)
        {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, makeSyncMessage());
        }
    }

    public static Optional<SpellHolder> getSpellHolder(Player player)
    {
        SpellHolder holder = player.getData(SpellsCapabilities.SPELL_HOLDER.get());
        holder.initPlayer(player);
        return Optional.of(holder);
    }
}

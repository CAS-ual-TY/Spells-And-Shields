package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
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
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Arrays;

public class SpellHolder implements INBTSerializable<ListTag>
{
    public static final int SPELL_SLOTS = 5;
    
    public static final String EMPTY_SLOT = "";
    
    protected final SpellInstance[] slots;
    protected final Player player;
    
    public SpellHolder(Player player)
    {
        slots = new SpellInstance[SPELL_SLOTS];
        this.player = player;
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
        if(!player.level.isClientSide)
        {
            if(slots[slot] != null)
            {
                slots[slot].run(player.level, player, BuiltinEvents.ON_UNEQUIP.activation, ctx -> ctx.setCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.SPELL_SLOT.name, slot));
            }
            
            if(spell != null)
            {
                spell.run(player.level, player, BuiltinEvents.ON_EQUIP.activation, ctx -> ctx.setCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.SPELL_SLOT.name, slot));
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
                s.run(player.level, player, activation, ctx -> ctx.setCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.SPELL_SLOT.name, slot));
            }
        }
    }
    
    public int getAmountSpellEquipped(Spell spell)
    {
        int amount = 0;
        
        for(int i = 0; i < SPELL_SLOTS; ++i)
        {
            SpellInstance spellInstance = getSpell(i);
            if(spellInstance != null && spellInstance.getSpell().get() == spell)
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
        Registry<Spell> registry = Spells.getRegistry(player.getLevel());
        return new SpellsSyncMessage(player.getId(), Arrays.stream(slots).map(s -> s != null ? s.getSpell().unwrap().map(ResourceKey::location, registry::getKey) : null).toArray(ResourceLocation[]::new), Arrays.stream(slots).map(s -> s != null ? s.getNodeId() : null).toArray(SpellNodeId[]::new));
    }
    
    @Override
    public ListTag serializeNBT()
    {
        Registry<Spell> spellRegistry = Spells.getRegistry(player.getLevel());
        
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
    public void deserializeNBT(ListTag tag)
    {
        Registry<SpellTree> spellTreeRegistry = SpellTrees.getRegistry(player.getLevel());
        Registry<Spell> spellRegistry = Spells.getRegistry(player.getLevel());
        
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
            SpellsAndShields.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), this.makeSyncMessage());
        }
    }
    
    public static LazyOptional<SpellHolder> getSpellHolder(Player player)
    {
        return player.getCapability(SpellsCapabilities.SPELLS_CAPABILITY).cast();
    }
}

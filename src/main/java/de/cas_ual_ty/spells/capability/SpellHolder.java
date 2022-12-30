package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.network.SpellsSyncMessage;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Arrays;

public class SpellHolder implements ISpellHolder
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
    
    @Override
    public int getSlots()
    {
        return SPELL_SLOTS;
    }
    
    @Override
    public SpellInstance getSpell(int slot)
    {
        return slots[slot];
    }
    
    @Override
    public void setSpell(int slot, @Nullable SpellInstance spell)
    {
        if(slots[slot] != null)
        {
            slots[slot].activate(this, BuiltinActivations.ON_UNEQUIP.activation);
        }
        
        if(spell != null)
        {
            spell.activate(this, BuiltinActivations.ON_EQUIP.activation);
        }
        
        slots[slot] = spell;
    }
    
    @Override
    public Player getPlayer()
    {
        return player;
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
        return new SpellsSyncMessage(player.getId(), Arrays.stream(slots).map(s -> s != null ? registry.getKey(s.getSpell().get()) : null).toArray(ResourceLocation[]::new));
    }
    
    @Override
    public ListTag serializeNBT()
    {
        Registry<Spell> registry = Spells.getRegistry(player.getLevel());
        
        ListTag list = new ListTag();
        for(int i = 0; i < SPELL_SLOTS; ++i)
        {
            CompoundTag tag = new CompoundTag();
            
            if(slots[i] != null && slots[i].getNodeId() != null)
            {
                slots[i].getNodeId().toNbt(tag);
            }
            
            list.add(i, tag);
        }
        return list;
    }
    
    @Override
    public void deserializeNBT(ListTag tag)
    {
        Registry<SpellTree> registry = SpellTrees.getRegistry(player.getLevel());
        
        if(tag.getElementType() != Tag.TAG_COMPOUND)
        {
            return;
        }
        
        for(int i = 0; i < SPELL_SLOTS && i < tag.size(); ++i)
        {
            SpellNodeId nodeId = SpellNodeId.fromNbt(tag.getCompound(i));
            
            if(nodeId == null)
            {
                slots[i] = null;
            }
            else
            {
                slots[i] = nodeId.getSpellInstance(registry);
            }
        }
    }
    
    @Override
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

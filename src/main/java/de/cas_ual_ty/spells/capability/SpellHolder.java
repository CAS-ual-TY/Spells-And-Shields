package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.network.SpellsSyncMessage;
import de.cas_ual_ty.spells.spell.NewSpell;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
    
    protected final NewSpell[] slots;
    protected final Player player;
    
    public SpellHolder(Player player)
    {
        slots = new NewSpell[SPELL_SLOTS];
        this.player = player;
    }
    
    @Override
    public int getSlots()
    {
        return SPELL_SLOTS;
    }
    
    @Override
    public NewSpell getSpell(int slot)
    {
        return slots[slot];
    }
    
    @Override
    public void setSpell(int slot, @Nullable NewSpell spell)
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
    
    public int getAmountSpellEquipped(NewSpell spell)
    {
        int amount = 0;
        
        for(int i = 0; i < SPELL_SLOTS; ++i)
        {
            if(getSpell(i) == spell)
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
        Registry<NewSpell> registry = SpellsUtil.getSpellRegistry(player.getLevel());
        return new SpellsSyncMessage(player.getId(), Arrays.stream(slots).map(s -> s != null ? registry.getKey(s) : null).toArray(ResourceLocation[]::new));
    }
    
    @Override
    public ListTag serializeNBT()
    {
        Registry<NewSpell> registry = SpellsUtil.getSpellRegistry(player.getLevel());
        
        ListTag tag = new ListTag();
        for(int i = 0; i < SPELL_SLOTS; ++i)
        {
            NewSpell spell = this.getSpell(i);
            
            if(spell != null)
            {
                tag.add(i, StringTag.valueOf(registry.getKey(spell).toString()));
            }
            else
            {
                tag.add(i, StringTag.valueOf(EMPTY_SLOT));
            }
        }
        return tag;
    }
    
    @Override
    public void deserializeNBT(ListTag tag)
    {
        Registry<NewSpell> registry = SpellsUtil.getSpellRegistry(player.getLevel());
        
        for(int i = 0; i < SPELL_SLOTS && i < tag.size(); ++i)
        {
            if(tag.get(i).getId() != Tag.TAG_STRING)
            {
                continue;
            }
            
            String key = tag.getString(i);
            
            if(!key.equals(EMPTY_SLOT))
            {
                slots[i] = registry.get(new ResourceLocation(key));
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

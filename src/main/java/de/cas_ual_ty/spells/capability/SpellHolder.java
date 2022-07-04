package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsUtil;
import de.cas_ual_ty.spells.network.SpellsSyncMessage;
import de.cas_ual_ty.spells.spell.base.ISpell;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import java.util.Arrays;

public class SpellHolder implements ISpellHolder
{
    public static final String EMPTY_SLOT = "";
    
    protected final ISpell[] slots;
    protected final Player player;
    
    public SpellHolder(Player player)
    {
        slots = new ISpell[SpellsAndShields.SPELL_SLOTS];
        this.player = player;
    }
    
    @Override
    public int getSlots()
    {
        return SpellsAndShields.SPELL_SLOTS;
    }
    
    @Override
    public ISpell getSpell(int slot)
    {
        return slots[slot];
    }
    
    @Override
    public void setSpell(int slot, ISpell spell)
    {
        slots[slot] = spell;
    }
    
    @Override
    public Player getPlayer()
    {
        return player;
    }
    
    public void sync()
    {
        if(player instanceof ServerPlayer serverPlayer)
        {
            SpellsAndShields.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), this.makeSyncMessage());
        }
    }
    
    public void clear()
    {
        Arrays.fill(slots, null);
    }
    
    public SpellsSyncMessage makeSyncMessage()
    {
        return new SpellsSyncMessage(player.getId(), slots);
    }
    
    @Override
    public ListTag serializeNBT()
    {
        ListTag tag = new ListTag();
        for(int i = 0; i < SpellsAndShields.SPELL_SLOTS; ++i)
        {
            ISpell spell = this.getSpell(i);
            
            if(spell != null)
            {
                tag.add(i, StringTag.valueOf(spell.getRegistryName().toString()));
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
        for(int i = 0; i < SpellsAndShields.SPELL_SLOTS && i < tag.size(); ++i)
        {
            if(tag.get(i).getId() != Tag.TAG_STRING)
            {
                continue;
            }
            
            String key = tag.getString(i);
            
            if(!key.equals(EMPTY_SLOT))
            {
                slots[i] = SpellsUtil.getSpell(new ResourceLocation(key));
            }
        }
    }
    
    public static LazyOptional<SpellHolder> getSpellHolder(Player player)
    {
        return player.getCapability(SpellsCapabilities.SPELLS_CAPABILITY).cast();
    }
}

package de.cas_ual_ty.spells.capability;

import com.mojang.serialization.DataResult;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.network.SpellsSyncMessage;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        Registry<Spell> registry = SpellsUtil.getSpellRegistry(player.getLevel());
        return new SpellsSyncMessage(player.getId(), Arrays.stream(slots).map(s -> s != null ? registry.getKey(s.getSpell().get()) : null).toArray(ResourceLocation[]::new));
    }
    
    @Override
    public ListTag serializeNBT()
    {
        Registry<Spell> registry = SpellsUtil.getSpellRegistry(player.getLevel());
        
        ListTag list = new ListTag();
        for(int i = 0; i < SPELL_SLOTS; ++i)
        {
            CompoundTag tag = new CompoundTag();
            
            SpellInstance spell = this.getSpell(i);
            if(spell != null)
            {
                tag.put("spell", StringTag.valueOf(registry.getKey(spell.getSpell().get()).toString()));
                SpellsCodecs.CTX_VAR.listOf().encodeStart(NbtOps.INSTANCE, spell.getVariables()).result().ifPresent(vars -> tag.put("variables", vars));
            }
            else
            {
                tag.put("spell", StringTag.valueOf(EMPTY_SLOT));
            }
            
            list.add(i, tag);
        }
        return list;
    }
    
    @Override
    public void deserializeNBT(ListTag tag)
    {
        Registry<Spell> registry = SpellsUtil.getSpellRegistry(player.getLevel());
        
        for(int i = 0; i < SPELL_SLOTS && i < tag.size(); ++i)
        {
            if(tag.get(i).getId() != Tag.TAG_COMPOUND)
            {
                continue;
            }
            
            CompoundTag compoundTag = tag.getCompound(i);
            String key = compoundTag.getString("spell");
            
            if(!key.equals(EMPTY_SLOT))
            {
                Spell spell = registry.get(new ResourceLocation(key));
                
                if(spell == null)
                {
                    continue;
                }
                
                DataResult<List<CtxVar<?>>> vars = SpellsCodecs.CTX_VAR.listOf().parse(NbtOps.INSTANCE, compoundTag.get("variables"));
                SpellInstance spellInstance = new SpellInstance(Holder.direct(spell), vars.result().orElse(new LinkedList<>()));
                
                slots[i] = spellInstance;
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

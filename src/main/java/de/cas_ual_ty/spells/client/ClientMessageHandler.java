package de.cas_ual_ty.spells.client;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.ParticleEmitterHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.network.*;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientMessageHandler
{
    public static void handleManaSync(ManaSyncMessage msg)
    {
        Level level = Minecraft.getInstance().level;
        
        if(level != null && level.getEntity(msg.entityId()) instanceof LivingEntity livingEntity)
        {
            ManaHolder.getManaHolder(livingEntity).ifPresent(manaHolder ->
            {
                if(manaHolder.changeTime == -1)
                {
                    manaHolder.setMana(msg.mana());
                    manaHolder.setExtraMana(msg.extraMana());
                    manaHolder.changeTime = 0;
                }
                else
                {
                    manaHolder.setMana(msg.mana());
                    manaHolder.setExtraMana(msg.extraMana());
                }
            });
        }
    }
    
    public static void handleSpellsSync(SpellsSyncMessage msg)
    {
        Level level = Minecraft.getInstance().level;
        
        if(level != null && level.getEntity(msg.entityId()) instanceof Player player)
        {
            Registry<Spell> registry = Spells.getRegistry(player.level());
            
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                for(int i = 0; i < spellHolder.getSlots() && i < msg.spells().length; ++i)
                {
                    if(msg.spells()[i] != null)
                    {
                        SpellInstance spellInst = new SpellInstance(registry.getHolderOrThrow(ResourceKey.create(Spells.REGISTRY_KEY, msg.spells()[i])));
                        
                        if(msg.nodeIds()[i] != null)
                        {
                            spellInst.initId(msg.nodeIds()[i]);
                        }
                        
                        spellHolder.setSpell(i, spellInst);
                    }
                    else
                    {
                        spellHolder.setSpell(i, null);
                    }
                }
            });
        }
    }
    
    public static void handleSpellProgressionSync(SpellProgressionSyncMessage msg)
    {
        if(Minecraft.getInstance().screen instanceof SpellProgressionScreen screen)
        {
            screen.getMenu().spellTrees = msg.spellTrees();
            screen.getMenu().spellProgression = msg.map();
            screen.spellTreesUpdated();
        }
    }
    
    public static void handleParticleEmitterSync(ParticleEmitterSyncMessage msg)
    {
        Level level = Minecraft.getInstance().level;
        
        if(level == null)
        {
            return;
        }
        
        Entity entity = level.getEntity(msg.entityId());
        
        if(entity == null)
        {
            return;
        }
        
        ParticleEmitterHolder.getHolder(entity).ifPresent(holder ->
        {
            if(msg.clear())
            {
                holder.clear();
            }
            
            msg.list().forEach(holder::addParticleEmitter);
        });
    }
    
    public static void handleSpellAction(RunActionOnClientMessage msg)
    {
        if(msg.action() != null)
        {
            msg.action().execute(SpellsClientUtil.getClientLevel(), SpellsClientUtil.getClientPlayer());
        }
    }
}

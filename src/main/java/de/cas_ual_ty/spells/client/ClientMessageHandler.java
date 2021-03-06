package de.cas_ual_ty.spells.client;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.network.FireClientSpellMessage;
import de.cas_ual_ty.spells.network.ManaSyncMessage;
import de.cas_ual_ty.spells.network.SpellProgressionSyncMessage;
import de.cas_ual_ty.spells.network.SpellsSyncMessage;
import net.minecraft.client.Minecraft;
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
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                for(int i = 0; i < spellHolder.getSlots() && i < msg.spells().length; ++i)
                {
                    spellHolder.setSpell(i, msg.spells()[i]);
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
    
    public static void handleFireSpell(FireClientSpellMessage msg)
    {
        SpellsClientUtil.getClientManaHolder().ifPresent(manaHolder ->
        {
            msg.spell().performOnClient(manaHolder);
        });
    }
}

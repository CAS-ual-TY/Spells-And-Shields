package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.util.SpellHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class SpellKeyBindings
{
    public static final String CATEGORY = "key." + SpellsAndShields.MOD_ID + ".categories.spell_slots";
    public static final int COOLDOWN = 10; // in ticks
    
    public static KeyMapping[] slotKeys;
    public static int[] cooldowns;
    
    public static void clientSetup(FMLClientSetupEvent event)
    {
        slotKeys = new KeyMapping[SpellHolder.SPELL_SLOTS];
        cooldowns = new int[SpellHolder.SPELL_SLOTS];
        
        for(int i = 0; i < slotKeys.length; ++i)
        {
            slotKeys[i] = new KeyMapping(key(i), KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, CATEGORY);
            ClientRegistry.registerKeyBinding(slotKeys[i]);
        }
    }
    
    public static String key(int slot)
    {
        return "key." + SpellsAndShields.MOD_ID + ".key.slot_" + (slot + 1);
    }
    
    public static void clientTick(TickEvent.ClientTickEvent event)
    {
        Player player = Minecraft.getInstance().player;
        
        if(event.phase == TickEvent.Phase.END && player != null)
        {
            for(int i = 0; i < slotKeys.length; ++i)
            {
                if(cooldowns[i] > 0)
                {
                    cooldowns[i]--;
                }
                else if(slotKeys[i].isDown())
                {
                    SpellHelper.fireSpellSlot(player, i);
                    cooldowns[i] = COOLDOWN;
                }
            }
        }
    }
    
    public static void register()
    {
        MinecraftForge.EVENT_BUS.addListener(SpellKeyBindings::clientTick);
    }
}

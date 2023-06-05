package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.util.SpellHelper;
import de.cas_ual_ty.spells.util.SpellsDowngrade;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SpellKeyBindings
{
    public static final String CATEGORY = "key." + SpellsAndShields.MOD_ID + ".categories.spell_slots";
    public static final int COOLDOWN = 10; // in ticks
    
    public static KeyMapping[] slotKeys;
    public static int[] cooldowns;
    
    public static KeyMapping radialMenu;
    
    private static void clientSetup(FMLClientSetupEvent event)
    {
        slotKeys = new KeyMapping[SpellHolder.SPELL_SLOTS];
        cooldowns = new int[SpellHolder.SPELL_SLOTS];
        
        for(int i = 0; i < slotKeys.length; ++i)
        {
            slotKeys[i] = new KeyMapping(key(i), KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, CATEGORY);
            ClientRegistry.registerKeyBinding(slotKeys[i]);
        }
        
        radialMenu = new KeyMapping(keyRadialMenu(), KeyConflictContext.IN_GAME, InputConstants.getKey(InputConstants.KEY_V, 0), CATEGORY);
        ClientRegistry.registerKeyBinding(radialMenu);
    }
    
    public static String key(int slot)
    {
        return "key." + SpellsAndShields.MOD_ID + ".key.slot_" + (slot + 1);
    }
    
    public static String keyRadialMenu()
    {
        return "key." + SpellsAndShields.MOD_ID + ".key.radial_menu";
    }
    
    public static MutableComponent getBaseTooltip()
    {
        return SpellsDowngrade.translatable("controls.keybinds.title");
    }
    
    public static MutableComponent getTooltip(int slot)
    {
        return SpellsDowngrade.literal(SpellKeyBindings.slotKeys[slot].getTranslatedKeyMessage().getString());
    }
    
    private static void clientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
        {
            return;
        }
        
        Player player = Minecraft.getInstance().player;
        
        if(player != null)
        {
            if(radialMenu.isDown())
            {
                if(Minecraft.getInstance().screen == null && !RadialMenu.wasClosed)
                {
                    Minecraft.getInstance().setScreen(new RadialMenu());
                }
                return;
            }
            else
            {
                if(RadialMenu.wasClosed)
                {
                    RadialMenu.wasClosed = false;
                }
            }
            
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellKeyBindings::clientSetup);
        MinecraftForge.EVENT_BUS.addListener(SpellKeyBindings::clientTick);
    }
}

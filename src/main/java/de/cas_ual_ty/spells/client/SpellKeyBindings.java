package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.util.SpellHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

public class SpellKeyBindings
{
    public static final String CATEGORY = "key." + SpellsAndShields.MOD_ID + ".categories.spell_slots";
    public static final int COOLDOWN = 10; // in ticks
    
    public static KeyMapping[] slotKeys;
    public static int[] cooldowns;
    
    public static KeyMapping radialMenu;
    
    private static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        slotKeys = new KeyMapping[SpellHolder.SPELL_SLOTS];
        cooldowns = new int[SpellHolder.SPELL_SLOTS];
        
        for(int i = 0; i < slotKeys.length; ++i)
        {
            slotKeys[i] = new KeyMapping(key(i), KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, CATEGORY);
            event.register(slotKeys[i]);
        }
        
        radialMenu = new KeyMapping(keyRadialMenu(), KeyConflictContext.IN_GAME, InputConstants.getKey(InputConstants.KEY_V, 0), CATEGORY);
        event.register(radialMenu);
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
        return Component.translatable("controls.keybinds.title");
    }
    
    public static MutableComponent getTooltip(int slot)
    {
        return Component.literal(SpellKeyBindings.slotKeys[slot].getTranslatedKeyMessage().getString());
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
    
    private static void movementInputUpdate(MovementInputUpdateEvent event)
    {
        // Credit to gigaherz for giving guidance in the NeoForge Discord
        
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        
        if(localPlayer != null && Minecraft.getInstance().screen instanceof RadialMenu)
        {
            Options options = Minecraft.getInstance().options;
            Input input = event.getInput();
            
            //See KeyboardInput#tick
            input.up = isKeyDown(options.keyUp);
            input.down = isKeyDown(options.keyDown);
            input.left = isKeyDown(options.keyLeft);
            input.right = isKeyDown(options.keyRight);
            
            //See KeyboardInput#calculateImpulse
            input.forwardImpulse = input.up == input.down ? 0F : (input.up ? 1F : -1F);
            input.leftImpulse = input.left == input.right ? 0F : (input.left ? 1F : -1F);
            input.jumping = isKeyDown(options.keyJump);
            input.shiftKeyDown = isKeyDown(options.keyShift);
            
            //See source of KeyboardInput#tick call (inside LocalPlayer#aiStep)
            if(localPlayer.isMovingSlowly())
            {
                float f = Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(localPlayer), 0F, 1F);
                input.leftImpulse *= f;
                input.forwardImpulse *= f;
            }
        }
    }
    
    private static boolean isKeyDown(KeyMapping key)
    {
        if(key.isUnbound())
        {
            return false;
        }
        
        if(key.getKey().getType() == InputConstants.Type.KEYSYM)
        {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue());
        }
        else if(key.getKey().getType() == InputConstants.Type.MOUSE)
        {
            return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue()) == GLFW.GLFW_PRESS;
        }
        
        return false;
    }
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellKeyBindings::registerKeyMappings);
        MinecraftForge.EVENT_BUS.addListener(SpellKeyBindings::clientTick);
        MinecraftForge.EVENT_BUS.addListener(SpellKeyBindings::movementInputUpdate);
    }
}

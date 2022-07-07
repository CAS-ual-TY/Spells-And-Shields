package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.progression.SpellInteractButton;
import de.cas_ual_ty.spells.client.progression.SpellNodeWidget;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.client.progression.SpellSlotWidget;
import de.cas_ual_ty.spells.network.RequestSpellProgressionMenuMessage;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.spell.base.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class SpellsClientUtil
{
    public static void onModConstruct()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpellsClientConfig.CLIENT_SPEC, SpellsAndShields.MOD_ID + "/client" + ".toml");
        
        MinecraftForge.EVENT_BUS.addListener(SpellsClientUtil::rightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(SpellsClientUtil::initScreen);
        MinecraftForge.EVENT_BUS.addListener(SpellsClientUtil::renderScreen);
        SpellKeyBindings.register();
    }
    
    public static void clientSetup(FMLClientSetupEvent event)
    {
        ManaRenderer.clientSetup(event);
        SpellKeyBindings.clientSetup(event);
        MenuScreens.register(SpellsRegistries.SPELL_PROGRESSION_MENU.get(), SpellProgressionScreen::new);
        EntityRenderers.register(SpellsRegistries.SPELL_PROJECTILE.get(), (context) -> new EntityRenderer<>(context)
        {
            @Override
            public ResourceLocation getTextureLocation(SpellProjectile pEntity)
            {
                return null;
            }
        });
        EntityRenderers.register(SpellsRegistries.HOMING_SPELL_PROJECTILE.get(), (context) -> new EntityRenderer<>(context)
        {
            @Override
            public ResourceLocation getTextureLocation(HomingSpellProjectile pEntity)
            {
                return null;
            }
        });
    }
    
    private static BlockPos lastRightClickedBlock = null;
    
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        lastRightClickedBlock = event.getPos();
    }
    
    private static List<SpellSlotWidget> spellSlotWidgets = new ArrayList<>(SpellHolder.SPELL_SLOTS);
    
    public static void initScreen(ScreenEvent.InitScreenEvent.Post event)
    {
        if(Minecraft.getInstance().player != null)
        {
            if(event.getScreen() instanceof EnchantmentScreen screen)
            {
                event.addListener(new SpellInteractButton(screen.getGuiLeft(), screen.getGuiTop() - SpellNodeWidget.FRAME_HEIGHT, 176, SpellNodeWidget.FRAME_HEIGHT, SpellProgressionMenu.TITLE,
                        (b) -> SpellsAndShields.CHANNEL.send(PacketDistributor.SERVER.noArg(), new RequestSpellProgressionMenuMessage(lastRightClickedBlock)), 0));
            }
            else if(event.getScreen() instanceof InventoryScreen screen)
            {
                spellSlotWidgets.clear();
                
                for(int i = 0; i < SpellHolder.SPELL_SLOTS; ++i)
                {
                    int x = screen.getGuiLeft() - SpellNodeWidget.FRAME_WIDTH;
                    int y = screen.getGuiTop() + i * (SpellNodeWidget.FRAME_HEIGHT + 1);
                    int slot = i;
                    SpellSlotWidget s = new SpellSlotWidget(x, y, i, (j) -> {}, (b, pS, mX, mY) -> SpellSlotWidget.spellSlotToolTip(screen, pS, mX, mY, slot));
                    spellSlotWidgets.add(s);
                    event.addListener(s);
                    s.active = false;
                }
            }
        }
    }
    
    public static void renderScreen(ScreenEvent.DrawScreenEvent.Post event)
    {
        if(event.getScreen() instanceof InventoryScreen screen)
        {
            for(SpellSlotWidget s : spellSlotWidgets)
            {
                RenderSystem.disableDepthTest();
                s.renderToolTip(event.getPoseStack(), event.getMouseX(), event.getMouseY());
                RenderSystem.enableDepthTest();
            }
        }
    }
}

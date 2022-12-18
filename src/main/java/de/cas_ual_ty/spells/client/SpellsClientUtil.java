package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.progression.SpellInteractButton;
import de.cas_ual_ty.spells.client.progression.SpellNodeWidget;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.client.progression.SpellSlotWidget;
import de.cas_ual_ty.spells.network.RequestSpellProgressionMenuMessage;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.registers.BuiltinRegistries;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import de.cas_ual_ty.spells.spell.base.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.util.ManaTooltipComponent;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

public class SpellsClientUtil
{
    public static void onModConstruct()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpellsClientConfig.CLIENT_SPEC, SpellsAndShields.MOD_ID + "/client" + ".toml");
        
        SpellKeyBindings.register();
        ManaRenderer.register();
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellsClientUtil::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellsClientUtil::entityRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellsClientUtil::registerClientTooltipComponent);
        
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, SpellsClientUtil::rightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(SpellsClientUtil::initScreen);
        MinecraftForge.EVENT_BUS.addListener(SpellsClientUtil::renderScreen);
    }
    
    private static void clientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(BuiltinRegistries.SPELL_PROGRESSION_MENU.get(), SpellProgressionScreen::new);
        SpellIconRegistry.register(SpellIconTypes.DEFAULT_SPELL_ICON.get(), SpellIconRegistry.DEFAULT_RENDERER);
    }
    
    private static void entityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(BuiltinRegistries.SPELL_PROJECTILE.get(), (context) -> new EntityRenderer<>(context)
        {
            @Override
            public ResourceLocation getTextureLocation(SpellProjectile pEntity)
            {
                return null;
            }
        });
        event.registerEntityRenderer(BuiltinRegistries.HOMING_SPELL_PROJECTILE.get(), (context) -> new EntityRenderer<>(context)
        {
            @Override
            public ResourceLocation getTextureLocation(HomingSpellProjectile pEntity)
            {
                return null;
            }
        });
    }
    
    public static BlockPos lastRightClickedBlockPos = null;
    public static Block lastRightClickedBlock = null;
    
    private static void rightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        lastRightClickedBlockPos = event.getPos();
        lastRightClickedBlock = event.getEntity().level.getBlockState(event.getPos()).getBlock();
    }
    
    private static List<SpellSlotWidget> spellSlotWidgets = new ArrayList<>(SpellHolder.SPELL_SLOTS);
    
    private static void initScreen(ScreenEvent.Init.Post event)
    {
        if(Minecraft.getInstance().player != null && event.getScreen() instanceof AbstractContainerScreen screen)
        {
            if(SpellsUtil.isEnchantingTable(lastRightClickedBlock))
            {
                lastRightClickedBlock = null;
                event.addListener(new SpellInteractButton(screen.getGuiLeft(), screen.getGuiTop() - SpellNodeWidget.FRAME_HEIGHT, Math.min(176, screen.width), SpellNodeWidget.FRAME_HEIGHT, SpellProgressionMenu.TITLE,
                        (b) ->
                        {
                            SpellsAndShields.CHANNEL.send(PacketDistributor.SERVER.noArg(), new RequestSpellProgressionMenuMessage(lastRightClickedBlockPos));
                        },
                        0));
            }
            else if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
            {
                if(screen.getGuiLeft() == 0 && screen.getGuiTop() == 0)
                {
                    return;
                }
                
                spellSlotWidgets.clear();
                
                RecipeBookComponent recipeBook = null;
                
                if(screen instanceof InventoryScreen)
                {
                    for(GuiEventListener l : event.getListenersList())
                    {
                        if(l instanceof RecipeBookComponent c)
                        {
                            recipeBook = c;
                        }
                    }
                }
                
                final RecipeBookComponent finalRecipeBook = recipeBook;
                
                BooleanSupplier isRecipeBookClosed = finalRecipeBook != null ? () -> !finalRecipeBook.isVisible() : () -> true;
                BooleanSupplier hasSpellLearned = () ->
                {
                    if(SpellsClientConfig.ALWAYS_SHOW_SPELL_SLOTS.get())
                    {
                        return true;
                    }
                    
                    AtomicBoolean ret = new AtomicBoolean(false);
                    
                    if(Minecraft.getInstance().player != null)
                    {
                        SpellHolder.getSpellHolder(Minecraft.getInstance().player).ifPresent(spellHolder ->
                        {
                            for(int i = 0; i < spellHolder.getSlots(); i++)
                            {
                                if(spellHolder.getSpell(i) != null)
                                {
                                    ret.set(true);
                                    return;
                                }
                            }
                        });
                    }
                    
                    return ret.get();
                };
                
                for(int i = 0; i < SpellHolder.SPELL_SLOTS; ++i)
                {
                    int x = screen.getGuiLeft() - SpellNodeWidget.FRAME_WIDTH;
                    int y = screen.getGuiTop() + i * (SpellNodeWidget.FRAME_HEIGHT + 1);
                    
                    // if the recipe book is open already, fix position
                    if(!isRecipeBookClosed.getAsBoolean())
                    {
                        x -= 77;
                    }
                    
                    int slot = i;
                    SpellSlotWidget s = new SpellSlotWidget(x, y, i, (j) -> {}, (b, pS, mX, mY) -> SpellSlotWidget.spellSlotToolTip(screen, pS, mX, mY, slot))
                    {
                        @Override
                        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
                        {
                            this.visible = isRecipeBookClosed.getAsBoolean() && hasSpellLearned.getAsBoolean();
                            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
                        }
                    };
                    spellSlotWidgets.add(s);
                    event.addListener(s);
                    s.active = false;
                }
            }
        }
    }
    
    private static void renderScreen(ScreenEvent.Render.Post event)
    {
        if(event.getScreen() instanceof AbstractContainerScreen screen && (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen))
        {
            for(SpellSlotWidget s : spellSlotWidgets)
            {
                RenderSystem.disableDepthTest();
                s.renderToolTip(event.getPoseStack(), event.getMouseX(), event.getMouseY());
                RenderSystem.enableDepthTest();
            }
        }
    }
    
    private static void registerClientTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(ManaTooltipComponent.class, tooltip -> new ManaClientTooltipComponent(tooltip.mana));
    }
    
    public static LazyOptional<ManaHolder> getClientManaHolder()
    {
        if(Minecraft.getInstance().player != null)
        {
            return ManaHolder.getManaHolder(Minecraft.getInstance().player);
        }
        else
        {
            return LazyOptional.empty();
        }
    }
}

package de.cas_ual_ty.spells.client;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.capability.ParticleEmitterHolder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.progression.SpellInteractButton;
import de.cas_ual_ty.spells.client.progression.SpellNodeWidget;
import de.cas_ual_ty.spells.client.progression.SpellProgressionScreen;
import de.cas_ual_ty.spells.client.progression.SpellSlotWidget;
import de.cas_ual_ty.spells.network.RequestSpellProgressionMenuMessage;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import de.cas_ual_ty.spells.spell.projectile.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.projectile.SpellProjectile;
import de.cas_ual_ty.spells.util.ManaTooltipComponent;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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
        
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, SpellsClientUtil::rightClickBlock);
        NeoForge.EVENT_BUS.addListener(SpellsClientUtil::initScreen);
        NeoForge.EVENT_BUS.addListener(SpellsClientUtil::renderScreen);
        NeoForge.EVENT_BUS.addListener(SpellsClientUtil::levelTick);
        //MinecraftForge.EVENT_BUS.addListener(ScreenDebugHelper::event);
    }
    
    private static void clientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(BuiltInRegisters.SPELL_PROGRESSION_MENU.get(), SpellProgressionScreen::new);
        SpellIconRegistry.register(SpellIconTypes.DEFAULT.get(), SpellIconRegistry.DEFAULT_RENDERER);
        SpellIconRegistry.register(SpellIconTypes.SIZED.get(), SpellIconRegistry.SIZED_RENDERER);
        SpellIconRegistry.register(SpellIconTypes.ADVANCED.get(), SpellIconRegistry.ADVANCED_RENDERER);
        SpellIconRegistry.register(SpellIconTypes.ITEM.get(), SpellIconRegistry.ITEM_RENDERER);
        SpellIconRegistry.register(SpellIconTypes.LAYERED.get(), SpellIconRegistry.LAYERED_RENDERER);
    }
    
    private static void entityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(BuiltInRegisters.SPELL_PROJECTILE.get(), (context) -> new EntityRenderer<>(context)
        {
            @Override
            public ResourceLocation getTextureLocation(SpellProjectile pEntity)
            {
                return null;
            }
        });
        event.registerEntityRenderer(BuiltInRegisters.HOMING_SPELL_PROJECTILE.get(), (context) -> new EntityRenderer<>(context)
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
        if(event.getLevel().isClientSide)
        {
            lastRightClickedBlockPos = event.getPos();
            lastRightClickedBlock = event.getEntity().level().getBlockState(event.getPos()).getBlock();
        }
    }
    
    private static List<SpellSlotWidget> spellSlotWidgets = new ArrayList<>(SpellHolder.SPELL_SLOTS);
    
    private static void initScreen(ScreenEvent.Init.Post event)
    {
        if(Minecraft.getInstance().player != null && event.getScreen() instanceof AbstractContainerScreen screen)
        {
            if(SpellsUtil.isTrueEnchantingTable(lastRightClickedBlock))
            {
                lastRightClickedBlock = null;
                event.addListener(new SpellInteractButton(screen.getGuiLeft(), screen.getGuiTop() - SpellNodeWidget.FRAME_HEIGHT, Math.min(176, screen.width), SpellNodeWidget.FRAME_HEIGHT, SpellProgressionMenu.TITLE,
                        (b) ->
                        {
                            SpellsAndShields.CHANNEL.send(PacketDistributor.SERVER.noArg(), new RequestSpellProgressionMenuMessage(lastRightClickedBlockPos));
                        },
                        AdvancementWidgetType.OBTAINED.boxSprite()));
            }
            else if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
            {
                if(screen.getGuiLeft() == 0 && screen.getGuiTop() == 0)
                {
                    return;
                }
                
                spellSlotWidgets.clear();
                
                // -----------------------------------------
                // recipe book
                
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
                
                // -----------------------------------------
                
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
                
                // -----------------------------------------
                
                int guiLeft = (screen.width - screen.getXSize()) / 2;
                
                // total width of the recipe book together with
                // space in between inventory and this part of the gui
                // and also includes width of the tabs at the left
                final int recipeBookGuiWidth = 181;
                
                SlotsPosition slotsPosition;
                int offX;
                int offY;
                int margin;
                
                if(screen instanceof InventoryScreen)
                {
                    slotsPosition = SlotsPosition.fromId(SpellsClientConfig.SPELL_SLOTS_POSITION_SURVIVAL.get());
                    offX = SpellsClientConfig.SPELL_SLOTS_POSITION_SURVIVAL_OFFSET_X.get();
                    offY = SpellsClientConfig.SPELL_SLOTS_POSITION_SURVIVAL_OFFSET_Y.get();
                    margin = SpellsClientConfig.SPELL_SLOTS_POSITION_SURVIVAL_SPACING.get();
                }
                else
                {
                    slotsPosition = SlotsPosition.fromId(SpellsClientConfig.SPELL_SLOTS_POSITION_CREATIVE.get());
                    offX = SpellsClientConfig.SPELL_SLOTS_POSITION_CREATIVE_OFFSET_X.get();
                    offY = SpellsClientConfig.SPELL_SLOTS_POSITION_CREATIVE_OFFSET_Y.get();
                    margin = SpellsClientConfig.SPELL_SLOTS_POSITION_CREATIVE_SPACING.get();
                }
                
                for(int i = 0; i < SpellHolder.SPELL_SLOTS; ++i)
                {
                    int x1 = offX + i * slotsPosition.incrementX(SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, margin);
                    int y1 = slotsPosition.startPositionY(screen.width, screen.height, screen.getGuiLeft(), screen.getGuiTop(), screen.getXSize(), screen.getYSize(), SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, SpellHolder.SPELL_SLOTS, margin)
                            + offY + i * slotsPosition.incrementY(SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, margin);
                    
                    int slot = i;
                    SpellSlotWidget s = new SpellSlotWidget(0, y1, i, (j) -> {})
                    {
                        @Override
                        public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick)
                        {
                            visible = hasSpellLearned.getAsBoolean();
                            
                            if(visible)
                            {
                                if(isRecipeBookClosed.getAsBoolean())
                                {
                                    setX(x1 + slotsPosition.startPositionX(screen.width, screen.height, screen.getGuiLeft(), screen.getGuiTop(), screen.getXSize(), screen.getYSize(), SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, SpellHolder.SPELL_SLOTS, margin));
                                }
                                else
                                {
                                    int altLeft = screen.getGuiLeft() - recipeBookGuiWidth;
                                    int altXSize = screen.getXSize() + recipeBookGuiWidth;
                                    
                                    int x2 = slotsPosition.startPositionX(screen.width, screen.height, altLeft, screen.getGuiTop(), altXSize, screen.getYSize(), SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, SpellHolder.SPELL_SLOTS, margin);
                                    
                                    if(slotsPosition.isVertical && slotsPosition.atScreenEdge)
                                    {
                                        int x3 = slotsPosition.transform().startPositionX(screen.width, screen.height, altLeft, screen.getGuiTop(), altXSize, screen.getYSize(), SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, SpellHolder.SPELL_SLOTS, margin);
                                        
                                        if(slotsPosition.isLeft)
                                        {
                                            x2 = Math.min(x2, x3);
                                        }
                                        else if(slotsPosition.isRight)
                                        {
                                            x2 = Math.max(x2, x3);
                                        }
                                    }
                                    
                                    setX(x1 + x2);
                                }
                            }
                            
                            super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
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
                if(s.isMouseOver(event.getMouseX(), event.getMouseY()))
                {
                    SpellSlotWidget.spellSlotToolTip(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), s.slot);
                }
            }
        }
    }
    
    private static void registerClientTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(ManaTooltipComponent.class, tooltip -> new ManaClientTooltipComponent(tooltip.mana));
    }
    
    private static void levelTick(TickEvent.LevelTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END && event.level instanceof ClientLevel level)
        {
            for(Entity entity : level.getEntities().getAll())
            {
                ParticleEmitterHolder.getHolder(entity).ifPresent(h -> h.tick(true));
            }
        }
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
    
    public static Level getClientLevel()
    {
        return Minecraft.getInstance().level;
    }
    
    public static Player getClientPlayer()
    {
        return Minecraft.getInstance().player;
    }
}

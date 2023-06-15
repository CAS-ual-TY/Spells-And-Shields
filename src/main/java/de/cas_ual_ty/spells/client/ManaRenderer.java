package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.BuiltinRegistries;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Random;

public class ManaRenderer implements IGuiOverlay
{
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation(SpellsAndShields.MOD_ID, "textures/gui/spells_icons.png");
    
    protected Minecraft minecraft;
    
    protected final Random random = new Random();
    
    protected int lastMana;
    protected int displayMana;
    protected long lastManaTime;
    protected long manaBlinkTime;
    
    protected long lastManaChangeTime;
    
    protected final boolean above;
    protected final boolean right;
    
    public ManaRenderer(boolean above, boolean right)
    {
        minecraft = Minecraft.getInstance();
        this.above = above;
        this.right = right;
    }
    
    @Override
    public void render(ForgeGui gui, PoseStack mStack, float partialTicks, int width, int height)
    {
        if(above == SpellsClientConfig.MANA_ABOVE_FOOD.get() && right == !SpellsClientConfig.MANA_BY_HEALTH.get() && !Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements())
        {
            gui.setupOverlayRenderState(true, false);
            renderMana(gui, width, height, mStack);
        }
    }
    
    public void renderMana(ForgeGui gui, int width, int height, PoseStack pStack)
    {
        if(minecraft.getCameraEntity() instanceof Player player)
        {
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION); //== bind
            RenderSystem.enableBlend();
            
            ManaHolder.getManaHolder(player).ifPresent(manaHolder ->
            {
                float maxMana = manaHolder.getMaxMana();
                
                if(maxMana <= 0)
                {
                    return;
                }
                
                int mana = Mth.ceil(manaHolder.getMana());
                
                if(manaHolder.getMana() >= maxMana)
                {
                    if(mana != lastMana)
                    {
                        lastManaChangeTime = Util.getMillis();
                    }
                    
                    int hideManaTime = SpellsClientConfig.MANA_HIDE_DELAY.get();
                    if(hideManaTime != 0 && UnitType.forPlayer(player) == UnitType.NORMAL && (Util.getMillis() - this.lastManaChangeTime) >= hideManaTime * 50L)
                    {
                        return;
                    }
                }
                
                boolean highlight = manaBlinkTime > (long) gui.getGuiTicks() && (manaBlinkTime - (long) gui.getGuiTicks()) / 3L % 2L == 1L;
                
                if(mana < this.lastMana && manaHolder.changeTime > 0)
                {
                    this.lastManaTime = Util.getMillis();
                    this.manaBlinkTime = gui.getGuiTicks() + 20;
                }
                else if(mana > this.lastMana && manaHolder.changeTime > 0)
                {
                    this.lastManaTime = Util.getMillis();
                    this.manaBlinkTime = gui.getGuiTicks() + 10;
                }
                
                if(Util.getMillis() - this.lastManaTime > 1000L)
                {
                    this.lastMana = mana;
                    this.displayMana = mana;
                    this.lastManaTime = Util.getMillis();
                }
                
                this.lastMana = mana;
                int manaLast = this.displayMana;
                
                float manaMax = Math.max(maxMana, Math.max(manaLast, mana));
                int extra = Mth.ceil(manaHolder.getExtraMana());
                
                int rows = Mth.ceil((manaMax + extra) / 2F / 10F);
                int rowHeight = Math.max(10 - (rows - 2), 3);
                
                this.random.setSeed(gui.getGuiTicks() * 27L);
                
                int left = right ? width / 2 + 10 : width / 2 - 91;
                
                int top = height - (right ? gui.rightHeight : gui.leftHeight);
                
                if(!right && above && player.getArmorValue() <= 0)
                {
                    top += 10;
                }
                
                int regen = -1;
                
                if(player.hasEffect(BuiltinRegistries.REPLENISHMENT_EFFECT.get()))
                {
                    regen = gui.getGuiTicks() % Mth.ceil(manaMax + 5F);
                }
                
                this.renderUnit(gui, pStack, player, left, top, rowHeight, regen, manaMax, mana, manaLast, extra, highlight);
                
                int change = (rows * rowHeight) + (rowHeight != 10 ? 10 - rowHeight : 0);
                
                if(right)
                {
                    gui.rightHeight += change;
                }
                else
                {
                    gui.leftHeight += change;
                }
            });
            
            RenderSystem.disableBlend();
        }
    }
    
    protected void renderUnit(ForgeGui gui, PoseStack poseStack, Player player, int left, int top, int rowHeight, int regen, float manaMax, int mana, int manaLast, int extra, boolean highlight)
    {
        UnitType unitType = UnitType.forPlayer(player);
        
        int v = 0;
        int totalUnits = Mth.ceil((double) manaMax / 2D);
        int totalExtraUnits = Mth.ceil((double) extra / 2D);
        int manaCeil = totalUnits * 2;
        
        for(int idx = totalUnits + totalExtraUnits - 1; idx >= 0; --idx)
        {
            int row = idx / 10;
            int column = idx % 10;
            int x = left + column * 8;
            int y = top - row * rowHeight;
            
            if(SpellsClientConfig.MANA_JITTER.get() && mana + extra <= 4)
            {
                y += this.random.nextInt(2);
            }
            
            if(idx < totalUnits && idx == regen)
            {
                y -= 2;
            }
            
            this.renderUnit(gui, poseStack, UnitType.CONTAINER, x, y, v, highlight, false);
            
            int idx2 = idx * 2;
            boolean renderExtra = idx >= totalUnits;
            
            if(renderExtra)
            {
                int i = idx2 - manaCeil;
                if(i < extra)
                {
                    boolean half = i + 1 == extra;
                    this.renderUnit(gui, poseStack, UnitType.EXTRA, x, y, v, false, half);
                }
            }
            
            if(highlight && idx2 < manaLast)
            {
                boolean half = idx2 + 1 == manaLast;
                this.renderUnit(gui, poseStack, unitType, x, y, v, true, half);
            }
            
            if(idx2 < mana)
            {
                boolean half = idx2 + 1 == mana;
                this.renderUnit(gui, poseStack, unitType, x, y, v, false, half);
            }
        }
    }
    
    private void renderUnit(ForgeGui gui, PoseStack poseStack, UnitType unitType, int x, int y, int v, boolean highlight, boolean half)
    {
        GuiComponent.blit(poseStack, x, y, unitType.getU(half, highlight), v, 9, 9);
    }
    
    public enum UnitType
    {
        CONTAINER(0, false),
        NORMAL(2, true),
        DRAINED(4, true),
        EXTRA(6, false);
        
        private final int index;
        private final boolean whiteFlash;
        
        UnitType(int index, boolean canBlink)
        {
            this.index = index;
            this.whiteFlash = canBlink;
        }
        
        public int getU(boolean half, boolean highlight)
        {
            int i;
            
            if(this == CONTAINER)
            {
                i = highlight ? 1 : 0;
            }
            else
            {
                i = (half ? 1 : 0) + (this.whiteFlash && highlight ? 2 : 0);
            }
            
            return (this.index * 2 + i) * 9;
        }
        
        static UnitType forPlayer(Player player)
        {
            if(player.hasEffect(BuiltinRegistries.LEAKING_MOB_EFFECT.get()))
            {
                return DRAINED;
            }
            else
            {
                return NORMAL;
            }
        }
    }
    
    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event)
    {
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "player_mana_above_hunger", new ManaRenderer(true, true));
        event.registerBelow(VanillaGuiOverlay.FOOD_LEVEL.id(), "player_mana_below_hunger", new ManaRenderer(true, false));
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "player_mana_above_health", new ManaRenderer(false, true));
        event.registerBelow(VanillaGuiOverlay.PLAYER_HEALTH.id(), "player_mana_below_health", new ManaRenderer(false, false));
    }
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ManaRenderer::registerGuiOverlays);
    }
}

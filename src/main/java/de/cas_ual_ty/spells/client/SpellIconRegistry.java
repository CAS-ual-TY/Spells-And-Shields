package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import de.cas_ual_ty.spells.spell.icon.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SpellIconRegistry
{
    private static final DefaultSpellIcon ERROR_FALLBACK = new DefaultSpellIcon(SpellIconTypes.DEFAULT.get(), new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/default_fallback.png"));
    
    public static final SpellIconRenderer<DefaultSpellIcon> DEFAULT_RENDERER = (icon, guiGraphics, width, height, x, y, partialTicks) -> {
        RenderSystem.enableBlend();
        int offX = (width - 18) / 2;
        int offY = (height - 18) / 2;
        guiGraphics.blit(icon.getTexture(), x + offX, y + offY, 18, 18, 0, 0, 18, 18, 18, 18);
        RenderSystem.disableBlend();
    };
    
    public static final SpellIconRenderer<SizedSpellIcon> SIZED_RENDERER = (icon, guiGraphics, width, height, x, y, partialTicks) -> {
        RenderSystem.enableBlend();
        int offX = (width - icon.getSize()) / 2;
        int offY = (height - icon.getSize()) / 2;
        guiGraphics.blit(icon.getTexture(), x + offX, y + offY, icon.getSize(), icon.getSize(), 0, 0, icon.getSize(), icon.getSize(), icon.getSize(), icon.getSize());
        RenderSystem.disableBlend();
    };
    
    public static final SpellIconRenderer<ItemSpellIcon> ITEM_RENDERER = (icon, guiGraphics, width, height, x, y, partialTicks) -> {
        RenderSystem.enableBlend();
        guiGraphics.renderItem(icon.getItem(), x + (width - 16) / 2, y + (width - 16) / 2);
        RenderSystem.disableBlend();
    };
    
    public static final SpellIconRenderer<AdvancedSpellIcon> ADVANCED_RENDERER = (icon, guiGraphics, width, height, x, y, partialTicks) -> {
        RenderSystem.enableBlend();
        int offX = (width - icon.getWidth()) / 2 + icon.getOffsetX();
        int offY = (height - icon.getHeight()) / 2 + icon.getOffsetY();
        guiGraphics.blit(icon.getTexture(), x + offX, y + offY, icon.getWidth(), icon.getHeight(), icon.getU(), icon.getV(), icon.getWidth(), icon.getHeight(), icon.getTextureWidth(), icon.getTextureHeight());
        RenderSystem.disableBlend();
    };
    
    public static final SpellIconRenderer<LayeredSpellIcon> LAYERED_RENDERER = (icon, guiGraphics, width, height, x, y, partialTicks) -> {
        for(SpellIcon i : icon.getList())
        {
            render(i, guiGraphics, width, height, x, y, partialTicks);
        }
    };
    
    public static final SpellIconRenderer<DefaultSpellIcon> ERROR_FALLBACK_RENDERER = (icon, guiGraphics, width, height, x, y, partialTicks) -> {
        DEFAULT_RENDERER.render(ERROR_FALLBACK, guiGraphics, width, height, x, y, partialTicks);
    };
    
    private static Map<SpellIconType<?>, SpellIconRenderer<?>> rendererMap = new HashMap<>();
    
    public static <I extends SpellIcon> void register(SpellIconType<I> iconType, SpellIconRenderer<I> renderer)
    {
        rendererMap.put(iconType, renderer);
    }
    
    public static <I extends SpellIcon> void render(I icon, GuiGraphics guiGraphics, int frameWidth, int frameHeight, int x, int y, float partialTicks)
    {
        SpellIconRenderer<I> renderer = (SpellIconRenderer<I>) rendererMap.getOrDefault(icon.type, ERROR_FALLBACK_RENDERER);
        renderer.render(icon, guiGraphics, frameWidth, frameHeight, x, y, partialTicks);
    }
    
    public interface SpellIconRenderer<I extends SpellIcon>
    {
        void render(I icon, GuiGraphics guiGraphics, int frameWidth, int frameHeight, int x, int y, float partialTicks);
    }
}

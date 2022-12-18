package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import de.cas_ual_ty.spells.spell.icon.DefaultSpellIcon;
import de.cas_ual_ty.spells.spell.icon.SizedSpellIcon;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spell.icon.SpellIconType;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SpellIconRegistry
{
    private static final DefaultSpellIcon ERROR_FALLBACK = new DefaultSpellIcon(SpellIconTypes.DEFAULT.get(), new ResourceLocation(SpellsAndShields.MOD_ID, "textures/spell/default_fallback.png"));
    
    public static final SpellIconRenderer<DefaultSpellIcon> DEFAULT_RENDERER = (icon, poseStack, width, height, x, y, partialTicks) -> {
        RenderSystem.setShaderTexture(0, icon.getTexture());
        RenderSystem.enableBlend();
        int offX = (width - 18) / 2;
        int offY = (height - 18) / 2;
        GuiComponent.blit(poseStack, x + offX, y + offY, 18, 18, 0, 0, 18, 18, 18, 18);
        RenderSystem.disableBlend();
    };
    
    public static final SpellIconRenderer<SizedSpellIcon> SIZED_RENDERER = (icon, poseStack, width, height, x, y, partialTicks) -> {
        RenderSystem.setShaderTexture(0, icon.getTexture());
        RenderSystem.enableBlend();
        int offX = (width - icon.getSize()) / 2;
        int offY = (height - icon.getSize()) / 2;
        GuiComponent.blit(poseStack, x + offX, y + offY, icon.getSize(), icon.getSize(), 0, 0, icon.getSize(), icon.getSize(), icon.getSize(), icon.getSize());
        RenderSystem.disableBlend();
    };
    
    public static final SpellIconRenderer<DefaultSpellIcon> ERROR_FALLBACK_RENDERER = (icon, poseStack, width, height, x, y, partialTicks) -> {
        DEFAULT_RENDERER.render(ERROR_FALLBACK, poseStack, width, height, x, y, partialTicks);
    };
    
    private static Map<SpellIconType<?>, SpellIconRenderer<?>> rendererMap = new HashMap<>();
    
    public static <I extends SpellIcon> void register(SpellIconType<I> iconType, SpellIconRenderer<I> renderer)
    {
        rendererMap.put(iconType, renderer);
    }
    
    public static <I extends SpellIcon> void render(I icon, PoseStack poseStack, int frameWidth, int frameHeight, int x, int y, float partialTicks)
    {
        SpellIconRenderer<I> renderer = (SpellIconRenderer<I>) rendererMap.getOrDefault(icon.type, ERROR_FALLBACK_RENDERER);
        renderer.render(icon, poseStack, frameWidth, frameHeight, x, y, partialTicks);
    }
    
    public interface SpellIconRenderer<I extends SpellIcon>
    {
        void render(I icon, PoseStack poseStack, int frameWidth, int frameHeight, int x, int y, float partialTicks);
    }
}

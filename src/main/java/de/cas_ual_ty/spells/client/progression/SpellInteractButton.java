package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpellInteractButton extends Button
{
    private static final ResourceLocation TITLE_BOX_SPRITE = new ResourceLocation("advancements/title_box");
    
    public final ResourceLocation v;
    
    public SpellInteractButton(int x, int y, int width, int height, Component component, OnPress onPress, ResourceLocation v)
    {
        super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
        this.v = v;
    }
    
    public SpellInteractButton(int x, int y, int width, int height, Component component, OnPress onPress)
    {
        this(x, y, width, height, component, onPress, TITLE_BOX_SPRITE);
    }
    
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick)
    {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
        ResourceLocation sprite = isHoveredOrFocused() && active ? v : TITLE_BOX_SPRITE;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(sprite, getX(), getY(), width, height);
        Font font = minecraft.font;
        renderTitle(guiGraphics, mouseX, mouseY, deltaTick, font);
    }
    
    public void renderTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick, Font font)
    {
        int color = getFGColor();
        guiGraphics.drawCenteredString(font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, color | Mth.ceil(alpha * 255F) << 24);
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return visible && mouseX >= (double) getX() && mouseY >= (double) getY() && mouseX < (double) (getX() + width) && mouseY < (double) (getY() + height);
    }
}

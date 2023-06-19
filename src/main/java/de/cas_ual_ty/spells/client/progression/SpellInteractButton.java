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
    public static final ResourceLocation ADVANCEMENT_WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    
    public final int v;
    
    public SpellInteractButton(int x, int y, int width, int height, Component component, OnPress onPress, int v)
    {
        super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
        this.v = v;
    }
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick)
    {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, this.alpha);
        int i = this.isHoveredOrFocused() && active ? this.v : 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blit(ADVANCEMENT_WIDGETS_LOCATION, this.getX(), this.getY(), 0, i * SpellNodeWidget.FRAME_HEIGHT, this.width / 2, this.height);
        guiGraphics.blit(ADVANCEMENT_WIDGETS_LOCATION, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, i * SpellNodeWidget.FRAME_HEIGHT, this.width / 2, this.height);
        Font font = minecraft.font;
        this.renderTitle(guiGraphics, mouseX, mouseY, deltaTick, font);
    }
    
    public void renderTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick, Font font)
    {
        int color = getFGColor();
        guiGraphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color | Mth.ceil(this.alpha * 255F) << 24);
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return this.visible && mouseX >= (double) this.getX() && mouseY >= (double) this.getY() && mouseX < (double) (this.getX() + this.width) && mouseY < (double) (this.getY() + this.height);
    }
}

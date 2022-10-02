package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class SpellInteractButton extends Button
{
    public final int v;
    
    public SpellInteractButton(int x, int y, int width, int height, Component component, OnPress onPress, int v, OnTooltip tooltip)
    {
        super(x, y, width, height, component, onPress, tooltip);
        this.v = v;
    }
    
    public SpellInteractButton(int x, int y, int width, int height, Component component, OnPress onPress, int v)
    {
        this(x, y, width, height, component, onPress, v, NO_TOOLTIP);
    }
    
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
    {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SpellNodeWidget.WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1F, 1F, 1F, this.alpha);
        int i = this.isHoveredOrFocused() && active ? this.v : 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, this.x, this.y, 0, i * SpellNodeWidget.FRAME_HEIGHT, this.width / 2, this.height);
        this.blit(poseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, i * SpellNodeWidget.FRAME_HEIGHT, this.width / 2, this.height);
        this.renderBg(poseStack, minecraft, mouseX, mouseY);
        Font font = minecraft.font;
        this.renderTitle(poseStack, mouseX, mouseY, deltaTick, font);
    }
    
    public void renderTitle(PoseStack poseStack, int mouseX, int mouseY, float deltaTick, Font font)
    {
        int color = getFGColor();
        drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, color | Mth.ceil(this.alpha * 255F) << 24);
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return this.visible && mouseX >= (double) this.x && mouseY >= (double) this.y && mouseX < (double) (this.x + this.width) && mouseY < (double) (this.y + this.height);
    }
    
    @Override
    public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY)
    {
        super.renderToolTip(pPoseStack, pMouseX, pMouseY);
    }
}

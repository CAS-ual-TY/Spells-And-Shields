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
        RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
        int i = isHoveredOrFocused() && active ? v : 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(poseStack, x, y, 0, i * SpellNodeWidget.FRAME_HEIGHT, width / 2, height);
        blit(poseStack, x + width / 2, y, 200 - width / 2, i * SpellNodeWidget.FRAME_HEIGHT, width / 2, height);
        renderBg(poseStack, minecraft, mouseX, mouseY);
        Font font = minecraft.font;
        renderTitle(poseStack, mouseX, mouseY, deltaTick, font);
    }
    
    public void renderTitle(PoseStack poseStack, int mouseX, int mouseY, float deltaTick, Font font)
    {
        int color = getFGColor();
        drawCenteredString(poseStack, font, getMessage(), x + width / 2, y + (height - 8) / 2, color | Mth.ceil(alpha * 255F) << 24);
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return visible && mouseX >= (double) x && mouseY >= (double) y && mouseX < (double) (x + width) && mouseY < (double) (y + height);
    }
    
    @Override
    public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY)
    {
        super.renderToolTip(pPoseStack, pMouseX, pMouseY);
    }
}

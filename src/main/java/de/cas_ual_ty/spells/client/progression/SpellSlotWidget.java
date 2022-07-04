package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellsClientUtil;
import de.cas_ual_ty.spells.spell.base.ISpell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.function.IntConsumer;

public class SpellSlotWidget extends Button
{
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    
    protected final int slot;
    
    public SpellSlotWidget(int x, int y, int slot, IntConsumer onPress, OnTooltip tooltip)
    {
        super(x, y, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, TextComponent.EMPTY, (b) -> onPress.accept(slot), tooltip);
        this.slot = slot;
    }
    
    
    protected void renderFrame(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
    {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        
        if(!active || isMouseOver(mouseX, mouseY))
        {
            // gold frame
            this.blit(poseStack, x, y, 2 * SpellNodeWidget.FRAME_WIDTH, 128 + SpellNodeWidget.FRAME_HEIGHT, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT);
        }
        else
        {
            // white frame
            this.blit(poseStack, x, y, 2 * SpellNodeWidget.FRAME_WIDTH, 128, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT);
        }
    }
    
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        
        renderFrame(poseStack, mouseX, mouseY, deltaTick);
        
        Player player = Minecraft.getInstance().player;
        
        if(player != null)
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                ISpell spell = spellHolder.getSpell(slot);
                
                if(spell != null)
                {
                    int offX = (SpellNodeWidget.FRAME_WIDTH - SpellNodeWidget.SPELL_WIDTH) / 2;
                    int offY = (SpellNodeWidget.FRAME_HEIGHT - SpellNodeWidget.SPELL_HEIGHT) / 2;
                    
                    ResourceLocation texture = SpellsClientUtil.getSpellTexture(spell);
                    RenderSystem.setShaderTexture(0, texture);
                    
                    // render spell icon
                    blit(poseStack, x + offX, y + offY, this.getBlitOffset(), 0, 0, SpellNodeWidget.SPELL_WIDTH, SpellNodeWidget.SPELL_HEIGHT, SpellNodeWidget.SPELL_HEIGHT, SpellNodeWidget.SPELL_WIDTH);
                }
            });
        }
        
        RenderSystem.disableBlend();
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return this.visible && mouseX >= (double) this.x && mouseY >= (double) this.y && mouseX < (double) (this.x + this.width) && mouseY < (double) (this.y + this.height);
    }
    
    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY)
    {
        if(this.isMouseOver(mouseX, mouseY))
        {
            super.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}

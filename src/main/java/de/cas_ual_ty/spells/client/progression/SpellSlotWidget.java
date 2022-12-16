package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellIconRegistry;
import de.cas_ual_ty.spells.client.SpellKeyBindings;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

public class SpellSlotWidget extends Button
{
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    
    protected final int slot;
    
    public SpellSlotWidget(int x, int y, int slot, IntConsumer onPress, OnTooltip tooltip)
    {
        super(x, y, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, Component.empty(), (b) -> onPress.accept(slot), tooltip);
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
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        
        renderFrame(poseStack, mouseX, mouseY, deltaTick);
        
        Player player = Minecraft.getInstance().player;
        
        if(player != null)
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                Spell spell = spellHolder.getSpell(slot);
                
                if(spell != null)
                {
                    SpellIcon icon = spell.getIcon();
                    SpellIconRegistry.render(icon, poseStack, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, x, y, deltaTick);
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
        if(this.visible && this.isMouseOver(mouseX, mouseY))
        {
            super.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
    
    public static void spellSlotToolTip(Screen screen, PoseStack poseStack, int mouseX, int mouseY, int slot)
    {
        Player player = Minecraft.getInstance().player;
        
        if(player != null)
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                Spell spell = spellHolder.getSpell(slot);
                
                if(spell != null)
                {
                    RenderSystem.enableDepthTest();
                    poseStack.pushPose();
                    poseStack.translate(0, 0, 10D);
                    
                    Component keyBindTooltip = SpellKeyBindings.getBaseTooltip().append(": ").append(SpellKeyBindings.getTooltip(slot).withStyle(ChatFormatting.YELLOW));
                    List<Component> tooltip = spell.getTooltip(keyBindTooltip);
                    Optional<TooltipComponent> tooltipComponent = spell.getTooltipComponent();
                    
                    screen.renderTooltip(poseStack, tooltip, tooltipComponent, mouseX, mouseY);
                    
                    poseStack.popPose();
                }
            });
        }
    }
}

package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellKeyBindings;
import de.cas_ual_ty.spells.spell.base.IPassiveSpell;
import de.cas_ual_ty.spells.spell.base.ISpell;
import de.cas_ual_ty.spells.spell.base.SpellIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
        RenderSystem.enableDepthTest();
        
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
                    
                    SpellIcon icon = spell.getIcon();
                    RenderSystem.setShaderTexture(0, icon.getTexture());
                    
                    // render spell icon
                    blit(poseStack, x + offX, y + offY, SpellNodeWidget.SPELL_WIDTH, SpellNodeWidget.SPELL_HEIGHT, icon.getU(), icon.getV(), icon.getWidth(), icon.getHeight(), icon.getTextureWidth(), icon.getTextureHeight());
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
    
    public static void spellSlotToolTip(Screen screen, PoseStack poseStack, int mouseX, int mouseY, int slot)
    {
        Player player = Minecraft.getInstance().player;
        
        if(player != null)
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                RenderSystem.enableDepthTest();
                poseStack.pushPose();
                poseStack.translate(0, 0, 10D);
                
                ISpell spell = spellHolder.getSpell(slot);
                
                List<Component> tooltip = new LinkedList<>();
                List<Component> desc = null;
                
                if(spell != null)
                {
                    tooltip.add(spell.getSpellName());
                    desc = spell.getSpellDescription();
                }
                
                if(!(spell instanceof IPassiveSpell))
                {
                    if(!SpellKeyBindings.slotKeys[slot].isUnbound())
                    {
                        tooltip.add(new TranslatableComponent("controls.keybinds.title").append(": ")
                                .append(new TextComponent(SpellKeyBindings.slotKeys[slot].getTranslatedKeyMessage().getString()).withStyle(ChatFormatting.YELLOW)));
                    }
                    else
                    {
                        tooltip.add(new TranslatableComponent("controls.keybinds.title").append(": ")
                                .append(new TranslatableComponent("key.keyboard.unknown").withStyle(ChatFormatting.RED)));
                    }
                }
                
                if(desc != null && !desc.isEmpty())
                {
                    tooltip.addAll(desc);
                }
                
                screen.renderTooltip(poseStack, tooltip, Optional.empty(), mouseX, mouseY);
                
                poseStack.popPose();
            });
        }
    }
}

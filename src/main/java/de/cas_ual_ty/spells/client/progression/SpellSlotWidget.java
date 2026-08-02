package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellsClientConfig;
import de.cas_ual_ty.spells.client.SpellsClientUtil;
import de.cas_ual_ty.spells.client.SpellsKeyBindings;
import de.cas_ual_ty.spells.client.hud.SpellIconRegistry;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

public class SpellSlotWidget extends Button
{
    public final int slot;
    public final Font font;
    
    public SpellSlotWidget(int x, int y, int slot, Font font, IntConsumer onPress)
    {
        super(x, y, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, Component.empty(), (b) -> onPress.accept(slot), DEFAULT_NARRATION);
        this.slot = slot;
        this.font = font;
    }
    
    protected void renderFrame(GuiGraphics guiGraphics, int mouseX, int mouseY, SpellHolder spellHolder, SpellInstance spell, int cooldown)
    {
        if(!active || isMouseOver(mouseX, mouseY) || cooldown > 0)
        {
            // white frame
            guiGraphics.blitSprite(AdvancementWidgetType.UNOBTAINED.frameSprite(AdvancementType.GOAL), getX(), getY(), SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT);
        }
        else
        {
            // gold frame
            guiGraphics.blitSprite(AdvancementWidgetType.OBTAINED.frameSprite(AdvancementType.GOAL), getX(), getY(), SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT);
        }
    }

    protected void renderCooldownOverlay(GuiGraphics guiGraphics, SpellHolder spellHolder, SpellInstance spell, int cooldown)
    {
        guiGraphics.drawCenteredString(font, String.valueOf((cooldown + 20 - 1) / 20), getX() + SpellNodeWidget.FRAME_WIDTH/2, getY() + (SpellNodeWidget.FRAME_HEIGHT - font.lineHeight) / 2, -1);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick)
    {
        Player player = Minecraft.getInstance().player;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableDepthTest();

        if(player != null)
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                int cooldown = spellHolder.getCooldown(slot);
                SpellInstance spell = spellHolder.getSpell(slot);

                if(spell == null || spell.getSpell() == null)
                {
                    RenderSystem.enableBlend();
                    renderFrame(guiGraphics, mouseX, mouseY, spellHolder, spell, cooldown);
                    RenderSystem.disableBlend();
                    return;
                }

                if(cooldown > 0)
                {
                    RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1F);
                }

                RenderSystem.enableBlend();
                renderFrame(guiGraphics, mouseX, mouseY, spellHolder, spell, cooldown);
                RenderSystem.disableBlend();
                SpellIcon icon = spell.getSpell().value().getIcon();
                SpellIconRegistry.render(icon, guiGraphics, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, getX(), getY(), deltaTick);

                if(cooldown > 0)
                {
                    RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                    RenderSystem.enableBlend();
                    renderCooldownOverlay(guiGraphics, spellHolder, spell, cooldown);
                    RenderSystem.disableBlend();
                }
            });
        }
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return visible && mouseX >= (double) getX() && mouseY >= (double) getY() && mouseX < (double) (getX() + width) && mouseY < (double) (getY() + height);
    }
    
    public static void spellSlotToolTip(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, int slot)
    {
        Player player = Minecraft.getInstance().player;
        
        if(player != null)
        {
            SpellHolder.getSpellHolder(player).ifPresent(spellHolder ->
            {
                SpellInstance spell = spellHolder.getSpell(slot);
                
                if(spell != null && spell.getSpell() != null)
                {
                    RenderSystem.enableDepthTest();
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, 10D);
                    
                    Component keyBindTooltip = SpellsKeyBindings.getBaseTooltip().append(": ").append(SpellsKeyBindings.getTooltip(slot).withStyle(ChatFormatting.YELLOW));
                    List<Component> tooltip = spell.getSpell().value().makeTooltipList(keyBindTooltip);
                    Optional<TooltipComponent> tooltipComponent = spell.getTooltipComponent();
                    
                    if(SpellsClientConfig.SHOW_IDS.get())
                    {
                        Registry<Spell> spellRegistry = Spells.getRegistry(SpellsClientUtil.getClientLevel());
                        tooltip.add(Component.literal(spell.getSpell().unwrap().map(ResourceKey::location, spellRegistry::getKey).toString()).withStyle(ChatFormatting.DARK_GRAY));
                        if(spell.getNodeId() != null)
                        {
                            tooltip.add(Component.literal(spell.getNodeId().getIDText()).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }
                    
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, tooltipComponent, mouseX, mouseY);
                    
                    guiGraphics.pose().popPose();
                }
            });
        }
    }
}

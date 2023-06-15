package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.SpellIconRegistry;
import de.cas_ual_ty.spells.client.SpellKeyBindings;
import de.cas_ual_ty.spells.client.SpellsClientConfig;
import de.cas_ual_ty.spells.client.SpellsClientUtil;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

public class SpellSlotWidget extends Button
{
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    
    public final int slot;
    
    public SpellSlotWidget(int x, int y, int slot, IntConsumer onPress)
    {
        super(x, y, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, Component.empty(), (b) -> onPress.accept(slot), DEFAULT_NARRATION);
        this.slot = slot;
    }
    
    protected void renderFrame(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
    {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        
        if(!active || isMouseOver(mouseX, mouseY))
        {
            // gold frame
            blit(poseStack, getX(), getY(), 2 * SpellNodeWidget.FRAME_WIDTH, 128 + SpellNodeWidget.FRAME_HEIGHT, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT);
        }
        else
        {
            // white frame
            blit(poseStack, getX(), getY(), 2 * SpellNodeWidget.FRAME_WIDTH, 128, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT);
        }
    }
    
    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
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
                SpellInstance spell = spellHolder.getSpell(slot);
                
                if(spell != null && spell.getSpell() != null)
                {
                    SpellIcon icon = spell.getSpell().get().getIcon();
                    SpellIconRegistry.render(icon, poseStack, SpellNodeWidget.FRAME_WIDTH, SpellNodeWidget.FRAME_HEIGHT, getX(), getY(), deltaTick);
                }
            });
        }
        
        RenderSystem.disableBlend();
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return this.visible && mouseX >= (double) getX() && mouseY >= (double) getY() && mouseX < (double) (getX() + this.width) && mouseY < (double) (getY() + this.height);
    }
    
    public static void spellSlotToolTip(Screen screen, PoseStack poseStack, int mouseX, int mouseY, int slot)
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
                    poseStack.pushPose();
                    poseStack.translate(0, 0, 10D);
                    
                    Component keyBindTooltip = SpellKeyBindings.getBaseTooltip().append(": ").append(SpellKeyBindings.getTooltip(slot).withStyle(ChatFormatting.YELLOW));
                    List<Component> tooltip = spell.getSpell().get().makeTooltipList(keyBindTooltip);
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
                    
                    screen.renderTooltip(poseStack, tooltip, tooltipComponent, mouseX, mouseY);
                    
                    poseStack.popPose();
                }
            });
        }
    }
}

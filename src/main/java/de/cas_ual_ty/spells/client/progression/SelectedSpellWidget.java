package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import de.cas_ual_ty.spells.client.SpellIconRegistry;
import de.cas_ual_ty.spells.client.SpellsClientConfig;
import de.cas_ual_ty.spells.client.SpellsClientUtil;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Optional;

import static de.cas_ual_ty.spells.client.progression.SpellNodeWidget.*;

public class SelectedSpellWidget
{
    protected int x;
    protected int y;
    protected int w;
    
    public final Font font;
    
    public boolean active = true;
    public SpellNodeWidget clickedWidget;
    
    public SelectedSpellWidget(int x, int y, int w)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        
        this.font = Minecraft.getInstance().font;
        
        clickedWidget = null;
    }
    
    public void setClickedWidget(SpellNodeWidget clickedWidget)
    {
        this.clickedWidget = clickedWidget;
    }
    
    public void drawHover(GuiGraphics guiGraphics, float deltaTick)
    {
        if(active && clickedWidget != null)
        {
            int w1 = 60;
            int w2 = this.w - 60;
            
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.enableBlend();
            
            int x = this.x;
            int y = this.y;
            
            guiGraphics.blit(WIDGETS_LOCATION, x, y, 0, clickedWidget.titleIcon * FRAME_HEIGHT, w1, FRAME_HEIGHT);
            guiGraphics.blit(WIDGETS_LOCATION, x + w1, y, BAR_WIDTH - w2, clickedWidget.titleIcon * FRAME_HEIGHT, w2, FRAME_HEIGHT);
            
            guiGraphics.blit(WIDGETS_LOCATION, this.x + TITLE_PADDING_LEFT, this.y, clickedWidget.frameIcon * FRAME_WIDTH, 128 + (clickedWidget.spellStatus.isAvailable() ? 0 : 1) * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
            guiGraphics.drawString(this.font, clickedWidget.title, (float) (this.x + TITLE_X), (float) (this.y + TITLE_Y), 0xFFFFFFFF, true);
            
            SpellIconRegistry.render(clickedWidget.spellTexture, guiGraphics, SPELL_WIDTH, SPELL_HEIGHT, this.x + SpellNodeWidget.FRAME_OFF_X, this.y + FRAME_OFF_Y, deltaTick);
        }
    }
    
    public void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, Screen screen)
    {
        if(active && clickedWidget != null && clickedWidget.spellNode != null && mouseX >= this.x && mouseX < this.x + this.w && mouseY >= this.y && mouseY < this.y + FRAME_HEIGHT)
        {
            SpellInstance spellInstance = clickedWidget.spellNode.getSpellInstance();
            
            if(spellInstance != null)
            {
                RenderSystem.enableDepthTest();
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 400D);
                
                List<Component> tooltip = spellInstance.getSpell().get().makeTooltipList(null);
                Optional<TooltipComponent> tooltipComponent = clickedWidget.spellNode.getSpellInstance().getTooltipComponent();
                
                if(SpellsClientConfig.SHOW_IDS.get())
                {
                    Registry<Spell> spellRegistry = Spells.getRegistry(SpellsClientUtil.getClientLevel());
                    tooltip.add(Component.literal(spellInstance.getSpell().unwrap().map(ResourceKey::location, spellRegistry::getKey).toString()).withStyle(ChatFormatting.DARK_GRAY));
                    
                    if(clickedWidget.spellNode.getNodeId() != null)
                    {
                        tooltip.add(Component.literal(clickedWidget.spellNode.getNodeId().getIDText()).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
                
                guiGraphics.renderTooltip(this.font, tooltip, tooltipComponent, mouseX, mouseY);
                
                guiGraphics.pose().popPose();
            }
        }
    }
}

package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.client.SpellIconRegistry;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.cas_ual_ty.spells.client.progression.SpellNodeWidget.*;

public class SelectedSpellWidget extends GuiComponent
{
    protected int x;
    protected int y;
    protected int w;
    
    public boolean active = true;
    
    public final Font font;
    
    public SpellNode spell;
    public SpellStatus spellStatus;
    
    public SpellIcon spellTexture;
    public int frameIcon;
    public int titleIcon;
    public FormattedCharSequence title;
    
    public SelectedSpellWidget(int x, int y, int w)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        
        this.font = Minecraft.getInstance().font;
    }
    
    public void setContents(SpellNode spell, Map<SpellNodeId, SpellStatus> progression, SpellStatus spellStatus, FormattedCharSequence title)
    {
        this.spell = spell;
        this.spellStatus = spellStatus;
        this.title = title;
        
        this.spellTexture = spell.getSpellDirect().getIcon();
        
        if(spellStatus == SpellStatus.FORGOTTEN)
        {
            this.frameIcon = 1;
        }
        else
        {
            this.frameIcon = 0;
        }
        
        // 0 = gold = available
        // 1 = blue = buyable
        // 2 = black = not available & not buyable
        this.titleIcon = (spellStatus == SpellStatus.LEARNED ? 0 : ProgressionHelper.isFullyLinked(spell, progression) ? 1 : 2);
    }
    
    public void setClickedWidget(SpellNodeWidget clickedWidget)
    {
        this.setContents(clickedWidget.spell, clickedWidget.tab.getScreen().getMenu().spellProgression, clickedWidget.spellStatus, clickedWidget.title);
    }
    
    public void drawHover(PoseStack poseStack, float deltaTick)
    {
        if(active)
        {
            int w1 = 60;
            int w2 = this.w - 60;
            
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            RenderSystem.enableBlend();
            
            int x = this.x;
            int y = this.y;
            
            this.blit(poseStack, x, y, 0, titleIcon * FRAME_HEIGHT, w1, FRAME_HEIGHT);
            this.blit(poseStack, x + w1, y, BAR_WIDTH - w2, titleIcon * FRAME_HEIGHT, w2, FRAME_HEIGHT);
            
            this.blit(poseStack, this.x + TITLE_PADDING_LEFT, this.y, frameIcon * FRAME_WIDTH, 128 + (spellStatus.isAvailable() ? 0 : 1) * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
            this.font.drawShadow(poseStack, title, (float) (this.x + TITLE_X), (float) (this.y + TITLE_Y), 0xFFFFFFFF);
            
            SpellIconRegistry.render(spellTexture, poseStack, SPELL_WIDTH, SPELL_HEIGHT, this.x + SpellNodeWidget.FRAME_OFF_X, this.y + FRAME_OFF_Y, deltaTick);
        }
    }
    
    public void drawTooltip(PoseStack poseStack, int mouseX, int mouseY, Screen screen)
    {
        if(active && mouseX >= this.x && mouseX < this.x + this.w && mouseY >= this.y && mouseY < this.y + FRAME_HEIGHT)
        {
            Spell spell = this.spell.getSpellDirect();
            
            if(spell != null)
            {
                RenderSystem.enableDepthTest();
                poseStack.pushPose();
                poseStack.translate(0, 0, 400D);
                
                List<Component> tooltip = spell.getTooltip(null);
                Optional<TooltipComponent> tooltipComponent = spell.getTooltipComponent();
                
                screen.renderTooltip(poseStack, tooltip, tooltipComponent, mouseX, mouseY);
                
                poseStack.popPose();
            }
        }
    }
}

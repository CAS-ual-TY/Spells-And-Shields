package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import de.cas_ual_ty.spells.client.SpellIconRegistry;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class SpellNodeWidget
{
    public static final ResourceLocation TITLE_BOX_SPRITE = new ResourceLocation("advancements/title_box");
    public static final int[] TEXT_OFFSETS = new int[] {0, 10, -10, 25, -25};
    
    public static final int BAR_WIDTH = 200;
    public static final int BAR_HEIGHT = 26;
    
    public static final int TITLE_PADDING_LEFT = 3;
    public static final int TITLE_PADDING_RIGHT = 5;
    public static final int TITLE_X = 32;
    public static final int TITLE_Y = 9;
    
    public static final int SPELL_WIDTH = 18;
    public static final int SPELL_HEIGHT = 18;
    
    public static final int FRAME_WIDTH = 26;
    public static final int FRAME_HEIGHT = 26;
    public static final int FRAME_OFF_X = (FRAME_WIDTH - SPELL_WIDTH) / 2 + TITLE_PADDING_LEFT;
    public static final int FRAME_OFF_Y = (FRAME_HEIGHT - SPELL_HEIGHT) / 2;
    
    public static final int TITLE_MAX_WIDTH = BAR_WIDTH - FRAME_WIDTH - 2 * TITLE_PADDING_LEFT - TITLE_PADDING_RIGHT;
    
    public final SpellTreeTab tab;
    public final SpellNode spellNode;
    public final SpellStatus spellStatus;
    
    public final int width;
    
    public final SpellIcon spellTexture;
    public final FormattedCharSequence title;
    
    public final Font font;
    
    //@Nullable
    public SpellNodeWidget parent;
    
    public final LinkedList<SpellNodeWidget> children = new LinkedList<>();
    
    protected int x;
    protected int y;
    
    public final ResourceLocation frameIcon;
    public final ResourceLocation titleIcon;
    
    public SpellNodeWidget(SpellTreeTab skillTreeTab, SpellNode spell, SpellStatus spellStatus)
    {
        tab = skillTreeTab;
        spellNode = spell;
        this.spellStatus = spellStatus;
        
        font = Minecraft.getInstance().font;
        
        spellTexture = spell.getSpellDirect().getIcon();
        title = Language.getInstance().getVisualOrder(font.substrByWidth(spell.getSpellDirect().getTitle(), TITLE_MAX_WIDTH));
        
        // Position fixup later, after all widgets are done, in SpellTreeTab
        x = 0;
        y = 0;
        
        width = 29 + font.width(title) + TITLE_PADDING_LEFT + TITLE_PADDING_RIGHT;
        
        frameIcon = spellStatus == SpellStatus.LEARNED ? AdvancementWidgetType.OBTAINED.frameSprite(FrameType.values()[spell.getFrame()]) : AdvancementWidgetType.UNOBTAINED.frameSprite(FrameType.values()[spell.getFrame()]);
        
        // 0 = gold = available
        // 1 = blue = buyable
        // 2 = black = not available & not buyable
        titleIcon = (spellStatus == SpellStatus.LEARNED ? AdvancementWidgetType.OBTAINED.boxSprite() : ProgressionHelper.isFullyLinked(spell, tab.getScreen().getMenu().spellProgression) ? AdvancementWidgetType.UNOBTAINED.boxSprite() : TITLE_BOX_SPRITE);
    }
    
    private static float getMaxWidth(StringSplitter stringSplitter, List<FormattedText> list)
    {
        return (float) list.stream().mapToDouble(stringSplitter::stringWidth).max().orElse(0D);
    }
    
    private List<FormattedText> findOptimalLines(Component component, int w)
    {
        StringSplitter splitter = font.getSplitter();
        List<FormattedText> candidate = null;
        
        float smallest = Float.MAX_VALUE;
        
        for(int offset : TEXT_OFFSETS)
        {
            List<FormattedText> split = splitter.splitLines(component, w - offset, Style.EMPTY);
            float length = Math.abs(getMaxWidth(splitter, split) - (float) w);
            
            if(length <= 10F)
            {
                return split;
            }
            
            if(length < smallest)
            {
                smallest = length;
                candidate = split;
            }
        }
        
        return candidate;
    }
    
    public void drawLinkedConnectivity(GuiGraphics guiGraphics, int x, int y, int color, Predicate<SpellNodeWidget> childPredicate)
    {
        //TODO make and use proper constants
        
        final int xOff = 15;
        
        if(!children.isEmpty() && children.stream().anyMatch(childPredicate))
        {
            int xMid = x + this.x + xOff;
            int bot = y + this.y + 23;
            
            if(children.size() >= 2)
            {
                // multiple children, require horizontal and vertical line(s)
                int hY = bot + 10;
                
                guiGraphics.vLine(xMid, bot, hY, color);
                
                for(SpellNodeWidget child : children)
                {
                    if(!childPredicate.test(child))
                    {
                        continue;
                    }
                    
                    int childX = x + child.x + xOff;
                    int childTop = y + child.y + 1;
                    
                    guiGraphics.vLine(childX, childTop, hY, color);
                    guiGraphics.hLine(childX, xMid, hY, color);
                }
            }
            else
            {
                // single child, requires only 1 vertical line
                SpellNodeWidget child = children.getFirst();
                
                if(childPredicate.test(child))
                {
                    int childX = x + child.x + xOff;
                    int childTop = y + child.y + 1;
                    
                    guiGraphics.vLine(childX, childTop, bot, color);
                }
            }
            
            for(SpellNodeWidget spellNodeWidget : children)
            {
                if(childPredicate.test(spellNodeWidget))
                {
                    spellNodeWidget.drawLinkedConnectivity(guiGraphics, x, y, color, childPredicate);
                }
            }
        }
    }
    
    public void drawBackgroundConnectivity(GuiGraphics guiGraphics, int x, int y)
    {
        //TODO make and use proper constants
        
        final int xOff = 15;
        
        if(!children.isEmpty())
        {
            int xMid = x + this.x + xOff;
            int bot = y + this.y + 23;
            
            int color = 0xFF000000;
            
            if(children.size() >= 2)
            {
                int hX1 = x + children.getFirst().x + xOff;
                int hX2 = x + children.getLast().x + xOff;
                int hY = bot + 10;
                
                guiGraphics.vLine(xMid - 1, bot, hY + 1, color);
                guiGraphics.vLine(xMid + 1, bot, hY + 1, color);
                
                guiGraphics.hLine(hX1 - 1, hX2 + 1, hY - 1, color);
                guiGraphics.hLine(hX1 - 1, hX2 + 1, hY + 1, color);
                
                for(SpellNodeWidget child : children)
                {
                    int childX = x + child.x + xOff;
                    int childTop = y + child.y + 1;
                    guiGraphics.vLine(childX - 1, childTop, hY - 1, color);
                    guiGraphics.vLine(childX + 1, childTop, hY - 1, color);
                }
            }
            else
            {
                SpellNodeWidget child = children.getFirst();
                
                int childX = x + child.x + xOff;
                int childTop = y + child.y + 1;
                
                guiGraphics.vLine(childX - 1, childTop, bot, color);
                guiGraphics.vLine(childX + 1, childTop, bot, color);
            }
            
            for(SpellNodeWidget spellNodeWidget : children)
            {
                spellNodeWidget.drawBackgroundConnectivity(guiGraphics, x, y);
            }
        }
    }
    
    public void draw(GuiGraphics guiGraphics, int x, int y, float deltaTick)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        
        // frame icon
        guiGraphics.blitSprite(frameIcon, x + this.x + TITLE_PADDING_LEFT, y + this.y, FRAME_WIDTH, FRAME_HEIGHT);
        //guiGraphics.blit(WIDGETS_LOCATION, x + this.x + TITLE_PADDING_LEFT, y + this.y, frameIcon * FRAME_WIDTH, 128 + (spellStatus.isAvailable() ? 0 : 1) * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
        
        // spell icon
        SpellIconRegistry.render(spellTexture, guiGraphics, SPELL_WIDTH, SPELL_HEIGHT, this.x + x + FRAME_OFF_X, this.y + y + FRAME_OFF_Y, deltaTick);
        
        RenderSystem.disableBlend();
        
        for(SpellNodeWidget spellNodeWidget : children)
        {
            spellNodeWidget.draw(guiGraphics, x, y, deltaTick);
        }
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public void addChild(SpellNodeWidget spellNodeWidget)
    {
        children.add(spellNodeWidget);
    }
    
    public void drawHover(GuiGraphics guiGraphics, int scrollX, int scrollY, int width, int height, float deltaTick)
    {
        boolean drawLeft = width + scrollX + x + this.width + FRAME_WIDTH >= tab.getScreen().width;
        
        int left = this.width / 2;
        int right = this.width - left;
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        
        int renderY = scrollY + y;
        int renderX = scrollX + x + (drawLeft ? 6 - this.width + FRAME_WIDTH : 0);
        
        // wide back frame
        guiGraphics.blitSprite(titleIcon, renderX, renderY, this.width, BAR_HEIGHT);
        //guiGraphics.blit(WIDGETS_LOCATION, renderX, renderY, 0, titleIcon * BAR_HEIGHT, left, BAR_HEIGHT);
        //guiGraphics.blit(WIDGETS_LOCATION, renderX + left, renderY, BAR_WIDTH - right, titleIcon * BAR_HEIGHT, right, BAR_HEIGHT);
        
        // front frame icon
        guiGraphics.blitSprite(frameIcon, scrollX + x + TITLE_PADDING_LEFT, scrollY + y, FRAME_WIDTH, FRAME_HEIGHT);
        //guiGraphics.blit(WIDGETS_LOCATION, scrollX + x + TITLE_PADDING_LEFT, scrollY + y, frameIcon * FRAME_WIDTH, 128 + (spellStatus.isAvailable() ? 0 : 1) * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
        
        if(drawLeft)
        {
            guiGraphics.drawString(font, title, (float) (renderX + 5), (float) (scrollY + y + TITLE_Y), 0xFFFFFFFF, true);
        }
        else
        {
            guiGraphics.drawString(font, title, (float) (scrollX + x + TITLE_X), (float) (scrollY + y + TITLE_Y), 0xFFFFFFFF, true);
        }
        
        SpellIconRegistry.render(spellTexture, guiGraphics, SPELL_WIDTH, SPELL_HEIGHT, scrollX + x + FRAME_OFF_X, scrollY + y + FRAME_OFF_Y, deltaTick);
    }
    
    public boolean isMouseOver(int x, int y, int mouseX, int mouseY)
    {
        int minX = x + this.x;
        int maxX = minX + FRAME_WIDTH;
        int minY = y + this.y;
        int maxY = minY + FRAME_WIDTH;
        
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }
    
    public void attachToParent()
    {
        if(parent == null && spellNode.getParent() != null)
        {
            parent = tab.getWidget(spellNode.getParent());
            
            if(parent != null)
            {
                parent.addChild(this);
            }
        }
    }
    
    public void fixPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
}

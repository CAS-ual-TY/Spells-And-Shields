package de.cas_ual_ty.spells.client.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.base.SpellIcon;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
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

public class SpellNodeWidget extends GuiComponent
{
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
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
    public final SpellNode spell;
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
    
    public final int frameIcon;
    public final int titleIcon;
    
    public SpellNodeWidget(SpellTreeTab skillTreeTab, SpellNode spell, SpellStatus spellStatus)
    {
        this.tab = skillTreeTab;
        this.spell = spell;
        this.spellStatus = spellStatus;
        
        this.font = Minecraft.getInstance().font;
        
        this.spellTexture = spell.getSpell().getIcon();
        this.title = Language.getInstance().getVisualOrder(this.font.substrByWidth(spell.getSpell().getSpellName(), TITLE_MAX_WIDTH));
        
        // Position fixup later, after all widgets are done, in SpellTreeTab
        this.x = 0;
        this.y = 0;
        
        this.width = 29 + this.font.width(this.title) + TITLE_PADDING_LEFT + TITLE_PADDING_RIGHT;
        
        // 0 = squared = available
        // 1 = fancy = lost
        // 2 = round = locked
        SpellStatus status = skillTreeTab.getScreen().getMenu().spellProgression.getOrDefault(spell.getSpell(), SpellStatus.LOCKED);
        
        this.frameIcon = status == SpellStatus.FORGOTTEN ? 1 : 0;
        
        // 0 = gold = available
        // 1 = blue = buyable
        // 2 = black = not available & not buyable
        this.titleIcon = (spellStatus == SpellStatus.LEARNED ? 0 : ProgressionHelper.isFullyLinked(spell, tab.getScreen().getMenu().spellProgression) ? 1 : 2);
    }
    
    private static float getMaxWidth(StringSplitter stringSplitter, List<FormattedText> list)
    {
        return (float) list.stream().mapToDouble(stringSplitter::stringWidth).max().orElse(0.0D);
    }
    
    private List<FormattedText> findOptimalLines(Component component, int w)
    {
        StringSplitter splitter = this.font.getSplitter();
        List<FormattedText> candidate = null;
        
        float smallest = Float.MAX_VALUE;
        
        for(int offset : TEXT_OFFSETS)
        {
            List<FormattedText> split = splitter.splitLines(component, w - offset, Style.EMPTY);
            float length = Math.abs(getMaxWidth(splitter, split) - (float) w);
            
            if(length <= 10.0F)
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
    
    public void drawLinkedConnectivity(PoseStack poseStack, int x, int y, int color, Predicate<SpellNodeWidget> childPredicate)
    {
        //TODO make and use proper constants
        
        final int xOff = 15;
        
        if(!this.children.isEmpty() && children.stream().anyMatch(childPredicate))
        {
            int xMid = x + this.x + xOff;
            int bot = y + this.y + 23;
            
            if(children.size() >= 2)
            {
                // multiple children, require horizontal and vertical line(s)
                int hY = bot + 10;
                
                this.vLine(poseStack, xMid, bot, hY, color);
                
                for(SpellNodeWidget child : children)
                {
                    if(!childPredicate.test(child))
                    {
                        continue;
                    }
                    
                    int childX = x + child.x + xOff;
                    int childTop = y + child.y + 1;
                    
                    this.vLine(poseStack, childX, childTop, hY, color);
                    this.hLine(poseStack, childX, xMid, hY, color);
                }
            }
            else
            {
                // single child, requires only 1 vertical line
                SpellNodeWidget child = this.children.getFirst();
                
                if(childPredicate.test(child))
                {
                    int childX = x + child.x + xOff;
                    int childTop = y + child.y + 1;
                    
                    this.vLine(poseStack, childX, childTop, bot, color);
                }
            }
            
            for(SpellNodeWidget spellNodeWidget : this.children)
            {
                if(childPredicate.test(spellNodeWidget))
                {
                    spellNodeWidget.drawLinkedConnectivity(poseStack, x, y, color, childPredicate);
                }
            }
        }
    }
    
    public void drawBackgroundConnectivity(PoseStack poseStack, int x, int y)
    {
        //TODO make and use proper constants
        
        final int xOff = 15;
        
        if(!this.children.isEmpty())
        {
            int xMid = x + this.x + xOff;
            int bot = y + this.y + 23;
            
            int color = 0xFF000000;
            
            if(children.size() >= 2)
            {
                int hX1 = x + children.getFirst().x + xOff;
                int hX2 = x + children.getLast().x + xOff;
                int hY = bot + 10;
                
                this.vLine(poseStack, xMid - 1, bot, hY + 1, color);
                this.vLine(poseStack, xMid + 1, bot, hY + 1, color);
                
                this.hLine(poseStack, hX1 - 1, hX2 + 1, hY - 1, color);
                this.hLine(poseStack, hX1 - 1, hX2 + 1, hY + 1, color);
                
                for(SpellNodeWidget child : children)
                {
                    int childX = x + child.x + xOff;
                    int childTop = y + child.y + 1;
                    this.vLine(poseStack, childX - 1, childTop, hY - 1, color);
                    this.vLine(poseStack, childX + 1, childTop, hY - 1, color);
                }
            }
            else
            {
                SpellNodeWidget child = this.children.getFirst();
                
                int childX = x + child.x + xOff;
                int childTop = y + child.y + 1;
                
                this.vLine(poseStack, childX - 1, childTop, bot, color);
                this.vLine(poseStack, childX + 1, childTop, bot, color);
            }
            
            for(SpellNodeWidget spellNodeWidget : this.children)
            {
                spellNodeWidget.drawBackgroundConnectivity(poseStack, x, y);
            }
        }
    }
    
    public void draw(PoseStack poseStack, int x, int y)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        
        // frame icon
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        this.blit(poseStack, x + this.x + TITLE_PADDING_LEFT, y + this.y, frameIcon * FRAME_WIDTH, 128 + (spellStatus.isAvailable() ? 0 : 1) * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
        
        // spell icon
        RenderSystem.setShaderTexture(0, spellTexture.getTexture());
        int offX = (SPELL_WIDTH - spellTexture.getWidth()) / 2;
        int offY = (SPELL_HEIGHT - spellTexture.getHeight()) / 2;
        blit(poseStack, x + this.x + FRAME_OFF_X + offX, y + this.y + FRAME_OFF_Y + offY, spellTexture.getWidth(), spellTexture.getHeight(), spellTexture.getU(), spellTexture.getV(), spellTexture.getWidth(), spellTexture.getHeight(), spellTexture.getTextureWidth(), spellTexture.getTextureHeight());
        
        RenderSystem.disableBlend();
        
        for(SpellNodeWidget spellNodeWidget : this.children)
        {
            spellNodeWidget.draw(poseStack, x, y);
        }
    }
    
    public int getWidth()
    {
        return this.width;
    }
    
    public void addChild(SpellNodeWidget spellNodeWidget)
    {
        this.children.add(spellNodeWidget);
    }
    
    public void drawHover(PoseStack poseStack, int scrollX, int scrollY, int width, int height)
    {
        boolean drawLeft = width + scrollX + this.x + this.width + FRAME_WIDTH >= this.tab.getScreen().width;
        
        int left = this.width / 2;
        int right = this.width - left;
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        
        int renderY = scrollY + this.y;
        int renderX = scrollX + this.x + (drawLeft ? 6 - this.width + FRAME_WIDTH : 0);
        
        // wide back frame
        this.blit(poseStack, renderX, renderY, 0, titleIcon * BAR_HEIGHT, left, BAR_HEIGHT);
        this.blit(poseStack, renderX + left, renderY, BAR_WIDTH - right, titleIcon * BAR_HEIGHT, right, BAR_HEIGHT);
        
        // front frame icon
        this.blit(poseStack, scrollX + this.x + TITLE_PADDING_LEFT, scrollY + this.y, frameIcon * FRAME_WIDTH, 128 + (spellStatus.isAvailable() ? 0 : 1) * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
        
        if(drawLeft)
        {
            this.font.drawShadow(poseStack, this.title, (float) (renderX + 5), (float) (scrollY + this.y + TITLE_Y), 0xFFFFFFFF);
        }
        else
        {
            this.font.drawShadow(poseStack, this.title, (float) (scrollX + this.x + TITLE_X), (float) (scrollY + this.y + TITLE_Y), 0xFFFFFFFF);
        }
        
        RenderSystem.enableBlend();
        
        RenderSystem.setShaderTexture(0, spellTexture.getTexture());
        int offX = (SPELL_WIDTH - spellTexture.getWidth()) / 2;
        int offY = (SPELL_HEIGHT - spellTexture.getHeight()) / 2;
        blit(poseStack, scrollX + this.x + FRAME_OFF_X + offX, scrollY + this.y + FRAME_OFF_Y + offY, spellTexture.getWidth(), spellTexture.getHeight(), spellTexture.getU(), spellTexture.getV(), spellTexture.getWidth(), spellTexture.getHeight(), spellTexture.getTextureWidth(), spellTexture.getTextureHeight());
        
        RenderSystem.disableBlend();
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
        if(this.parent == null && this.spell.getParent() != null)
        {
            this.parent = this.tab.getWidget(this.spell.getParent());
            
            if(this.parent != null)
            {
                this.parent.addChild(this);
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
        return this.x;
    }
    
    public int getY()
    {
        return this.y;
    }
}

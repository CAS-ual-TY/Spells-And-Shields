package de.cas_ual_ty.spells.client.progression;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.client.SpellIconRegistry;
import de.cas_ual_ty.spells.client.SpellsClientConfig;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.icon.SpellIcon;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerLevelAccess;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class SpellTreeTab
{
    public static ResourceLocation background = new ResourceLocation("textures/block/obsidian.png");
    
    private final Minecraft minecraft;
    private final SpellProgressionScreen screen;
    private final int index;
    private final SpellNode spellNode;
    private final SpellIcon icon;
    public final SpellTree spellTree;
    public final SpellNodeWidget root;
    public final Map<SpellNode, SpellNodeWidget> widgets = Maps.newLinkedHashMap();
    private double scrollX;
    private double scrollY;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private float fade;
    private int page;
    
    public SpellTreeTab(Minecraft minecraft, SpellProgressionScreen mainScreen, int index, SpellTree spellTree)
    {
        this.minecraft = minecraft;
        screen = mainScreen;
        this.index = index;
        this.spellTree = spellTree;
        spellNode = spellTree.getRoot();
        icon = spellTree.getIcon();
        
        spellTree.forEach(spellNode -> addNode(spellNode, mainScreen.getMenu().spellProgression.getOrDefault(spellNode.getNodeId(), SpellStatus.LOCKED)));
        root = widgets.get(spellTree.getRoot());
        fixPositions();
        
        // centralize, add extra FRAME_WIDTH to give more room to drag around
        minX = SpellProgressionScreen.WINDOW_WIDTH / 2 - SpellNodeWidget.FRAME_WIDTH;
        maxX = SpellProgressionScreen.WINDOW_WIDTH / 2 + SpellNodeWidget.FRAME_WIDTH;
        minY = SpellProgressionScreen.WINDOW_HEIGHT / 2 - SpellNodeWidget.FRAME_WIDTH;
        maxY = SpellProgressionScreen.WINDOW_HEIGHT / 2 + SpellNodeWidget.FRAME_WIDTH;
        
        // find extremes
        int x1 = widgets.values().stream().mapToInt(SpellNodeWidget::getX).min().orElse(0);
        int x2 = widgets.values().stream().mapToInt(SpellNodeWidget::getX).max().orElse(0) + SpellNodeWidget.FRAME_WIDTH;
        int y1 = widgets.values().stream().mapToInt(SpellNodeWidget::getY).min().orElse(0);
        int y2 = widgets.values().stream().mapToInt(SpellNodeWidget::getY).max().orElse(0) + SpellNodeWidget.FRAME_WIDTH;
        
        // get tree width/height
        int w = (x2 - x1);
        int h = (y2 - y1);
        
        // move tree center to window center
        minX -= w / 2;
        maxX -= w / 2;
        minY -= h / 2;
        maxY -= h / 2;
        
        int offX = (SpellProgressionScreen.WINDOW_WIDTH - w) / 2;
        offX = (w > SpellProgressionScreen.WINDOW_WIDTH) ? offX : -offX;
        
        minX += offX;
        maxX -= offX;
        
        int offY = (SpellProgressionScreen.WINDOW_HEIGHT - h) / 2;
        offY = (h > SpellProgressionScreen.WINDOW_HEIGHT) ? offY : -offY;
        minY += offY;
        maxY -= offY;
        
        scrollX = (minX + maxX) / 2F;
        scrollY = h > SpellProgressionScreen.WINDOW_HEIGHT ? 0 : (minY + maxY) / 2F;
        
        // call this to make sure the values are clamped anyways (should not do anything)
        scroll(0, 0);
    }
    
    public SpellTreeTab(Minecraft mc, SpellProgressionScreen screen, int index, int page, SpellTree spellTree)
    {
        this(mc, screen, index, spellTree);
        this.page = page;
    }
    
    public int getPage()
    {
        return page;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public SpellNode getSpellNode()
    {
        return spellNode;
    }
    
    public Component getTitle()
    {
        return spellTree.getTitle();
    }
    
    public List<Component> getTooltip(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        List<Component> list = spellTree.getTooltip(spellProgressionHolder, access);
        
        if(SpellsClientConfig.SHOW_IDS.get())
        {
            list.add(Component.literal(spellTree.getId().toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        
        return list;
    }
    
    public void drawTab(GuiGraphics guiGraphics, int x, int y, boolean selected)
    {
        int i = 0;
        
        if(index > 0)
        {
            i += 28;
        }
        
        if(index == 8 - 1)
        {
            i += 28;
        }
        
        int j = selected ? 32 : 0;
        
        AdvancementTabType.ABOVE.draw(guiGraphics, x, y, selected, index);
        //guiGraphics.blit(SpellProgressionScreen.TABS_LOCATION, x + 32 * index, y - 28, i, j, 28, 32);
    }
    
    public void drawIcon(GuiGraphics guiGraphics, int x, int y, float deltaTick)
    {
        SpellIconRegistry.render(icon, guiGraphics, SpellNodeWidget.SPELL_WIDTH, SpellNodeWidget.SPELL_HEIGHT, x + 32 * index + 5, y - 28 + 9, deltaTick);
    }
    
    public void drawContents(GuiGraphics guiGraphics, float deltaTick)
    {
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(false, false, false, false);
        
        guiGraphics.fill(4680, 2260, -4680, -2260, 0xFF000000);
        
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(518);
        
        guiGraphics.fill(SpellProgressionScreen.WINDOW_WIDTH, SpellProgressionScreen.WINDOW_HEIGHT, 0, 0, 0xFF000000);
        
        RenderSystem.depthFunc(515);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        
        int scrollX = Mth.floor(this.scrollX);
        int scrollY = Mth.floor(this.scrollY);
        
        int scrollX16 = scrollX % 16;
        int scrollY16 = scrollY % 16;
        
        for(int bgX = -1; bgX <= SpellProgressionScreen.BACKGROUND_TILE_COUNT_X + 1; ++bgX)
        {
            for(int bgY = -1; bgY <= SpellProgressionScreen.BACKGROUND_TILE_COUNT_Y + 1; ++bgY)
            {
                guiGraphics.blit(background, scrollX16 + 16 * bgX, scrollY16 + 16 * bgY, 0F, 0F, 16, 16, 16, 16);
            }
        }
        
        if(root != null)
        {
            root.drawBackgroundConnectivity(guiGraphics, scrollX, scrollY);
            root.drawLinkedConnectivity(guiGraphics, scrollX, scrollY, 0xFFFFFFFF, s -> true);
            root.drawLinkedConnectivity(guiGraphics, scrollX, scrollY, 0xFF036A96 + 0x00202020, s -> s.parent.spellStatus.isAvailable());
            root.drawLinkedConnectivity(guiGraphics, scrollX, scrollY, 0xFFB98F2C + 0x00202020, s -> s.parent.spellStatus.isAvailable() && s.spellStatus.isAvailable());
            
            root.draw(guiGraphics, scrollX, scrollY, deltaTick);
        }
        
        // DEBUGGING scrollX scrollY minX maxX minY maxY
        /*vLine(guiGraphics, minX, -10, SpellProgressionScreen.WINDOW_INSIDE_HEIGHT + 10, 0xFFFF0000);
        vLine(guiGraphics, maxX, -10, SpellProgressionScreen.WINDOW_INSIDE_HEIGHT + 10, 0xFFFF0000);
        hLine(guiGraphics, -10, SpellProgressionScreen.WINDOW_INSIDE_WIDTH + 10, minY, 0xFFFF0000);
        hLine(guiGraphics, -10, SpellProgressionScreen.WINDOW_INSIDE_WIDTH + 10, maxY, 0xFFFF0000);
    
        vLine(guiGraphics, (int) scrollX, (int) (scrollY - 10), (int) (scrollY + 10), 0xFF0000FF);
        hLine(guiGraphics, (int) (scrollX - 10), (int) (scrollX + 10), (int) scrollY, 0xFF0000FF);*/
        
        RenderSystem.depthFunc(GlConst.GL_GEQUAL);
        RenderSystem.colorMask(false, false, false, false);
        
        guiGraphics.fill(4680, 2260, -4680, -2260, 0xFF000000);
        
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GlConst.GL_LEQUAL);
    }
    
    public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, int offX, int offY, float deltaTick)
    {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0D, 0D, -200D);
        
        guiGraphics.fill(0, 0, SpellProgressionScreen.WINDOW_WIDTH, SpellProgressionScreen.WINDOW_HEIGHT, Mth.floor(fade * 255F) << 24);
        
        boolean found = false;
        
        int scrollX = Mth.floor(this.scrollX);
        int scrollY = Mth.floor(this.scrollY);
        
        if(mouseX > 0 && mouseX < SpellProgressionScreen.WINDOW_WIDTH && mouseY > 0 && mouseY < SpellProgressionScreen.WINDOW_HEIGHT)
        {
            for(SpellNodeWidget w : widgets.values())
            {
                if(w.isMouseOver(scrollX, scrollY, mouseX, mouseY))
                {
                    found = true;
                    w.drawHover(guiGraphics, scrollX, scrollY, offX, offY, deltaTick);
                    break;
                }
            }
        }
        
        guiGraphics.pose().popPose();
        
        if(found)
        {
            fade = Mth.clamp(fade + 0.02F, 0F, 0.3F);
        }
        else
        {
            fade = Mth.clamp(fade - 0.04F, 0F, 1F);
        }
    }
    
    public boolean isMouseOver(int x, int y, double mouseX, double mouseY)
    {
        int minX = x + SpellProgressionScreen.TAB_FRAME_WIDTH * index;
        int minY = y - SpellProgressionScreen.TAB_FRAME_HEIGHT_S;
        return mouseX > (double) minX && mouseX < (double) (minX + SpellProgressionScreen.TAB_FRAME_WIDTH_S) && mouseY > (double) minY && mouseY < (double) (minY + SpellProgressionScreen.TAB_FRAME_HEIGHT);
    }
    
    public void scroll(double scrollX, double scrollY)
    {
        this.scrollX = Mth.clamp(this.scrollX + scrollX, minX, maxX);
        this.scrollY = Mth.clamp(this.scrollY + scrollY, minY, maxY);
    }
    
    public void addNode(SpellNode spellNode, SpellStatus spellStatus)
    {
        SpellNodeWidget spellNodeWidget = new SpellNodeWidget(this, spellNode, spellStatus);
        addWidget(spellNodeWidget, spellNode);
    }
    
    private void addWidget(SpellNodeWidget spellNodeWidget, SpellNode spellNode)
    {
        widgets.put(spellNode, spellNodeWidget);
        spellNodeWidget.attachToParent();
    }
    
    @Nullable
    public SpellNodeWidget getWidget(SpellNode spellNode)
    {
        return widgets.get(spellNode);
    }
    
    public SpellProgressionScreen getScreen()
    {
        return screen;
    }
    
    private void fixPositions()
    {
        if(root == null)
        {
            return;
        }
        
        final float size = 40F;
        fixLeafPositions(0, 0, size, 1, root);
    }
    
    private int fixLeafPositions(int x, int y, float size, int increment, SpellNodeWidget widget)
    {
        if(widget.children.isEmpty())
        {
            widget.fixPosition(Mth.floor(x * size), Mth.floor(y * size));
            
            x += increment;
        }
        else
        {
            for(SpellNodeWidget child : widget.children)
            {
                x = fixLeafPositions(x, y + increment, size, increment, child);
            }
            
            widget.fixPosition((widget.children.getFirst().getX() + widget.children.getLast().getX()) / 2,
                    Mth.floor(y * size));
        }
        
        return x;
    }
    
    public double getScrollX()
    {
        return scrollX;
    }
    
    public double getScrollY()
    {
        return scrollY;
    }
}

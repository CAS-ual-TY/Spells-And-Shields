package de.cas_ual_ty.spells.client.progression;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.network.RequestEquipSpellMessage;
import de.cas_ual_ty.spells.network.RequestLearnSpellMessage;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class SpellProgressionScreen extends AbstractContainerScreen<SpellProgressionMenu>
{
    public static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    public static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    
    public static final String KEY_LEARN = "spell_progression.learn";
    public static final String KEY_EQUIP = "spell_progression.equip";
    public static final String KEY_UNAVAILABLE = "spell_progression.unavailable";
    public static final String KEY_CHOOSE_SLOT = "spell_progression.choose";
    
    public static final int GUI_WIDTH = 252;
    public static final int GUI_HEIGHT = 140;
    
    private static final int WINDOW_OFF_X = 9;
    private static final int WINDOW_OFF_Y = 18;
    
    public static final int WINDOW_WIDTH = 234;
    public static final int WINDOW_HEIGHT = 113;
    
    public static final int BACKGROUND_TEXTURE_WIDTH = 16;
    public static final int BACKGROUND_TEXTURE_HEIGHT = 16;
    
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;
    
    public static final int TAB_ICON_WIDTH = 16;
    public static final int TAB_ICON_HEIGHT = 16;
    
    public static final int MAX_TABS = 8;
    
    public static final int TAB_FRAME_WIDTH = 32;
    public static final int TAB_FRAME_WIDTH_S = 28;
    public static final int TAB_FRAME_HEIGHT = 32;
    public static final int TAB_FRAME_HEIGHT_S = 28;
    
    public static final Component VERY_SAD_LABEL = Component.translatable("advancements.sad_label");
    public static final Component NO_ADVANCEMENTS_LABEL = Component.translatable("advancements.empty");
    
    private final Map<SpellNode, SpellTreeTab> tabs;
    
    @Nullable
    private SpellTreeTab selectedTab;
    private boolean isScrolling;
    private static int tabPage, maxPages;
    
    public SelectedSpellWidget selectedSpellWidget;
    
    public SpellInteractButton learnButton;
    public SpellInteractButton equipButton;
    public SpellInteractButton unavailableButton;
    public SpellInteractButton chooseButton;
    
    public SpellSlotWidget[] spellSlotButtons;
    
    public SpellProgressionHolder spellProgressionHolder;
    
    public Registry<Spell> spellRegistry;
    
    public SpellProgressionScreen(SpellProgressionMenu menu, Inventory inventory, Component component)
    {
        super(menu, inventory, component);
        tabs = Maps.newLinkedHashMap();
        minecraft = Minecraft.getInstance();
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        tabPage = 0;
        spellProgressionHolder = SpellProgressionHolder.getSpellProgressionHolder(menu.player).orElse(null);
        spellRegistry = Spells.getRegistry(SpellsUtil.getClientLevel());
    }
    
    public void spellTreesUpdated()
    {
        init();
    }
    
    @Override
    protected void init()
    {
        tabs.clear();
        SpellTreeTab previous = selectedTab;
        selectedTab = null;
        clearWidgets();
        
        super.init();
        
        maxPages = 0;
        
        if(menu.spellTrees != null)
        {
            int index = 0;
            for(SpellTree tree : menu.spellTrees)
            {
                tabs.put(tree.getRoot(), new SpellTreeTab(minecraft, this, index % MAX_TABS, index / MAX_TABS, tree));
                index++;
            }
        }
        
        if(!tabs.isEmpty())
        {
            if(previous != null)
            {
                Optional<SpellTreeTab> optionalSpellTreeTab = tabs.values().stream().filter(tree -> previous.spellTree.getId().equals(tree.spellTree.getId())).findFirst();
                
                optionalSpellTreeTab.ifPresent(tab ->
                {
                    selectedTab = tab;
                    
                    // scroll back to 0/0
                    selectedTab.scroll(-selectedTab.getScrollX(), -selectedTab.getScrollY());
                    
                    // scroll to previous position
                    selectedTab.scroll(previous.getScrollX(), previous.getScrollY());
                    
                    if(previous.root != null && selectedTab.root != null)
                    {
                        // keep root node on same position
                        selectedTab.scroll(previous.root.getX() - selectedTab.root.getX(), previous.root.getY() - selectedTab.root.getY());
                    }
                });
            }
        }
        
        if(selectedTab == null && !tabs.isEmpty())
        {
            selectedTab = tabs.values().iterator().next();
        }
        
        if(tabs.size() > MAX_TABS)
        {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> tabPage = Math.max(tabPage - 1, 0)).bounds(getGuiLeft(), getGuiTop() - 50, 20, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> tabPage = Math.min(tabPage + 1, maxPages)).bounds(getGuiLeft() + GUI_WIDTH - 20, getGuiTop() - 50, 20, 20).build());
            maxPages = tabs.size() / MAX_TABS;
        }
        
        int totalW = 280;
        int leftW = 150;
        int rightW = totalW - 180;
        
        selectedSpellWidget = new SelectedSpellWidget(getGuiLeft(), getGuiTop() + GUI_HEIGHT, leftW);
        
        learnButton = new SpellInteractButton(getGuiLeft() + GUI_WIDTH - rightW, getGuiTop() + GUI_HEIGHT, rightW, SpellNodeWidget.FRAME_HEIGHT, Component.translatable(KEY_LEARN), this::buttonClicked, 1)
        {
            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick)
            {
                active = selectedSpellWidget.clickedWidget != null && selectedSpellWidget.clickedWidget.spellNode.canLearn(spellProgressionHolder, menu.access);
                super.render(guiGraphics, mouseX, mouseY, deltaTick);
            }
            
            @Override
            public void renderTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick, Font font)
            {
                super.renderTitle(guiGraphics, mouseX, mouseY, deltaTick, font);
                
                if(selectedSpellWidget.clickedWidget != null)
                {
                    int cost = selectedSpellWidget.clickedWidget.spellNode.getLevelCost();
                    
                    // active ? lime green : dark green
                    int color = active ? 0x80FF20 : 0x407F10;
                    
                    String costStr = String.valueOf(cost);
                    int x = getX() + width - font.width(costStr) - 2;
                    int y = getY() + height - font.lineHeight - 4;
                    guiGraphics.drawString(font, costStr, x, y, color);
                }
            }
        };
        equipButton = new SpellInteractButton(getGuiLeft() + GUI_WIDTH - rightW, getGuiTop() + GUI_HEIGHT, rightW, SpellNodeWidget.FRAME_HEIGHT, Component.translatable(KEY_EQUIP), this::buttonClicked, 0);
        unavailableButton = new SpellInteractButton(getGuiLeft() + GUI_WIDTH - rightW, getGuiTop() + GUI_HEIGHT, rightW, SpellNodeWidget.FRAME_HEIGHT, Component.translatable(KEY_UNAVAILABLE), this::buttonClicked, 2);
        chooseButton = new SpellInteractButton(getGuiLeft() + GUI_WIDTH - rightW, getGuiTop() + GUI_HEIGHT, rightW, SpellNodeWidget.FRAME_HEIGHT, Component.translatable(KEY_CHOOSE_SLOT), this::buttonClicked, 2);
        unavailableButton.active = false;
        chooseButton.active = false;
        
        addWidget(learnButton);
        addWidget(equipButton);
        addWidget(unavailableButton);
        addWidget(chooseButton);
        
        spellSlotButtons = new SpellSlotWidget[SpellHolder.SPELL_SLOTS];
        
        for(int i = 0; i < spellSlotButtons.length; ++i)
        {
            int x = getGuiLeft() - SpellNodeWidget.FRAME_WIDTH;
            int y = getGuiTop() + i * (SpellNodeWidget.FRAME_HEIGHT + 1);
            spellSlotButtons[i] = new SpellSlotWidget(x, y, i, this::slotChosen);
        }
        
        disableSlotButtons();
        
        spellClicked(null);
    }
    
    private void learnButtonTooltip(Button button, GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        if(selectedSpellWidget.clickedWidget != null && button.isMouseOver(mouseX, mouseY))
        {
            guiGraphics.renderTooltip(font, selectedSpellWidget.clickedWidget.spellNode.getTooltip(spellProgressionHolder, menu.access), Optional.empty(), mouseX, mouseY);
        }
    }
    
    private void buttonClicked(Button button)
    {
        if(button == equipButton)
        {
            enableSlotButtons();
            equipButton.visible = false;
            chooseButton.visible = true;
        }
        else if(button == learnButton && selectedTab != null && selectedSpellWidget.clickedWidget != null)
        {
            SpellsAndShields.CHANNEL.send(PacketDistributor.SERVER.noArg(), new RequestLearnSpellMessage(selectedSpellWidget.clickedWidget.spellNode.getNodeId()));
        }
    }
    
    private void slotChosen(int slot)
    {
        if(selectedTab != null && selectedSpellWidget.clickedWidget != null)
        {
            SpellsAndShields.CHANNEL.send(PacketDistributor.SERVER.noArg(), new RequestEquipSpellMessage((byte) slot, selectedSpellWidget.clickedWidget.spellNode.getNodeId()));
            spellClicked(null);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(learnButton.isMouseOver(mouseX, mouseY) || equipButton.isMouseOver(mouseX, mouseY) || unavailableButton.isMouseOver(mouseX, mouseY))
        {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        
        for(SpellSlotWidget b : spellSlotButtons)
        {
            if(b.isMouseOver(mouseX, mouseY))
            {
                return b.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
        
        spellClicked(null);
        
        if(mouseButton == 0)
        {
            for(SpellTreeTab tab : tabs.values())
            {
                if(tab.getPage() == tabPage)
                {
                    if(tab.isMouseOver(getGuiLeft(), getGuiTop(), mouseX, mouseY))
                    {
                        selectedTab = tab;
                        break;
                    }
                    else if(tab == selectedTab)
                    {
                        if(mouseX >= getGuiLeft() + WINDOW_OFF_X && mouseX < getGuiLeft() + WINDOW_OFF_X + WINDOW_WIDTH &&
                                mouseY >= getGuiTop() + WINDOW_OFF_Y && mouseY < getGuiTop() + WINDOW_OFF_Y + WINDOW_HEIGHT)
                        {
                            int x = Mth.floor(getGuiLeft() + tab.getScrollX());
                            int y = Mth.floor(getGuiTop() + tab.getScrollY());
                            int mX = Mth.floor(mouseX - WINDOW_OFF_X);
                            int mY = Mth.floor(mouseY - WINDOW_OFF_Y);
                            
                            for(SpellNodeWidget w : tab.widgets.values())
                            {
                                if(w.isMouseOver(x, y, mX, mY))
                                {
                                    spellClicked(w);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public void spellClicked(SpellNodeWidget w)
    {
        disableSlotButtons();
        
        chooseButton.visible = false;
        
        if(w == null)
        {
            selectedSpellWidget.active = false;
            learnButton.visible = false;
            equipButton.visible = false;
            unavailableButton.visible = false;
        }
        else
        {
            selectedSpellWidget.setClickedWidget(w);
            selectedSpellWidget.active = true;
            
            learnButton.visible = ProgressionHelper.isFullyLinked(w.spellNode, menu.spellProgression) && (w.spellStatus == SpellStatus.LOCKED || w.spellStatus == SpellStatus.FORGOTTEN);
            equipButton.visible = w.spellStatus == SpellStatus.LEARNED;
            unavailableButton.visible = !learnButton.visible && !equipButton.visible;
        }
    }
    
    protected void enableSlotButtons()
    {
        for(SpellSlotWidget w : spellSlotButtons)
        {
            w.active = true;
        }
    }
    
    protected void disableSlotButtons()
    {
        for(SpellSlotWidget w : spellSlotButtons)
        {
            w.active = false;
        }
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float deltaTick, int mouseX, int mouseY)
    {
        renderBackground(guiGraphics);
        if(maxPages != 0)
        {
            Component page = Component.literal(String.format("%d / %d", tabPage + 1, maxPages + 1));
            int width = font.width(page);
            guiGraphics.drawString(font, page.getVisualOrderText(), getGuiLeft() + (GUI_WIDTH / 2F) - (width / 2F), getGuiTop() - 44, -1, true);
        }
        renderBottom(guiGraphics, mouseX, mouseY, deltaTick);
        renderSpellSlots(guiGraphics, mouseX, mouseY, deltaTick);
        renderInside(guiGraphics, mouseX, mouseY, getGuiLeft(), getGuiTop(), deltaTick);
        renderWindow(guiGraphics, deltaTick, getGuiLeft(), getGuiTop());
        renderTooltips(guiGraphics, mouseX, mouseY, getGuiLeft(), getGuiTop(), deltaTick);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY)
    {
        if(mouseButton != 0)
        {
            isScrolling = false;
            return false;
        }
        else
        {
            if(!isScrolling)
            {
                isScrolling = true;
            }
            else if(selectedTab != null)
            {
                selectedTab.scroll(dragX, dragY);
            }
            
            return true;
        }
    }
    
    private void renderInside(GuiGraphics guiGraphics, int mouseX, int mouseY, int offX, int offY, float deltaTick)
    {
        SpellTreeTab tab = selectedTab;
        if(tab == null)
        {
            guiGraphics.fill(offX + WINDOW_OFF_X, offY + WINDOW_OFF_Y, offX + WINDOW_OFF_X + WINDOW_WIDTH, offY + WINDOW_OFF_Y + WINDOW_HEIGHT, 0xFF000000);
            int i = offX + WINDOW_OFF_X + WINDOW_WIDTH / 2;
            guiGraphics.drawCenteredString(font, NO_ADVANCEMENTS_LABEL, i, offY + WINDOW_OFF_Y + WINDOW_HEIGHT / 2 - WINDOW_OFF_X / 2, -1);
            guiGraphics.drawCenteredString(font, VERY_SAD_LABEL, i, offY + WINDOW_OFF_Y + WINDOW_HEIGHT - WINDOW_OFF_X, -1);
        }
        else
        {
            PoseStack posestack = guiGraphics.pose();
            guiGraphics.enableScissor(offX + WINDOW_OFF_X, offY + WINDOW_OFF_Y, offX + WINDOW_OFF_X + WINDOW_WIDTH, offY + WINDOW_OFF_Y + WINDOW_HEIGHT);
            posestack.pushPose();
            posestack.translate(offX + WINDOW_OFF_X, offY + WINDOW_OFF_Y, 0D);
            RenderSystem.applyModelViewMatrix();
            tab.drawContents(guiGraphics, deltaTick);
            posestack.popPose();
            guiGraphics.disableScissor();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(GlConst.GL_LEQUAL);
            RenderSystem.disableDepthTest();
        }
    }
    
    public void renderWindow(GuiGraphics guiGraphics, float deltaTick, int offX, int offY)
    {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        guiGraphics.blit(WINDOW_LOCATION, offX, offY, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        if(tabs.size() > 0)
        {
            RenderSystem.setShaderTexture(0, TABS_LOCATION);
            
            for(SpellTreeTab tab : tabs.values())
            {
                if(tab.getPage() == tabPage)
                {
                    tab.drawTab(guiGraphics, offX, offY, tab == selectedTab);
                }
            }
            
            RenderSystem.defaultBlendFunc();
            
            for(SpellTreeTab tab : tabs.values())
            {
                if(tab.getPage() == tabPage)
                {
                    tab.drawIcon(guiGraphics, offX, offY, deltaTick);
                }
            }
            
            RenderSystem.disableBlend();
        }
    }
    
    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, int offX, int offY, float deltaTick)
    {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        if(selectedTab != null)
        {
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate(offX + WINDOW_OFF_X, offY + WINDOW_OFF_Y, 400D);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            selectedTab.drawTooltips(guiGraphics, mouseX - offX - WINDOW_OFF_X, mouseY - offY - WINDOW_OFF_Y, offX, offY, deltaTick);
            RenderSystem.disableDepthTest();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        
        selectedSpellWidget.drawTooltip(guiGraphics, mouseX, mouseY, this);
        
        for(SpellSlotWidget b : spellSlotButtons)
        {
            if(b.isMouseOver(mouseX, mouseY))
            {
                SpellSlotWidget.spellSlotToolTip(this, guiGraphics, mouseX, mouseY, b.slot);
            }
        }
        
        if(tabs.size() > 0)
        {
            for(SpellTreeTab tab : tabs.values())
            {
                if(tab.getPage() == tabPage && tab.isMouseOver(offX, offY, mouseX, mouseY))
                {
                    guiGraphics.renderTooltip(font, tab.getTooltip(spellProgressionHolder, menu.access), Optional.empty(), mouseX, mouseY);
                }
            }
        }
        
        if(learnButton.isMouseOver(mouseX, mouseY))
        {
            learnButtonTooltip(learnButton, guiGraphics, mouseX, mouseY);
        }
    }
    
    private void renderBottom(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick)
    {
        learnButton.render(guiGraphics, mouseX, mouseY, deltaTick);
        equipButton.render(guiGraphics, mouseX, mouseY, deltaTick);
        unavailableButton.render(guiGraphics, mouseX, mouseY, deltaTick);
        chooseButton.render(guiGraphics, mouseX, mouseY, deltaTick);
        selectedSpellWidget.drawHover(guiGraphics, deltaTick);
    }
    
    private void renderSpellSlots(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTick)
    {
        for(SpellSlotWidget w : spellSlotButtons)
        {
            w.render(guiGraphics, mouseX, mouseY, deltaTick);
        }
    }
}

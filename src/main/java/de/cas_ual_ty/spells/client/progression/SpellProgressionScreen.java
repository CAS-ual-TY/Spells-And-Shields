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
import de.cas_ual_ty.spells.spelltree.SpellNode;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class SpellProgressionScreen extends AbstractContainerScreen<SpellProgressionMenu>
{
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    
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
    
    public static final Component VERY_SAD_LABEL = new TranslatableComponent("advancements.sad_label");
    public static final Component NO_ADVANCEMENTS_LABEL = new TranslatableComponent("advancements.empty");
    
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
    
    public SpellProgressionScreen(SpellProgressionMenu menu, Inventory inventory, Component component)
    {
        super(menu, inventory, component);
        this.tabs = Maps.newLinkedHashMap();
        this.minecraft = Minecraft.getInstance();
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        tabPage = 0;
        this.spellProgressionHolder = SpellProgressionHolder.getSpellProgressionHolder(menu.player).orElse(null);
    }
    
    public void spellTreesUpdated()
    {
        init();
    }
    
    @Override
    protected void init()
    {
        this.tabs.clear();
        SpellTreeTab previous = this.selectedTab;
        this.selectedTab = null;
        this.clearWidgets();
        
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
        
        if(!this.tabs.isEmpty())
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
        
        if(this.tabs.size() > MAX_TABS)
        {
            addRenderableWidget(new Button(getGuiLeft(), getGuiTop() - 50, 20, 20, new TextComponent("<"), b -> tabPage = Math.max(tabPage - 1, 0)));
            addRenderableWidget(new Button(getGuiLeft() + GUI_WIDTH - 20, getGuiTop() - 50, 20, 20, new TextComponent(">"), b -> tabPage = Math.min(tabPage + 1, maxPages)));
            maxPages = this.tabs.size() / MAX_TABS;
        }
        
        int totalW = 280;
        int leftW = 150;
        int rightW = totalW - 180;
        
        this.selectedSpellWidget = new SelectedSpellWidget(getGuiLeft(), getGuiTop() + GUI_HEIGHT, leftW);
        
        this.learnButton = new SpellInteractButton(getGuiLeft() + GUI_WIDTH - rightW, getGuiTop() + GUI_HEIGHT, rightW, SpellNodeWidget.FRAME_HEIGHT, new TranslatableComponent(KEY_LEARN), this::buttonClicked, 1, this::learnButtonTooltip)
        {
            @Override
            public void render(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
            {
                this.active = selectedSpellWidget.spell != null && selectedSpellWidget.spell.canLearn(spellProgressionHolder, menu.access);
                super.render(poseStack, mouseX, mouseY, deltaTick);
            }
            
            @Override
            public void renderTitle(PoseStack poseStack, int mouseX, int mouseY, float deltaTick, Font font)
            {
                super.renderTitle(poseStack, mouseX, mouseY, deltaTick, font);
                
                if(selectedSpellWidget.spell != null)
                {
                    int cost = selectedSpellWidget.spell.getLevelCost();
                    
                    // active ? lime green : dark green
                    int color = this.active ? 0x80FF20 : 0x407F10;
                    
                    String costStr = String.valueOf(cost);
                    int x = this.x + this.width - font.width(costStr) - 2;
                    int y = this.y + this.height - font.lineHeight - 4;
                    font.draw(poseStack, costStr, x, y, color);
                }
            }
        };
        this.equipButton = new SpellInteractButton(getGuiLeft() + GUI_WIDTH - rightW, getGuiTop() + GUI_HEIGHT, rightW, SpellNodeWidget.FRAME_HEIGHT, new TranslatableComponent(KEY_EQUIP), this::buttonClicked, 0);
        this.unavailableButton = new SpellInteractButton(getGuiLeft() + GUI_WIDTH - rightW, getGuiTop() + GUI_HEIGHT, rightW, SpellNodeWidget.FRAME_HEIGHT, new TranslatableComponent(KEY_UNAVAILABLE), this::buttonClicked, 2);
        this.chooseButton = new SpellInteractButton(getGuiLeft() + GUI_WIDTH - rightW, getGuiTop() + GUI_HEIGHT, rightW, SpellNodeWidget.FRAME_HEIGHT, new TranslatableComponent(KEY_CHOOSE_SLOT), this::buttonClicked, 2);
        this.unavailableButton.active = false;
        this.chooseButton.active = false;
        
        addWidget(learnButton);
        addWidget(equipButton);
        addWidget(unavailableButton);
        addWidget(chooseButton);
        
        spellSlotButtons = new SpellSlotWidget[SpellHolder.SPELL_SLOTS];
        
        for(int i = 0; i < spellSlotButtons.length; ++i)
        {
            int x = getGuiLeft() - SpellNodeWidget.FRAME_WIDTH;
            int y = getGuiTop() + i * (SpellNodeWidget.FRAME_HEIGHT + 1);
            int slot = i;
            spellSlotButtons[i] = new SpellSlotWidget(x, y, i, this::slotChosen, (b, pS, mX, mY) -> SpellSlotWidget.spellSlotToolTip(this, pS, mX, mY, slot));
        }
        
        disableSlotButtons();
        
        this.spellClicked(null);
    }
    
    private void learnButtonTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY)
    {
        if(selectedSpellWidget.spell != null && button.isMouseOver(mouseX, mouseY))
        {
            renderTooltip(poseStack, selectedSpellWidget.spell.getTooltip(spellProgressionHolder, menu.access), Optional.empty(), mouseX, mouseY);
        }
    }
    
    private void buttonClicked(Button button)
    {
        if(button == equipButton)
        {
            enableSlotButtons();
            this.equipButton.visible = false;
            this.chooseButton.visible = true;
        }
        else if(button == learnButton && selectedTab != null)
        {
            SpellsAndShields.CHANNEL.send(PacketDistributor.SERVER.noArg(), new RequestLearnSpellMessage(selectedSpellWidget.spell.getId(), selectedSpellWidget.spell.getSpell(), selectedTab.spellTree.getId()));
        }
    }
    
    private void slotChosen(int slot)
    {
        if(selectedTab != null)
        {
            SpellsAndShields.CHANNEL.send(PacketDistributor.SERVER.noArg(), new RequestEquipSpellMessage(selectedSpellWidget.spell.getSpell(), (byte) slot, selectedTab.spellTree.getId()));
            this.spellClicked(null);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(this.learnButton.isMouseOver(mouseX, mouseY) || this.equipButton.isMouseOver(mouseX, mouseY) || this.unavailableButton.isMouseOver(mouseX, mouseY))
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
        
        this.spellClicked(null);
        
        if(mouseButton == 0)
        {
            for(SpellTreeTab tab : this.tabs.values())
            {
                if(tab.getPage() == tabPage)
                {
                    if(tab.isMouseOver(getGuiLeft(), getGuiTop(), mouseX, mouseY))
                    {
                        this.selectedTab = tab;
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
        
        this.chooseButton.visible = false;
        
        if(w == null)
        {
            this.selectedSpellWidget.active = false;
            this.learnButton.visible = false;
            this.equipButton.visible = false;
            this.unavailableButton.visible = false;
        }
        else
        {
            this.selectedSpellWidget.setClickedWidget(w);
            this.selectedSpellWidget.active = true;
            
            this.learnButton.visible = ProgressionHelper.isFullyLinked(w.spell, menu.spellProgression) && (w.spellStatus == SpellStatus.LOCKED || w.spellStatus == SpellStatus.FORGOTTEN);
            this.equipButton.visible = w.spellStatus == SpellStatus.LEARNED;
            this.unavailableButton.visible = !this.learnButton.visible && !this.equipButton.visible;
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
    protected void renderBg(PoseStack poseStack, float deltaTick, int mouseX, int mouseY)
    {
        this.renderBackground(poseStack);
        if(maxPages != 0)
        {
            Component page = new TextComponent(String.format("%d / %d", tabPage + 1, maxPages + 1));
            int width = this.font.width(page);
            this.font.drawShadow(poseStack, page.getVisualOrderText(), getGuiLeft() + (GUI_WIDTH / 2F) - (width / 2F), getGuiTop() - 44, -1);
        }
        this.renderBottom(poseStack, mouseX, mouseY, deltaTick);
        this.renderSpellSlots(poseStack, mouseX, mouseY, deltaTick);
        this.renderInside(poseStack, mouseX, mouseY, getGuiLeft(), getGuiTop());
        this.renderWindow(poseStack, getGuiLeft(), getGuiTop());
        this.renderTooltips(poseStack, mouseX, mouseY, getGuiLeft(), getGuiTop());
    }
    
    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
    {
        this.font.draw(poseStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY)
    {
        if(mouseButton != 0)
        {
            this.isScrolling = false;
            return false;
        }
        else
        {
            if(!this.isScrolling)
            {
                this.isScrolling = true;
            }
            else if(this.selectedTab != null)
            {
                this.selectedTab.scroll(dragX, dragY);
            }
            
            return true;
        }
    }
    
    private void renderInside(PoseStack poseStack, int mouseX, int mouseY, int offX, int offY)
    {
        SpellTreeTab tab = this.selectedTab;
        if(tab == null)
        {
            fill(poseStack, offX + WINDOW_OFF_X, offY + WINDOW_OFF_Y, offX + WINDOW_OFF_X + WINDOW_WIDTH, offY + WINDOW_OFF_Y + WINDOW_HEIGHT, 0xFF000000);
            int i = offX + WINDOW_OFF_X + WINDOW_WIDTH / 2;
            drawCenteredString(poseStack, this.font, NO_ADVANCEMENTS_LABEL, i, offY + WINDOW_OFF_Y + WINDOW_HEIGHT / 2 - WINDOW_OFF_X / 2, -1);
            drawCenteredString(poseStack, this.font, VERY_SAD_LABEL, i, offY + WINDOW_OFF_Y + WINDOW_HEIGHT - WINDOW_OFF_X, -1);
        }
        else
        {
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate(offX + WINDOW_OFF_X, offY + WINDOW_OFF_Y, 0D);
            RenderSystem.applyModelViewMatrix();
            tab.drawContents(poseStack);
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(GlConst.GL_LEQUAL);
            RenderSystem.disableDepthTest();
        }
    }
    
    public void renderWindow(PoseStack poseStack, int offX, int offY)
    {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_LOCATION);
        this.blit(poseStack, offX, offY, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        if(this.tabs.size() > 1)
        {
            RenderSystem.setShaderTexture(0, TABS_LOCATION);
            
            for(SpellTreeTab tab : this.tabs.values())
            {
                if(tab.getPage() == tabPage)
                {
                    tab.drawTab(poseStack, offX, offY, tab == this.selectedTab);
                }
            }
            
            RenderSystem.defaultBlendFunc();
            
            for(SpellTreeTab tab : this.tabs.values())
            {
                if(tab.getPage() == tabPage)
                {
                    tab.drawIcon(poseStack, offX, offY);
                }
            }
            
            RenderSystem.disableBlend();
        }
    }
    
    private void renderTooltips(PoseStack poseStack, int mouseX, int mouseY, int offX, int offY)
    {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        if(this.selectedTab != null)
        {
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate(offX + WINDOW_OFF_X, offY + WINDOW_OFF_Y, 400D);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            this.selectedTab.drawTooltips(poseStack, mouseX - offX - WINDOW_OFF_X, mouseY - offY - WINDOW_OFF_Y, offX, offY);
            RenderSystem.disableDepthTest();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        
        this.selectedSpellWidget.drawTooltip(poseStack, mouseX, mouseY, this);
        
        for(SpellSlotWidget b : this.spellSlotButtons)
        {
            b.renderToolTip(poseStack, mouseX, mouseY);
        }
        
        if(this.tabs.size() > 1)
        {
            for(SpellTreeTab tab : this.tabs.values())
            {
                if(tab.getPage() == tabPage && tab.isMouseOver(offX, offY, mouseX, mouseY))
                {
                    this.renderTooltip(poseStack, tab.getTooltip(spellProgressionHolder, menu.access), Optional.empty(), mouseX, mouseY);
                }
            }
        }
        
        learnButton.renderToolTip(poseStack, mouseX, mouseY);
    }
    
    private void renderBottom(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
    {
        learnButton.render(poseStack, mouseX, mouseY, deltaTick);
        equipButton.render(poseStack, mouseX, mouseY, deltaTick);
        unavailableButton.render(poseStack, mouseX, mouseY, deltaTick);
        chooseButton.render(poseStack, mouseX, mouseY, deltaTick);
        selectedSpellWidget.drawHover(poseStack, deltaTick);
    }
    
    private void renderSpellSlots(PoseStack poseStack, int mouseX, int mouseY, float deltaTick)
    {
        for(SpellSlotWidget w : this.spellSlotButtons)
        {
            w.render(poseStack, mouseX, mouseY, deltaTick);
        }
    }
    
}

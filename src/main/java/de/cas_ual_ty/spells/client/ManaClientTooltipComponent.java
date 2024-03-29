package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.cas_ual_ty.spells.util.ManaTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class ManaClientTooltipComponent extends ManaTooltipComponent implements TooltipComponent, ClientTooltipComponent
{
    protected int rows;
    protected int rowHeight;
    protected int leftOver;
    
    public ManaClientTooltipComponent(float mana)
    {
        super(mana);
        rows = Mth.ceil(mana / 20F);
        rowHeight = Math.max(10 - (rows - 2), 3);
        leftOver = this.mana % 20;
    }
    
    @Override
    public int getHeight()
    {
        return Math.max(rows, 1) * rowHeight;
    }
    
    @Override
    public int getWidth(Font font)
    {
        return 10 * 8;
    }
    
    @Override
    public void renderImage(Font font, int left, int top, GuiGraphics guiGraphics)
    {
        int v = 0;
        int totalUnits = Mth.ceil(20 / 2F);
        int manaCeil = totalUnits * 2;
        
        for(int idx = totalUnits - 1; idx >= 0; --idx)
        {
            int row = idx / 10;
            int column = idx % 10;
            int x = left + column * 8;
            int y = top - row * rowHeight;
            
            int idx2 = idx * 2;
            boolean half = idx2 + 1 == mana;
            
            RenderSystem.setShaderTexture(0, ManaRenderer.GUI_ICONS_LOCATION);
            RenderSystem.enableBlend();
            
            guiGraphics.blit(ManaRenderer.GUI_ICONS_LOCATION, x, y, 9, 9, ManaRenderer.UnitType.CONTAINER.getU(false, false), 0, 9, 9, 256, 256);
            
            if(idx2 < mana)
            {
                guiGraphics.blit(ManaRenderer.GUI_ICONS_LOCATION, x, y, 9, 9, ManaRenderer.UnitType.NORMAL.getU(half, false), 0, 9, 9, 256, 256);
            }
            
            RenderSystem.disableBlend();
        }
    }
}

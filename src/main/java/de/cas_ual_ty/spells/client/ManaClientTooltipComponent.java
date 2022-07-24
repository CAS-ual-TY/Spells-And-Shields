package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.cas_ual_ty.spells.util.ManaTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
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
        this.rows = Mth.ceil(mana / 20F);
        this.rowHeight = Math.max(10 - (rows - 2), 3);
        this.leftOver = this.mana % 20;
    }
    
    @Override
    public int getHeight()
    {
        return rows * rowHeight;
    }
    
    @Override
    public int getWidth(Font font)
    {
        if(rows == 0)
        {
            return 0;
        }
        else if(rows == 1)
        {
            return (leftOver / 2 + leftOver % 2) * 9;
        }
        else
        {
            return 10 * 9;
        }
    }
    
    @Override
    public void renderImage(Font font, int left, int top, PoseStack poseStack, ItemRenderer itemRenderer, int p_194053_)
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
            
            Screen.blit(poseStack, x, y, 9, 9, ManaRenderer.UnitType.CONTAINER.getU(false, false), 0, 9, 9, 256, 256);
            
            if(idx2 < mana)
            {
                Screen.blit(poseStack, x, y, 9, 9, ManaRenderer.UnitType.NORMAL.getU(half, false), 0, 9, 9, 256, 256);
            }
            
            RenderSystem.disableBlend();
        }
    }
}

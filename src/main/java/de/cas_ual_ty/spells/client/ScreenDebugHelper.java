package de.cas_ual_ty.spells.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class ScreenDebugHelper
{
    public static void event(ScreenEvent.Render.Post event)
    {
        if(event.getScreen() instanceof AbstractContainerScreen s && (event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen))
        {
            cross(event.getGuiGraphics(), s.width / 2, s.height / 2, 0xFFFFFFFF, s);
            
            drawBounds(event.getGuiGraphics(), s);
            cross(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), 0xFF00FF00, s);
            
            Font font = Minecraft.getInstance().font;
            event.getGuiGraphics().drawString(font, event.getMouseX() + "/" + event.getMouseY(), event.getMouseX(), event.getMouseY(), 0xFFFFFFFF);
        }
    }
    
    public static void hLine(GuiGraphics guiGraphics, int minX, int maxX, int y, int color)
    {
        if(maxX < minX)
        {
            int i = minX;
            minX = maxX;
            maxX = i;
        }
        
        guiGraphics.fill(minX, y, maxX + 1, y + 1, color);
    }
    
    public static void vLine(GuiGraphics guiGraphics, int x, int minY, int maxY, int color)
    {
        if(maxY < minY)
        {
            int i = minY;
            minY = maxY;
            maxY = i;
        }
        
        guiGraphics.fill(x, minY + 1, x + 1, maxY, color);
    }
    
    public static void hLine(GuiGraphics guiGraphics, int y, int color, Screen screen)
    {
        hLine(guiGraphics, 0, screen.width, y, color);
    }
    
    public static void vLine(GuiGraphics guiGraphics, int x, int color, Screen screen)
    {
        vLine(guiGraphics, x, 0, screen.height, color);
    }
    
    public static void cross(GuiGraphics guiGraphics, int x, int y, int color, Screen screen)
    {
        hLine(guiGraphics, y, color, screen);
        vLine(guiGraphics, x, color, screen);
    }
    
    public static void drawBounds(GuiGraphics guiGraphics, AbstractContainerScreen screen)
    {
        hLine(guiGraphics, screen.getGuiTop(), 0xFF0000FF, screen);
        hLine(guiGraphics, screen.getGuiTop() + screen.getYSize(), 0xFF0000FF, screen);
        vLine(guiGraphics, screen.getGuiLeft(), 0xFFFF0000, screen);
        vLine(guiGraphics, screen.getGuiLeft() + screen.getXSize(), 0xFFFF0000, screen);
    }
}

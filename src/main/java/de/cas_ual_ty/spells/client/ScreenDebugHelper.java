package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenDebugHelper
{
    //@SubscribeEvent
    public static void event(ScreenEvent.Render.Post event)
    {
        if(event.getScreen() instanceof AbstractContainerScreen s && (event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof CreativeModeInventoryScreen))
        {
            cross(event.getPoseStack(), s.width / 2, s.height / 2, 0xFFFFFFFF, s);
            
            drawBounds(event.getPoseStack(), s);
            cross(event.getPoseStack(), event.getMouseX(), event.getMouseY(), 0xFF00FF00, s);
            
            Font font = Minecraft.getInstance().font;
            font.draw(event.getPoseStack(), event.getMouseX() + "/" + event.getMouseY(), event.getMouseX(), event.getMouseY(), 0xFFFFFFFF);
        }
    }
    
    public static void hLine(PoseStack poseStack, int minX, int maxX, int y, int color)
    {
        if(maxX < minX)
        {
            int i = minX;
            minX = maxX;
            maxX = i;
        }
        
        GuiComponent.fill(poseStack, minX, y, maxX + 1, y + 1, color);
    }
    
    public static void vLine(PoseStack poseStack, int x, int minY, int maxY, int color)
    {
        if(maxY < minY)
        {
            int i = minY;
            minY = maxY;
            maxY = i;
        }
        
        GuiComponent.fill(poseStack, x, minY + 1, x + 1, maxY, color);
    }
    
    public static void hLine(PoseStack poseStack, int y, int color, Screen screen)
    {
        hLine(poseStack, 0, screen.width, y, color);
    }
    
    public static void vLine(PoseStack poseStack, int x, int color, Screen screen)
    {
        vLine(poseStack, x, 0, screen.height, color);
    }
    
    public static void cross(PoseStack poseStack, int x, int y, int color, Screen screen)
    {
        hLine(poseStack, y, color, screen);
        vLine(poseStack, x, color, screen);
    }
    
    public static void drawBounds(PoseStack poseStack, AbstractContainerScreen screen)
    {
        hLine(poseStack, screen.getGuiTop(), 0xFF0000FF, screen);
        hLine(poseStack, screen.getGuiTop() + screen.getYSize(), 0xFF0000FF, screen);
        vLine(poseStack, screen.getGuiLeft(), 0xFFFF0000, screen);
        vLine(poseStack, screen.getGuiLeft() + screen.getXSize(), 0xFFFF0000, screen);
    }
}

package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.progression.SpellNodeWidget;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.util.SpellHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

public class RadialMenu extends Screen
{
    public static boolean wasClosed = false;
    
    private int slots;
    private Vec2[] outerPoints;
    private Vec2[] innerPoints;
    private Vec2 center;
    private Vec2[] iconPositionPoints;
    private Vec2[] iconBgPositionPoints;
    private Vec2[] mousePoints;
    
    private float outerDist = 80F;
    private float innerDist = 20F;
    private float textureDist = (outerDist + innerDist) * 0.5F;
    private float minMouseDistSq = innerDist * innerDist * 0.5F;
    
    private int iconWidth = SpellNodeWidget.SPELL_WIDTH;
    private int iconHeight = SpellNodeWidget.SPELL_HEIGHT;
    private int iconBgMargin = 1;
    private int iconBgWidth = iconWidth + iconBgMargin * 2;
    private int iconBgHeight = iconHeight + iconBgMargin * 2;
    
    public RadialMenu()
    {
        super(Component.empty());
    }
    
    @Override
    protected void init()
    {
        super.init();
        
        if(getMinecraft().player == null)
        {
            return;
        }
        
        SpellHolder holder = SpellHolder.getSpellHolder(getMinecraft().player).orElse(null);
        
        if(holder == null)
        {
            return;
        }
        
        slots = holder.getSlots();
        
        outerPoints = new Vec2[slots];
        innerPoints = new Vec2[slots];
        center = new Vec2(width * 0.5F, height * 0.5F);
        iconPositionPoints = new Vec2[slots];
        iconBgPositionPoints = new Vec2[slots];
        mousePoints = new Vec2[slots];
        
        double angle = (2 * Math.PI) / slots;
        
        // outer pentagon points
        Vec2[] points = new Vec2[slots];
        for(int i = 0; i < slots; i++)
        {
            double a = i * angle;
            double x = -Math.sin(a);
            double y = -Math.cos(a);
            points[i] = new Vec2((float) x, (float) y);
            outerPoints[i] = points[i].scale(outerDist).add(center);
            innerPoints[i] = points[i].scale(innerDist).add(center);
        }
        
        // vectors pointing from center to the middle of triangles of pentagon
        // spell icon positions
        Vec2[] halfVecs = new Vec2[slots];
        Vec2 iconOff = new Vec2(-iconWidth * 0.5F, -iconHeight * 0.5F);
        for(int i = 0; i < slots; i++)
        {
            Vec2 vec1 = points[i];
            Vec2 vec2 = points[(i + 1) % slots];
            halfVecs[i] = vec1.add(vec2).normalized();
            //iconPositionPoints[i] = halfVecs[i].scale(textureDist).add(iconOff).add(center);
            iconPositionPoints[i] = vec1.scale(textureDist).add(vec2.scale(textureDist)).scale(0.5F).add(iconOff).add(center);
            iconBgPositionPoints[i] = iconPositionPoints[i].add(-iconBgMargin);
            mousePoints[i] = halfVecs[i].scale(innerDist).add(center);
        }
        
        for(int i = 0; i < slots; i++)
        {
            outerPoints[i] = roundVec(outerPoints[i]);
            innerPoints[i] = roundVec(innerPoints[i]);
            iconPositionPoints[i] = roundVec(iconPositionPoints[i]);
            iconBgPositionPoints[i] = roundVec(iconBgPositionPoints[i]);
            mousePoints[i] = roundVec(mousePoints[i]);
        }
    }
    
    @Override
    public void tick()
    {
        super.tick();
    }
    
    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        
        if(getMinecraft().player == null)
        {
            return;
        }
        
        SpellHolder holder = SpellHolder.getSpellHolder(getMinecraft().player).orElse(null);
        
        if(holder == null)
        {
            return;
        }
        
        int hovered = getHoveredSection(pMouseX, pMouseY);
        
        Matrix4f pose = pPoseStack.last().pose();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        
        for(int i = 0; i < slots; i++)
        {
            Vec2 outer1 = outerPoints[i];
            Vec2 inner1 = innerPoints[i];
            
            int i2 = (i + 1) % slots;
            Vec2 outer2 = outerPoints[i2];
            Vec2 inner2 = innerPoints[i2];
            
            float color = 0.25F;
            float iconC = 0F;
            
            if(i == hovered)
            {
                color += 0.25;
            }
            
            // render dark background
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(pose, inner1.x, inner1.y, 0).color(color, color, color, 0.5F).endVertex();
            bufferbuilder.vertex(pose, outer1.x, outer1.y, 0).color(color, color, color, 0.5F).endVertex();
            bufferbuilder.vertex(pose, outer2.x, outer2.y, 0).color(color, color, color, 0.5F).endVertex();
            bufferbuilder.vertex(pose, inner2.x, inner2.y, 0).color(color, color, color, 0.5F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());
            
            if(holder.getSpell(i) != null)
            {
                // render icon background for filled slots
                Vec2 iconVec = iconBgPositionPoints[i];
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(pose, iconVec.x, iconVec.y, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x, iconVec.y + iconBgHeight, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x + iconBgWidth, iconVec.y + iconBgHeight, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x + iconBgWidth, iconVec.y, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                BufferUploader.drawWithShader(bufferbuilder.end());
            }
            else
            {
                // render icon background for empty slots
                Vec2 iconVec = iconPositionPoints[i];
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(pose, iconVec.x, iconVec.y, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x, iconVec.y + iconHeight, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x + iconWidth, iconVec.y + iconHeight, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x + iconWidth, iconVec.y, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                BufferUploader.drawWithShader(bufferbuilder.end());
            }
        }
        
        RenderSystem.disableBlend();
        
        // render spell icons
        for(int i = 0; i < slots; i++)
        {
            SpellInstance spell = holder.getSpell(i);
            
            if(spell != null)
            {
                Vec2 pos = iconPositionPoints[i];
                SpellIconRegistry.render(spell.getSpell().get().getIcon(), pPoseStack, iconWidth, iconHeight, Math.round(pos.x), Math.round(pos.y), pPartialTick);
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
        if(pButton == 0)
        {
            Player player = Minecraft.getInstance().player;
            SpellHolder holder = SpellHolder.getSpellHolder(player).orElse(null);
            
            if(holder != null)
            {
                int hovered = getHoveredSection((float) pMouseX, (float) pMouseY);
                if(hovered != -1 && hovered < holder.getSlots())
                {
                    SpellInstance spell = holder.getSpell(hovered);
                    if(spell != null)
                    {
                        SpellHelper.fireSpellSlot(player, hovered);
                        onClose();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    
    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers)
    {
        if(pKeyCode == SpellKeyBindings.radialMenu.getKey().getValue())
        {
            onClose();
            return true;
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }
    
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
    
    @Override
    public void onClose()
    {
        wasClosed = true;
        super.onClose();
    }
    
    public int getHoveredSection(float pMouseX, float pMouseY)
    {
        // cheap way of checking which triangle is hovered
        Vec2 relMouseVec = new Vec2(pMouseX, pMouseY).add(center.scale(-1F));
        float minDist = Float.MAX_VALUE;
        int hovered = -1;
        if(relMouseVec.lengthSquared() >= minMouseDistSq)
        {
            Vec2 mouseVec = new Vec2(pMouseX, pMouseY);
            for(int i = 0; i < slots; i++)
            {
                float dist = mouseVec.distanceToSqr(mousePoints[i]);
                if(dist < minDist)
                {
                    hovered = i;
                    minDist = dist;
                }
            }
        }
        return hovered;
    }
    
    private static Vec2 roundVec(Vec2 v)
    {
        return new Vec2(Math.round(v.x), Math.round(v.y));
    }
}

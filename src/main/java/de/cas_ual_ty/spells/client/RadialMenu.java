package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.client.progression.SpellNodeWidget;
import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class RadialMenu extends Screen
{
    private int slots;
    private Vec2[] outerPoints;
    private Vec2 innerPoint;
    private Vec2[][] smallOuterPoints;
    private Vec2[] smallInnerPoints;
    private Vec2[] iconPositionPoints;
    
    private float size = 80F;
    private float innerMargin = 4F;
    
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
        innerPoint = new Vec2(width * 0.5F, height * 0.5F);
        smallOuterPoints = new Vec2[slots][2];
        smallInnerPoints = new Vec2[slots];
        iconPositionPoints = new Vec2[slots];
        
        double angle = (2 * Math.PI) / slots;
        
        // outer pentagon points
        Vec2[] points = new Vec2[slots];
        for(int i = 0; i < slots; i++)
        {
            double a = i * angle;
            double x = -Math.sin(a);
            double y = -Math.cos(a);
            points[i] = new Vec2((float) x, (float) y);
            outerPoints[i] = points[i].scale(size).add(innerPoint);
        }
        
        // vectors pointing from center to the middle of triangles of pentagon
        Vec2[] halfVecs = new Vec2[slots];
        for(int i = 0; i < slots; i++)
        {
            Vec2 vec1 = points[i];
            Vec2 vec2 = points[(i + 1) % slots];
            halfVecs[i] = vec1.add(vec2).normalized();
            smallInnerPoints[i] = halfVecs[i].scale(innerMargin).add(innerPoint);
        }
        
        // spell icon positions
        float halfSize = size * 0.5F;
        Vec2 iconOff = new Vec2(-SpellNodeWidget.SPELL_WIDTH * 0.5F, -SpellNodeWidget.SPELL_HEIGHT * 0.5F);
        for(int i = 0; i < slots; i++)
        {
            iconPositionPoints[i] = halfVecs[i].scale(halfSize).add(iconOff).add(innerPoint);
        }
        
        for(int i = 0; i < slots; i++)
        {
            Vec2 vec1 = points[i];
            Vec2 vec2 = points[(i + 1) % slots];
            
            // inner triangle margin
            smallOuterPoints[i][0] = vec1.scale(size).add(vec1.scale(-1F).add(vec1.scale(-1F).add(vec2).normalized()).normalized().scale(innerMargin)).add(innerPoint);
            smallOuterPoints[i][1] = vec2.scale(size).add(vec2.scale(-1F).add(vec2.scale(-1F).add(vec1).normalized()).normalized().scale(innerMargin)).add(innerPoint);
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
        
        // cheap way of checking which triangle is hovered
        Vec2 relMouseVec = new Vec2(pMouseX, pMouseY).add(innerPoint.scale(-1F));
        float minDist = Float.MAX_VALUE;
        int hovered = -1;
        if(relMouseVec.lengthSquared() >= innerMargin * innerMargin)
        {
            Vec2 mouseVec = new Vec2(pMouseX, pMouseY);
            for(int i = 0; i < slots; i++)
            {
                float dist = mouseVec.distanceToSqr(smallInnerPoints[i]);
                if(dist < minDist)
                {
                    hovered = i;
                    minDist = dist;
                }
            }
        }
        
        Matrix4f pose = pPoseStack.last().pose();
        
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        
        for(int i = 0; i < slots; i++)
        {
            Vec2 vec1 = outerPoints[i];
            Vec2 vec2 = outerPoints[(i + 1) % slots];
            
            float outsideC = 0F;
            float insideC = 0.75F;
            float iconC = 0F;
            
            if(i == hovered)
            {
                outsideC += 0.25;
                insideC += 0.25;
            }
            
            // render dark background
            bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(pose, innerPoint.x, innerPoint.y, 0).color(outsideC, outsideC, outsideC, 0.25F).endVertex();
            bufferbuilder.vertex(pose, vec1.x, vec1.y, 0).color(outsideC, outsideC, outsideC, 0.25F).endVertex();
            bufferbuilder.vertex(pose, vec2.x, vec2.y, 0).color(outsideC, outsideC, outsideC, 0.25F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());
            
            Vec2 smallInnerPoint = smallInnerPoints[i];
            Vec2 smallVec1 = smallOuterPoints[i][0];
            Vec2 smallVec2 = smallOuterPoints[i][1];
            
            // render inner triangle
            bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(pose, smallInnerPoint.x, smallInnerPoint.y, 0).color(insideC, insideC, insideC, 0.5F).endVertex();
            bufferbuilder.vertex(pose, smallVec1.x, smallVec1.y, 0).color(insideC, insideC, insideC, 0.5F).endVertex();
            bufferbuilder.vertex(pose, smallVec2.x, smallVec2.y, 0).color(insideC, insideC, insideC, 0.5F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());
            
            if(holder.getSpell(i) == null)
            {
                // render icon background for empty slots
                Vec2 iconVec = iconPositionPoints[i];
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(pose, iconVec.x, iconVec.y, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x, iconVec.y + SpellNodeWidget.SPELL_HEIGHT, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x + SpellNodeWidget.SPELL_WIDTH, iconVec.y + SpellNodeWidget.SPELL_HEIGHT, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, iconVec.x + SpellNodeWidget.SPELL_WIDTH, iconVec.y, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                BufferUploader.drawWithShader(bufferbuilder.end());
            }
        }
        
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        
        // render spell icons
        for(int i = 0; i < slots; i++)
        {
            SpellInstance spell = holder.getSpell(i);
            
            if(spell != null)
            {
                Vec2 pos = iconPositionPoints[i];
                SpellIconRegistry.render(spell.getSpell().get().getIcon(), pPoseStack, SpellNodeWidget.SPELL_WIDTH, SpellNodeWidget.SPELL_HEIGHT, Math.round(pos.x), Math.round(pos.y), pPartialTick);
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
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
}

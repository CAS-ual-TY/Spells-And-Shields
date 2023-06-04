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
    public RadialMenu()
    {
        super(Component.empty());
    }
    
    @Override
    protected void init()
    {
        super.init();
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
        
        //TODO cache all the math of this. Also, it is a little rushed
        // maybe calculate everything in init()
        
        int slots = holder.getSlots();
        
        int midX = width / 2;
        int midY = height / 2;
        
        
        double angle = (2 * Math.PI) / slots;
        float size = 80F;
        float innerMargin = 4F;
        
        // outer pentagon points
        Vec2[] points = new Vec2[slots];
        for(int i = 0; i < slots; i++)
        {
            double a = i * angle;
            double x = -Math.sin(a);
            double y = -Math.cos(a);
            points[i] = new Vec2((float) x, (float) y);
        }
        
        // vectors pointing from center to the middle of triangles of pentagon
        Vec2[] halfVecs = new Vec2[slots];
        for(int i = 0; i < slots; i++)
        {
            Vec2 vec1 = points[i];
            Vec2 vec2 = points[(i + 1) % slots];
            halfVecs[i] = vec1.add(vec2).normalized();
        }
        
        // cheap way of checking which triangle is hovered
        Vec2 mouseVec = new Vec2(pMouseX - midX, pMouseY - midY);
        float minDist = Float.MAX_VALUE;
        int hovered = -1;
        if(mouseVec.lengthSquared() >= innerMargin)
        {
            for(int i = 0; i < slots; i++)
            {
                float dist = mouseVec.scale(10).distanceToSqr(halfVecs[i].scale(10));
                if(dist < minDist)
                {
                    hovered = i;
                    minDist = dist;
                }
            }
        }
        
        // spell icon positions
        float halfSize = size * 0.5F;
        Vec2[] iconPositions = new Vec2[slots];
        Vec2 iconOff = new Vec2(-SpellNodeWidget.SPELL_WIDTH * 0.5F, -SpellNodeWidget.SPELL_HEIGHT * 0.5F);
        for(int i = 0; i < slots; i++)
        {
            iconPositions[i] = halfVecs[i].scale(halfSize).add(iconOff);
        }
        
        Matrix4f pose = pPoseStack.last().pose();
        
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        
        for(int i = 0; i < slots; i++)
        {
            Vec2 vec1 = points[i].scale(size);
            Vec2 vec2 = points[(i + 1) % slots].scale(size);
            
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
            bufferbuilder.vertex(pose, midX, midY, 0).color(outsideC, outsideC, outsideC, 0.25F).endVertex();
            bufferbuilder.vertex(pose, midX + vec1.x, midY + vec1.y, 0).color(outsideC, outsideC, outsideC, 0.25F).endVertex();
            bufferbuilder.vertex(pose, midX + vec2.x, midY + vec2.y, 0).color(outsideC, outsideC, outsideC, 0.25F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());
            
            // inner triangle margin
            Vec2 vec0 = halfVecs[i].scale(innerMargin);
            vec1 = vec1.add(vec1.scale(-1F).normalized().add(vec1.scale(-1F).add(vec2).normalized()).normalized().scale(innerMargin));
            vec2 = vec2.add(vec2.scale(-1F).normalized().add(vec2.scale(-1F).add(vec1).normalized()).normalized().scale(innerMargin));
            
            // render inner triangle
            bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(pose, midX + vec0.x, midY + vec0.y, 0).color(insideC, insideC, insideC, 0.5F).endVertex();
            bufferbuilder.vertex(pose, midX + vec1.x, midY + vec1.y, 0).color(insideC, insideC, insideC, 0.5F).endVertex();
            bufferbuilder.vertex(pose, midX + vec2.x, midY + vec2.y, 0).color(insideC, insideC, insideC, 0.5F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());
            
            if(holder.getSpell(i) == null)
            {
                // render icon background for empty slots
                Vec2 iconVec = iconPositions[i];
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(pose, midX + iconVec.x, midY + iconVec.y, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, midX + iconVec.x, midY + iconVec.y + SpellNodeWidget.SPELL_HEIGHT, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, midX + iconVec.x + SpellNodeWidget.SPELL_WIDTH, midY + iconVec.y + SpellNodeWidget.SPELL_HEIGHT, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
                bufferbuilder.vertex(pose, midX + iconVec.x + SpellNodeWidget.SPELL_WIDTH, midY + iconVec.y, 0).color(iconC, iconC, iconC, 0.5F).endVertex();
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
                Vec2 pos = iconPositions[i];
                SpellIconRegistry.render(spell.getSpell().get().getIcon(), pPoseStack, SpellNodeWidget.SPELL_WIDTH, SpellNodeWidget.SPELL_HEIGHT, midX + Math.round(pos.x), midY + Math.round(pos.y), pPartialTick);
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

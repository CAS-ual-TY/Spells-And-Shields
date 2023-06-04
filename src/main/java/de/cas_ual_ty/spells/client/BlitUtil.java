package de.cas_ual_ty.spells.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class BlitUtil
{
    public static float blitOffset = 0;
    
    /**
     * Draws the entire texture at the given coordinates.
     *
     * @see #advancedBlit(PoseStack, float, float, float, float, float, float, float, float, int, int)
     */
    public static void fullBlit(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight)
    {
        BlitUtil.advancedBlit(ms, renderX, renderY, renderWidth, renderHeight, 0, 0, 1, 1, 1, 1);
    }
    
    /**
     * Draws the entire texture at the given coordinates, rotated by 90 degrees.
     *
     * @see #fullBlit(PoseStack, float, float, float, float)
     */
    public static void fullBlit90Degrees(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight)
    {
        BlitUtil.advancedBlit90Degrees(ms, renderX, renderY, renderWidth, renderHeight, 0, 0, 1, 1, 1, 1);
    }
    
    /**
     * Draws the entire texture at the given coordinates, rotated by 180 degrees.
     *
     * @see #fullBlit(PoseStack, float, float, float, float)
     */
    public static void fullBlit180Degrees(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight)
    {
        BlitUtil.advancedBlit180Degrees(ms, renderX, renderY, renderWidth, renderHeight, 0, 0, 1, 1, 1, 1);
    }
    
    /**
     * Draws the entire texture at the given coordinates, rotated by 270 degrees.
     *
     * @see #fullBlit(PoseStack, float, float, float, float)
     */
    public static void fullBlit270Degrees(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight)
    {
        BlitUtil.advancedBlit270Degrees(ms, renderX, renderY, renderWidth, renderHeight, 0, 0, 1, 1, 1, 1);
    }
    
    /**
     * <p>Advanced blit method.</p>
     *
     * <p>Param 1-4: Where to and how big to draw on the screen.
     * Param 5-8: What part of the texture file to cut out and draw.
     * Param 9-10: How big the entire texture file is in general (pow2 only).</p>
     *
     * @param ms                     PoseStack
     * @param renderX                Where to draw on the screen
     * @param renderY                Where to draw on the screen
     * @param renderWidth            How big to draw on the screen
     * @param renderHeight           How big to draw on the screen
     * @param textureX               The left coordinate of the rect that is taken from the texture file and drawn
     * @param textureY               The top coordinate of the rect that is taken from the texture file and drawn
     * @param textureWidth           The width of the rect that is taken from the texture file and drawn
     * @param textureHeight          The height of the rect that is taken from the texture file and drawn
     * @param totalTextureFileWidth  The total texture file width
     * @param totalTextureFileHeight The total texture file height
     * @see #fullBlit(PoseStack, float, float, float, float)
     */
    public static void advancedBlit(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight, float textureX, float textureY, float textureWidth, float textureHeight, int totalTextureFileWidth, int totalTextureFileHeight)
    {
        BlitUtil.customInnerBlit(ms.last().pose(), renderX, renderX + renderWidth, renderY, renderY + renderHeight, BlitUtil.blitOffset, textureX / totalTextureFileWidth, (textureX + textureWidth) / totalTextureFileWidth, textureY / totalTextureFileHeight, (textureY + textureHeight) / totalTextureFileHeight);
    }
    
    /**
     * <p>Advanced blit method, rotated by 90 degrees.</p>
     *
     * <p>Param 1-4: Where to and how big to draw on the screen.
     * Param 5-8: What part of the texture file to cut out and draw.
     * Param 9-10: How big the entire texture file is in general (pow2 only).</p>
     *
     * @param ms                     PoseStack
     * @param renderX                Where to draw on the screen
     * @param renderY                Where to draw on the screen
     * @param renderWidth            How big to draw on the screen
     * @param renderHeight           How big to draw on the screen
     * @param textureX               The left coordinate of the rect that is taken from the texture file and drawn
     * @param textureY               The top coordinate of the rect that is taken from the texture file and drawn
     * @param textureWidth           The width of the rect that is taken from the texture file and drawn
     * @param textureHeight          The height of the rect that is taken from the texture file and drawn
     * @param totalTextureFileWidth  The total texture file width
     * @param totalTextureFileHeight The total texture file height
     * @see #fullBlit90Degrees(PoseStack, float, float, float, float)
     */
    public static void advancedBlit90Degrees(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight, float textureX, float textureY, float textureWidth, float textureHeight, int totalTextureFileWidth, int totalTextureFileHeight)
    {
        float x1 = textureX / totalTextureFileWidth;
        float y1 = textureY / totalTextureFileHeight;
        float x2 = (textureX + textureWidth) / totalTextureFileWidth;
        float y2 = (textureY + textureHeight) / totalTextureFileHeight;
        BlitUtil.customInnerBlit(ms.last().pose(), renderX, renderX + renderWidth, renderY, renderY + renderHeight, BlitUtil.blitOffset, x2, y1, x2, y2, x1, y2, x1, y1);
    }
    
    /**
     * <p>Advanced blit method, rotated by 180 degrees.</p>
     *
     * <p>Param 1-4: Where to and how big to draw on the screen.
     * Param 5-8: What part of the texture file to cut out and draw.
     * Param 9-10: How big the entire texture file is in general (pow2 only).</p>
     *
     * @param ms                     PoseStack
     * @param renderX                Where to draw on the screen
     * @param renderY                Where to draw on the screen
     * @param renderWidth            How big to draw on the screen
     * @param renderHeight           How big to draw on the screen
     * @param textureX               The left coordinate of the rect that is taken from the texture file and drawn
     * @param textureY               The top coordinate of the rect that is taken from the texture file and drawn
     * @param textureWidth           The width of the rect that is taken from the texture file and drawn
     * @param textureHeight          The height of the rect that is taken from the texture file and drawn
     * @param totalTextureFileWidth  The total texture file width
     * @param totalTextureFileHeight The total texture file height
     * @see #fullBlit180Degrees(PoseStack, float, float, float, float)
     */
    public static void advancedBlit180Degrees(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight, float textureX, float textureY, float textureWidth, float textureHeight, int totalTextureFileWidth, int totalTextureFileHeight)
    {
        float x1 = textureX / totalTextureFileWidth;
        float y1 = textureY / totalTextureFileHeight;
        float x2 = (textureX + textureWidth) / totalTextureFileWidth;
        float y2 = (textureY + textureHeight) / totalTextureFileHeight;
        BlitUtil.customInnerBlit(ms.last().pose(), renderX, renderX + renderWidth, renderY, renderY + renderHeight, BlitUtil.blitOffset, x2, y2, x1, y2, x1, y1, x2, y1);
    }
    
    /**
     * <p>Advanced blit method, rotated by 270 degrees.</p>
     *
     * <p>Param 1-4: Where to and how big to draw on the screen.
     * Param 5-8: What part of the texture file to cut out and draw.
     * Param 9-10: How big the entire texture file is in general (pow2 only).</p>
     *
     * @param ms                     PoseStack
     * @param renderX                Where to draw on the screen
     * @param renderY                Where to draw on the screen
     * @param renderWidth            How big to draw on the screen
     * @param renderHeight           How big to draw on the screen
     * @param textureX               The left coordinate of the rect that is taken from the texture file and drawn
     * @param textureY               The top coordinate of the rect that is taken from the texture file and drawn
     * @param textureWidth           The width of the rect that is taken from the texture file and drawn
     * @param textureHeight          The height of the rect that is taken from the texture file and drawn
     * @param totalTextureFileWidth  The total texture file width
     * @param totalTextureFileHeight The total texture file height
     * @see #fullBlit270Degrees(PoseStack, float, float, float, float)
     */
    public static void advancedBlit270Degrees(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight, float textureX, float textureY, float textureWidth, float textureHeight, int totalTextureFileWidth, int totalTextureFileHeight)
    {
        float x1 = textureX / totalTextureFileWidth;
        float y1 = textureY / totalTextureFileHeight;
        float x2 = (textureX + textureWidth) / totalTextureFileWidth;
        float y2 = (textureY + textureHeight) / totalTextureFileHeight;
        BlitUtil.customInnerBlit(ms.last().pose(), renderX, renderX + renderWidth, renderY, renderY + renderHeight, BlitUtil.blitOffset, x1, y2, x1, y1, x2, y1, x2, y2);
    }
    
    /**
     * <p>Draws an entire texture with a mask at the given coordinates. The mask's alpha overrides the texture's alpha (thus the texture's alpha and the mask's color is ignored).<p>
     * <p>Convenience method which calls the vanilla texture manager to bind the texture, and {@link #fullBlit(PoseStack, float, float, float, float)} to draw the texture.</p>
     *
     * @param ms
     * @param renderX
     * @param renderY
     * @param renderWidth
     * @param renderHeight
     * @see #fullBlit(PoseStack, float, float, float, float)
     * @see #advancedMaskedBlit(PoseStack, Runnable, Runnable)
     */
    public static void fullMaskedBlit(PoseStack ms, float renderX, float renderY, float renderWidth, float renderHeight, ResourceLocation mask, ResourceLocation texture)
    {
        Minecraft mc = Minecraft.getInstance();
        
        Runnable maskBinderAndDrawer = () ->
        {
            RenderSystem.setShaderTexture(0, mask);
            BlitUtil.fullBlit(ms, renderX, renderY, renderWidth, renderHeight);
        };
        
        Runnable textureBinderAndDrawer = () ->
        {
            RenderSystem.setShaderTexture(0, texture);
            BlitUtil.fullBlit(ms, renderX, renderY, renderWidth, renderHeight);
        };
        
        BlitUtil.advancedMaskedBlit(ms, maskBinderAndDrawer, textureBinderAndDrawer);
    }
    
    /**
     * <p>Draws a texture with a mask at the given coordinates. The mask's alpha overrides the texture's alpha (thus the texture's alpha and the mask's color is ignored).<p>
     * <p>Allows you to use custom texture binder and draw methods (and parameters), instead of the default Minecraft methods.</p>
     *
     * @param ms
     * @param maskBinderAndDrawer    Runnable which binds and draws the mask
     * @param textureBinderAndDrawer Runnable which binds and draws the texture
     * @see #fullMaskedBlit(PoseStack, float, float, float, float, ResourceLocation, ResourceLocation)
     * @see #advancedBlit(PoseStack, float, float, float, float, float, float, float, float, int, int)
     * @see <a target="_blank" href="https://stackoverflow.com/questions/5097145/opengl-mask-with-multiple-textures">https://stackoverflow.com/questions/5097145/opengl-mask-with-multiple-textures</a>
     */
    public static void advancedMaskedBlit(PoseStack ms, Runnable maskBinderAndDrawer, Runnable textureBinderAndDrawer)
    {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        
        // We want a blendfunc that doesn't change the color of any pixels,
        // but rather replaces the framebuffer alpha values with values based
        // on the whiteness of the mask. In other words, if a pixel is white in the mask,
        // then the corresponding framebuffer pixel's alpha will be set to 1.
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ZERO);
        
        // Addendum to previous comment: Making sure that we write ALL pixels with ANY alpha.
        GL11.glEnable(GL11.GL_ALPHA_TEST); //TODO direct call
        GL11.glAlphaFunc(GL11.GL_ALWAYS, 0); //TODO direct call
        
        // Now "draw" the mask (again, this doesn't produce a visible result, it just
        // changes the alpha values in the framebuffer)
        maskBinderAndDrawer.run();
        
        // Finally, we want a blendfunc that makes the foreground visible only in
        // areas with high alpha.
        RenderSystem.blendFunc(SourceFactor.DST_ALPHA, DestFactor.ONE_MINUS_DST_ALPHA);
        textureBinderAndDrawer.run();
        
        RenderSystem.disableBlend();
    }
    
    protected static void customInnerBlit(Matrix4f matrix, float posX1, float posX2, float posY1, float posY2, float posZ, float texX1, float texX2, float texY1, float texY2)
    {
        BlitUtil.customInnerBlit(matrix, posX1, posX2, posY1, posY2, posZ, texX1, texY1, texX2, texY1, texX2, texY2, texX1, texY2);
    }
    
    protected static void customInnerBlit(Matrix4f matrix, float posX1, float posX2, float posY1, float posY2, float posZ, float topLeftX, float topLeftY, float topRightX, float topRightY, float botRightX, float botRightY, float botLeftX, float botLeftY)
    {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, posX1, posY2, posZ).uv(botLeftX, botLeftY).endVertex();
        bufferbuilder.vertex(matrix, posX2, posY2, posZ).uv(botRightX, botRightY).endVertex();
        bufferbuilder.vertex(matrix, posX2, posY1, posZ).uv(topRightX, topRightY).endVertex();
        bufferbuilder.vertex(matrix, posX1, posY1, posZ).uv(topLeftX, topLeftY).endVertex();
        bufferbuilder.end();
        GL11.glEnable(GL11.GL_ALPHA_TEST); //TODO direct call
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}
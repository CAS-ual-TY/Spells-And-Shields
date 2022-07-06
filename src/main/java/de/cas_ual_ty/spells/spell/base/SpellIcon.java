package de.cas_ual_ty.spells.spell.base;

import net.minecraft.resources.ResourceLocation;

public class SpellIcon
{
    protected ResourceLocation texture;
    protected int u;
    protected int v;
    protected int width;
    protected int height;
    protected int textureWidth;
    protected int textureHeight;
    
    public SpellIcon(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }
    
    public ResourceLocation getTexture()
    {
        return texture;
    }
    
    public int getU()
    {
        return u;
    }
    
    public int getV()
    {
        return v;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public int getTextureWidth()
    {
        return textureWidth;
    }
    
    public int getTextureHeight()
    {
        return textureHeight;
    }
}

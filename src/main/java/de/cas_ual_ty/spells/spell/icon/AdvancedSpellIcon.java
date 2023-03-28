package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import net.minecraft.resources.ResourceLocation;

public class AdvancedSpellIcon extends SpellIcon
{
    public static Codec<AdvancedSpellIcon> makeCodec(SpellIconType<AdvancedSpellIcon> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(AdvancedSpellIcon::getTexture),
                Codec.INT.fieldOf("u").forGetter(AdvancedSpellIcon::getU),
                Codec.INT.fieldOf("v").forGetter(AdvancedSpellIcon::getV),
                Codec.INT.fieldOf("width").forGetter(AdvancedSpellIcon::getWidth),
                Codec.INT.fieldOf("height").forGetter(AdvancedSpellIcon::getHeight),
                Codec.INT.fieldOf("textureWidth").forGetter(AdvancedSpellIcon::getTextureWidth),
                Codec.INT.fieldOf("textureHeight").forGetter(AdvancedSpellIcon::getTextureHeight)
        ).apply(instance, (texture, u, v, width, height, textureWidth, textureHeight) -> new AdvancedSpellIcon(type, texture, u, v, width, height, textureWidth, textureHeight)));
    }
    
    public static AdvancedSpellIcon make(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
        return new AdvancedSpellIcon(SpellIconTypes.ADVANCED.get(), texture, u, v, width, height, textureWidth, textureHeight);
    }
    
    protected ResourceLocation texture;
    protected int u;
    protected int v;
    protected int width;
    protected int height;
    protected int textureWidth;
    protected int textureHeight;
    
    public AdvancedSpellIcon(SpellIconType<?> type)
    {
        super(type);
    }
    
    public AdvancedSpellIcon(SpellIconType<?> type, ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
        this(type);
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

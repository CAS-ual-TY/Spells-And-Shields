package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

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
                Codec.INT.fieldOf("textureHeight").forGetter(AdvancedSpellIcon::getTextureHeight),
                Codec.optionalField("offsetX", Codec.INT).xmap(optional -> optional.orElse(0), i -> i == 0 ? Optional.empty() : Optional.of(i)).forGetter(AdvancedSpellIcon::getOffsetX),
                Codec.optionalField("offsetY", Codec.INT).xmap(optional -> optional.orElse(0), i -> i == 0 ? Optional.empty() : Optional.of(i)).forGetter(AdvancedSpellIcon::getOffsetY)
        ).apply(instance, (texture, u, v, width, height, textureWidth, textureHeight, offsetX, offsetY) -> new AdvancedSpellIcon(type, texture, u, v, width, height, textureWidth, textureHeight, offsetX, offsetY)));
    }
    
    public static AdvancedSpellIcon make(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight, int offsetX, int offsetY)
    {
        return new AdvancedSpellIcon(SpellIconTypes.ADVANCED.get(), texture, u, v, width, height, textureWidth, textureHeight, offsetX, offsetY);
    }
    
    public static AdvancedSpellIcon make(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
        return new AdvancedSpellIcon(SpellIconTypes.ADVANCED.get(), texture, u, v, width, height, textureWidth, textureHeight, 0, 0);
    }
    
    protected ResourceLocation texture;
    protected int u;
    protected int v;
    protected int width;
    protected int height;
    protected int textureWidth;
    protected int textureHeight;
    protected int offsetX;
    protected int offsetY;
    
    public AdvancedSpellIcon(SpellIconType<?> type)
    {
        super(type);
    }
    
    public AdvancedSpellIcon(SpellIconType<?> type, ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight, int offsetX, int offsetY)
    {
        this(type);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
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
    
    public int getOffsetX()
    {
        return offsetX;
    }
    
    public int getOffsetY()
    {
        return offsetY;
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        texture = buf.readResourceLocation();
        u = buf.readShort(); //could also use unsigned, of course, but this is large enough regardless
        v = buf.readShort();
        width = buf.readShort();
        height = buf.readShort();
        textureWidth = buf.readShort();
        textureHeight = buf.readShort();
        offsetX = buf.readByte();
        offsetY = buf.readByte();
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(texture);
        buf.writeShort(u);
        buf.writeShort(v);
        buf.writeShort(width);
        buf.writeShort(height);
        buf.writeShort(textureWidth);
        buf.writeShort(textureHeight);
        buf.writeByte(offsetX);
        buf.writeByte(offsetY);
    }
}

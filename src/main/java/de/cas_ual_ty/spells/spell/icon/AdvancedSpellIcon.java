package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
                Codec.INT.optionalFieldOf("offsetX").xmap(optional -> optional.orElse(0), i -> i == 0 ? Optional.empty() : Optional.of(i)).forGetter(AdvancedSpellIcon::getOffsetX),
                Codec.INT.optionalFieldOf("offsetY").xmap(optional -> optional.orElse(0), i -> i == 0 ? Optional.empty() : Optional.of(i)).forGetter(AdvancedSpellIcon::getOffsetY),
                Codec.INT.optionalFieldOf("sizeX").xmap(optional -> optional.orElse(-1), i -> i < 0 ? Optional.empty() : Optional.of(i)).forGetter(icon -> icon.width == icon.sizeX ? -1 : icon.sizeX),
                Codec.INT.optionalFieldOf("sizeY").xmap(optional -> optional.orElse(-1), i -> i < 0 ? Optional.empty() : Optional.of(i)).forGetter(icon -> icon.height == icon.sizeY ? -1 : icon.sizeY)
        ).apply(instance, (texture, u, v, width, height, textureWidth, textureHeight, offsetX, offsetY, sizeX, sizeY) ->
                new AdvancedSpellIcon(type, texture, u, v, width, height, textureWidth, textureHeight, offsetX, offsetY, sizeX < 0 ? width : sizeX, sizeY < 0 ? height : sizeY)));
    }

    /**
     * Full control: renders a {@code sizeX}x{@code sizeY} area, sampling a {@code width}x{@code height} area of
     * the texture (eg. crop a 32x32 region but render it into a 16x16 space by passing {@code sizeX}/{@code sizeY}
     * of 16 alongside a {@code width}/{@code height} of 32).
     */
    public static AdvancedSpellIcon make(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight, int offsetX, int offsetY, int sizeX, int sizeY)
    {
        return new AdvancedSpellIcon(SpellIconTypes.ADVANCED.get(), texture, u, v, width, height, textureWidth, textureHeight, offsetX, offsetY, sizeX, sizeY);
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
    protected int sizeX;
    protected int sizeY;

    public AdvancedSpellIcon(SpellIconType<?> type)
    {
        super(type);
    }

    /**
     * Renders at native size (no scaling) - {@code sizeX}/{@code sizeY} default to {@code width}/{@code height}.
     */
    public AdvancedSpellIcon(SpellIconType<?> type, ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight, int offsetX, int offsetY)
    {
        this(type, texture, u, v, width, height, textureWidth, textureHeight, offsetX, offsetY, width, height);
    }

    public AdvancedSpellIcon(SpellIconType<?> type, ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight, int offsetX, int offsetY, int sizeX, int sizeY)
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
        this.sizeX = sizeX;
        this.sizeY = sizeY;
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

    /**
     * The on-screen render width - defaults to {@link #getWidth()} (native size, no scaling) unless explicitly
     * given a different value.
     */
    public int getSizeX()
    {
        return sizeX;
    }

    /**
     * The on-screen render height - defaults to {@link #getHeight()} (native size, no scaling) unless explicitly
     * given a different value.
     */
    public int getSizeY()
    {
        return sizeY;
    }

    @Override
    public void readFromBuf(RegistryFriendlyByteBuf buf)
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
        sizeX = buf.readShort();
        sizeY = buf.readShort();
    }

    @Override
    public void writeToBuf(RegistryFriendlyByteBuf buf)
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
        buf.writeShort(sizeX);
        buf.writeShort(sizeY);
    }
}

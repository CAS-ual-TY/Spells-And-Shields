package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SizedSpellIcon extends SpellIcon
{
    public static Codec<SizedSpellIcon> makeCodec(SpellIconType<SizedSpellIcon> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(SizedSpellIcon::getTexture),
                Codec.INT.fieldOf("size").forGetter(SizedSpellIcon::getSize)
        ).apply(instance, (texture, size) -> new SizedSpellIcon(type, texture, size)));
    }
    
    public static SizedSpellIcon make(ResourceLocation texture, int size)
    {
        return new SizedSpellIcon(SpellIconTypes.SIZED.get(), texture, size);
    }
    
    protected ResourceLocation texture;
    protected int size;
    
    public SizedSpellIcon(SpellIconType<?> type)
    {
        super(type);
    }
    
    public SizedSpellIcon(SpellIconType<?> type, ResourceLocation texture, int size)
    {
        this(type);
        this.texture = texture;
        this.size = size;
    }
    
    public ResourceLocation getTexture()
    {
        return texture;
    }
    
    public int getSize()
    {
        return size;
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        texture = buf.readResourceLocation();
        size = buf.readShort();
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(texture);
        buf.writeShort(size);
    }
}

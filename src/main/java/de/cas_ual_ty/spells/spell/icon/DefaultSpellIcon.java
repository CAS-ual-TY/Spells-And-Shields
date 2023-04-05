package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellIconTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class DefaultSpellIcon extends SpellIcon
{
    public static Codec<DefaultSpellIcon> makeCodec(SpellIconType<DefaultSpellIcon> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(DefaultSpellIcon::getTexture)
        ).apply(instance, (texture) -> new DefaultSpellIcon(type, texture)));
    }
    
    public static DefaultSpellIcon make(ResourceLocation texture)
    {
        return new DefaultSpellIcon(SpellIconTypes.DEFAULT.get(), texture);
    }
    
    protected ResourceLocation texture;
    
    public DefaultSpellIcon(SpellIconType<?> type)
    {
        super(type);
    }
    
    public DefaultSpellIcon(SpellIconType<?> type, ResourceLocation texture)
    {
        this(type);
        this.texture = texture;
    }
    
    public ResourceLocation getTexture()
    {
        return texture;
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        texture = buf.readResourceLocation();
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(texture);
    }
}

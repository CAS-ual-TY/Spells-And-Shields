package de.cas_ual_ty.spells.spell.icon;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public class SpellIconType<I extends SpellIcon>
{
    private Function<SpellIconType<I>, I> constructor;
    private Codec<I> codec;
    
    public SpellIconType(Function<SpellIconType<I>, I> constructor, Function<SpellIconType<I>, Codec<I>> codec)
    {
        this.constructor = constructor;
        this.codec = codec.apply(this);
    }
    
    public Codec<I> getCodec()
    {
        return codec;
    }
    
    public I makeInstance()
    {
        return constructor.apply(this);
    }
}

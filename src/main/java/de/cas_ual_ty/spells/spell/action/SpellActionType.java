package de.cas_ual_ty.spells.spell.action;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public class SpellActionType<A extends SpellAction>
{
    private Function<SpellActionType<A>, A> constructor;
    private Codec<A> codec;
    
    public SpellActionType(Function<SpellActionType<A>, A> constructor, Function<SpellActionType<A>, Codec<A>> codec)
    {
        this.constructor = constructor;
        this.codec = codec.apply(this);
    }
    
    public Codec<A> getCodec()
    {
        return codec;
    }
    
    public A makeInstance()
    {
        return constructor.apply(this);
    }
}

package de.cas_ual_ty.spells.spell.action;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class SpellActionType<A extends SpellAction> extends ForgeRegistryEntry<SpellActionType<?>>
{
    protected Function<SpellActionType<A>, A> constructor;
    protected Function<SpellActionType<A>, Codec<A>> codec;
    
    // 1.19.2 -> 1.18.2 downgrade
    public SpellActionType()
    {
    
    }
    
    public SpellActionType(Function<SpellActionType<A>, A> constructor, Function<SpellActionType<A>, Codec<A>> codec)
    {
        this.constructor = constructor;
        this.codec = codec;
    }
    
    public Codec<A> getCodec()
    {
        return codec.apply(this);
    }
    
    public A makeInstance()
    {
        return constructor.apply(this);
    }
}

package de.cas_ual_ty.spells.spell.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CtxVarType<T>
{
    private Codec<T> immCodec;
    private Codec<CtxVar<T>> codec;
    
    private Map<CtxVarType<?>, Function<T, ?>> converters;
    
    public CtxVarType(Codec<T> immCodec)
    {
        this.immCodec = immCodec;
        this.converters = new HashMap<>();
        this.codec = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(CtxVar::getName),
                getImmCodec().fieldOf("value").forGetter(CtxVar::getValue)
        ).apply(instance, (name, value) -> new CtxVar<>(this, name, value)));
    }
    
    public <U> CtxVarType<T> addConverter(CtxVarType<U> typeTo, Function<T, U> converter)
    {
        converters.put(typeTo, converter);
        return this;
    }
    
    public Codec<CtxVar<T>> getCodec()
    {
        return codec;
    }
    
    public Codec<T> getImmCodec()
    {
        return immCodec;
    }
    
    public <U> boolean trySet(CtxVarType<U> typeFrom, U value, CtxVar<T> ctxVar)
    {
        if(typeFrom == ctxVar.getType())
        {
            ctxVar.setValue((T) value);
            return true;
        }
        
        Function<U, T> converter = (Function<U, T>) typeFrom.converters.get(this);
        if(converter != null)
        {
            ctxVar.setValue(converter.apply(value));
            return true;
        }
        
        return false;
    }
    
    public <U> Optional<U> tryGetAs(CtxVarType<U> typeTo, CtxVar<T> ctxVar)
    {
        if(typeTo == ctxVar.getType())
        {
            return (Optional<U>) ctxVar.getOptional();
        }
        
        Function<T, U> converter = (Function<T, U>) converters.get(typeTo);
        if(converter != null)
        {
            return Optional.of(converter.apply(ctxVar.getValue()));
        }
        
        return Optional.empty();
    }
    
    public Codec<CtxVarRef<T>> refCodec()
    {
        return CtxVarRef.makeCodec(this);
    }
    
    public CtxVarRef<T> ref(T value)
    {
        return new CtxVarRef.CtxVarImm<>(this, value);
    }
}

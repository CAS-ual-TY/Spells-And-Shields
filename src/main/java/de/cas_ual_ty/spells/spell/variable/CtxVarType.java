package de.cas_ual_ty.spells.spell.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CtxVarType<T> extends ForgeRegistryEntry<CtxVarType<?>>
{
    private Function<T, T> copyFunc;
    private Codec<T> immCodec;
    private Codec<CtxVar<T>> codec;
    
    private Map<CtxVarType<?>, Function<T, ?>> converters;
    
    // 1.19.2 -> 1.18.2 downgrade
    public CtxVarType()
    {
    
    }
    
    public CtxVarType(Function<T, T> copyFunc, Codec<T> immCodec)
    {
        this.copyFunc = copyFunc;
        this.immCodec = immCodec;
        converters = new HashMap<>();
        codec = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf(ParamNames.var("name")).forGetter(CtxVar::getName),
                getImmCodec().fieldOf("value").forGetter(CtxVar::getValue)
        ).apply(instance, (name, value) -> new CtxVar<>(this, name, value)));
    }
    
    public <U> CtxVarType<T> addConverter(CtxVarType<U> typeTo, Function<T, U> converter)
    {
        converters.put(typeTo, converter);
        return this;
    }
    
    public T copy(T t)
    {
        return copyFunc.apply(t);
    }
    
    public Codec<CtxVar<T>> getCodec()
    {
        return codec;
    }
    
    public Codec<T> getImmCodec()
    {
        return immCodec;
    }
    
    @Nullable
    public <U> U convertTo(CtxVarType<U> typeTo, T value)
    {
        if(typeTo == this)
        {
            return (U) value;
        }
        
        Function<T, U> converter = (Function<T, U>) converters.get(typeTo);
        if(converter != null)
        {
            return converter.apply(value);
        }
        
        return null;
    }
    
    public <U> boolean canConvertTo(CtxVarType<U> typeTo)
    {
        return typeTo == this || converters.containsKey(typeTo);
    }
    
    public Codec<DynamicCtxVar<T>> refCodec()
    {
        return DynamicCtxVar.makeCodec(this);
    }
    
    public MapCodec<DynamicCtxVar<T>> optionalRefCodec(String field, T replacement)
    {
        return Codec.optionalField(field, DynamicCtxVar.makeCodec(this)).xmap(o -> o.orElse(immediate(replacement)), Optional::ofNullable);
    }
    
    public MapCodec<DynamicCtxVar<T>> optionalRefCodec(String field)
    {
        return optionalRefCodec(field, null);
    }
    
    public DynamicCtxVar<T> immediate(T value)
    {
        return new ImmediateCtxVar<>(this, value);
    }
    
    public DynamicCtxVar<T> reference(Object name)
    {
        return new ReferencedCtxVar<>(this, name.toString(), (ctx) -> ctx.getCtxVar(this, name.toString()));
    }
    
    @Override
    public String toString()
    {
        return CtxVarTypes.REGISTRY.get().getKey(this).toString();
    }
}

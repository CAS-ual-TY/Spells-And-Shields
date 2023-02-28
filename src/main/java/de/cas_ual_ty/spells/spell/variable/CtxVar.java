package de.cas_ual_ty.spells.spell.variable;

import javax.annotation.Nullable;
import java.util.Optional;

public class CtxVar<T>
{
    protected CtxVarType<T> type;
    protected String name;
    protected T value;
    protected Optional<T> optional;
    
    public CtxVar(CtxVarType<T> type)
    {
        this.type = type;
    }
    
    public CtxVar(CtxVarType<T> type, String name, T value)
    {
        this(type);
        this.name = name;
        setValue(value);
    }
    
    public CtxVar(CtxVarType<T> type, T value)
    {
        this(type, null, value);
    }
    
    public CtxVarType<T> getType()
    {
        return type;
    }
    
    public String getName()
    {
        return name;
    }
    
    public T getValue()
    {
        return type.copy(value);
    }
    
    public Optional<T> getOptional()
    {
        return optional;
    }
    
    public void setValue(T value)
    {
        this.value = value;
        optional = Optional.of(value);
    }
    
    public <U> boolean trySet(CtxVarType<U> type, U value)
    {
        if(type.canConvertTo(this.type))
        {
            setValue(type.convertTo(this.type, value));
            return true;
        }
        
        return false;
    }
    
    public <U> Optional<U> tryGetAs(CtxVarType<U> type)
    {
        return Optional.ofNullable(this.type.convertTo(type, this.value));
    }
    
    @Nullable
    public <U> U tryConvertTo(CtxVarType<U> type)
    {
        return this.type.convertTo(type, this.value);
    }
    
    public CtxVar<T> copy()
    {
        return new CtxVar<>(type, name, value);
    }
}

package de.cas_ual_ty.spells.spell.variable;

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
        return value;
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
        return this.type.trySet(type, value, this);
    }
    
    public <U> Optional<U> tryGetAs(CtxVarType<U> type)
    {
        return this.type.tryGetAs(type, this);
    }
}

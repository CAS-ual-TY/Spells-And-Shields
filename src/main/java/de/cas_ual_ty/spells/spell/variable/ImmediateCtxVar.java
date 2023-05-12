package de.cas_ual_ty.spells.spell.variable;

import de.cas_ual_ty.spells.spell.context.SpellContext;

import javax.annotation.Nullable;
import java.util.Optional;

public class ImmediateCtxVar<T> extends DynamicCtxVar<T>
{
    private T imm;
    private Optional<T> optional;
    
    public ImmediateCtxVar(CtxVarType<T> type, T value)
    {
        super(type);
        imm = value;
        optional = Optional.ofNullable(value);
    }
    
    @Override
    public Optional<T> getValue(@Nullable SpellContext ctx)
    {
        return optional;
    }
    
    public T getImm()
    {
        return imm;
    }
}

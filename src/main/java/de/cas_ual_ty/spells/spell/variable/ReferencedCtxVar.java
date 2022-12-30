package de.cas_ual_ty.spells.spell.variable;

import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.context.SpellContext;

import java.util.Optional;

public class ReferencedCtxVar<T> extends DynamicCtxVar<T>
{
    private String str;
    private Expression<T> expression;
    
    public ReferencedCtxVar(CtxVarType<T> type, String str, Expression<T> expression)
    {
        super(type);
        this.str = str;
        this.expression = expression;
    }
    
    @Override
    public Optional<T> getValue(SpellContext ctx)
    {
        return expression.getValue(ctx);
    }
    
    public String getStr()
    {
        return str;
    }
    
    public String compiledString()
    {
        return VAR_PREFIX + str + VAR_SUFFIX;
    }
    
    public interface Expression<T>
    {
        Optional<T> getValue(SpellContext ctx);
    }
    
    public static <T> ReferencedCtxVar<T> makeExpression(CtxVarType<T> type, String str)
    {
        return Compiler.compile(str, type);
    }
}

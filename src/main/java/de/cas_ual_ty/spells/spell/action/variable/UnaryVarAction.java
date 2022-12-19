package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;

public abstract class UnaryVarAction extends SpellAction
{
    public static <T extends UnaryVarAction> RecordCodecBuilder<T, String> operantCodec()
    {
        return Codec.STRING.fieldOf("operant1").forGetter(UnaryVarAction::getOperant);
    }
    
    public static <T extends UnaryVarAction> RecordCodecBuilder<T, String> resultCodec()
    {
        return Codec.STRING.fieldOf("result").forGetter(UnaryVarAction::getResult);
    }
    
    protected String operant;
    protected String result;
    
    public UnaryVarAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public UnaryVarAction(SpellActionType<?> type, String activation, String operant, String result)
    {
        super(type, activation);
        this.operant = operant;
        this.result = result;
    }
    
    public String getOperant()
    {
        return operant;
    }
    
    public String getResult()
    {
        return result;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        CtxVar<?> operant = ctx.getCtxVar(this.operant);
        
        if(operant == null)
        {
            return;
        }
        
        CtxVar<?> result = ctx.getCtxVar(this.result);
        
        if(result == null)
        {
            return;
        }
        
        tryCalculate(ctx, operant, result);
    }
    
    protected abstract <T, U> void tryCalculate(SpellContext ctx, CtxVar<T> operant, CtxVar<U> result);
}

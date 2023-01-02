package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.util.ParamNames;

import java.util.function.BiConsumer;

public abstract class TernaryVarAction extends SpellAction
{
    public static <T extends TernaryVarAction> RecordCodecBuilder<T, String> operant1Codec()
    {
        return Codec.STRING.fieldOf(ParamNames.var("operant1")).forGetter(TernaryVarAction::getOperant1);
    }
    
    public static <T extends TernaryVarAction> RecordCodecBuilder<T, String> operant2Codec()
    {
        return Codec.STRING.fieldOf(ParamNames.var("operant2")).forGetter(TernaryVarAction::getOperant2);
    }
    
    public static <T extends TernaryVarAction> RecordCodecBuilder<T, String> operant3Codec()
    {
        return Codec.STRING.fieldOf(ParamNames.var("operant3")).forGetter(TernaryVarAction::getOperant2);
    }
    
    public static <T extends TernaryVarAction> RecordCodecBuilder<T, String> resultCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.varResult()).forGetter(TernaryVarAction::getResult);
    }
    
    protected String operant1;
    protected String operant2;
    protected String operant3;
    protected String result;
    
    public TernaryVarAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TernaryVarAction(SpellActionType<?> type, String activation, String operant1, String operant2, String operant3, String result)
    {
        super(type, activation);
        this.operant1 = operant1;
        this.operant2 = operant2;
        this.operant3 = operant3;
        this.result = result;
    }
    
    public String getOperant1()
    {
        return operant1;
    }
    
    public String getOperant2()
    {
        return operant2;
    }
    
    public String getOperant3()
    {
        return operant3;
    }
    
    public String getResult()
    {
        return result;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        CtxVar<?> operant1 = ctx.getCtxVar(this.operant1);
        
        if(operant1 == null)
        {
            return;
        }
        
        CtxVar<?> operant2 = ctx.getCtxVar(this.operant2);
        
        if(operant2 == null)
        {
            return;
        }
        
        CtxVar<?> operant3 = ctx.getCtxVar(this.operant3);
        
        if(operant3 == null)
        {
            return;
        }
        
        tryCalculate(ctx, operant1, operant2, operant3, (type, value) -> ctx.setCtxVar(type, result, value));
    }
    
    protected abstract <T, U, V, W> void tryCalculate(SpellContext ctx, CtxVar<T> operant1, CtxVar<U> operant2, CtxVar<V> operant3, BiConsumer<CtxVarType<W>, W> result);
}

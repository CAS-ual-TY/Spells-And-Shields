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

public abstract class BinaryVarAction extends SpellAction
{
    public static <T extends BinaryVarAction> RecordCodecBuilder<T, String> operant1Codec()
    {
        return Codec.STRING.fieldOf(ParamNames.var("operant_1")).forGetter(BinaryVarAction::getOperant1);
    }
    
    public static <T extends BinaryVarAction> RecordCodecBuilder<T, String> operant2Codec()
    {
        return Codec.STRING.fieldOf(ParamNames.var("operant_2")).forGetter(BinaryVarAction::getOperant2);
    }
    
    public static <T extends BinaryVarAction> RecordCodecBuilder<T, String> resultCodec()
    {
        return Codec.STRING.fieldOf(ParamNames.varResult()).forGetter(BinaryVarAction::getResult);
    }
    
    protected String operant1;
    protected String operant2;
    protected String result;
    
    public BinaryVarAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public BinaryVarAction(SpellActionType<?> type, String activation, String operant1, String operant2, String result)
    {
        super(type, activation);
        this.operant1 = operant1;
        this.operant2 = operant2;
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
        
        tryCalculate(ctx, operant1, operant2, (type, value) -> ctx.setCtxVar(type, result, value));
    }
    
    protected abstract <T, U, V> void tryCalculate(SpellContext ctx, CtxVar<T> operant1, CtxVar<U> operant2, BiConsumer<CtxVarType<V>, V> result);
}

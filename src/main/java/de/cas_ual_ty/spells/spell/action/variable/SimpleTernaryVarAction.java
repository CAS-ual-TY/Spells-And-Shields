package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.compiler.TernaryOperation;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SimpleTernaryVarAction<X, Y, Z, W> extends TernaryVarAction
{
    public static <X, Y, Z, W> Codec<SimpleTernaryVarAction<X, Y, Z, W>> makeCodec(SpellActionType<SimpleTernaryVarAction<X, Y, Z, W>> type, Supplier<TernaryOperation.Entry<X, Y, Z, W>> function)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operant1Codec(),
                operant2Codec(),
                operant3Codec(),
                resultCodec()
        ).apply(instance, (activation, operant1, operant2, operant3, result) -> new SimpleTernaryVarAction<>(type, activation, operant1, operant2, operant3, result, function)));
    }
    
    protected TernaryOperation.Entry<X, Y, Z, W> function;
    
    public SimpleTernaryVarAction(SpellActionType<?> type, Supplier<TernaryOperation.Entry<X, Y, Z, W>> function)
    {
        super(type);
        this.function = function.get();
    }
    
    public SimpleTernaryVarAction(SpellActionType<?> type, String activation, String operant1, String operant2, String operant3, String result, Supplier<TernaryOperation.Entry<X, Y, Z, W>> function)
    {
        super(type, activation, operant1, operant2, operant3, result);
        this.function = function.get();
    }
    
    @Override
    protected <T, U, V, S> void tryCalculate(SpellContext ctx, CtxVar<T> operant1, CtxVar<U> operant2, CtxVar<V> operant3, BiConsumer<CtxVarType<S>, S> result)
    {
        if(function.areTypesIndirectlyApplicable(operant1.getType(), operant2.getType(), operant3.getType()))
        {
            function.applyAndSet(operant1, operant2, operant3, result);
        }
    }
    
    public static <X, Y, Z, W> SpellActionType<SimpleTernaryVarAction<X, Y, Z, W>> makeType(Supplier<CtxVarType<X>> operant1, Supplier<CtxVarType<Y>> operant2, Supplier<CtxVarType<Z>> operant3, Supplier<CtxVarType<W>> result, TernaryOperation.TriFunction<X, Y, Z, W> function)
    {
        return makeType(() -> new TernaryOperation.Entry<>(operant1.get(), operant2.get(), operant3.get(), result.get(), function));
    }
    
    public static <X, Y, Z, W> SpellActionType<SimpleTernaryVarAction<X, Y, Z, W>> makeType(Supplier<TernaryOperation.Entry<X, Y, Z, W>> function)
    {
        return new SpellActionType<>((type) -> new SimpleTernaryVarAction<>(type, function), (type) -> makeCodec(type, function));
    }
    
    public static <X, Y, Z, W> SimpleTernaryVarAction<X, Y, Z, W> makeInstance(SpellActionType<SimpleTernaryVarAction<X, Y, Z, W>> type, String activation, String operant1, String operant2, String operant3, String result)
    {
        //TODO very ugly, figure something out or just leave this up forever
        SimpleTernaryVarAction<X, Y, Z, W> action = type.makeInstance();
        action.activation = activation;
        action.operant1 = operant1;
        action.operant2 = operant2;
        action.operant3 = operant3;
        action.result = result;
        return action;
    }
}

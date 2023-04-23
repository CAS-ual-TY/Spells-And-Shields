package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.compiler.BinaryOperation;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SimpleBinaryVarAction<X, Y, Z> extends BinaryVarAction
{
    public static <X, Y, Z> Codec<SimpleBinaryVarAction<X, Y, Z>> makeCodec(SpellActionType<SimpleBinaryVarAction<X, Y, Z>> type, Supplier<BinaryOperation.Entry<X, Y, Z>> function)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operant1Codec(),
                operant2Codec(),
                resultCodec()
        ).apply(instance, (activation, operant1, operant2, result) -> new SimpleBinaryVarAction<>(type, activation, operant1, operant2, result, function)));
    }
    
    protected BinaryOperation.Entry<X, Y, Z> function;
    
    public SimpleBinaryVarAction(SpellActionType<?> type, Supplier<BinaryOperation.Entry<X, Y, Z>> function)
    {
        super(type);
        this.function = function.get();
    }
    
    public SimpleBinaryVarAction(SpellActionType<?> type, String activation, String operant1, String operant2, String result, Supplier<BinaryOperation.Entry<X, Y, Z>> function)
    {
        super(type, activation, operant1, operant2, result);
        this.function = function.get();
    }
    
    @Override
    protected <T, U, V> void tryCalculate(SpellContext ctx, CtxVar<T> operant1, CtxVar<U> operant2, BiConsumer<CtxVarType<V>, V> result)
    {
        if(function.areTypesIndirectlyApplicable(operant1.getType(), operant2.getType()))
        {
            function.applyAndSet(operant1, operant2, result);
        }
    }
    
    public static <X, Y, Z> SpellActionType<SimpleBinaryVarAction<X, Y, Z>> makeType(Supplier<CtxVarType<X>> operant1, Supplier<CtxVarType<Y>> operant2, Supplier<CtxVarType<Z>> result, BiFunction<X, Y, Z> function)
    {
        return makeType(() -> new BinaryOperation.Entry<>(operant1.get(), operant2.get(), result.get(), function));
    }
    
    public static <X, Y, Z> SpellActionType<SimpleBinaryVarAction<X, Y, Z>> makeType(Supplier<BinaryOperation.Entry<X, Y, Z>> function)
    {
        return new SpellActionType<>((type) -> new SimpleBinaryVarAction<>(type, function), (type) -> makeCodec(type, function));
    }
    
    public static <X, Y, Z> SimpleBinaryVarAction<X, Y, Z> makeInstance(SpellActionType<SimpleBinaryVarAction<X, Y, Z>> type, Object activation, String operant1, String operant2, String result)
    {
        //TODO very ugly, figure something out or just leave this up forever
        SimpleBinaryVarAction<X, Y, Z> action = type.makeInstance();
        action.activation = activation.toString();
        action.operant1 = operant1;
        action.operant2 = operant2;
        action.result = result;
        return action;
    }
}

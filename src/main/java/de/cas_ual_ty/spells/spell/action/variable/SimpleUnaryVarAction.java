package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.compiler.UnaryOperation;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleUnaryVarAction<X, Y> extends UnaryVarAction
{
    public static <X, Y> Codec<SimpleUnaryVarAction<X, Y>> makeCodec(SpellActionType<SimpleUnaryVarAction<X, Y>> type, Supplier<UnaryOperation.Entry<X, Y>> function)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operantCodec(),
                resultCodec()
        ).apply(instance, (activation, operant, result) -> new SimpleUnaryVarAction<>(type, activation, operant, result, function)));
    }
    
    protected UnaryOperation.Entry<X, Y> function;
    
    public SimpleUnaryVarAction(SpellActionType<?> type, Supplier<UnaryOperation.Entry<X, Y>> function)
    {
        super(type);
        this.function = function.get();
    }
    
    public SimpleUnaryVarAction(SpellActionType<?> type, String activation, String operant, String result, Supplier<UnaryOperation.Entry<X, Y>> function)
    {
        super(type, activation, operant, result);
        this.function = function.get();
    }
    
    @Override
    protected <T, U> void tryCalculate(SpellContext ctx, CtxVar<T> operant, BiConsumer<CtxVarType<U>, U> result)
    {
        if(function.areTypesIndirectlyApplicable(operant.getType()))
        {
            function.applyAndSet(operant, result);
        }
    }
    
    public static <X, Y> SpellActionType<SimpleUnaryVarAction<X, Y>> makeType(Supplier<CtxVarType<X>> operant, Supplier<CtxVarType<Y>> result, Function<X, Y> function)
    {
        return makeType(() -> new UnaryOperation.Entry<>(operant.get(), result.get(), function));
    }
    
    public static <X, Y> SpellActionType<SimpleUnaryVarAction<X, Y>> makeType(Supplier<UnaryOperation.Entry<X, Y>> function)
    {
        return new SpellActionType<>((type) -> new SimpleUnaryVarAction<>(type, function), (type) -> makeCodec(type, function));
    }
    
    public static <X, Y> SimpleUnaryVarAction<X, Y> makeInstance(SpellActionType<SimpleUnaryVarAction<X, Y>> type, String activation, String operant, String result)
    {
        //TODO very ugly, figure something out or just leave this up forever
        SimpleUnaryVarAction<X, Y> action = type.makeInstance();
        action.activation = activation;
        action.operant = operant;
        action.result = result;
        return action;
    }
}

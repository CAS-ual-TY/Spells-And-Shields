package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleUnaryVarAction<X, Y> extends UnaryVarAction
{
    public static <X, Y> Codec<SimpleUnaryVarAction<X, Y>> makeCodec(SpellActionType<SimpleUnaryVarAction<X, Y>> type, Supplier<MappedUnaryVarAction.UnaryOperatorMapEntry<X, Y>> function)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operantCodec(),
                resultCodec()
        ).apply(instance, (activation, operant, result) -> new SimpleUnaryVarAction<>(type, activation, operant, result, function)));
    }
    
    protected MappedUnaryVarAction.UnaryOperatorMapEntry<X, Y> function;
    
    public SimpleUnaryVarAction(SpellActionType<?> type, Supplier<MappedUnaryVarAction.UnaryOperatorMapEntry<X, Y>> function)
    {
        super(type);
        this.function = function.get();
    }
    
    public SimpleUnaryVarAction(SpellActionType<?> type, String activation, String operant, String result, Supplier<MappedUnaryVarAction.UnaryOperatorMapEntry<X, Y>> function)
    {
        super(type, activation, operant, result);
        this.function = function.get();
    }
    
    @Override
    protected <T, U> void tryCalculate(SpellContext ctx, CtxVar<T> operant, CtxVar<U> result)
    {
        if(function.result().canConvertTo(result.getType()) && function.areTypesIndirectlyApplicable(operant.getType(), function.result()))
        {
            function.applyAndSet(operant, res -> result.trySet(function.result(), res));
        }
    }
    
    public static <X, Y> SpellActionType<SimpleUnaryVarAction<X, Y>> makeType(Supplier<CtxVarType<X>> operant, Supplier<CtxVarType<Y>> result, Function<X, Y> function)
    {
        return makeType(() -> new MappedUnaryVarAction.UnaryOperatorMapEntry<>(operant.get(), result.get(), function));
    }
    
    public static <X, Y> SpellActionType<SimpleUnaryVarAction<X, Y>> makeType(Supplier<MappedUnaryVarAction.UnaryOperatorMapEntry<X, Y>> function)
    {
        return new SpellActionType<>((type) -> new SimpleUnaryVarAction<>(type, function), (type) -> makeCodec(type, function));
    }
}
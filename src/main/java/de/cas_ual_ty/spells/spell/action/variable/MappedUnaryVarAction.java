package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.compiler.UnaryOperation;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.function.BiConsumer;

public class MappedUnaryVarAction extends UnaryVarAction
{
    public static Codec<MappedUnaryVarAction> makeCodec(SpellActionType<MappedUnaryVarAction> type, UnaryOperation map)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operantCodec(),
                resultCodec()
        ).apply(instance, (activation, operant, result) -> new MappedUnaryVarAction(type, activation, operant, result, map)));
    }
    
    protected UnaryOperation map;
    
    public MappedUnaryVarAction(SpellActionType<?> type, UnaryOperation map)
    {
        super(type);
        this.map = map;
    }
    
    public MappedUnaryVarAction(SpellActionType<?> type, String activation, String operant, String result, UnaryOperation map)
    {
        super(type, activation, operant, result);
        this.map = map;
    }
    
    @Override
    protected <T, U> void tryCalculate(SpellContext ctx, CtxVar<T> operant, BiConsumer<CtxVarType<U>, U> result)
    {
        map.applyAndSet(operant, result);
    }
    
    public static SpellActionType<MappedUnaryVarAction> makeType(UnaryOperation map)
    {
        return new SpellActionType<>((type) -> new MappedUnaryVarAction(type, map), (type) -> makeCodec(type, map));
    }
}

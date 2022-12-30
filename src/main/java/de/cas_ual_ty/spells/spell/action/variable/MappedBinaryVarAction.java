package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.BinaryOperation;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.function.BiConsumer;

public class MappedBinaryVarAction extends BinaryVarAction
{
    public static Codec<MappedBinaryVarAction> makeCodec(SpellActionType<MappedBinaryVarAction> type, BinaryOperation map)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operant1Codec(),
                operant2Codec(),
                resultCodec()
        ).apply(instance, (activation, operant1, operant2, result) -> new MappedBinaryVarAction(type, activation, operant1, operant2, result, map)));
    }
    
    protected BinaryOperation map;
    
    public MappedBinaryVarAction(SpellActionType<?> type, BinaryOperation map)
    {
        super(type);
        this.map = map;
    }
    
    public MappedBinaryVarAction(SpellActionType<?> type, String activation, String operant1, String operant2, String result, BinaryOperation map)
    {
        super(type, activation, operant1, operant2, result);
        this.map = map;
    }
    
    @Override
    protected <T, U, V> void tryCalculate(SpellContext ctx, CtxVar<T> operant1, CtxVar<U> operant2, BiConsumer<CtxVarType<V>, V> result)
    {
        map.applyAndSet(operant1, operant2, result);
    }
    
    public static SpellActionType<MappedBinaryVarAction> makeType(BinaryOperation map)
    {
        return new SpellActionType<>((type) -> new MappedBinaryVarAction(type, map), (type) -> makeCodec(type, map));
    }
}

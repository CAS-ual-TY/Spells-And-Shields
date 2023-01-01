package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.compiler.TernaryOperation;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.function.BiConsumer;

public class MappedTernaryVarAction extends TernaryVarAction
{
    public static Codec<MappedTernaryVarAction> makeCodec(SpellActionType<MappedTernaryVarAction> type, TernaryOperation map)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operant1Codec(),
                operant2Codec(),
                operant3Codec(),
                resultCodec()
        ).apply(instance, (activation, operant1, operant2, operant3, result) -> new MappedTernaryVarAction(type, activation, operant1, operant2, operant3, result, map)));
    }
    
    protected TernaryOperation map;
    
    public MappedTernaryVarAction(SpellActionType<?> type, TernaryOperation map)
    {
        super(type);
        this.map = map;
    }
    
    public MappedTernaryVarAction(SpellActionType<?> type, String activation, String operant1, String operant2, String operant3, String result, TernaryOperation map)
    {
        super(type, activation, operant1, operant2, operant3, result);
        this.map = map;
    }
    
    @Override
    protected <T, U, V, W> void tryCalculate(SpellContext ctx, CtxVar<T> operant1, CtxVar<U> operant2, CtxVar<V> operant3, BiConsumer<CtxVarType<W>, W> result)
    {
        map.applyAndSet(operant1, operant2, operant3, result);
    }
    
    public static SpellActionType<MappedTernaryVarAction> makeType(TernaryOperation map)
    {
        return new SpellActionType<>((type) -> new MappedTernaryVarAction(type, map), (type) -> makeCodec(type, map));
    }
}

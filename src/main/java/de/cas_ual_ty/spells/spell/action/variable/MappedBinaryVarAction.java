package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class MappedBinaryVarAction extends BinaryVarAction
{
    public static Codec<MappedBinaryVarAction> makeCodec(SpellActionType<MappedBinaryVarAction> type, BinaryOperatorMap map)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operant1Codec(),
                operant2Codec(),
                resultCodec()
        ).apply(instance, (activation, operant1, operant2, result) -> new MappedBinaryVarAction(type, activation, operant1, operant2, result, map)));
    }
    
    protected BinaryOperatorMap map;
    
    public MappedBinaryVarAction(SpellActionType<?> type, BinaryOperatorMap map)
    {
        super(type);
        this.map = map;
    }
    
    public MappedBinaryVarAction(SpellActionType<?> type, String activation, String operant1, String operant2, String result, BinaryOperatorMap map)
    {
        super(type, activation, operant1, operant2, result);
        this.map = map;
    }
    
    @Override
    protected <T, U, V> void tryCalculate(SpellContext ctx, CtxVar<T> operant1, CtxVar<U> operant2, BiConsumer<CtxVarType<V>, V> result)
    {
        map.applyAndSet(operant1, operant2, result);
    }
    
    public static class BinaryOperatorMap
    {
        private List<BinaryOperatorMapEntry<?, ?, ?>> map;
        
        public BinaryOperatorMap()
        {
            map = new LinkedList<>();
        }
        
        public <T, U, V> BinaryOperatorMap register(CtxVarType<T> operant1, CtxVarType<U> operant2, CtxVarType<V> result, BiFunction<T, U, V> function)
        {
            BinaryOperatorMapEntry<T, U, V> entry = new BinaryOperatorMapEntry<>(operant1, operant2, result, function);
            map.add(entry);
            return this;
        }
        
        public BinaryOperatorMapEntry<?, ?, ?> getEntry(CtxVarType<?> operant1, CtxVarType<?> operant2)
        {
            BinaryOperatorMapEntry<?, ?, ?> entry = map.stream().filter(e -> e.areTypesDirectlyApplicable(operant1, operant2)).findFirst()
                    .orElse(map.stream().filter(e -> e.areTypesDirectlyApplicable(operant2, operant1)).findFirst().orElse(null));
            
            if(entry != null)
            {
                return entry;
            }
            else
            {
                return map.stream().filter(e -> e.areTypesIndirectlyApplicable(operant1, operant2)).findFirst()
                        .orElse(map.stream().filter(e -> e.areTypesIndirectlyApplicable(operant2, operant1)).findFirst().orElse(null));
            }
        }
        
        public <V> boolean applyAndSet(CtxVar<?> operant1, CtxVar<?> operant2, BiConsumer<CtxVarType<V>, V> result)
        {
            BinaryOperatorMapEntry<?, ?, ?> entry = getEntry(operant1.getType(), operant2.getType());
            
            if(entry != null)
            {
                return entry.applyAndSet(operant1, operant2, result);
            }
            else
            {
                return false;
            }
        }
    }
    
    public static record BinaryOperatorMapEntry<T, U, V>(CtxVarType<T> operant1, CtxVarType<U> operant2,
                                                         CtxVarType<V> result, BiFunction<T, U, V> function)
    {
        public boolean areTypesIndirectlyApplicable(CtxVarType<?> operant1, CtxVarType<?> operant2)
        {
            return operant1.canConvertTo(this.operant1) && operant2.canConvertTo(operant2);
        }
        
        public boolean areTypesDirectlyApplicable(CtxVarType<?> operant1, CtxVarType<?> operant2)
        {
            return operant1 == this.operant1 && operant2 == this.operant2;
        }
        
        public <X> boolean applyAndSet(CtxVar<?> operant1, CtxVar<?> operant2, BiConsumer<CtxVarType<X>, X> result)
        {
            AtomicBoolean success = new AtomicBoolean(false);
            
            operant1.tryGetAs(this.operant1).ifPresent(op1 ->
            {
                operant2.tryGetAs(this.operant2).ifPresent(op2 ->
                {
                    result.accept((CtxVarType<X>) this.result(), (X) this.function.apply(op1, op2));
                    success.set(true);
                });
            });
            
            return success.get();
        }
    }
    
    public static SpellActionType<MappedBinaryVarAction> makeType(BinaryOperatorMap map)
    {
        return new SpellActionType<>((type) -> new MappedBinaryVarAction(type, map), (type) -> makeCodec(type, map));
    }
}

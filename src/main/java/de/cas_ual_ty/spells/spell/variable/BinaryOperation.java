package de.cas_ual_ty.spells.spell.variable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class BinaryOperation
{
    private List<Entry<?, ?, ?>> map;
    
    public BinaryOperation()
    {
        map = new LinkedList<>();
    }
    
    public <T, U, V> BinaryOperation register(CtxVarType<T> operant1, CtxVarType<U> operant2, CtxVarType<V> result, BiFunction<T, U, V> function)
    {
        Entry<T, U, V> entry = new Entry<>(operant1, operant2, result, function);
        map.add(entry);
        return this;
    }
    
    public Entry<?, ?, ?> getEntry(CtxVarType<?> operant1, CtxVarType<?> operant2)
    {
        Entry<?, ?, ?> entry = map.stream().filter(e -> e.areTypesDirectlyApplicable(operant1, operant2)).findFirst()
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
        Entry<?, ?, ?> entry = getEntry(operant1.getType(), operant2.getType());
        
        if(entry != null)
        {
            return entry.applyAndSet(operant1, operant2, result);
        }
        else
        {
            return false;
        }
    }
    
    public static record Entry<T, U, V>(CtxVarType<T> operant1, CtxVarType<U> operant2, CtxVarType<V> result,
                                        BiFunction<T, U, V> function)
    {
        public boolean areTypesIndirectlyApplicable(CtxVarType<?> operant1, CtxVarType<?> operant2)
        {
            return operant1.canConvertTo(this.operant1) && operant2.canConvertTo(this.operant2);
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
}

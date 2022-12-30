package de.cas_ual_ty.spells.spell.compiler;

import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class TernaryOperation
{
    // compiler built-in
    public static final TernaryOperation VEC3 = new TernaryOperation();
    public static final TernaryOperation BLOCK_POS = new TernaryOperation();
    
    public static void registerToCompiler()
    {
        Compiler.registerTernaryFunction("vec3", VEC3);
        Compiler.registerTernaryFunction("block_pos", BLOCK_POS);
    }
    
    private List<Entry<?, ?, ?, ?>> map;
    
    public TernaryOperation()
    {
        map = new LinkedList<>();
    }
    
    public <T, U, V, W> TernaryOperation register(CtxVarType<T> operant1, CtxVarType<U> operant2, CtxVarType<V> operant3, CtxVarType<W> result, TriFunction<T, U, V, W> function)
    {
        Entry<T, U, V, W> entry = new Entry<>(operant1, operant2, operant3, result, function);
        map.add(entry);
        return this;
    }
    
    public Entry<?, ?, ?, ?> getEntry(CtxVarType<?> operant1, CtxVarType<?> operant2, CtxVarType<?> operant3)
    {
        Entry<?, ?, ?, ?> entry = map.stream().filter(e -> e.areTypesDirectlyApplicable(operant1, operant2, operant3)).findFirst()
                .orElse(map.stream().filter(e -> e.areTypesDirectlyApplicable(operant2, operant1, operant3)).findFirst().orElse(null));
        
        if(entry != null)
        {
            return entry;
        }
        else
        {
            return map.stream().filter(e -> e.areTypesIndirectlyApplicable(operant1, operant2, operant3)).findFirst()
                    .orElse(map.stream().filter(e -> e.areTypesIndirectlyApplicable(operant2, operant1, operant3)).findFirst().orElse(null));
        }
    }
    
    public <V> boolean applyAndSet(CtxVar<?> operant1, CtxVar<?> operant2, CtxVar<?> operant3, BiConsumer<CtxVarType<V>, V> result)
    {
        Entry<?, ?, ?, ?> entry = getEntry(operant1.getType(), operant2.getType(), operant3.getType());
        
        if(entry != null)
        {
            return entry.applyAndSet(operant1, operant2, operant3, result);
        }
        else
        {
            return false;
        }
    }
    
    public static record Entry<T, U, V, W>(CtxVarType<T> operant1, CtxVarType<U> operant2, CtxVarType<V> operant3,
                                           CtxVarType<W> result,
                                           TriFunction<T, U, V, W> function)
    {
        public boolean areTypesIndirectlyApplicable(CtxVarType<?> operant1, CtxVarType<?> operant2, CtxVarType<?> operant3)
        {
            return operant1.canConvertTo(this.operant1) && operant2.canConvertTo(this.operant2);
        }
        
        public boolean areTypesDirectlyApplicable(CtxVarType<?> operant1, CtxVarType<?> operant2, CtxVarType<?> operant3)
        {
            return operant1 == this.operant1 && operant2 == this.operant2;
        }
        
        public <X> boolean applyAndSet(CtxVar<?> operant1, CtxVar<?> operant2, CtxVar<?> operant3, BiConsumer<CtxVarType<X>, X> result)
        {
            AtomicBoolean success = new AtomicBoolean(false);
            
            operant1.tryGetAs(this.operant1).ifPresent(op1 ->
            {
                operant2.tryGetAs(this.operant2).ifPresent(op2 ->
                {
                    operant3.tryGetAs(this.operant3).ifPresent(op3 ->
                    {
                        result.accept((CtxVarType<X>) this.result(), (X) this.function.apply(op1, op2, op3));
                        success.set(true);
                    });
                });
            });
            
            return success.get();
        }
    }
    
    public interface TriFunction<T, U, V, W>
    {
        W apply(T t, U u, V v);
    }
}

package de.cas_ual_ty.spells.spell.compiler;

import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class UnaryOperation
{
    // compiler built-in
    public static final UnaryOperation NEGATE = new UnaryOperation();
    
    public static final UnaryOperation SQRT = new UnaryOperation();
    public static final UnaryOperation GET_X = new UnaryOperation();
    public static final UnaryOperation GET_Y = new UnaryOperation();
    public static final UnaryOperation GET_Z = new UnaryOperation();
    
    public static void registerToCompiler()
    {
        Compiler.registerUnaryFunction("sqrt", SQRT);
        Compiler.registerUnaryFunction("get_x", GET_X);
        Compiler.registerUnaryFunction("get_y", GET_Y);
        Compiler.registerUnaryFunction("get_z", GET_Z);
    }
    
    private List<Entry<?, ?>> map;
    
    public UnaryOperation()
    {
        map = new LinkedList<>();
    }
    
    public <T, U> UnaryOperation register(CtxVarType<T> operant, CtxVarType<U> result, Function<T, U> function)
    {
        Entry<T, U> entry = new Entry<>(operant, result, function);
        map.add(entry);
        return this;
    }
    
    public Entry<?, ?> getEntry(CtxVarType<?> operant)
    {
        Entry<?, ?> entry = map.stream().filter(e -> e.areTypesDirectlyApplicable(operant)).findFirst().orElse(null);
        
        if(entry != null)
        {
            return entry;
        }
        else
        {
            return map.stream().filter(e -> e.areTypesIndirectlyApplicable(operant)).findFirst().orElse(null);
        }
    }
    
    public <T, U> boolean applyAndSet(CtxVar<?> operant, BiConsumer<CtxVarType<U>, U> result)
    {
        Entry<?, ?> entry = getEntry(operant.getType());
        
        if(entry != null)
        {
            return entry.applyAndSet(operant, result);
        }
        else
        {
            return false;
        }
    }
    
    public static record Entry<T, U>(CtxVarType<T> operant, CtxVarType<U> result, Function<T, U> function)
    {
        public boolean areTypesIndirectlyApplicable(CtxVarType<?> operant)
        {
            return operant.canConvertTo(this.operant);
        }
        
        public boolean areTypesDirectlyApplicable(CtxVarType<?> operant)
        {
            return operant == this.operant;
        }
        
        public <X> boolean applyAndSet(CtxVar<?> operant, BiConsumer<CtxVarType<X>, X> result)
        {
            AtomicBoolean success = new AtomicBoolean(false);
            
            operant.tryGetAs(this.operant).ifPresent(op ->
            {
                result.accept((CtxVarType<X>) this.result(), (X) this.function.apply(op));
                success.set(true);
            });
            
            return success.get();
        }
    }
}

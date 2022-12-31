package de.cas_ual_ty.spells.spell.compiler;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
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
    public static final UnaryOperation NEGATE = new UnaryOperation("-");
    
    public static final UnaryOperation ROUND = new UnaryOperation("round");
    public static final UnaryOperation FLOOR = new UnaryOperation("floor");
    public static final UnaryOperation CEIL = new UnaryOperation("ceil");
    public static final UnaryOperation SQRT = new UnaryOperation("sqrt");
    public static final UnaryOperation GET_X = new UnaryOperation("get_x");
    public static final UnaryOperation GET_Y = new UnaryOperation("get_y");
    public static final UnaryOperation GET_Z = new UnaryOperation("get_z");
    public static final UnaryOperation LENGTH = new UnaryOperation("length");
    public static final UnaryOperation NORMALIZE = new UnaryOperation("normalize");
    
    public static void registerToCompiler()
    {
        Compiler.registerUnaryFunction("round", ROUND);
        Compiler.registerUnaryFunction("floor", FLOOR);
        Compiler.registerUnaryFunction("ceil", CEIL);
        Compiler.registerUnaryFunction("sqrt", SQRT);
        Compiler.registerUnaryFunction("get_x", GET_X);
        Compiler.registerUnaryFunction("get_y", GET_Y);
        Compiler.registerUnaryFunction("get_z", GET_Z);
        Compiler.registerUnaryFunction("length", LENGTH);
        Compiler.registerUnaryFunction("normalize", NORMALIZE);
    }
    
    public final String name;
    private List<Entry<?, ?>> map;
    
    public UnaryOperation(String name)
    {
        this.name = name;
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
        
        if(entry == null)
        {
            entry = map.stream().filter(e -> e.areTypesIndirectlyApplicable(operant)).findFirst().orElse(null);
        }
        
        if(entry == null && SpellsConfig.DEBUG_SPELLS.get())
        {
            SpellsAndShields.LOGGER.info("Can not execute unary operation \"" + name + "\" with types " + CtxVarTypes.REGISTRY.get().getKey(operant));
        }
        
        return entry;
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

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
import java.util.function.BiFunction;

public class BinaryOperation
{
    // compiler built-in
    public static final BinaryOperation ADD = new BinaryOperation("+");
    public static final BinaryOperation SUB = new BinaryOperation("-");
    public static final BinaryOperation MUL = new BinaryOperation("*");
    public static final BinaryOperation DIV = new BinaryOperation("/");
    public static final BinaryOperation REM = new BinaryOperation("%");
    public static final BinaryOperation EQ = new BinaryOperation("==");
    public static final BinaryOperation NEQ = new BinaryOperation("!=");
    public static final BinaryOperation GT = new BinaryOperation(">");
    public static final BinaryOperation GEQ = new BinaryOperation(">=");
    public static final BinaryOperation LT = new BinaryOperation("<");
    public static final BinaryOperation LEQ = new BinaryOperation("<=");
    public static final BinaryOperation AND = new BinaryOperation("&&");
    public static final BinaryOperation OR = new BinaryOperation("||");
    
    public static final BinaryOperation MIN = new BinaryOperation("min");
    public static final BinaryOperation MAX = new BinaryOperation("max");
    public static final BinaryOperation MOVE_X = new BinaryOperation("move_x");
    public static final BinaryOperation MOVE_Y = new BinaryOperation("move_y");
    public static final BinaryOperation MOVE_Z = new BinaryOperation("move_z");
    public static final BinaryOperation GET_NBT_INT = new BinaryOperation("get_nbt_int");
    public static final BinaryOperation GET_NBT_DOUBLE = new BinaryOperation("get_nbt_double");
    public static final BinaryOperation GET_NBT_BOOLEAN = new BinaryOperation("get_nbt_boolean");
    public static final BinaryOperation GET_NBT_COMPOUND_TAG = new BinaryOperation("get_nbt_compound_tag");
    public static final BinaryOperation GET_NBT_STRING = new BinaryOperation("get_nbt_string");
    
    public static void registerToCompiler()
    {
        Compiler.registerBinaryFunction("min", MIN);
        Compiler.registerBinaryFunction("max", MAX);
        Compiler.registerBinaryFunction("move_x", MOVE_X);
        Compiler.registerBinaryFunction("move_y", MOVE_Y);
        Compiler.registerBinaryFunction("move_z", MOVE_Z);
        Compiler.registerBinaryFunction("get_nbt_int", GET_NBT_INT);
        Compiler.registerBinaryFunction("get_nbt_double", GET_NBT_DOUBLE);
        Compiler.registerBinaryFunction("get_nbt_boolean", GET_NBT_BOOLEAN);
        Compiler.registerBinaryFunction("get_nbt_compound_tag", GET_NBT_COMPOUND_TAG);
        Compiler.registerBinaryFunction("get_nbt_string", GET_NBT_STRING);
    }
    
    public final String name;
    private List<Entry<?, ?, ?>> map;
    
    public BinaryOperation(String name)
    {
        this.name = name;
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
        
        if(entry == null)
        {
            entry = map.stream().filter(e -> e.areTypesIndirectlyApplicable(operant1, operant2)).findFirst()
                    .orElse(map.stream().filter(e -> e.areTypesIndirectlyApplicable(operant2, operant1)).findFirst().orElse(null));
        }
        
        if(entry == null && SpellsConfig.DEBUG_SPELLS.get())
        {
            SpellsAndShields.LOGGER.info("Can not execute binary operation \"" + name + "\" with types " + CtxVarTypes.REGISTRY.get().getKey(operant1) + ", " + CtxVarTypes.REGISTRY.get().getKey(operant2));
        }
        
        return entry;
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
                    X value = (X) this.function.apply(op1, op2);
                    
                    if(value != null)
                    {
                        result.accept((CtxVarType<X>) this.result(), value);
                        success.set(true);
                    }
                });
            });
            
            return success.get();
        }
    }
}

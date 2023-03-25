package de.cas_ual_ty.spells.spell.compiler;

import com.mojang.serialization.DataResult;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.spell.variable.ReferencedCtxVar;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Compiler
{
    private static Map<String, Supplier<CtxVar<?>>> SUPPLIERS = new HashMap<>();
    private static Map<String, UnaryOperation> UNARY_FUNCTIONS = new HashMap<>();
    private static Map<String, BinaryOperation> BINARY_FUNCTIONS = new HashMap<>();
    private static Map<String, TernaryOperation> TERNARY_FUNCTIONS = new HashMap<>();
    
    public static <T> void registerSuppliersToCompiler()
    {
        registerSupplier("pi", CtxVarTypes.DOUBLE, () -> Math.PI);
        
        double sqrt2 = Math.sqrt(2D);
        registerSupplier("sqrt2", CtxVarTypes.DOUBLE, () -> sqrt2);
        
        Random random = new Random();
        registerSupplier("random_int", CtxVarTypes.INT, random::nextInt);
        registerSupplier("random_double", CtxVarTypes.DOUBLE, random::nextDouble);
        registerSupplier("random_uuid", CtxVarTypes.STRING, () -> UUID.randomUUID().toString());
    }
    
    public static <T> void registerSupplier(String name, Supplier<CtxVarType<T>> type, Supplier<T> value)
    {
        SUPPLIERS.put(name, () -> new CtxVar<>(type.get(), value.get()));
    }
    
    public static void registerUnaryFunction(String name, UnaryOperation op)
    {
        UNARY_FUNCTIONS.put(name, op);
    }
    
    public static void registerBinaryFunction(String name, BinaryOperation op)
    {
        BINARY_FUNCTIONS.put(name, op);
    }
    
    public static void registerTernaryFunction(String name, TernaryOperation op)
    {
        TERNARY_FUNCTIONS.put(name, op);
    }
    
    private static int position;
    private static String s;
    
    public static <T> DataResult<DynamicCtxVar<T>> compileData(String input, CtxVarType<T> type)
    {
        try
        {
            return DataResult.success(compile(input, type));
        }
        catch(InlineCompilationException e)
        {
            return DataResult.error(e.getMessage());
        }
    }
    
    public static <T> ReferencedCtxVar<T> compileString(String input, CtxVarType<T> type)
    {
        try
        {
            return compile(input, type);
        }
        catch(InlineCompilationException e)
        {
            SpellsAndShields.LOGGER.error(e.getMessage());
            return new ReferencedCtxVar<>(type, input, (ctx) -> Optional.empty());
        }
    }
    
    private static <T> ReferencedCtxVar<T> compile(String input, CtxVarType<T> type) throws InlineCompilationException
    {
        position = 0;
        s = input;
        
        Part part;
        
        try
        {
            part = compile();
        }
        catch(InlineCompilationException e)
        {
            SpellsAndShields.LOGGER.error(e.getMessage());
            return new ReferencedCtxVar<>(type, input, (ctx) -> Optional.empty());
        }
        
        if(position < s.length())
        {
            throw makeException("Expected end of string.");
        }
        
        return new ReferencedCtxVar<T>(type, input, (ctx) -> part.getValue(ctx).map(v -> v.tryConvertTo(type)));
    }
    
    private static InlineCompilationException makeException(String msg)
    {
        return new InlineCompilationException("Can not compile \"" + s + "\" at index " + position + ": " + msg);
    }
    
    private static void nextChar()
    {
        position++;
    }
    
    private static char getChar()
    {
        if(position >= s.length())
        {
            return 0;
        }
        
        return s.charAt(position);
    }
    
    private static void skipSpaces()
    {
        while(getChar() == ' ')
        {
            nextChar();
        }
    }
    
    private static void nextCharSkipSpaces()
    {
        nextChar();
        skipSpaces();
    }
    
    private static String readName()
    {
        int start = position;
        
        if(Character.isAlphabetic(getChar()) || getChar() == '_')
        {
            nextChar();
        }
        
        while(Character.isAlphabetic(getChar()) || Character.isDigit(getChar()) || getChar() == '_')
        {
            nextChar();
        }
        
        if(position == start)
        {
            throw makeException("Expected identifier.");
        }
        
        final int end = position;
        
        skipSpaces();
        
        return s.substring(start, end);
    }
    
    private static Part readImmediate()
    {
        boolean floatingPoint = false;
        
        final int start = position;
        
        while(Character.isDigit(getChar()))
        {
            nextChar();
        }
        
        if(getChar() == '.')
        {
            nextChar();
            
            floatingPoint = true;
            
            while(Character.isDigit(getChar()))
            {
                nextChar();
            }
        }
        
        if(position == start)
        {
            throw makeException("Expected identifier.");
        }
        
        final int end = position;
        
        skipSpaces();
        
        if(floatingPoint)
        {
            double value = Double.parseDouble(s.substring(start, end));
            return (ctx) -> Optional.of(new CtxVar<>(CtxVarTypes.DOUBLE.get(), value));
        }
        else
        {
            int value = Integer.parseInt(s.substring(start, end));
            return (ctx) -> Optional.of(new CtxVar<>(CtxVarTypes.INT.get(), value));
        }
    }
    
    private static Part readString()
    {
        if(getChar() != '\'')
        {
            throw makeException("Expected string to start with '''.");
        }
        
        nextChar();
        
        StringBuilder s = new StringBuilder();
        
        char c;
        while((c = getChar()) != '\'' && position < Compiler.s.length())
        {
            s.append(c);
            nextChar();
        }
        
        if(c == '\'')
        {
            nextChar();
        }
        else
        {
            throw makeException("Expected string to end with '''.");
        }
        
        skipSpaces();
        
        return (ctx) -> Optional.of(new CtxVar<>(CtxVarTypes.STRING.get(), s.toString()));
    }
    
    private static Part makeUnaryFunc(UnaryOperation op, Part operant1)
    {
        return (ctx) ->
        {
            Optional<CtxVar<?>> optional1 = operant1.getValue(ctx);
            
            AtomicReference<CtxVar<?>> newVar = new AtomicReference<>(null);
            
            optional1.ifPresent(op1 ->
            {
                op.applyAndSet(op1, (type, value) -> newVar.set(new CtxVar<>(type, value)));
            });
            
            if((optional1.isEmpty() || newVar.get() == null) && SpellsConfig.DEBUG_SPELLS.get())
            {
                SpellsAndShields.LOGGER.info("Error executing compiled unary operation \"" + op.name + "\":");
                if(optional1.isEmpty())
                {
                    SpellsAndShields.LOGGER.info("Operant 1 does not exist.");
                }
                if(newVar.get() == null)
                {
                    SpellsAndShields.LOGGER.info("Result does not exist.");
                }
            }
            
            return Optional.ofNullable(newVar.get());
        };
    }
    
    private static Part makeBinaryFunc(BinaryOperation op, Part operant1, Part operant2)
    {
        return (ctx) ->
        {
            Optional<CtxVar<?>> optional1 = operant1.getValue(ctx);
            Optional<CtxVar<?>> optional2 = operant2.getValue(ctx);
            
            AtomicReference<CtxVar<?>> newVar = new AtomicReference<>(null);
            
            optional1.ifPresent(op1 ->
            {
                optional2.ifPresent(op2 ->
                {
                    op.applyAndSet(op1, op2, (type, value) -> newVar.set(new CtxVar<>(type, value)));
                });
            });
            
            if((optional1.isEmpty() || optional2.isEmpty() || newVar.get() == null) && SpellsConfig.DEBUG_SPELLS.get())
            {
                SpellsAndShields.LOGGER.info("Error executing compiled binary operation \"" + op.name + "\":");
                if(optional1.isEmpty())
                {
                    SpellsAndShields.LOGGER.info("Operant 1 does not exist.");
                }
                if(optional2.isEmpty())
                {
                    SpellsAndShields.LOGGER.info("Operant 2 does not exist.");
                }
                if(newVar.get() == null)
                {
                    SpellsAndShields.LOGGER.info("Result does not exist.");
                }
            }
            
            return Optional.ofNullable(newVar.get());
        };
    }
    
    private static Part makeTernaryFunc(TernaryOperation op, Part operant1, Part operant2, Part operant3)
    {
        return (ctx) ->
        {
            Optional<CtxVar<?>> optional1 = operant1.getValue(ctx);
            Optional<CtxVar<?>> optional2 = operant2.getValue(ctx);
            Optional<CtxVar<?>> optional3 = operant3.getValue(ctx);
            
            AtomicReference<CtxVar<?>> newVar = new AtomicReference<>(null);
            
            optional1.ifPresent(op1 ->
            {
                optional2.ifPresent(op2 ->
                {
                    optional3.ifPresent(op3 ->
                    {
                        op.applyAndSet(op1, op2, op3, (type, value) -> newVar.set(new CtxVar<>(type, value)));
                    });
                });
            });
            
            if((optional1.isEmpty() || optional2.isEmpty() || optional3.isEmpty() || newVar.get() == null) && SpellsConfig.DEBUG_SPELLS.get())
            {
                SpellsAndShields.LOGGER.info("Error executing compiled ternary operation \"" + op.name + "\":");
                if(optional1.isEmpty())
                {
                    SpellsAndShields.LOGGER.info("Operant 1 does not exist.");
                }
                if(optional2.isEmpty())
                {
                    SpellsAndShields.LOGGER.info("Operant 2 does not exist.");
                }
                if(optional3.isEmpty())
                {
                    SpellsAndShields.LOGGER.info("Operant 3 does not exist.");
                }
                if(newVar.get() == null)
                {
                    SpellsAndShields.LOGGER.info("Result does not exist.");
                }
            }
            
            return Optional.ofNullable(newVar.get());
        };
    }
    
    private static Part compileFactor()
    {
        boolean negate = false;
        boolean not = false;
        
        Part ref;
        
        if(getChar() == '-')
        {
            nextCharSkipSpaces();
            negate = true;
        }
        else if(getChar() == '!')
        {
            nextCharSkipSpaces();
            not = true;
        }
        
        if(getChar() == '(')
        {
            nextCharSkipSpaces();
            
            ref = compileExpression();
            
            if(getChar() == ')')
            {
                nextCharSkipSpaces();
            }
            else
            {
                throw makeException("Expected ')'");
            }
        }
        else if(Character.isDigit(getChar()))
        {
            ref = readImmediate();
        }
        else if(getChar() == '\'')
        {
            ref = readString();
        }
        else
        {
            String name = readName();
            
            if(getChar() == '(')
            {
                nextCharSkipSpaces();
                
                List<Part> arguments = new ArrayList<>();
                
                if(getChar() != ')')
                {
                    arguments.add(compileExpression());
                    
                    while(getChar() == ',')
                    {
                        nextCharSkipSpaces();
                        arguments.add(compileExpression());
                    }
                }
                
                if(getChar() == ')')
                {
                    nextCharSkipSpaces();
                    ref = null;
                    
                    if(arguments.isEmpty())
                    {
                        Supplier<CtxVar<?>> supplier = SUPPLIERS.get(name);
                        ref = (ctx) -> Optional.ofNullable(supplier).map(Supplier::get);
                    }
                    else if(arguments.size() == 1)
                    {
                        UnaryOperation op = UNARY_FUNCTIONS.get(name);
                        if(op != null)
                        {
                            ref = makeUnaryFunc(op, arguments.get(0));
                        }
                    }
                    else if(arguments.size() == 2)
                    {
                        BinaryOperation op = BINARY_FUNCTIONS.get(name);
                        if(op != null)
                        {
                            ref = makeBinaryFunc(op, arguments.get(0), arguments.get(1));
                        }
                    }
                    else if(arguments.size() == 3)
                    {
                        TernaryOperation op = TERNARY_FUNCTIONS.get(name);
                        if(op != null)
                        {
                            ref = makeTernaryFunc(op, arguments.get(0), arguments.get(1), arguments.get(2));
                        }
                    }
                    
                    if(ref == null)
                    {
                        throw makeException("Unknown function \"" + name + "\" with number of arguments: " + arguments.size());
                    }
                }
                else
                {
                    throw makeException("Expected ')'");
                }
            }
            else
            {
                ref = (ctx) -> Optional.ofNullable(ctx.getCtxVar(name));
            }
        }
        
        if(negate)
        {
            ref = makeUnaryFunc(UnaryOperation.NEGATE, ref);
        }
        else if(not)
        {
            ref = makeUnaryFunc(UnaryOperation.NOT, ref);
        }
        
        return ref;
    }
    
    private static Part compileProduct()
    {
        Part currentOp = compileFactor();
        
        char sign;
        
        while((sign = getChar()) == '*' || sign == '/')
        {
            nextCharSkipSpaces();
            
            Part op2 = compileFactor();
            
            if(sign == '*')
            {
                currentOp = makeBinaryFunc(BinaryOperation.MUL, currentOp, op2);
            }
            else
            {
                currentOp = makeBinaryFunc(BinaryOperation.DIV, currentOp, op2);
            }
        }
        
        return currentOp;
    }
    
    private static Part compileSum()
    {
        Part currentOp = compileProduct();
        
        char sign;
        
        while((sign = getChar()) == '+' || sign == '-')
        {
            nextCharSkipSpaces();
            
            Part op2 = compileProduct();
            
            if(sign == '+')
            {
                currentOp = makeBinaryFunc(BinaryOperation.ADD, currentOp, op2);
            }
            else
            {
                currentOp = makeBinaryFunc(BinaryOperation.SUB, currentOp, op2);
            }
        }
        
        return currentOp;
    }
    
    private static Part compileRelation()
    {
        Part currentOp = compileSum();
        
        char sign;
        
        while((sign = getChar()) == '>' || sign == '<')
        {
            nextChar();
            
            BinaryOperation op;
            
            if(sign == '>')
            {
                if(getChar() == '=')
                {
                    nextCharSkipSpaces();
                    op = BinaryOperation.GEQ;
                }
                else
                {
                    op = BinaryOperation.GT;
                }
            }
            else
            {
                if(getChar() == '=')
                {
                    nextCharSkipSpaces();
                    op = BinaryOperation.LEQ;
                }
                else
                {
                    op = BinaryOperation.LT;
                }
            }
            
            Part op2 = compileSum();
            
            currentOp = makeBinaryFunc(op, currentOp, op2);
        }
        
        return currentOp;
    }
    
    private static Part compileComparison()
    {
        Part currentOp = compileRelation();
        
        char sign;
        
        while((sign = getChar()) == '=' || sign == '!')
        {
            nextChar();
            
            BinaryOperation op;
            
            if(sign == '=')
            {
                if(getChar() == '=')
                {
                    nextCharSkipSpaces();
                    op = BinaryOperation.EQ;
                }
                else
                {
                    throw makeException("Expected '='");
                }
            }
            else
            {
                if(getChar() == '=')
                {
                    nextCharSkipSpaces();
                    op = BinaryOperation.NEQ;
                }
                else
                {
                    throw makeException("Expected '='");
                }
            }
            
            Part op2 = compileRelation();
            
            currentOp = makeBinaryFunc(op, currentOp, op2);
        }
        
        return currentOp;
    }
    
    private static Part compileConjunction()
    {
        Part currentOp = compileComparison();
        
        char sign;
        
        while(getChar() == '&')
        {
            nextChar();
            
            if(getChar() == '&')
            {
                nextCharSkipSpaces();
            }
            else
            {
                throw makeException("Expected '&'");
            }
            
            Part op2 = compileComparison();
            
            currentOp = makeBinaryFunc(BinaryOperation.AND, currentOp, op2);
        }
        
        return currentOp;
    }
    
    private static Part compileDisjunction()
    {
        Part currentOp = compileConjunction();
        
        while(getChar() == '|')
        {
            nextChar();
            
            if(getChar() == '|')
            {
                nextCharSkipSpaces();
            }
            else
            {
                throw makeException("Expected '|'");
            }
            
            Part op2 = compileConjunction();
            
            currentOp = makeBinaryFunc(BinaryOperation.OR, currentOp, op2);
        }
        
        return currentOp;
    }
    
    private static Part compileConditional()
    {
        Part conditional = compileDisjunction();
        
        if(getChar() == '?')
        {
            nextCharSkipSpaces();
            
            Part op1 = compileDisjunction();
            
            if(getChar() == ':')
            {
                nextChar();
            }
            else
            {
                throw makeException("Expected ':'");
            }
            
            Part op2 = compileDisjunction();
            
            return makeTernaryFunc(TernaryOperation.CONDITIONAL, conditional, op1, op2);
        }
        
        return conditional;
    }
    
    private static Part compileExpression()
    {
        return compileConditional();
    }
    
    private static Part compile()
    {
        skipSpaces();
        Part part = compileExpression();
        skipSpaces();
        return part;
    }
    
    private interface Part
    {
        Optional<CtxVar<?>> getValue(SpellContext ctx);
    }
    
    private static class InlineCompilationException extends RuntimeException
    {
        public InlineCompilationException()
        {
            super();
        }
        
        public InlineCompilationException(String message)
        {
            super(message);
        }
    }
}

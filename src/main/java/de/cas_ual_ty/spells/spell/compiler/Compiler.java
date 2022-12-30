package de.cas_ual_ty.spells.spell.compiler;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.ReferencedCtxVar;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Compiler
{
    private static Map<String, UnaryOperation> UNARY_FUNCTIONS = new HashMap<>();
    private static Map<String, BinaryOperation> BINARY_FUNCTIONS = new HashMap<>();
    private static Map<String, TernaryOperation> TERNARY_FUNCTIONS = new HashMap<>();
    
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
    
    public static <T> ReferencedCtxVar<T> compile(String input, CtxVarType<T> type)
    {
        position = 0;
        s = input;
        
        Part part;
        
        try
        {
            part = compileExpression();
        }
        catch(InlineCompilationException e)
        {
            SpellsAndShields.LOGGER.error(e.getMessage());
            return new ReferencedCtxVar<>(type, input, (ctx) -> Optional.empty());
        }
        
        if(position < s.length())
        {
            SpellsAndShields.LOGGER.warn("############## " + s.substring(position) + " /// " + position);
        }
        
        return new ReferencedCtxVar<T>(type, input, (ctx) -> part.getValue(ctx).map(v -> v.tryConvertTo(type)));
    }
    
    private static InlineCompilationException makeException(String msg)
    {
        return new InlineCompilationException("Can not compile \"" + s + "\" at index " + position + " " + msg);
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
        
        return s.substring(start, position);
    }
    
    private static Part makeUnaryFunc(UnaryOperation op, Part operant1)
    {
        return (ctx) ->
        {
            Optional<CtxVar<?>> optional1 = operant1.getValue(ctx);
            
            AtomicReference<CtxVar<?>> newVar = new AtomicReference<>(null);
            
            optional1.ifPresent(op1 ->
            {
                op.applyAndSet(op1, (type, value) -> newVar.set(new CtxVar<>(type, null, value)));
            });
            
            return Optional.ofNullable(newVar.get());
        };
    }
    
    private static Part makeTernaryFunc(BinaryOperation op, Part operant1, Part operant2)
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
                    op.applyAndSet(op1, op2, (type, value) -> newVar.set(new CtxVar<>(type, null, value)));
                });
            });
            
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
                        op.applyAndSet(op1, op2, op3, (type, value) -> newVar.set(new CtxVar<>(type, null, value)));
                    });
                });
            });
            
            return Optional.ofNullable(newVar.get());
        };
    }
    
    private static Part compileRef()
    {
        String name = readName();
        return (ctx) -> Optional.ofNullable(ctx.getCtxVar(name));
    }
    
    private static Part compileFactor()
    {
        boolean negate = false;
        
        Part ref;
        
        if(getChar() == '-')
        {
            nextChar();
            negate = true;
        }
        
        if(getChar() == '(')
        {
            nextChar();
            
            ref = compileExpression();
            
            if(getChar() == ')')
            {
                nextChar();
            }
            else
            {
                throw makeException("Expected ')'");
            }
        }
        else
        {
            String name = readName();
            
            if(getChar() == '(')
            {
                nextChar();
                
                List<Part> arguments = new ArrayList<>();
                arguments.add(compileExpression());
                
                while(getChar() == ',')
                {
                    nextChar();
                    arguments.add(compileExpression());
                }
                
                if(getChar() == ')')
                {
                    nextChar();
                    ref = null;
                    
                    if(arguments.size() == 1)
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
                            ref = makeTernaryFunc(op, arguments.get(0), arguments.get(1));
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
        
        return ref;
    }
    
    private static Part compileProduct()
    {
        Part currentOp = compileFactor();
        
        char sign;
        
        while((sign = getChar()) == '*' || sign == '/')
        {
            nextChar();
            
            Part op2 = compileFactor();
            
            if(sign == '*')
            {
                currentOp = makeTernaryFunc(BinaryOperation.MUL, currentOp, op2);
            }
            else
            {
                currentOp = makeTernaryFunc(BinaryOperation.DIV, currentOp, op2);
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
            nextChar();
            
            Part op2 = compileProduct();
            
            if(sign == '+')
            {
                currentOp = makeTernaryFunc(BinaryOperation.ADD, currentOp, op2);
            }
            else
            {
                currentOp = makeTernaryFunc(BinaryOperation.SUB, currentOp, op2);
            }
        }
        
        return currentOp;
    }
    
    private static Part compileExpression()
    {
        return compileSum();
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

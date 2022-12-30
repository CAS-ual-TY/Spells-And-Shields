package de.cas_ual_ty.spells.spell.variable;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.BinaryOperation;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import de.cas_ual_ty.spells.spell.variable.ReferencedCtxVar;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class Compiler
{
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
    
    private static void throwException(String msg)
    {
        throw new InlineCompilationException("Can not compile \"" + s + "\" at index " + position + " " + msg);
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
            throwException("Expected identifier.");
        }
        
        return s.substring(start, position);
    }
    
    private static Part compileRef()
    {
        String name = readName();
        return (ctx) -> Optional.ofNullable(ctx.getCtxVar(name));
    }
    
    private static Part compileFactor()
    {
        Part ref;
        
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
                throwException("Expected ')'");
            }
        }
        else
        {
            String name = readName();
            ref = (ctx) -> Optional.ofNullable(ctx.getCtxVar(name));
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
            
            Part op1 = currentOp;
            Part op2 = compileFactor();
            
            BinaryOperation op;
            
            if(sign == '*')
            {
                op = SpellActionTypes.MUL_MAP;
            }
            else
            {
                op = SpellActionTypes.DIV_MAP;
            }
            
            currentOp = (ctx) ->
            {
                Optional<CtxVar<?>> optional1 = op1.getValue(ctx);
                Optional<CtxVar<?>> optional2 = op2.getValue(ctx);
                
                AtomicReference<CtxVar<?>> newVar = new AtomicReference<>(null);
                
                if(optional1.isPresent() && optional2.isPresent())
                {
                    op.applyAndSet(optional1.get(), optional2.get(), (type, value) -> newVar.set(new CtxVar<>(type, null, value)));
                }
                
                return Optional.ofNullable(newVar.get());
            };
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
            
            Part op1 = currentOp;
            Part op2 = compileProduct();
            
            BinaryOperation op;
            
            if(sign == '+')
            {
                op = SpellActionTypes.ADD_MAP;
            }
            else
            {
                op = SpellActionTypes.SUB_MAP;
            }
            
            currentOp = (ctx) ->
            {
                Optional<CtxVar<?>> optional1 = op1.getValue(ctx);
                Optional<CtxVar<?>> optional2 = op2.getValue(ctx);
                
                AtomicReference<CtxVar<?>> newVar = new AtomicReference<>(null);
                
                if(optional1.isPresent() && optional2.isPresent())
                {
                    op.applyAndSet(optional1.get(), optional2.get(), (type, value) -> newVar.set(new CtxVar<>(type, null, value)));
                }
                
                return Optional.ofNullable(newVar.get());
            };
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

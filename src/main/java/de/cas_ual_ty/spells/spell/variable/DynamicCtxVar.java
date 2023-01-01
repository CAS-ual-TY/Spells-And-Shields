package de.cas_ual_ty.spells.spell.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.context.SpellContext;

import java.util.Optional;

public abstract class DynamicCtxVar<I>
{
    public static final String VAR_PREFIX = "<<";
    public static final String VAR_SUFFIX = ">>";
    
    public static <I> Codec<DynamicCtxVar<I>> makeCodec(CtxVarType<I> type)
    {
        return new PrimitiveCodec<DynamicCtxVar<I>>()
        {
            @Override
            public <T> DataResult<DynamicCtxVar<I>> read(DynamicOps<T> ops, T input)
            {
                DataResult<String> nameResult = ops.getStringValue(input);
                if(nameResult.result().isPresent())
                {
                    String name = nameResult.result().get();
                    if(name.startsWith(VAR_PREFIX) && name.endsWith(VAR_SUFFIX))
                    {
                        name = name.substring(0, name.length() - VAR_SUFFIX.length()).substring(VAR_PREFIX.length());
                        return Compiler.compileData(name, type);
                    }
                }
                
                DataResult<I> immResult = type.getImmCodec().parse(ops, input);
                return immResult.map(imm -> new ImmediateCtxVar<>(type, imm));
            }
            
            @Override
            public <T> T write(DynamicOps<T> ops, DynamicCtxVar<I> value)
            {
                if(value instanceof ReferencedCtxVar<I> dyn)
                {
                    return ops.createString(dyn.compiledString());
                }
                else if(value instanceof ImmediateCtxVar<I> imm)
                {
                    Optional<T> r = type.getImmCodec().encodeStart(ops, imm.getImm()).result();
                    
                    if(r.isPresent())
                    {
                        return r.get();
                    }
                }
                
                throw new IllegalStateException();
            }
        };
    }
    
    private CtxVarType<I> type;
    
    public DynamicCtxVar(CtxVarType<I> type)
    {
        this.type = type;
    }
    
    public CtxVarType<I> getType()
    {
        return type;
    }
    
    public abstract Optional<I> getValue(SpellContext ctx);
    
    public <J> Optional<J> tryGetAs(SpellContext ctx, CtxVarType<J> type)
    {
        return getValue(ctx).map(i -> this.type.convertTo(type, i));
    }
}

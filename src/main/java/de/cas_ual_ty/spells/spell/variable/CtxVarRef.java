package de.cas_ual_ty.spells.spell.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import de.cas_ual_ty.spells.spell.context.SpellContext;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class CtxVarRef<I>
{
    public static final String VAR_PREFIX = "<<";
    public static final String VAR_SUFFIX = ">>";
    
    public static <I> Codec<CtxVarRef<I>> makeCodec(CtxVarType<I> type)
    {
        return new PrimitiveCodec<CtxVarRef<I>>()
        {
            @Override
            public <T> DataResult<CtxVarRef<I>> read(DynamicOps<T> ops, T input)
            {
                DataResult<String> nameResult = ops.getStringValue(input);
                if(nameResult.result().isPresent())
                {
                    String name = nameResult.result().get();
                    if(name.startsWith(VAR_PREFIX) && name.endsWith(VAR_SUFFIX))
                    {
                        name = name.substring(0, name.length() - VAR_SUFFIX.length()).substring(VAR_PREFIX.length());
                        return DataResult.success(new CtxVarDyn<>(type, name));
                    }
                }
                
                DataResult<I> immResult = type.getImmCodec().parse(ops, input);
                return immResult.map(imm -> new CtxVarImm<>(type, imm));
            }
            
            @Override
            public <T> T write(DynamicOps<T> ops, CtxVarRef<I> value)
            {
                if(value instanceof CtxVarDyn<I> dyn)
                {
                    return ops.createString(VAR_PREFIX + dyn.getName() + VAR_SUFFIX);
                }
                else if(value instanceof CtxVarImm<I> imm)
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
    
    public CtxVarRef(CtxVarType<I> type)
    {
        this.type = type;
    }
    
    public CtxVarType<I> getType()
    {
        return type;
    }
    
    public abstract Optional<I> getValue(SpellContext ctx);
    
    public static class CtxVarImm<T> extends CtxVarRef<T>
    {
        private T imm;
        private Optional<T> optional;
        
        public CtxVarImm(CtxVarType<T> type, T value)
        {
            super(type);
            imm = value;
            optional = Optional.of(value);
        }
        
        @Override
        public Optional<T> getValue(@Nullable SpellContext ctx)
        {
            return optional;
        }
        
        public T getImm()
        {
            return imm;
        }
    }
    
    public static class CtxVarDyn<T> extends CtxVarRef<T>
    {
        private String name;
        
        public CtxVarDyn(CtxVarType<T> type, String name)
        {
            super(type);
            this.name = name;
        }
        
        public String getName()
        {
            return name;
        }
        
        @Override
        public Optional<T> getValue(SpellContext ctx)
        {
            return ctx.getCtxVar(getType(), name);
        }
    }
}

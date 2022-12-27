package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MappedUnaryVarAction extends UnaryVarAction
{
    public static Codec<MappedUnaryVarAction> makeCodec(SpellActionType<MappedUnaryVarAction> type, UnaryOperatorMap map)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                operantCodec(),
                resultCodec()
        ).apply(instance, (activation, operant, result) -> new MappedUnaryVarAction(type, activation, operant, result, map)));
    }
    
    protected UnaryOperatorMap map;
    
    public MappedUnaryVarAction(SpellActionType<?> type, UnaryOperatorMap map)
    {
        super(type);
        this.map = map;
    }
    
    public MappedUnaryVarAction(SpellActionType<?> type, String activation, String operant, String result, UnaryOperatorMap map)
    {
        super(type, activation, operant, result);
        this.map = map;
    }
    
    @Override
    protected <T, U> void tryCalculate(SpellContext ctx, CtxVar<T> operant, BiConsumer<CtxVarType<U>, U> result)
    {
        map.applyAndSet(operant, result);
    }
    
    public static class UnaryOperatorMap
    {
        private List<UnaryOperatorMapEntry<?, ?>> map;
        
        public UnaryOperatorMap()
        {
            map = new LinkedList<>();
        }
        
        public <T, U> UnaryOperatorMap register(CtxVarType<T> operant, CtxVarType<U> result, Function<T, U> function)
        {
            UnaryOperatorMapEntry<T, U> entry = new UnaryOperatorMapEntry<>(operant, result, function);
            map.add(entry);
            return this;
        }
        
        public UnaryOperatorMapEntry<?, ?> getEntry(CtxVarType<?> operant)
        {
            UnaryOperatorMapEntry<?, ?> entry = map.stream().filter(e -> e.areTypesDirectlyApplicable(operant)).findFirst().orElse(null);
            
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
            UnaryOperatorMapEntry<?, ?> entry = getEntry(operant.getType());
            
            if(entry != null)
            {
                return entry.applyAndSet(operant, result);
            }
            else
            {
                return false;
            }
        }
    }
    
    public static record UnaryOperatorMapEntry<T, U>(CtxVarType<T> operant, CtxVarType<U> result,
                                                     Function<T, U> function)
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
    
    public static SpellActionType<MappedUnaryVarAction> makeType(UnaryOperatorMap map)
    {
        return new SpellActionType<>((type) -> new MappedUnaryVarAction(type, map), (type) -> makeCodec(type, map));
    }
}

package de.cas_ual_ty.spells.spell.action.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    protected <T, U> void tryCalculate(SpellContext ctx, CtxVar<T> operant, CtxVar<U> result)
    {
        map.applyAndSet(operant, result);
    }
    
    public static class UnaryOperatorMap
    {
        private Map<CtxVarType<?>, List<UnaryOperatorMapEntry<?, ?>>> map;
        
        public UnaryOperatorMap()
        {
            map = new HashMap<>();
        }
        
        public <T, U> UnaryOperatorMap register(CtxVarType<T> operant, CtxVarType<U> result, Function<T, U> function)
        {
            List<UnaryOperatorMapEntry<?, U>> list = getList(result);
            UnaryOperatorMapEntry<T, U> entry = new UnaryOperatorMapEntry<>(operant, result, function);
            
            if(list != null)
            {
                list.add(entry);
            }
            else
            {
                map.put(result, new LinkedList<>(List.of(entry)));
            }
            
            return this;
        }
        
        public <U> UnaryOperatorMapEntry<?, U> getEntry(CtxVarType<?> operant, CtxVarType<U> result)
        {
            List<UnaryOperatorMapEntry<?, U>> list = getList(result);
            
            if(list != null)
            {
                UnaryOperatorMapEntry<?, U> entry = list.stream().filter(e -> e.areTypesDirectlyApplicable(operant, result)).findFirst().orElse(null);
                
                if(entry != null)
                {
                    return entry;
                }
                else
                {
                    return list.stream().filter(e -> e.areTypesIndirectlyApplicable(operant, result)).findFirst().orElse(null);
                }
            }
            else
            {
                return null;
            }
        }
        
        public <T, U> boolean applyAndSet(CtxVar<?> operant, CtxVar<U> result)
        {
            UnaryOperatorMapEntry<?, U> entry = getEntry(operant.getType(), result.getType());
            
            if(entry != null)
            {
                return entry.applyAndSet(operant, result);
            }
            else
            {
                return false;
            }
        }
        
        public <U> List<UnaryOperatorMapEntry<?, U>> getList(CtxVarType<U> result)
        {
            List<UnaryOperatorMapEntry<?, ?>> list = map.get(result);
            
            if(list != null)
            {
                return list.stream().map(e -> (UnaryOperatorMapEntry<?, U>) e).collect(Collectors.toList());
            }
            else
            {
                return null;
            }
        }
    }
    
    public static record UnaryOperatorMapEntry<T, U>(CtxVarType<T> operant, CtxVarType<U> result,
                                                     Function<T, U> function)
    {
        public boolean areTypesIndirectlyApplicable(CtxVarType<?> operant, CtxVarType<U> result)
        {
            return operant.canConvertTo(this.operant) && result == this.result;
        }
        
        public boolean areTypesDirectlyApplicable(CtxVarType<?> operant, CtxVarType<U> result)
        {
            return operant == this.operant && result == this.result;
        }
        
        public boolean applyAndSet(CtxVar<?> operant, CtxVar<U> result)
        {
            return applyAndSet(operant, result::setValue);
        }
        
        public boolean applyAndSet(CtxVar<?> operant, Consumer<U> result)
        {
            AtomicBoolean success = new AtomicBoolean(false);
            
            operant.tryGetAs(this.operant).ifPresent(op ->
            {
                result.accept(this.function.apply(op));
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

package de.cas_ual_ty.spells.spell.target;

import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

// changed to a class for the 1.19.2 -> 1.18.2 downgrade
public class ITargetType<T extends Target> extends ForgeRegistryEntry<ITargetType<?>>
{
    private Predicate<Target> isTypeFunc;
    
    // 1.19.2 -> 1.18.2 downgrade
    public ITargetType()
    {
    
    }
    
    public ITargetType(Predicate<Target> isTypeFunc)
    {
        this.isTypeFunc = isTypeFunc;
    }
    
    public boolean isType(Target t)
    {
        return isTypeFunc.test(t);
    }
    
    public T asType(Target target)
    {
        return (T) target;
    }
    
    public void ifType(Target target, Consumer<T> consumer)
    {
        if(isType(target))
        {
            consumer.accept(asType(target));
        }
    }
    
    public Optional<T> ifType(Target target)
    {
        if(isType(target))
        {
            return Optional.of(asType(target));
        }
        else
        {
            return Optional.empty();
        }
    }
}

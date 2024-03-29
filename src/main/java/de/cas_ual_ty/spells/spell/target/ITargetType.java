package de.cas_ual_ty.spells.spell.target;

import java.util.Optional;
import java.util.function.Consumer;

public interface ITargetType<T extends Target>
{
    boolean isType(Target t);
    
    default T asType(Target target)
    {
        return (T) target;
    }
    
    default void ifType(Target target, Consumer<T> consumer)
    {
        if(isType(target))
        {
            consumer.accept(asType(target));
        }
    }
    
    default Optional<T> ifType(Target target)
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
    
    static <T extends Target> ITargetType<T> isTypeByRef()
    {
        return new ITargetType<T>()
        {
            @Override
            public boolean isType(Target t)
            {
                return t.type == this;
            }
        };
    }
}

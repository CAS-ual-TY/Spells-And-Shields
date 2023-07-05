package de.cas_ual_ty.spells.spell.context;

import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TargetGroup
{
    public final String id;
    private List<Target> targetsList;
    
    public TargetGroup(String id)
    {
        this.id = id;
        targetsList = new ArrayList<>();
    }
    
    public List<Target> getTargets()
    {
        return targetsList;
    }
    
    public void forEachTarget(Consumer<Target> consumer)
    {
        targetsList.forEach(consumer);
    }
    
    public void forEachTargetSafe(Consumer<Target> consumer)
    {
        List<Target> copy = new ArrayList<>(targetsList);
        copy.forEach(consumer);
    }
    
    public <T extends Target> void forEachType(ITargetType<T> type, Consumer<T> consumer)
    {
        forEachTarget(target ->
        {
            if(type.isType(target))
            {
                consumer.accept(type.asType(target));
            }
        });
    }
    
    public <T extends Target> void forEachTypeSafe(ITargetType<T> type, Consumer<T> consumer)
    {
        forEachTargetSafe(target ->
        {
            if(type.isType(target))
            {
                consumer.accept(type.asType(target));
            }
        });
    }
    
    public void addTargets(Target target)
    {
        targetsList.add(target);
    }
    
    public void addTargets(List<Target> targets)
    {
        targetsList.addAll(targets);
    }
    
    public void addTargets(Target... targets)
    {
        addTargets(Arrays.asList(targets));
    }
    
    public boolean isEmpty()
    {
        return targetsList.isEmpty();
    }
    
    public boolean isSingleTarget()
    {
        return targetsList.size() == 1;
    }
    
    public int size()
    {
        return targetsList.size();
    }
    
    public void clear()
    {
        targetsList.clear();
    }
    
    public void getSingleTarget(Consumer<Target> consumer)
    {
        if(isSingleTarget())
        {
            consumer.accept(targetsList.get(0));
        }
    }
    
    public Optional<Target> getSingleTarget()
    {
        if(isSingleTarget())
        {
            return Optional.of(targetsList.get(0));
        }
        else
        {
            return Optional.empty();
        }
    }
    
    public <T extends Target> void getSingleType(ITargetType<T> type, Consumer<T> consumer)
    {
        if(isSingleTarget())
        {
            if(type.isType(targetsList.get(0)))
            {
                consumer.accept(type.asType(targetsList.get(0)));
            }
        }
    }
    
    public static TargetGroup EMPTY = new TargetGroup("")
    {
        @Override
        public List<Target> getTargets()
        {
            return List.of();
        }
        
        @Override
        public void forEachTarget(Consumer<Target> consumer)
        {
        }
        
        @Override
        public <T extends Target> void forEachType(ITargetType<T> type, Consumer<T> consumer)
        {
        }
        
        @Override
        public void addTargets(Target target)
        {
        }
        
        @Override
        public void addTargets(List<Target> targets)
        {
        }
        
        @Override
        public void addTargets(Target... targets)
        {
        }
    };
}

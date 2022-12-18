package de.cas_ual_ty.spells.spell.context;

import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Consumer;

public class SpellContext
{
    public final Level level;
    public final SpellHolder spellHolder;
    public final SpellInstance spell;
    
    protected List<String> activationsList;
    protected Map<String, TargetGroup> targetGroups;
    protected Map<String, CtxVar<?>> ctxVars;
    protected boolean terminated;
    
    public SpellContext(Level level, SpellHolder spellHolder, SpellInstance spell)
    {
        this.level = level;
        this.spellHolder = spellHolder;
        this.spell = spell;
        activationsList = new ArrayList<>();
        targetGroups = new HashMap<>();
        ctxVars = new HashMap<>();
        terminated = false;
    }
    
    public Level getLevel()
    {
        return level;
    }
    
    public void activate(String activation)
    {
        activationsList.add(activation);
    }
    
    public void activate(List<String> activation)
    {
        activationsList.addAll(activation);
    }
    
    public void activate(String... activations)
    {
        activate(Arrays.asList(activations));
    }
    
    public void deactivate(String activation)
    {
        activationsList.remove(activation);
    }
    
    public void deactivate(List<String> activation)
    {
        activationsList.removeAll(activation);
    }
    
    public void deactivate(String... activations)
    {
        deactivate(Arrays.asList(activations));
    }
    
    public boolean isActivated(String activation)
    {
        return activationsList.stream().anyMatch(s -> s.equals(activation));
    }
    
    public boolean hasTargetGroup(String id)
    {
        return id.isEmpty() || targetGroups.containsKey(id);
    }
    
    public TargetGroup getTargetGroup(String id)
    {
        return id.isEmpty() ? TargetGroup.EMPTY : targetGroups.get(id);
    }
    
    public TargetGroup getOrCreateTargetGroup(String id)
    {
        TargetGroup tg = getTargetGroup(id);
        
        if(tg == null)
        {
            tg = new TargetGroup(id);
            targetGroups.put(id, tg);
        }
        
        return tg;
    }
    
    public void forTargetGroup(String id, Consumer<TargetGroup> consumer)
    {
        TargetGroup tg = targetGroups.get(id);
        
        if(tg != null)
        {
            consumer.accept(tg);
        }
    }
    
    public <T> Optional<T> getCtxVar(CtxVarType<T> type, String name)
    {
        CtxVar<?> ctxVar = ctxVars.get(name);
        
        if(ctxVar != null)
        {
            return ctxVar.tryGetAs(type);
        }
        
        return Optional.empty();
    }
    
    public <T> boolean setCtxVar(CtxVarType<T> type, String name, T value)
    {
        CtxVar<?> ctxVar = ctxVars.get(name);
        
        if(ctxVar != null)
        {
            return ctxVar.trySet(type, value);
        }
        
        return false;
    }
    
    public void terminate()
    {
        terminated = true;
    }
    
    public boolean isTerminated()
    {
        return terminated;
    }
}

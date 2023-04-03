package de.cas_ual_ty.spells.spell.context;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class SpellContext
{
    public final Level level;
    @Nullable
    public final Player owner;
    public final SpellInstance spell;
    
    protected List<String> activationsList;
    protected Map<String, TargetGroup> targetGroups;
    protected Map<String, CtxVar<?>> ctxVars;
    protected boolean terminated;
    
    protected int index;
    
    public SpellContext(Level level, @Nullable Player owner, SpellInstance spell)
    {
        this.level = level;
        this.owner = owner;
        this.spell = spell;
        activationsList = new LinkedList<>();
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
        if(!activation.isEmpty())
        {
            activationsList.add(activation);
        }
    }
    
    public void activate(List<String> activation)
    {
        activationsList.forEach(this::activate);
    }
    
    public void activate(String... activations)
    {
        Arrays.stream(activations).forEach(this::activate);
    }
    
    public void deactivate(String activation)
    {
        if(!activation.isEmpty())
        {
            activationsList.remove(activation);
        }
    }
    
    public void deactivate(List<String> activation)
    {
        activationsList.forEach(this::deactivate);
    }
    
    public void deactivate(String... activations)
    {
        Arrays.stream(activations).forEach(this::deactivate);
    }
    
    public boolean isActivated(String activation)
    {
        return activation.isEmpty() || activationsList.stream().anyMatch(s -> s.equals(activation));
    }
    
    public boolean hasTargetGroup(String id)
    {
        return id.isEmpty() || targetGroups.containsKey(id);
    }
    
    public TargetGroup getTargetGroup(String id)
    {
        return id.isEmpty() ? TargetGroup.EMPTY : targetGroups.getOrDefault(id, TargetGroup.EMPTY);
    }
    
    public TargetGroup getOrCreateTargetGroup(String id)
    {
        TargetGroup tg = getTargetGroup(id);
        
        if(!id.isEmpty() && tg == TargetGroup.EMPTY)
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
    
    @Nullable
    public CtxVar<?> getCtxVar(String name)
    {
        return name.isEmpty() ? null : ctxVars.get(name);
    }
    
    public <T> boolean initCtxVar(CtxVar<T> variable)
    {
        ctxVars.put(variable.getName(), variable.copy());
        return true;
    }
    
    public <T> Optional<T> getCtxVar(CtxVarType<T> type, String name)
    {
        CtxVar<?> ctxVar = getCtxVar(name);
        
        if(ctxVar != null)
        {
            return ctxVar.tryGetAs(type);
        }
        
        return Optional.empty();
    }
    
    public <T> boolean setCtxVar(CtxVarType<T> type, String name, T value)
    {
        CtxVar<?> ctxVar = getCtxVar(name);
        
        if(ctxVar != null)
        {
            return ctxVar.trySet(type, value);
        }
        else
        {
            initCtxVar(new CtxVar<>(type, name, value));
            return true;
        }
    }
    
    public void terminate()
    {
        terminated = true;
    }
    
    public boolean isTerminated()
    {
        return terminated;
    }
    
    public void setIndex(int index)
    {
        this.index = index;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public void run()
    {
        Spell spell = this.spell.getSpell().get();
        ResourceLocation spellRl = null;
        
        if(SpellsConfig.DEBUG_SPELLS.get())
        {
            spellRl = Spells.getRegistry(level).getKey(spell);
            SpellsAndShields.LOGGER.info("Running spell " + spellRl);
            SpellsAndShields.LOGGER.info("-".repeat(50));
            SpellsAndShields.LOGGER.info("Initial state:");
            debugActivations();
            debugTargetGroups();
            debugCtxVars();
            SpellsAndShields.LOGGER.info("-".repeat(50));
        }
        
        index = 0;
        List<SpellAction> actions = spell.getSpellActions();
        
        for(index = 0; index < actions.size() && index >= 0; index++)
        {
            SpellAction spellAction = actions.get(index);
            
            if(spellAction.doActivate(this))
            {
                ResourceLocation actionRl = null;
                
                if(SpellsConfig.DEBUG_SPELLS.get())
                {
                    actionRl = SpellActionTypes.REGISTRY.get().getKey(spellAction.getType());
                    SpellsAndShields.LOGGER.info("Starting action " + actionRl);
                }
                
                spellAction.doAction(this);
                
                if(SpellsConfig.DEBUG_SPELLS.get())
                {
                    SpellsAndShields.LOGGER.info("Finish action " + actionRl);
                    SpellsAndShields.LOGGER.info("-".repeat(50));
                    debugActivations();
                    debugTargetGroups();
                    debugCtxVars();
                    SpellsAndShields.LOGGER.info("-".repeat(50));
                }
                
                if(isTerminated())
                {
                    break;
                }
            }
        }
        
        if(SpellsConfig.DEBUG_SPELLS.get())
        {
            SpellsAndShields.LOGGER.info("Finished running spell " + spellRl);
            SpellsAndShields.LOGGER.info("-".repeat(50));
            SpellsAndShields.LOGGER.info("-".repeat(50));
        }
        
        terminate(); // make sure this context is not run again
    }
    
    public void debugCtxVars()
    {
        SpellsAndShields.LOGGER.info("  Context variables:");
        for(CtxVar<?> v : ctxVars.values())
        {
            SpellsAndShields.LOGGER.info("   - " + CtxVarTypes.REGISTRY.get().getKey(v.getType()) + " " + v.getName() + " / " + v.getValue().toString());
        }
    }
    
    public void debugTargetGroups()
    {
        SpellsAndShields.LOGGER.info("  Target groups:");
        for(Map.Entry<String, TargetGroup> entry : targetGroups.entrySet())
        {
            SpellsAndShields.LOGGER.info("    " + entry.getKey() + " / " + entry.getValue().size() + ":");
            entry.getValue().forEachTarget(target -> SpellsAndShields.LOGGER.info("      - " + TargetTypes.REGISTRY.get().getKey(target.type)));
        }
    }
    
    public void debugActivations()
    {
        SpellsAndShields.LOGGER.info("  Activations:");
        for(String a : activationsList)
        {
            SpellsAndShields.LOGGER.info("   - " + a);
        }
    }
}

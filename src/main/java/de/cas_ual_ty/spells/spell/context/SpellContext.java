package de.cas_ual_ty.spells.spell.context;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spell.SpellFunction;
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
    protected Map<String, Label> labels;
    protected int jumpLimit;
    protected int nestLimit;

    public SpellContext(Level level, @Nullable Player owner, SpellInstance spell)
    {
        this.level = level;
        this.owner = owner;
        this.spell = spell;
        activationsList = new LinkedList<>();
        targetGroups = new HashMap<>();
        ctxVars = new HashMap<>();
        terminated = false;
        index = 0;
        labels = new HashMap<>();
        jumpLimit = SpellsConfig.ACTION_JUMP_LIMIT.get();
        nestLimit = SpellsConfig.FUNCTION_NEST_LIMIT.get();
    }
    
    public Level getLevel()
    {
        return level;
    }
    
    public void activate(String activation)
    {
        if(!activation.isEmpty() && !activationsList.contains(activation))
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

    /**
     * Moves activation status from {@code from} to {@code to} (only if {@code from} was actually active) - used to
     * translate names across a {@code call_function} boundary. A no-op if either name is empty, if they are equal,
     * or if {@code from} was not active to begin with.
     */
    public void renameActivation(String from, String to)
    {
        if(from.isEmpty() || to.isEmpty() || from.equals(to))
        {
            return;
        }

        if(isActivated(from))
        {
            deactivate(from);
            activate(to);
        }
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

    /**
     * Moves the target group stored under {@code from} to {@code to} (only if one is present under {@code from}) -
     * used to translate names across a {@code call_function} boundary. A no-op if either name is empty, if they
     * are equal, or if there was no target group under {@code from} to begin with.
     */
    public void renameTargetGroup(String from, String to)
    {
        if(from.isEmpty() || to.isEmpty() || from.equals(to))
        {
            return;
        }

        TargetGroup tg = targetGroups.remove(from);

        if(tg != null)
        {
            targetGroups.put(to, tg);
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
        if(name.isEmpty())
        {
            return false;
        }
        
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

    /**
     * Moves the variable stored under {@code from} to {@code to} (only if one is present under {@code from}) -
     * used to translate names across a {@code call_function} boundary. A no-op if either name is empty, if they
     * are equal, or if there was no variable under {@code from} to begin with.
     */
    public void renameCtxVar(String from, String to)
    {
        if(from.isEmpty() || to.isEmpty() || from.equals(to))
        {
            return;
        }

        CtxVar<?> var = ctxVars.remove(from);

        if(var != null)
        {
            ctxVars.put(to, var);
        }
    }

    public void addLabel(String label, SpellAction spellAction)
    {
        addLabel(index, label, spellAction);
    }
    
    public void addLabel(int index, String label, SpellAction spellAction)
    {
        if(label.isEmpty())
        {
            return;
        }
        
        labels.put(label, new Label(index, label, spellAction));
    }
    
    public void jumpToLabel(String label)
    {
        Label l = labels.get(label);
        
        if(l != null)
        {
            setIndex(l.getIndex());
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
        if(jumpLimit-- > 0)
        {
            this.index = index;
        }
        else if(SpellsConfig.DEBUG_SPELLS.get())
        {
            SpellsAndShields.LOGGER.info("Hard jump limit reached! Skipping jump...");
        }
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public void run()
    {
        Spell spell = this.spell.getSpell().value();
        ResourceLocation spellRl = null;

        if(SpellsConfig.DEBUG_SPELLS.get())
        {
            spellRl = Spells.getRegistry(level).getKey(spell);
            SpellsAndShields.LOGGER.info("Running spell " + spellRl);
        }

        runActions(spell.getSpellActions());

        if(SpellsConfig.DEBUG_SPELLS.get())
        {
            SpellsAndShields.LOGGER.info("Finished running spell " + spellRl);
            SpellsAndShields.LOGGER.info("-".repeat(50));
            SpellsAndShields.LOGGER.info("-".repeat(50));
        }

        terminate(); // make sure this context is not run again
    }

    /**
     * Runs {@code function} as a nested, contained sub-run for a {@code call_function} call: same shared context
     * (activations/variables/target groups carry across exactly as translated by the caller beforehand), but its
     * own {@code index}/labels so jumps inside the function can never land in - or be landed on from - the
     * caller's own action list, which is a different list with unrelated indices. {@link #terminate()} is NOT
     * re-guarded here: if the function calls it, {@link #isTerminated()} stays true once this returns, and the
     * caller's own loop (which checks it right after every action, same as this one does) stops too - termination
     * is meant to cascade all the way out, not stay contained to whichever call is currently innermost.
     * <p>
     * {@link SpellFunction#getParameters()} are initialized first, after whatever the caller's own input maps
     * already renamed in - since {@link #initCtxVar(CtxVar)} unconditionally overwrites, this means the
     * function's own declared starting values always win for its own internal names.
     * <p>
     * Gated by {@link SpellsConfig#FUNCTION_NEST_LIMIT} to prevent unbounded/infinite recursion (eg. a function
     * that calls itself). Returns {@code false} without running anything if the limit is already exhausted.
     */
    public boolean runNestedActions(SpellFunction function)
    {
        if(nestLimit-- <= 0)
        {
            if(SpellsConfig.DEBUG_SPELLS.get())
            {
                SpellsAndShields.LOGGER.info("Hard function nest limit reached! Skipping function call...");
            }
            return false;
        }

        int savedIndex = index;
        Map<String, Label> savedLabels = labels;
        labels = new HashMap<>();

        function.getParameters().forEach(this::initCtxVar);
        runActions(function.getActions());

        labels = savedLabels;
        index = savedIndex;

        return true;
    }

    private void runActions(List<SpellAction> actions)
    {
        if(SpellsConfig.DEBUG_SPELLS.get())
        {
            SpellsAndShields.LOGGER.info("-".repeat(50));
            SpellsAndShields.LOGGER.info("Initial state:");
            debugActivations();
            debugTargetGroups();
            debugCtxVars();
            debugLabels();
            SpellsAndShields.LOGGER.info("-".repeat(50));
        }

        for(index = 0; index < actions.size() && index >= 0; index++)
        {
            SpellAction spellAction = actions.get(index);

            if(spellAction.doActivate(this))
            {
                ResourceLocation actionRl = null;

                if(SpellsConfig.DEBUG_SPELLS.get())
                {
                    actionRl = SpellActionTypes.REGISTRY.getKey(spellAction.getType());
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
                    debugLabels();
                    SpellsAndShields.LOGGER.info("-".repeat(50));
                }

                if(isTerminated())
                {
                    break;
                }
            }
        }
    }
    
    public void debugCtxVars()
    {
        SpellsAndShields.LOGGER.info("  Context variables:");
        for(CtxVar<?> v : ctxVars.values())
        {
            SpellsAndShields.LOGGER.info("   - " + CtxVarTypes.REGISTRY.getKey(v.getType()) + " " + v.getName() + " / " + v.getValue().toString());
        }
    }
    
    public void debugTargetGroups()
    {
        SpellsAndShields.LOGGER.info("  Target groups:");
        for(Map.Entry<String, TargetGroup> entry : targetGroups.entrySet())
        {
            SpellsAndShields.LOGGER.info("    " + entry.getKey() + " / " + entry.getValue().size() + ":");
            entry.getValue().forEachTarget(target -> SpellsAndShields.LOGGER.info("      - " + TargetTypes.REGISTRY.getKey(target.type)));
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
    
    public void debugLabels()
    {
        SpellsAndShields.LOGGER.info("  Labels:");
        for(Label l : labels.values())
        {
            SpellsAndShields.LOGGER.info("   - " + l.label);
        }
    }
    
    private static class Label
    {
        private int index;
        public final String label;
        public final SpellAction spellAction;
        
        public Label(int index, String label, SpellAction spellAction)
        {
            this.index = index;
            this.label = label;
            this.spellAction = spellAction;
        }
        
        public int getIndex()
        {
            return index;
        }
        
        public void setIndex(int index)
        {
            this.index = index;
        }
    }
}

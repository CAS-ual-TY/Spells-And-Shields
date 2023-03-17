package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class SpellInstance
{
    private Holder<Spell> spell;
    private List<CtxVar<?>> variables;
    
    private SpellNodeId nodeId;
    
    public SpellInstance(Holder<Spell> spell, List<CtxVar<?>> variables)
    {
        this.spell = spell;
        this.variables = variables;
        nodeId = null;
    }
    
    public SpellInstance(Holder<Spell> spell)
    {
        this(spell, new LinkedList<>());
    }
    
    public <T> SpellInstance addVariable(CtxVar<T> ctxVar)
    {
        variables.add(ctxVar);
        return this;
    }
    
    public void initId(SpellNodeId nodeId)
    {
        this.nodeId = nodeId;
    }
    
    public Holder<Spell> getSpell()
    {
        return spell;
    }
    
    public List<CtxVar<?>> getVariables()
    {
        return variables;
    }
    
    public SpellNodeId getNodeId()
    {
        return nodeId;
    }
    
    public void run(Player owner, String activation)
    {
        run(owner.level, owner, activation);
    }
    
    public void run(Level level, @Nullable Player owner, String activation)
    {
        run(level, owner, activation, (ctx) -> {});
    }
    
    public void run(Level level, @Nullable Player owner, String activation, Consumer<SpellContext> consumer)
    {
        SpellContext ctx = initializeContext(level, owner, activation);
        consumer.accept(ctx);
        this.spell.get().run(ctx);
    }
    
    public SpellContext initializeContext(Level level, @Nullable Player owner, String activation)
    {
        SpellContext ctx = new SpellContext(level, owner, this);
        
        ctx.activate(activation);
        ctx.initCtxVar(new CtxVar<>(CtxVarTypes.DOUBLE.get(), BuiltinVariables.MANA_COST.name, (double) spell.get().getManaCost()));
        
        if(owner != null)
        {
            ctx.getOrCreateTargetGroup(BuiltinTargetGroups.OWNER.targetGroup).addTargets(Target.of(owner));
        }
        
        spell.get().getParameters().forEach(ctx::initCtxVar);
        variables.forEach(ctx::initCtxVar);
        
        return ctx;
    }
    
    public SpellInstance copy()
    {
        return new SpellInstance(spell, new LinkedList<>(variables));
    }
}

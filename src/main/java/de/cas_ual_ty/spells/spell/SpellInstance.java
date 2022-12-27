package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVar;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.core.Holder;

import java.util.LinkedList;
import java.util.List;

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
    
    public void activate(SpellHolder spellHolder)
    {
        activate(spellHolder, BuiltinActivations.ACTIVE.activation);
    }
    
    public void activate(SpellHolder spellHolder, String activation)
    {
        this.spell.get().run(initializeContext(spellHolder, activation));
    }
    
    public SpellContext initializeContext(SpellHolder spellHolder, String activation)
    {
        SpellContext ctx = new SpellContext(spellHolder.getPlayer().level, spellHolder, this);
        ctx.activate(activation);
        ctx.getOrCreateTargetGroup(BuiltinTargetGroups.OWNER.targetGroup).addTargets(Target.of(spellHolder.getPlayer()));
        ctx.initCtxVar(new CtxVar<>(BuiltinVariables.MANA_COST.type.get(), BuiltinVariables.MANA_COST.name, (double) spell.get().getManaCost()));
        variables.forEach(ctx::initCtxVar);
        return ctx;
    }
    
    public SpellInstance copy()
    {
        return new SpellInstance(spell, new LinkedList<>(variables));
    }
}

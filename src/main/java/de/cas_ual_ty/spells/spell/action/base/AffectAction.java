package de.cas_ual_ty.spells.spell.action.base;

import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;

public abstract class AffectAction extends SpellAction
{
    protected String targets;
    
    public AffectAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AffectAction(SpellActionType<?> type, String activation, String targets)
    {
        super(type, activation);
        this.targets = targets;
    }
    
    public String getTargets()
    {
        return targets;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.forTargetGroup(targets, targetGroup -> targetGroup.forEachTarget(t -> affectTarget(ctx, targetGroup, t)));
    }
    
    public abstract void affectTarget(SpellContext ctx, TargetGroup group, Target t);
}

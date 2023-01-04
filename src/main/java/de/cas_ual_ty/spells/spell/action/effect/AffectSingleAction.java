package de.cas_ual_ty.spells.spell.action.effect;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;

public abstract class AffectSingleAction extends AffectAction
{
    public AffectSingleAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AffectSingleAction(SpellActionType<?> type, String activation, String targets)
    {
        super(type, activation, targets);
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, Target t)
    {
        if(group.isSingleTarget())
        {
            affectSingleTarget(ctx, group, t);
        }
    }
    
    public abstract void affectSingleTarget(SpellContext ctx, TargetGroup group, Target t);
}

package de.cas_ual_ty.spells.spell.action.effect;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;

public abstract class AffectSingleTypeAction<T extends Target> extends AffectTypeAction<T>
{
    public AffectSingleTypeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AffectSingleTypeAction(SpellActionType<?> type, String activation, String targets)
    {
        super(type, activation, targets);
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, T t)
    {
        if(group.getTargets().size() == 1)
        {
            affectSingleTarget(ctx, group, t);
        }
    }
    
    public abstract void affectSingleTarget(SpellContext ctx, TargetGroup group, T t);
}

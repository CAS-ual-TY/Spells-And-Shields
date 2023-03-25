package de.cas_ual_ty.spells.spell.action.base;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;

public abstract class FilterTargetsAction extends SrcDstTargetAction
{
    public FilterTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public FilterTargetsAction(SpellActionType<?> type, String activation, String dst, String src)
    {
        super(type, activation, dst, src);
    }
    
    protected abstract boolean acceptTarget(SpellContext ctx, Target target);
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        source.forEachTarget(t ->
        {
            if(acceptTarget(ctx, t))
            {
                destination.addTargets(t);
            }
        });
    }
}

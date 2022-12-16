package de.cas_ual_ty.spells.spell.action.target;

import de.cas_ual_ty.spells.spell.action.BaseSpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;

public abstract class DstTargetAction extends BaseSpellAction
{
    protected String dst;
    
    public DstTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public DstTargetAction(SpellActionType<?> type, String activation, String dst)
    {
        super(type, activation);
        this.dst = dst;
    }
    
    public String getDst()
    {
        return dst;
    }
    
    @Override
    public void wasActivated(SpellContext ctx)
    {
        findTargets(ctx, ctx.getOrCreateTargetGroup(dst));
    }
    
    public abstract void findTargets(SpellContext ctx, TargetGroup destination);
}

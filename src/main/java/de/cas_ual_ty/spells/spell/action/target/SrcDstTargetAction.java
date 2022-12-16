package de.cas_ual_ty.spells.spell.action.target;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;

public abstract class SrcDstTargetAction extends DstTargetAction
{
    protected String src;
    
    public SrcDstTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SrcDstTargetAction(SpellActionType<?> type, String activation, String dst, String src)
    {
        super(type, activation, dst);
        this.src = src;
    }
    
    public String getSrc()
    {
        return src;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup destination)
    {
        TargetGroup source = ctx.getTargetGroup(src);
        
        if(source != null)
        {
            findTargets(ctx, source, destination);
        }
    }
    
    public abstract void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination);
}

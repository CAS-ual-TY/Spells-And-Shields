package de.cas_ual_ty.spells.spell.action.target;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;

public class CopyAction extends SrcDstTargetAction
{
    public CopyAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public CopyAction(SpellActionType<?> type, String activation, String dest, String src)
    {
        super(type, activation, dest, src);
        this.src = src;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        destination.addTargets(source.getTargets());
    }
}

package de.cas_ual_ty.spells.spell.action.target;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;

public class CopyTargetsAction extends SrcDstTargetAction
{
    public CopyTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public CopyTargetsAction(SpellActionType<?> type, String activation, String dest, String src)
    {
        super(type, activation, dest, src);
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        destination.addTargets(source.getTargets());
    }
}

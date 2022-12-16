package de.cas_ual_ty.spells.spell.action.target;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;

public class PickAction extends CopyAction
{
    protected boolean remove;
    protected boolean random;
    
    public PickAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public PickAction(SpellActionType<?> type, String activation, String dst, String src, boolean remove, boolean random)
    {
        super(type, activation, dst, src);
        this.remove = remove;
        this.random = random;
    }
    
    public boolean getRemove()
    {
        return remove;
    }
    
    public boolean getRandom()
    {
        return random;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        if(source == null || source.getTargets().isEmpty())
        {
            return;
        }
        
        int pick = random ? ctx.level.getRandom().nextInt(source.getTargets().size()) : 0;
        Target t = remove ? source.getTargets().remove(pick) : source.getTargets().get(pick);
        
        destination.addTargets(t);
    }
}

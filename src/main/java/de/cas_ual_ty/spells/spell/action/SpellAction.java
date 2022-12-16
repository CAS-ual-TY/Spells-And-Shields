package de.cas_ual_ty.spells.spell.action;

import de.cas_ual_ty.spells.spell.context.SpellContext;

public abstract class SpellAction
{
    public final SpellActionType<?> type;
    
    public SpellAction(SpellActionType<?> type)
    {
        this.type = type;
    }
    
    public SpellActionType<?> getType()
    {
        return type;
    }
    
    public abstract void doAction(SpellContext ctx);
}

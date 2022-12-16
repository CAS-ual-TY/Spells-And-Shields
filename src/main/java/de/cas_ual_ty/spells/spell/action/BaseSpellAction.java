package de.cas_ual_ty.spells.spell.action;

import de.cas_ual_ty.spells.spell.context.SpellContext;

public abstract class BaseSpellAction extends SpellAction
{
    protected String activation;
    
    public BaseSpellAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public BaseSpellAction(SpellActionType<?> type, String activation)
    {
        this(type);
        this.activation = activation;
    }
    
    public String getActivations()
    {
        return activation;
    }
    
    @Override
    public void doAction(SpellContext ctx)
    {
        if(doActivate(ctx))
        {
            wasActivated(ctx);
        }
    }
    
    protected boolean doActivate(SpellContext ctx)
    {
        return activation.isEmpty() || ctx.isActivated(activation);
    }
    
    protected abstract void wasActivated(SpellContext ctx);
}

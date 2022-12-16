package de.cas_ual_ty.spells.spell.action.effect;

import de.cas_ual_ty.spells.spell.action.BaseSpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;

public abstract class AffectTypeAction<T extends Target> extends BaseSpellAction
{
    protected String targets;
    protected ITargetType<T> targetType;
    
    public AffectTypeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public AffectTypeAction(SpellActionType<?> type, String activation, String targets, ITargetType<T> targetType)
    {
        super(type, activation);
        this.targets = targets;
        this.targetType = targetType;
    }
    
    public String getTargets()
    {
        return targets;
    }
    
    public ITargetType<?> getTargetType()
    {
        return targetType;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        ctx.forTargetGroup(targets, targetGroup -> targetGroup.forEachType(targetType, t -> affectTarget(ctx, targetType.asType(t))));
    }
    
    public abstract void affectTarget(SpellContext ctx, T t);
}

package de.cas_ual_ty.spells.spell.action.base;

import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.CtxVarType;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public abstract class GetTargetAttributeAction<T extends Target> extends AffectSingleTypeAction<T>
{
    protected List<TargetAttribute<T, ?>> targetAttributes;
    protected List<VariableAttribute<T, ?>> variableAttributes;
    
    public GetTargetAttributeAction(SpellActionType<?> type)
    {
        super(type);
        targetAttributes = new LinkedList<>();
        variableAttributes = new LinkedList<>();
    }
    
    public GetTargetAttributeAction(SpellActionType<?> type, String activation, String targets)
    {
        super(type, activation, targets);
        targetAttributes = new LinkedList<>();
        variableAttributes = new LinkedList<>();
    }
    
    protected <C extends Target> void addTargetAttribute(Function<T, C> getter, String targetGroup)
    {
        targetAttributes.add(new TargetAttribute<>(getter, targetGroup));
    }
    
    protected <C> void addVariableAttribute(Function<T, C> getter, CtxVarType<C> varType, String varName)
    {
        variableAttributes.add(new VariableAttribute<>(getter, varType, varName));
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, T t)
    {
        for(TargetAttribute<T, ?> attribute : targetAttributes)
        {
            attribute.apply(ctx, t);
        }
        
        for(VariableAttribute<T, ?> attribute : variableAttributes)
        {
            attribute.apply(ctx, t);
        }
    }
    
    protected static record TargetAttribute<T extends Target, C extends Target>(Function<T, C> getter,
                                                                                String targetGroup)
    {
        public void apply(SpellContext ctx, T t)
        {
            C c = getter.apply(t);
            if(c != null)
            {
                ctx.getOrCreateTargetGroup(targetGroup).addTargets(c);
            }
        }
    }
    
    protected static record VariableAttribute<T extends Target, C>(Function<T, C> getter, CtxVarType<C> varType,
                                                                   String varName)
    {
        public void apply(SpellContext ctx, T t)
        {
            C c = getter.apply(t);
            if(c != null)
            {
                ctx.setCtxVar(varType, varName, c);
            }
        }
    }
}

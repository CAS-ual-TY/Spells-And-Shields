package de.cas_ual_ty.spells.spell.action.variable;

import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;

import java.util.List;

public class GetEntityPositionDirectionAction extends GetTargetAttributeAction<EntityTarget>
{
    public static final List<TargetAttribute<EntityTarget, ?>> ATTRIBUTES = List.of(
            new TargetAttribute<>(e -> e.getEntity().position(), CtxVarTypes.VEC3.get(), "position"),
            new TargetAttribute<>(e -> e.getEntity().getLookAngle(), CtxVarTypes.VEC3.get(), "direction")
    );
    
    public GetEntityPositionDirectionAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetEntityPositionDirectionAction(SpellActionType<?> type, String activation, String targets)
    {
        super(type, activation, targets);
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    @Override
    public List<TargetAttribute<EntityTarget, ?>> getAttributes()
    {
        return ATTRIBUTES;
    }
}

package de.cas_ual_ty.spells.spell.action.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.FilterTargetsAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class FilterEnemyTargetsAction extends FilterTargetsAction
{
    public static Codec<FilterEnemyTargetsAction> makeCodec(SpellActionType<FilterEnemyTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec(),
                moveCodec(),
                Codec.STRING.fieldOf(ParamNames.singleTarget("reference")).forGetter(FilterEnemyTargetsAction::getReference)
        ).apply(instance, (activation, dst, src, move, reference) -> new FilterEnemyTargetsAction(type, activation, dst, src, move, reference)));
    }

    public static FilterEnemyTargetsAction make(Object activation, Object dst, Object src, DynamicCtxVar<Boolean> move, Object reference)
    {
        return new FilterEnemyTargetsAction(SpellActionTypes.FILTER_ENEMY_TARGETS.get(), activation.toString(), dst.toString(), src.toString(), move, reference.toString());
    }

    protected String reference;

    public FilterEnemyTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }

    public FilterEnemyTargetsAction(SpellActionType<?> type, String activation, String dst, String src, DynamicCtxVar<Boolean> move, String reference)
    {
        super(type, activation, dst, src, move);
        this.reference = reference;
    }

    public String getReference()
    {
        return reference;
    }

    @Override
    protected boolean acceptTarget(SpellContext ctx, Target target)
    {
        if(!(target instanceof EntityTarget entityTarget))
        {
            return false;
        }

        TargetGroup referenceGroup = ctx.getTargetGroup(reference);

        if(referenceGroup == null)
        {
            return false;
        }

        return referenceGroup.getSingleTarget()
                .filter(t -> t instanceof EntityTarget)
                .map(t -> ((EntityTarget) t).getEntity())
                .map(refEntity -> !entityTarget.getEntity().isAlliedTo(refEntity))
                .orElse(false);
    }
}

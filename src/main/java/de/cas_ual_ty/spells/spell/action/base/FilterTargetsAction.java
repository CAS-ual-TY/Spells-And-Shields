package de.cas_ual_ty.spells.spell.action.base;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

import java.util.LinkedList;
import java.util.List;

public abstract class FilterTargetsAction extends SrcDstTargetAction
{
    public static <T extends FilterTargetsAction> RecordCodecBuilder<T, DynamicCtxVar<Boolean>> moveCodec()
    {
        return CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("move")).forGetter(FilterTargetsAction::getMove);
    }

    protected DynamicCtxVar<Boolean> move;

    public FilterTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }

    public FilterTargetsAction(SpellActionType<?> type, String activation, String dst, String src, DynamicCtxVar<Boolean> move)
    {
        super(type, activation, dst, src);
        this.move = move;
    }

    public DynamicCtxVar<Boolean> getMove()
    {
        return move;
    }

    protected abstract boolean acceptTarget(SpellContext ctx, Target target);

    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        if(source == destination)
        {
            return;
        }

        boolean doMove = move.getValue(ctx).orElse(false);
        List<Target> remaining = doMove ? new LinkedList<>() : null;

        source.forEachTarget(t ->
        {
            if(acceptTarget(ctx, t))
            {
                destination.addTargets(t);
            }
            else if(doMove)
            {
                remaining.add(t);
            }
        });

        if(doMove)
        {
            source.clear();
            source.addTargets(remaining);
        }
    }
}

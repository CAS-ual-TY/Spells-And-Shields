package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.FilterTargetsAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class FilterLivingTargetsAction extends FilterTargetsAction
{
    public static Codec<FilterLivingTargetsAction> makeCodec(SpellActionType<FilterLivingTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec(),
                moveCodec()
        ).apply(instance, (activation, dst, src, move) -> new FilterLivingTargetsAction(type, activation, dst, src, move)));
    }

    public static FilterLivingTargetsAction make(Object activation, Object dst, Object src, DynamicCtxVar<Boolean> move)
    {
        return new FilterLivingTargetsAction(SpellActionTypes.FILTER_LIVING_TARGETS.get(), activation.toString(), dst.toString(), src.toString(), move);
    }

    public FilterLivingTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }

    public FilterLivingTargetsAction(SpellActionType<?> type, String activation, String dst, String src, DynamicCtxVar<Boolean> move)
    {
        super(type, activation, dst, src, move);
    }

    @Override
    protected boolean acceptTarget(SpellContext ctx, Target target)
    {
        return TargetTypes.LIVING_ENTITY.get().isType(target);
    }
}

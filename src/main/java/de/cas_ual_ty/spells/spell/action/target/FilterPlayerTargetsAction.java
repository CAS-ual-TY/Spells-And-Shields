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

public class FilterPlayerTargetsAction extends FilterTargetsAction
{
    public static Codec<FilterPlayerTargetsAction> makeCodec(SpellActionType<FilterPlayerTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec(),
                moveCodec()
        ).apply(instance, (activation, dst, src, move) -> new FilterPlayerTargetsAction(type, activation, dst, src, move)));
    }

    public static FilterPlayerTargetsAction make(Object activation, Object dst, Object src, DynamicCtxVar<Boolean> move)
    {
        return new FilterPlayerTargetsAction(SpellActionTypes.FILTER_PLAYER_TARGETS.get(), activation.toString(), dst.toString(), src.toString(), move);
    }

    public FilterPlayerTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }

    public FilterPlayerTargetsAction(SpellActionType<?> type, String activation, String dst, String src, DynamicCtxVar<Boolean> move)
    {
        super(type, activation, dst, src, move);
    }

    @Override
    protected boolean acceptTarget(SpellContext ctx, Target target)
    {
        return TargetTypes.PLAYER.get().isType(target);
    }
}

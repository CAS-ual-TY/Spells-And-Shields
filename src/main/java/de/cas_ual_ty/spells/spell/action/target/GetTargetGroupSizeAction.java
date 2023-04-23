package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.DstTargetAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetTargetGroupSizeAction extends DstTargetAction
{
    public static Codec<GetTargetGroupSizeAction> makeCodec(SpellActionType<GetTargetGroupSizeAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec("source"),
                Codec.STRING.fieldOf(ParamNames.varResult()).forGetter(GetTargetGroupSizeAction::getResult)
        ).apply(instance, (activation, dst, result) -> new GetTargetGroupSizeAction(type, activation, dst, result)));
    }
    
    public static GetTargetGroupSizeAction make(Object activation, Object dst, String result)
    {
        return new GetTargetGroupSizeAction(SpellActionTypes.GET_TARGET_GROUP_SIZE.get(), activation.toString(), dst.toString(), result);
    }
    
    protected String result;
    
    public GetTargetGroupSizeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetTargetGroupSizeAction(SpellActionType<?> type, String activation, String dst, String result)
    {
        super(type, activation, dst);
        this.result = result;
    }
    
    public String getResult()
    {
        return result;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup destination)
    {
        ctx.setCtxVar(CtxVarTypes.INT.get(), result, destination.size());
    }
}

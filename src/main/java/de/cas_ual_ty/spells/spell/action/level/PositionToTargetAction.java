package de.cas_ual_ty.spells.spell.action.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.phys.Vec3;

public class PositionToTargetAction extends SpellAction
{
    public static Codec<PositionToTargetAction> makeCodec(SpellActionType<PositionToTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("result")).forGetter(PositionToTargetAction::getResult),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("position")).forGetter(PositionToTargetAction::getPosition)
        ).apply(instance, (activation, result, position) -> new PositionToTargetAction(type, activation, result, position)));
    }
    
    public static PositionToTargetAction make(String activation, String result, DynamicCtxVar<Vec3> position)
    {
        return new PositionToTargetAction(SpellActionTypes.POSITION_TO_TARGET.get(), activation, result, position);
    }
    
    protected String result;
    protected DynamicCtxVar<Vec3> position;
    
    public PositionToTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public PositionToTargetAction(SpellActionType<?> type, String activation, String result, DynamicCtxVar<Vec3> position)
    {
        super(type, activation);
        this.result = result;
        this.position = position;
    }
    
    public String getResult()
    {
        return result;
    }
    
    public DynamicCtxVar<Vec3> getPosition()
    {
        return position;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        position.getValue(ctx).ifPresent(position ->
        {
            TargetGroup newGroup = ctx.getOrCreateTargetGroup(result);
            newGroup.addTargets(Target.of(ctx.level, position));
        });
    }
}

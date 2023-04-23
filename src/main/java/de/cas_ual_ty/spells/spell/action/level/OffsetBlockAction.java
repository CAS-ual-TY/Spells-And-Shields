package de.cas_ual_ty.spells.spell.action.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.phys.Vec3;

public class OffsetBlockAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<OffsetBlockAction> makeCodec(SpellActionType<OffsetBlockAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("result")).forGetter(OffsetBlockAction::getResult),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("offset")).forGetter(OffsetBlockAction::getOffset)
        ).apply(instance, (activation, multiTargets, result, offset) -> new OffsetBlockAction(type, activation, multiTargets, result, offset)));
    }
    
    public static OffsetBlockAction make(Object activation, Object multiTargets, String result, DynamicCtxVar<Vec3> offset)
    {
        return new OffsetBlockAction(SpellActionTypes.OFFSET_BLOCK.get(), activation.toString(), multiTargets.toString(), result, offset);
    }
    
    protected String result;
    protected DynamicCtxVar<Vec3> offset;
    
    public OffsetBlockAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public OffsetBlockAction(SpellActionType<?> type, String activation, String multiTargets, String result, DynamicCtxVar<Vec3> offset)
    {
        super(type, activation, multiTargets);
        this.result = result;
        this.offset = offset;
    }
    
    public String getResult()
    {
        return result;
    }
    
    public DynamicCtxVar<Vec3> getOffset()
    {
        return offset;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        offset.getValue(ctx).ifPresent(offset ->
        {
            TargetGroup newGroup = ctx.getOrCreateTargetGroup(result);
            newGroup.addTargets(Target.of(positionTarget.getLevel(), positionTarget.getPosition().add(offset)));
        });
    }
}

package de.cas_ual_ty.spells.spell.action.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class GetBlockAttributesAction extends AffectSingleTypeAction<PositionTarget>
{
    public static Codec<GetBlockAttributesAction> makeCodec(SpellActionType<GetBlockAttributesAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.var("is_air")).forGetter(GetBlockAttributesAction::getIsAir),
                Codec.STRING.fieldOf(ParamNames.var("is_fluid")).forGetter(GetBlockAttributesAction::getIsFluid),
                Codec.STRING.fieldOf(ParamNames.var("has_collider")).forGetter(GetBlockAttributesAction::getHasCollider)
        ).apply(instance, (activation, singleTarget, isAir, isFluid, hasCollider) -> new GetBlockAttributesAction(type, activation, singleTarget, isAir, isFluid, hasCollider)));
    }
    
    public static GetBlockAttributesAction make(Object activation, Object singleTarget, String isAir, String isFluid, String hasCollider)
    {
        return new GetBlockAttributesAction(SpellActionTypes.GET_BLOCK_ATTRIBUTES.get(), activation.toString(), singleTarget.toString(), isAir, isFluid, hasCollider);
    }
    
    protected String isAir;
    protected String isFluid;
    protected String hasCollider;
    
    public GetBlockAttributesAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetBlockAttributesAction(SpellActionType<?> type, String activation, String singleTarget, String isAir, String isFluid, String hasCollider)
    {
        super(type, activation, singleTarget);
        this.isAir = isAir;
        this.isFluid = isFluid;
        this.hasCollider = hasCollider;
    }
    
    public String getIsAir()
    {
        return isAir;
    }
    
    public String getIsFluid()
    {
        return isFluid;
    }
    
    public String getHasCollider()
    {
        return hasCollider;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        BlockState blockState = ctx.level.getBlockState(positionTarget.getBlockPos());
        FluidState fluidState = blockState.getFluidState();
        
        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), isAir, blockState.isAir());
        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), isFluid, !fluidState.isEmpty());
        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), hasCollider, !blockState.getCollisionShape(ctx.level, positionTarget.getBlockPos()).isEmpty());
    }
}

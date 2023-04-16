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
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;

public class GetFluidAction extends AffectSingleTypeAction<PositionTarget>
{
    public static Codec<GetFluidAction> makeCodec(SpellActionType<GetFluidAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.var("fluid")).forGetter(GetFluidAction::getFluid),
                Codec.STRING.fieldOf(ParamNames.var("fluid_state")).forGetter(GetFluidAction::getFluidState),
                Codec.STRING.fieldOf(ParamNames.var("is_fluid")).forGetter(GetFluidAction::getIsFluid)
        ).apply(instance, (activation, singleTarget, fluid, fluidState, isFluid) -> new GetFluidAction(type, activation, singleTarget, fluid, fluidState, isFluid)));
    }
    
    public static GetFluidAction make(String activation, String singleTarget, String fluid, String fluidState, String isFluid)
    {
        return new GetFluidAction(SpellActionTypes.GET_FLUID.get(), activation, singleTarget, fluid, fluidState, isFluid);
    }
    
    protected String fluid;
    protected String fluidState;
    protected String isFluid;
    
    public GetFluidAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetFluidAction(SpellActionType<?> type, String activation, String singleTarget, String fluid, String fluidState, String isFluid)
    {
        super(type, activation, singleTarget);
        this.fluid = fluid;
        this.fluidState = fluidState;
        this.isFluid = isFluid;
    }
    
    public String getFluid()
    {
        return fluid;
    }
    
    public String getFluidState()
    {
        return fluidState;
    }
    
    public String getIsFluid()
    {
        return isFluid;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        FluidState fluidState = ctx.level.getFluidState(positionTarget.getBlockPos());
        ResourceLocation id = ForgeRegistries.FLUID_TYPES.get().getKey(fluidState.getFluidType());
        
        if(id == null)
        {
            return;
        }
        
        if(!fluidState.isEmpty())
        {
            ctx.setCtxVar(CtxVarTypes.TAG.get(), this.fluidState, SpellsUtil.stateToTag(fluidState));
        }
        
        ctx.setCtxVar(CtxVarTypes.STRING.get(), fluid, id.toString());
        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), isFluid, !fluidState.isEmpty());
    }
}

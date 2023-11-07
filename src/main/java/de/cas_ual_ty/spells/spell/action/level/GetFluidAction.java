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
import net.neoforged.neoforge.registries.ForgeRegistries;

public class GetFluidAction extends AffectSingleTypeAction<PositionTarget>
{
    public static Codec<GetFluidAction> makeCodec(SpellActionType<GetFluidAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.var("fluid")).forGetter(GetFluidAction::getFluid),
                Codec.STRING.fieldOf(ParamNames.var("fluid_state")).forGetter(GetFluidAction::getFluidState),
                Codec.STRING.fieldOf(ParamNames.var("is_fluid")).forGetter(GetFluidAction::getIsFluid),
                Codec.STRING.fieldOf(ParamNames.var("is_source")).forGetter(GetFluidAction::getIsSource)
        ).apply(instance, (activation, singleTarget, fluid, fluidState, isFluid, isSource) -> new GetFluidAction(type, activation, singleTarget, fluid, fluidState, isFluid, isSource)));
    }
    
    public static GetFluidAction make(Object activation, Object singleTarget, String fluid, String fluidState, String isFluid, String isSource)
    {
        return new GetFluidAction(SpellActionTypes.GET_FLUID.get(), activation.toString(), singleTarget.toString(), fluid, fluidState, isFluid, isSource);
    }
    
    protected String fluid;
    protected String fluidState;
    protected String isFluid;
    protected String isSource;
    
    public GetFluidAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetFluidAction(SpellActionType<?> type, String activation, String singleTarget, String fluid, String fluidState, String isFluid, String isSource)
    {
        super(type, activation, singleTarget);
        this.fluid = fluid;
        this.fluidState = fluidState;
        this.isFluid = isFluid;
        this.isSource = isSource;
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
    
    public String getIsSource()
    {
        return isSource;
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
        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), isSource, fluidState.isSource());
    }
}

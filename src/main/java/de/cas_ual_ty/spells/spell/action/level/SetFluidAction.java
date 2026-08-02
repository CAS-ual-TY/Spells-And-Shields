package de.cas_ual_ty.spells.spell.action.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.SpellsUtil;
import net.minecraft.core.registries.BuiltInRegistries;

public class SetFluidAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<SetFluidAction> makeCodec(SpellActionType<SetFluidAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("fluid")).forGetter(SetFluidAction::getFluid)
        ).apply(instance, (activation, multiTargets, fluid) -> new SetFluidAction(type, activation, multiTargets, fluid)));
    }

    public static SetFluidAction make(Object activation, Object multiTargets, DynamicCtxVar<String> fluid)
    {
        return new SetFluidAction(SpellActionTypes.SET_FLUID.get(), activation.toString(), multiTargets.toString(), fluid);
    }

    protected DynamicCtxVar<String> fluid;

    public SetFluidAction(SpellActionType<?> type)
    {
        super(type);
    }

    public SetFluidAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> fluid)
    {
        super(type, activation, multiTargets);
        this.fluid = fluid;
    }

    public DynamicCtxVar<String> getFluid()
    {
        return fluid;
    }

    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        SpellsUtil.stringToObject(ctx, fluid, BuiltInRegistries.FLUID).ifPresent(fluid ->
        {
            ctx.level.setBlockAndUpdate(positionTarget.getBlockPos(), fluid.defaultFluidState().createLegacyBlock());
        });
    }
}

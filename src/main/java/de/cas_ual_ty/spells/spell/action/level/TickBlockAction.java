package de.cas_ual_ty.spells.spell.action.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class TickBlockAction extends AffectSingleTypeAction<PositionTarget>
{
    public static Codec<TickBlockAction> makeCodec(SpellActionType<TickBlockAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("duration")).forGetter(TickBlockAction::getDuration)
        ).apply(instance, (activation, multiTargets, duration) -> new TickBlockAction(type, activation, multiTargets, duration)));
    }
    
    public static TickBlockAction make(String activation, String multiTargets, DynamicCtxVar<Integer> duration)
    {
        return new TickBlockAction(SpellActionTypes.TICK_BLOCK.get(), activation, multiTargets, duration);
    }
    
    protected DynamicCtxVar<Integer> duration;
    
    public TickBlockAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TickBlockAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> duration)
    {
        super(type, activation, multiTargets);
        this.duration = duration;
    }
    
    public DynamicCtxVar<Integer> getDuration()
    {
        return duration;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        duration.getValue(ctx).ifPresent(duration ->
        {
            BlockPos pos = positionTarget.getBlockPos();
            ctx.level.scheduleTick(pos, ctx.level.getBlockState(pos).getBlock(), duration);
        });
    }
    
    public static <X extends Comparable<X>> void addPropertyToTag(BlockState blockState, CompoundTag tag, Property<X> p)
    {
        p.valueCodec().encodeStart(NbtOps.INSTANCE, p.value(blockState)).result().ifPresentOrElse(element ->
        {
            tag.put(p.getName(), element);
        }, () -> SpellsAndShields.LOGGER.error("ERROR ERROR ERROR!!!!!!!!!!! ################################"));
    }
}

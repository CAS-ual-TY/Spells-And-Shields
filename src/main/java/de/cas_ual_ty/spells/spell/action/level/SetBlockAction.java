package de.cas_ual_ty.spells.spell.action.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import net.minecraft.world.level.block.state.BlockState;

public class SetBlockAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<SetBlockAction> makeCodec(SpellActionType<SetBlockAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                BlockState.CODEC.fieldOf("block_state").forGetter(SetBlockAction::getBlockState)
        ).apply(instance, (activation, multiTargets, blockState) -> new SetBlockAction(type, activation, multiTargets, blockState)));
    }
    
    public static SetBlockAction make(String activation, String multiTargets, BlockState blockState)
    {
        return new SetBlockAction(SpellActionTypes.SET_BLOCK.get(), activation, multiTargets, blockState);
    }
    
    protected BlockState blockState;
    
    public SetBlockAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SetBlockAction(SpellActionType<?> type, String activation, String multiTargets, BlockState blockState)
    {
        super(type, activation, multiTargets);
        this.blockState = blockState;
    }
    
    public BlockState getBlockState()
    {
        return blockState;
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        ctx.level.setBlockAndUpdate(positionTarget.getBlockPos(), blockState);
    }
}

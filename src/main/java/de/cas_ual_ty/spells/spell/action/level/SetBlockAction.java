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
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;

public class SetBlockAction extends AffectTypeAction<PositionTarget>
{
    public static Codec<SetBlockAction> makeCodec(SpellActionType<SetBlockAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("block")).forGetter(SetBlockAction::getBlock),
                CtxVarTypes.COMPOUND_TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("block_state")).forGetter(SetBlockAction::getBlockState)
        ).apply(instance, (activation, multiTargets, block, blockState) -> new SetBlockAction(type, activation, multiTargets, block, blockState)));
    }
    
    public static SetBlockAction make(String activation, String multiTargets, DynamicCtxVar<String> block, DynamicCtxVar<CompoundTag> blockState)
    {
        return new SetBlockAction(SpellActionTypes.SET_BLOCK.get(), activation, multiTargets, block, blockState);
    }
    
    protected DynamicCtxVar<String> block;
    protected DynamicCtxVar<CompoundTag> blockState;
    
    public SetBlockAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SetBlockAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> block, DynamicCtxVar<CompoundTag> blockState)
    {
        super(type, activation, multiTargets);
        this.block = block;
        this.blockState = blockState;
    }
    
    public DynamicCtxVar<String> getBlock()
    {
        return block;
    }
    
    public DynamicCtxVar<CompoundTag> getBlockState()
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
        SpellsUtil.stringToObject(ctx, block, ForgeRegistries.BLOCKS).ifPresent(block ->
        {
            blockState.getValue(ctx).ifPresent(blockState ->
            {
                ctx.level.setBlockAndUpdate(positionTarget.getBlockPos(), SpellsUtil.tagToState(block, blockState));
            });
        });
    }
}

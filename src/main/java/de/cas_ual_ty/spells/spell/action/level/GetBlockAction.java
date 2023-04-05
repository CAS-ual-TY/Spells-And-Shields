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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class GetBlockAction extends AffectSingleTypeAction<PositionTarget>
{
    public static Codec<GetBlockAction> makeCodec(SpellActionType<GetBlockAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.var("block")).forGetter(GetBlockAction::getBlock),
                Codec.STRING.fieldOf(ParamNames.var("block_state")).forGetter(GetBlockAction::getBlockState),
                Codec.STRING.fieldOf(ParamNames.var("is_air")).forGetter(GetBlockAction::getIsAir)
        ).apply(instance, (activation, singleTarget, block, blockState, isAir) -> new GetBlockAction(type, activation, singleTarget, block, blockState, isAir)));
    }
    
    public static GetBlockAction make(String activation, String singleTarget, String block, String blockState, String isAir)
    {
        return new GetBlockAction(SpellActionTypes.GET_BLOCK.get(), activation, singleTarget, block, blockState, isAir);
    }
    
    protected String block;
    protected String blockState;
    protected String isAir;
    
    public GetBlockAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetBlockAction(SpellActionType<?> type, String activation, String singleTarget, String block, String blockState, String isAir)
    {
        super(type, activation, singleTarget);
        this.block = block;
        this.blockState = blockState;
        this.isAir = isAir;
    }
    
    public String getBlock()
    {
        return block;
    }
    
    public String getBlockState()
    {
        return blockState;
    }
    
    public String getIsAir()
    {
        return isAir;
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
        
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(blockState.getBlock());
        
        if(id == null)
        {
            return;
        }
        
        if(!blockState.isAir())
        {
            ctx.setCtxVar(CtxVarTypes.COMPOUND_TAG.get(), this.blockState, SpellsUtil.stateToTag(blockState));
        }
        
        ctx.setCtxVar(CtxVarTypes.STRING.get(), block, id.toString());
        ctx.setCtxVar(CtxVarTypes.BOOLEAN.get(), isAir, blockState.isAir());
    }
}

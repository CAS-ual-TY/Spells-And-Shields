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
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class PlayerHarvestBlockAction extends AffectTypeAction<PlayerTarget>
{
    public static Codec<PlayerHarvestBlockAction> makeCodec(SpellActionType<PlayerHarvestBlockAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.multiTarget("blocks")).forGetter(PlayerHarvestBlockAction::getBlocks),
                SpellsUtil.namedEnumCodec(Direction::byName, Direction::getName).fieldOf("direction").forGetter(PlayerHarvestBlockAction::getDirection)
        ).apply(instance, (activation, source, blocks, direction) -> new PlayerHarvestBlockAction(type, activation, source, blocks, direction)));
    }
    
    public static PlayerHarvestBlockAction make(String activation, String source, String blocks, Direction direction)
    {
        return new PlayerHarvestBlockAction(SpellActionTypes.PLAYER_HARVEST_BLOCK.get(), activation, source, blocks, direction);
    }
    
    protected String blocks;
    protected Direction direction;
    
    public PlayerHarvestBlockAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public PlayerHarvestBlockAction(SpellActionType<?> type, String activation, String source, String blocks, Direction direction)
    {
        super(type, activation, source);
        this.blocks = blocks;
        this.direction = direction;
    }
    
    public String getBlocks()
    {
        return blocks;
    }
    
    public Direction getDirection()
    {
        return direction;
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        if(playerTarget.getPlayer() instanceof ServerPlayer player)
        {
            Level level = ctx.level;
            
            ctx.getTargetGroup(blocks).forEachTarget(target -> TargetTypes.POSITION.get().ifType(target, positionTarget ->
            {
                BlockPos pos = positionTarget.getBlockPos();
                BlockState block = level.getBlockState(pos);
                
                PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, pos, direction);
                
                if(!event.isCanceled() &&
                        !block.isAir() && player.canInteractWith(pos, 2D) &&
                        pos.getY() <= level.getMaxBuildHeight() &&
                        level.mayInteract(player, pos) &&
                        !player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer()) &&
                        block.getDestroySpeed(level, pos) != -1F)
                {
                    player.gameMode.destroyBlock(pos);
                }
            }));
        }
    }
}

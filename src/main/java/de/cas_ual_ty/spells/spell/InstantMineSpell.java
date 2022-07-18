package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.Spell;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class InstantMineSpell extends Spell
{
    public InstantMineSpell(float manaCost)
    {
        super(manaCost);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        if(manaHolder.getPlayer() instanceof ServerPlayer player)
        {
            Level level = player.level;
            BlockHitResult blockHitResult = SpellsUtil.rayTraceBlock(level, player, 4D, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
            
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState block = level.getBlockState(pos);
            
            PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, pos, blockHitResult.getDirection());
            
            if(!event.isCanceled() &&
                    !block.isAir() && player.canInteractWith(pos, 2D) &&
                    pos.getY() <= level.getMaxBuildHeight() &&
                    level.mayInteract(player, pos) &&
                    !player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer()) &&
                    block.getDestroySpeed(level, pos) != -1.0F)
            {
                player.gameMode.destroyBlock(pos);
            }
        }
    }
}

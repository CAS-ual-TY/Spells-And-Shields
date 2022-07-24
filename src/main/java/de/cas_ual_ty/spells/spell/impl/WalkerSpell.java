package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.IEventSpell;
import de.cas_ual_ty.spells.spell.base.PassiveSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

public class WalkerSpell extends PassiveSpell implements IEventSpell
{
    private void playerTick(TickEvent.PlayerTickEvent event)
    {
        SpellHolder.getSpellHolder(event.player).ifPresent(spellHolder ->
        {
            for(int i = 0; i < SpellHolder.SPELL_SLOTS; i++)
            {
                if(spellHolder.getSpell(i) == this)
                {
                    onEntityMoved(spellHolder.getPlayer(), 1, Blocks.WATER, Material.WATER, Blocks.FROSTED_ICE.defaultBlockState());
                }
            }
        });
    }
    
    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.addListener(this::playerTick);
    }
    
    public static void onEntityMoved(LivingEntity livingEntity, int extraRadius, Block oldBlock, Material oldMaterial, BlockState newBlock)
    {
        if(livingEntity.isOnGround())
        {
            Level level = livingEntity.level;
            BlockPos pos = livingEntity.blockPosition();
            double r = Math.min(16, 2 + extraRadius);
            
            for(BlockPos currentPos : BlockPos.betweenClosed(pos.offset(-r, -1D, -r), pos.offset(r, -1D, r)))
            {
                if(currentPos.closerToCenterThan(livingEntity.position(), r))
                {
                    BlockState above = level.getBlockState(currentPos.above());
                    
                    if(above.isAir())
                    {
                        BlockState currentBlock = level.getBlockState(currentPos);
                        
                        boolean isFull = currentBlock.getBlock() == oldBlock && currentBlock.getValue(LiquidBlock.LEVEL) == 0;
                        
                        if(currentBlock.getMaterial() == oldMaterial && isFull && newBlock.canSurvive(level, currentPos) &&
                                level.isUnobstructed(newBlock, currentPos, CollisionContext.empty()) &&
                                !net.minecraftforge.event.ForgeEventFactory.onBlockPlace(livingEntity, net.minecraftforge.common.util.BlockSnapshot.create(level.dimension(), level, currentPos), net.minecraft.core.Direction.UP))
                        {
                            level.setBlockAndUpdate(currentPos, newBlock);
                            level.scheduleTick(currentPos, newBlock.getBlock(), Mth.nextInt(livingEntity.getRandom(), 60, 120));
                        }
                    }
                }
            }
            
        }
    }
}

package de.cas_ual_ty.spells.spell.base;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public interface IProjectileSpell extends ISpell
{
    // performed on both sides
    void tick(SpellProjectile entity);
    
    // performed on both sides
    default void onEntityHit(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        entity.discard();
    }
    
    // performed on both sides
    default void onBlockHit(SpellProjectile entity, BlockHitResult blockHitResult)
    {
        BlockState blockstate = entity.level.getBlockState(blockHitResult.getBlockPos());
        blockstate.onProjectileHit(entity.level, blockstate, blockHitResult, entity);
        entity.discard();
    }
    
    default int getTimeout()
    {
        return 50;
    }
    
    default void onTimeout(SpellProjectile entity)
    {
        
    }
}

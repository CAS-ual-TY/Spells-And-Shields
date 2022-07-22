package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.BiConsumer;

public interface IProjectileSpell extends ISpell
{
    // performed on both sides
    default void tick(SpellProjectile entity)
    {
    
    }
    
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
    
    default float getVelocity()
    {
        return 2F;
    }
    
    default void shootStraight(ManaHolder manaHolder, float inaccuracy, BiConsumer<SpellProjectile, ServerLevel> followUp)
    {
        SpellProjectile.shoot(manaHolder.getPlayer(), this, getVelocity(), inaccuracy, followUp);
    }
    
    default void shootStraight(ManaHolder manaHolder, BiConsumer<SpellProjectile, ServerLevel> followUp)
    {
        shootStraight(manaHolder, 0F, followUp);
    }
    
    default void shootStraight(ManaHolder manaHolder, float inaccuracy)
    {
        shootStraight(manaHolder, inaccuracy, (spellProjectile, serverLevel) -> {});
    }
    
    default void shootStraight(ManaHolder manaHolder)
    {
        shootStraight(manaHolder, 0F);
    }
    
    default void shootHoming(ManaHolder manaHolder, Entity target, BiConsumer<HomingSpellProjectile, ServerLevel> followUp)
    {
        HomingSpellProjectile.home(manaHolder.getPlayer(), this, getVelocity(), target, followUp);
    }
    
    default void shootHoming(ManaHolder manaHolder, Entity target)
    {
        shootHoming(manaHolder, target, (homingSpellProjectile, serverLevel) -> {});
    }
}

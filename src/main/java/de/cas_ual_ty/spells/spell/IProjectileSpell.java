package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
    
    default void shootStraight(ManaHolder manaHolder, float velocity, float inaccuracy, BiConsumer<SpellProjectile, ServerLevel> followUp)
    {
        SpellProjectile.shoot(manaHolder.getPlayer(), this, velocity, inaccuracy, followUp);
    }
    
    default void shootStraight(ManaHolder manaHolder, float velocity, BiConsumer<SpellProjectile, ServerLevel> followUp)
    {
        shootStraight(manaHolder, velocity, 0F, followUp);
    }
    
    default void shootStraight(ManaHolder manaHolder, BiConsumer<SpellProjectile, ServerLevel> followUp)
    {
        shootStraight(manaHolder, 2F, followUp);
    }
    
    default void shootStraight(ManaHolder manaHolder, float velocity)
    {
        shootStraight(manaHolder, velocity, (spellProjectile, serverLevel) -> {});
    }
    
    default void shootStraight(ManaHolder manaHolder)
    {
        shootStraight(manaHolder, 2F);
    }
    
    default void shootHoming(ManaHolder manaHolder, Entity target, float velocity, BiConsumer<HomingSpellProjectile, ServerLevel> followUp)
    {
        HomingSpellProjectile.home(manaHolder.getPlayer(), this, velocity, target, followUp);
    }
    
    default void shootHoming(ManaHolder manaHolder, Entity target, BiConsumer<HomingSpellProjectile, ServerLevel> followUp)
    {
        shootHoming(manaHolder, target, 2F, followUp);
    }
    
    default void shootHoming(ManaHolder manaHolder, Entity target)
    {
        shootHoming(manaHolder, target, (homingSpellProjectile, serverLevel) -> {});
    }
    
    default float getInertia()
    {
        return 1F;
    }
    
    default ParticleOptions getTrailParticle()
    {
        return ParticleTypes.POOF;
    }
}

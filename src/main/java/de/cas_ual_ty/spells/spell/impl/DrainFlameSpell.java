package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IProjectileSpell;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.spell.base.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class DrainFlameSpell extends BaseIngredientsSpell implements IProjectileSpell
{
    public DrainFlameSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        super(manaCost, handIngredients, inventoryIngredients);
    }
    
    public DrainFlameSpell(float manaCost, ItemStack handIngredient)
    {
        super(manaCost, handIngredient);
    }
    
    public DrainFlameSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public DrainFlameSpell()
    {
        super(0F);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        LivingEntity entity = manaHolder.getPlayer();
        Level level = entity.level;
        HitResult hit = SpellsUtil.rayTrace(level, entity, 50, e -> e instanceof LivingEntity, 0.5F, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
        
        if(hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit)
        {
            BlockPos pos = blockHit.getBlockPos();
            
            for(int i = 0; i < 2; i++)
            {
                BlockState blockState = level.getBlockState(pos);
                BlockPos below = pos.below();
                
                // look at the fire (first iteration)
                // or at the block below the fire (second iteration)
                if(blockState.getBlock() instanceof BaseFireBlock baseFireBlock && level.getBlockState(below).isFireSource(level, below, Direction.UP))
                {
                    level.removeBlock(pos, false);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1F, 1F);
                    
                    Vec3 position = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                    Vec3 direction = entity.position().subtract(position).normalize();
                    
                    HomingSpellProjectile.home(position, direction, entity, entity, this, 1F, (projectile, pLevel) -> {});
                    
                    return;
                }
                
                pos = pos.above();
            }
        }
    }
    
    @Override
    public void projectileTick(SpellProjectile entity)
    {
        Vec3 pos = entity.position();
        Random random = new Random();
        
        if(entity.tickCount % 4 == 0)
        {
            entity.level.addParticle(ParticleTypes.LAVA, pos.x, pos.y, pos.z, 0, 0, 0);
        }
        
        final double spread = 0.2D;
        
        for(int i = 0; i < 2; ++i)
        {
            entity.level.addParticle(ParticleTypes.FLAME, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
            entity.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
        }
    }
    
    @Override
    public ParticleOptions getProjectileParticle()
    {
        return ParticleTypes.SOUL_FIRE_FLAME;
    }
    
    @Override
    public void projectileHitEntity(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        if(entityHitResult.getEntity() instanceof LivingEntity target)
        {
            target.addEffect(new MobEffectInstance(SpellsRegistries.REPLENISHMENT_EFFECT.get(), 200));
            target.level.playSound(null, target, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1F, 1F);
            IProjectileSpell.super.projectileHitEntity(entity, entityHitResult);
        }
    }
}

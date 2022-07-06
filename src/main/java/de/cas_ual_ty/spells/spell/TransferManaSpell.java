package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.spell.base.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.base.IProjectileSpell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class TransferManaSpell extends BaseIngredientsSpell implements IProjectileSpell
{
    public TransferManaSpell(float manaCost)
    {
        super(manaCost);
    }
    
    @Override
    public void tick(SpellProjectile entity)
    {
        if(entity.level.isClientSide)
        {
            Vec3 pos = entity.position();
            Random random = new Random();
            
            if(entity.tickCount % 2 == 0)
            {
                final double spread = 0.2D;
                
                for(int i = 0; i < 3; ++i)
                {
                    entity.level.addParticle(ParticleTypes.BUBBLE, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
                }
                
                for(int i = 0; i < 2; ++i)
                {
                    entity.level.addParticle(ParticleTypes.POOF, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
                }
            }
        }
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        if(manaHolder.getPlayer().level instanceof ServerLevel level)
        {
            LivingEntity player = manaHolder.getPlayer();
            
            Vec3 position = player.getEyePosition();
            Vec3 direction = player.getViewVector(1.0F).normalize();
            
            EntityHitResult entityHit = SpellsUtil.rayTraceEntity(level, player, 25D, (entity -> entity instanceof LivingEntity), 0.5F);
            
            if(entityHit != null)
            {
                HomingSpellProjectile.home(player, this, 1.0F, entityHit.getEntity(), (projectile, level1) -> level1.playSound(null, manaHolder.getPlayer(), SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.PLAYERS, 1.0F, 1.0F));
            }
        }
    }
    
    @Override
    public void onEntityHit(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        Entity hit = entityHitResult.getEntity();
        if(hit instanceof LivingEntity livingEntity)
        {
            ManaHolder.getManaHolder(livingEntity).ifPresent(manaHolder -> manaHolder.setMana(manaHolder.getMana() - manaHolder.getExtraMana() + this.manaCost));
        }
        IProjectileSpell.super.onEntityHit(entity, entityHitResult);
    }
}

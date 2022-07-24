package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IReturnProjectileSpell;
import de.cas_ual_ty.spells.spell.base.Spell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class StealFireResistanceSpell extends Spell implements IReturnProjectileSpell
{
    public static final String KEY_DIRECTION = "Direction";
    public static final String KEY_POTION = "Effect";
    
    public StealFireResistanceSpell(float manaCost)
    {
        super(manaCost);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        IReturnProjectileSpell.super.shootStraight(manaHolder);
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
                
                for(int i = 0; i < 8; ++i)
                {
                    entity.level.addParticle(ParticleTypes.ASH, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
                }
            }
        }
    }
    
    @Override
    public boolean onEntityHitDeparture(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        if(entityHitResult.getEntity() instanceof LivingEntity livingEntity && livingEntity.hasEffect(MobEffects.FIRE_RESISTANCE))
        {
            MobEffectInstance effect = livingEntity.getEffect(MobEffects.FIRE_RESISTANCE);
            
            if(effect != null)
            {
                livingEntity.removeEffect(MobEffects.FIRE_RESISTANCE);
                entity.getSpellDataTag().put(KEY_POTION, effect.save(new CompoundTag()));
                return true;
            }
            
            entity.discard();
        }
        
        return false;
    }
    
    @Override
    public boolean onBlockHitDeparture(SpellProjectile entity, BlockHitResult blockHitResult)
    {
        return false;
    }
    
    @Override
    public void onEntityHitReturn(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        if(!entity.getSpellDataTag().contains(KEY_POTION))
        {
            entity.discard();
        }
        else if(entityHitResult.getEntity() instanceof LivingEntity target)
        {
            MobEffectInstance effect = MobEffectInstance.load(entity.getSpellDataTag().getCompound(KEY_POTION));
            
            if(effect != null)
            {
                target.addEffect(effect);
            }
            
            entity.discard();
        }
    }
}

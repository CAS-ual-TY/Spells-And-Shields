package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.HomingSpellProjectile;
import de.cas_ual_ty.spells.spell.base.IProjectileSpell;
import de.cas_ual_ty.spells.spell.base.Spell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Random;

public class StealFireResistanceSpell extends Spell implements IProjectileSpell
{
    public static final String TAG_OUTWARDS = SpellsAndShields.MOD_ID + ":" + "outwards";
    public static final String TAG_RETURN = SpellsAndShields.MOD_ID + ":" + "return";
    
    public StealFireResistanceSpell(float manaCost)
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
                
                for(int i = 0; i < 8; ++i)
                {
                    entity.level.addParticle(ParticleTypes.ASH, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
                }
            }
        }
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        SpellProjectile.shoot(manaHolder.getPlayer(), this, 2.0F, 0.0F, (projectile, level) ->
        {
            projectile.addTag(TAG_OUTWARDS);
        });
    }
    
    @Override
    public void onEntityHit(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        if(entity.getTags().contains(TAG_OUTWARDS))
        {
            if(entityHitResult.getEntity() instanceof LivingEntity livingEntity && livingEntity.hasEffect(MobEffects.FIRE_RESISTANCE))
            {
                MobEffectInstance effect = livingEntity.getEffect(MobEffects.FIRE_RESISTANCE);
                livingEntity.removeEffect(MobEffects.FIRE_RESISTANCE);
                
                if(entity.getOwner() != null && effect != null)
                {
                    HomingSpellProjectile.home(entityHitResult.getEntity(), this, 2.0F, entity.getOwner(), (projectile, level) ->
                    {
                        projectile.addTag(TAG_RETURN);
                        projectile.addTag(effectToString(effect));
                    });
                }
            }
        }
        else if(entity.getTags().contains(TAG_RETURN))
        {
            if(entityHitResult.getEntity() instanceof LivingEntity target)
            {
                for(String tag : entity.getTags())
                {
                    MobEffectInstance effect = stringToEffect(tag);
                    
                    if(effect != null)
                    {
                        target.addEffect(effect);
                        break;
                    }
                }
                
            }
        }
        
        IProjectileSpell.super.onEntityHit(entity, entityHitResult);
    }
    
    public static String effectToString(MobEffectInstance effect)
    {
        return SpellsAndShields.MOD_ID
                + ":" + effect.getDuration()
                + ":" + effect.getAmplifier()
                + ":" + effect.isAmbient()
                + ":" + effect.isVisible()
                + ":" + effect.showIcon()
                + ":" + effect.isNoCounter();
    }
    
    @Nullable
    public static MobEffectInstance stringToEffect(String string)
    {
        String[] parts = string.split(":");
        
        if(parts.length == 7)
        {
            for(int i = 3; i <= 6; ++i)
            {
                if(!parts[i].equalsIgnoreCase("true") && !parts[i].equalsIgnoreCase("false"))
                {
                    return null;
                }
            }
            
            try
            {
                int duration = Integer.parseInt(parts[1]);
                int amplifier = Integer.parseInt(parts[2]);
                boolean ambient = Boolean.parseBoolean(parts[3]);
                boolean visible = Boolean.parseBoolean(parts[4]);
                boolean showIcon = Boolean.parseBoolean(parts[5]);
                boolean noCounter = Boolean.parseBoolean(parts[6]);
                
                MobEffectInstance effect = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3]), Boolean.parseBoolean(parts[4]), Boolean.parseBoolean(parts[5]));
                effect.setNoCounter(Boolean.parseBoolean(parts[6]));
                
                return effect;
            }
            catch(NumberFormatException e)
            {
                return null;
            }
        }
        
        return null;
    }
}

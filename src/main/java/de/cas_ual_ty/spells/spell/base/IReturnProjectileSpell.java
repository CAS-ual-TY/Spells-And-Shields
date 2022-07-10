package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.function.BiConsumer;

public interface IReturnProjectileSpell extends IProjectileSpell
{
    String KEY_DIRECTION = "Direction";
    String DEPART = "Depart";
    String RETURN = "Return";
    
    // return true to return it to sender
    // any data on the spell data tag is kept on the new entity
    default boolean onEntityHitDeparture(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        entity.discard();
        return true;
    }
    
    default void onEntityHitReturn(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        entity.discard();
    }
    
    default boolean onBlockHitDeparture(SpellProjectile entity, BlockHitResult blockHitResult)
    {
        entity.discard();
        return true;
    }
    
    default void onBlockHitReturn(SpellProjectile entity, BlockHitResult blockHitResult)
    {
    }
    
    @Override
    default void onEntityHit(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        String direction = entity.getSpellDataTag().getString(KEY_DIRECTION);
        
        if(DEPART.equals(direction))
        {
            if(onEntityHitDeparture(entity, entityHitResult))
            {
                shootReturning(entity);
            }
        }
        else if(RETURN.equals(direction))
        {
            onEntityHitReturn(entity, entityHitResult);
        }
    }
    
    @Override
    default void onBlockHit(SpellProjectile entity, BlockHitResult blockHitResult)
    {
        String direction = entity.getSpellDataTag().getString(KEY_DIRECTION);
        
        if(DEPART.equals(direction))
        {
            if(onBlockHitDeparture(entity, blockHitResult))
            {
                shootReturning(entity);
            }
        }
        else if(RETURN.equals(direction))
        {
            onBlockHitReturn(entity, blockHitResult);
        }
    }
    
    @Override
    default void onTimeout(SpellProjectile entity)
    {
        String direction = entity.getSpellDataTag().getString(KEY_DIRECTION);
        
        if(DEPART.equals(direction))
        {
            shootReturning(entity);
        }
    }
    
    default float getReturnVelocity()
    {
        return 2F;
    }
    
    @Override
    default void shootStraight(ManaHolder manaHolder, float inaccuracy, BiConsumer<SpellProjectile, ServerLevel> followUp)
    {
        IProjectileSpell.super.shootStraight(manaHolder, inaccuracy, (projectile, level) ->
        {
            projectile.getSpellDataTag().putString(KEY_DIRECTION, DEPART);
        });
    }
    
    @Override
    default void shootHoming(ManaHolder manaHolder, Entity target, BiConsumer<HomingSpellProjectile, ServerLevel> followUp)
    {
        IProjectileSpell.super.shootHoming(manaHolder, target, (projectile, level) ->
        {
            projectile.getSpellDataTag().putString(KEY_DIRECTION, DEPART);
        });
    }
    
    default void shootReturning(SpellProjectile entity, BiConsumer<HomingSpellProjectile, ServerLevel> followUp)
    {
        if(entity.getOwner() != null)
        {
            Entity owner = entity.getOwner();
            
            HomingSpellProjectile.home(entity.position(), owner.position().subtract(entity.position()), entity.getOwner(), entity.getOwner(), this, getReturnVelocity(), (entity2, level) ->
            {
                // transfer all data over
                // this makes it possible to set data inside onEntityHitDeparture
                
                for(String key : entity.getSpellDataTag().getAllKeys())
                {
                    entity2.getSpellDataTag().put(key, entity.getSpellDataTag().get(key));
                }
                
                entity2.getSpellDataTag().putString(KEY_DIRECTION, RETURN);
            });
        }
    }
    
    default void shootReturning(SpellProjectile entity)
    {
        shootReturning(entity, (projectile, level) -> {});
    }
}

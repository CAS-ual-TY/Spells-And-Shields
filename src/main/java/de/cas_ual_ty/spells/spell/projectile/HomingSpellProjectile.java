package de.cas_ual_ty.spells.spell.projectile;

import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import de.cas_ual_ty.spells.spell.SpellInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class HomingSpellProjectile extends SpellProjectile
{
    protected UUID targetUUID;
    protected Entity cachedTarget;
    
    public HomingSpellProjectile(EntityType<? extends HomingSpellProjectile> entityType, Level level)
    {
        super(entityType, level);
    }
    
    public HomingSpellProjectile(EntityType<? extends AbstractHurtingProjectile> pEntityType, Level pLevel, SpellInstance spell, int timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        super(pEntityType, pLevel, spell, timeout, blockHitActivation, entityHitActivation, timeoutActivation);
    }
    
    public void setOwnerAndTarget(Entity owner, Entity target)
    {
        setOwner(owner);
        setTarget(target);
    }
    
    public void home(float velocity)
    {
        Vec3 direction = cachedTarget.getEyePosition().subtract(position()).normalize();
        shoot(direction.x, direction.y, direction.z, velocity, 0F);
    }
    
    @Override
    public void tick()
    {
        if(getTarget() != null && !getTarget().isRemoved())
        {
            Vec3 movement = getDeltaMovement();
            home((float) movement.length());
        }
        
        super.tick();
    }
    
    public void setTarget(Entity target)
    {
        targetUUID = target.getUUID();
        cachedTarget = target;
    }
    
    public Entity getTarget()
    {
        if(cachedTarget != null && !cachedTarget.isRemoved())
        {
            return cachedTarget;
        }
        else if(targetUUID != null && level instanceof ServerLevel serverLevel)
        {
            return cachedTarget = serverLevel.getEntity(targetUUID);
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        
        if(targetUUID != null)
        {
            pCompound.putUUID("Target", targetUUID);
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        
        if(pCompound.hasUUID("Target"))
        {
            targetUUID = pCompound.getUUID("Target");
        }
    }
    
    public static HomingSpellProjectile home(Level level0, Vec3 position, @Nullable Entity source, Entity target, SpellInstance spell, float velocity, int timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        if(level0 instanceof ServerLevel level)
        {
            Vec3 direction = target.getEyePosition().subtract(position).normalize();
            
            HomingSpellProjectile projectile = new HomingSpellProjectile(BuiltInRegisters.HOMING_SPELL_PROJECTILE.get(), level, spell, timeout, blockHitActivation, entityHitActivation, timeoutActivation);
            projectile.setOwnerAndTarget(source, target);
            
            projectile.moveTo(position.x, position.y, position.z, 0F, 0F);
            projectile.shoot(direction.x, direction.y, direction.z, velocity, 0F);
            
            level.addFreshEntity(projectile);
            
            return projectile;
        }
        
        return null;
    }
    
    public static HomingSpellProjectile home(Entity source, Entity target, SpellInstance spell, float velocity, int timeout, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        return home(source.level, source.getEyePosition(), source, target, spell, velocity, timeout, blockHitActivation, entityHitActivation, timeoutActivation);
    }
}

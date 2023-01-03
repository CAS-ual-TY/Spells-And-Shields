package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.registers.BuiltinRegistries;
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
    
    public HomingSpellProjectile(EntityType<? extends AbstractHurtingProjectile> pEntityType, Level pLevel, SpellInstance spell, int timeout, String blockDest, String blockClipDest, String entityDest, String entityClipDest, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        super(pEntityType, pLevel, spell, timeout, blockDest, blockClipDest, entityDest, entityClipDest, blockHitActivation, entityHitActivation, timeoutActivation);
    }
    
    public void setOwnerAndTarget(Entity owner, Entity target)
    {
        this.setOwner(owner);
        this.setTarget(target);
    }
    
    public void home(float velocity)
    {
        Vec3 direction = cachedTarget.getEyePosition().subtract(this.position()).normalize();
        shoot(direction.x, direction.y, direction.z, velocity, 0F);
    }
    
    @Override
    public void tick()
    {
        if(this.getTarget() != null && !this.getTarget().isRemoved())
        {
            Vec3 movement = getDeltaMovement();
            home((float) movement.length());
        }
        
        super.tick();
    }
    
    public void setTarget(Entity target)
    {
        this.targetUUID = target.getUUID();
        this.cachedTarget = target;
    }
    
    public Entity getTarget()
    {
        if(this.cachedTarget != null && !this.cachedTarget.isRemoved())
        {
            return this.cachedTarget;
        }
        else if(this.targetUUID != null && this.level instanceof ServerLevel serverLevel)
        {
            return this.cachedTarget = serverLevel.getEntity(this.targetUUID);
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
        
        if(this.targetUUID != null)
        {
            pCompound.putUUID("Target", this.targetUUID);
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        
        if(pCompound.hasUUID("Target"))
        {
            this.targetUUID = pCompound.getUUID("Target");
        }
    }
    
    public static void home(Vec3 position, @Nullable Entity source, Entity target, SpellInstance spell, float velocity, int timeout, String blockDest, String blockClipDest, String entityDest, String entityClipDest, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        if(source.level instanceof ServerLevel level)
        {
            Vec3 direction = target.getEyePosition().subtract(position).normalize();
            
            HomingSpellProjectile projectile = new HomingSpellProjectile(BuiltinRegistries.HOMING_SPELL_PROJECTILE.get(), level, spell, timeout, blockDest, blockClipDest, entityDest, entityClipDest, blockHitActivation, entityHitActivation, timeoutActivation);
            projectile.setOwnerAndTarget(source, target);
            
            projectile.moveTo(position.x, position.y, position.z, 0F, 0F);
            projectile.shoot(direction.x, direction.y, direction.z, velocity, 0F);
            
            level.addFreshEntity(projectile);
        }
    }
    
    public static void home(Entity source, Entity target, SpellInstance spell, float velocity, int timeout, String blockDest, String blockClipDest, String entityDest, String entityClipDest, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        home(source.getEyePosition(), source, target, spell, velocity, timeout, blockDest, blockClipDest, entityDest, entityClipDest, blockHitActivation, entityHitActivation, timeoutActivation);
    }
}

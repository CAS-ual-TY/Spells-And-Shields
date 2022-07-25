package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.spell.IProjectileSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.BiConsumer;

public class HomingSpellProjectile extends SpellProjectile
{
    protected UUID targetUUID;
    protected Entity cachedTarget;
    
    public HomingSpellProjectile(EntityType<? extends HomingSpellProjectile> entityType, Level level)
    {
        super(entityType, level);
    }
    
    public HomingSpellProjectile(EntityType<? extends HomingSpellProjectile> entityType, Level level, IProjectileSpell spell)
    {
        super(entityType, level, spell);
    }
    
    public void setOwnerAndTarget(Entity owner, Entity target)
    {
        this.setOwner(owner);
        this.setTarget(target);
    }
    
    public void home(float velocity)
    {
        Vec3 direction = cachedTarget.getEyePosition().subtract(this.position()).normalize();
        shoot(direction.x, direction.y, direction.z, velocity, 0.0F);
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
    
    public static void home(Vec3 position, Vec3 direction, @Nullable Entity source, Entity target, IProjectileSpell spell, float velocity, BiConsumer<HomingSpellProjectile, ServerLevel> followUp)
    {
        if(source.level instanceof ServerLevel level)
        {
            HomingSpellProjectile projectile = new HomingSpellProjectile(SpellsRegistries.HOMING_SPELL_PROJECTILE.get(), level, spell);
            projectile.setOwnerAndTarget(source, target);
            
            projectile.moveTo(position.x, position.y, position.z, source.getXRot(), source.getYRot());
            projectile.shoot(direction.x, direction.y, direction.z, velocity, 0.0F);
            level.addFreshEntity(projectile);
            
            followUp.accept(projectile, level);
        }
    }
    
    public static void home(Entity source, IProjectileSpell spell, float velocity, Entity target, BiConsumer<HomingSpellProjectile, ServerLevel> followUp)
    {
        home(source.getEyePosition(), source.getViewVector(1.0F).normalize(), source, target, spell, velocity, followUp);
    }
    
    public static void home(Entity source, IProjectileSpell spell, float velocity, Entity target)
    {
        home(source, spell, velocity, target, (entity, level) -> {});
    }
}

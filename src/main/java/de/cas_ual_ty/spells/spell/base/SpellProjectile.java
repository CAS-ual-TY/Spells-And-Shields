package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.SpellsRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.BiConsumer;

public class SpellProjectile extends AbstractHurtingProjectile implements IEntityAdditionalSpawnData
{
    protected IProjectileSpell spell;
    
    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level)
    {
        super(entityType, level);
    }
    
    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level, IProjectileSpell spell)
    {
        this(entityType, level);
        this.spell = spell;
    }
    
    @Override
    public void tick()
    {
        super.tick();
        
        IProjectileSpell spell = this.getSpell();
        spell.tick(this);
        
        if(this.tickCount >= spell.getTimeout() && !this.level.isClientSide)
        {
            spell.onTimeout(this);
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        IProjectileSpell spell = this.getSpell();
        spell.onEntityHit(this, entityHitResult);
    }
    
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        IProjectileSpell spell = this.getSpell();
        spell.onBlockHit(this, blockHitResult);
    }
    
    @Override
    public boolean shouldBurn()
    {
        return false;
    }
    
    public IProjectileSpell getSpell()
    {
        return this.spell;
    }
    
    public void setSpell(IProjectileSpell spell)
    {
        this.spell = spell;
    }
    
    public static void shoot(Entity source, IProjectileSpell spell, float velocity, float inaccuracy, BiConsumer<SpellProjectile, ServerLevel> followUp)
    {
        if(source.level instanceof ServerLevel level)
        {
            Vec3 position = source.getEyePosition();
            Vec3 direction = source.getViewVector(1.0F).normalize();
            
            SpellProjectile projectile = new SpellProjectile(SpellsRegistries.SPELL_PROJECTILE.get(), level, spell);
            
            projectile.setOwner(source);
            projectile.moveTo(position.x, position.y, position.z, source.getXRot(), source.getYRot());
            projectile.shoot(direction.x, direction.y, direction.z, velocity, inaccuracy);
            
            level.addFreshEntity(projectile);
            
            followUp.accept(projectile, level);
        }
    }
    
    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    @Override
    public void writeSpawnData(FriendlyByteBuf buf)
    {
        buf.writeRegistryId(this.getSpell());
    }
    
    @Override
    public void readSpawnData(FriendlyByteBuf buf)
    {
        this.spell = (IProjectileSpell) buf.readRegistryId();
    }
}

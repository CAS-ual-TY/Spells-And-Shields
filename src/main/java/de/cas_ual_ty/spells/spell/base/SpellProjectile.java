package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.registers.BuiltinRegistries;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.spell.SpellInstance;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class SpellProjectile extends AbstractHurtingProjectile
{
    protected SpellInstance spell;
    
    protected int timeout;
    
    protected String blockDest;
    protected String blockClipDest;
    protected String entityDest;
    protected String entityClipDest;
    
    protected String blockHitActivation;
    protected String entityHitActivation;
    protected String timeoutActivation;
    
    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level)
    {
        super(entityType, level);
        this.spell = null;
        this.timeout = -1;
        this.blockDest = null;
        this.blockClipDest = null;
        this.entityDest = null;
        this.entityClipDest = null;
        this.blockHitActivation = null;
        this.entityHitActivation = null;
        this.timeoutActivation = null;
    }
    
    public SpellProjectile(EntityType<? extends AbstractHurtingProjectile> pEntityType, Level pLevel, SpellInstance spell, int timeout, String blockDest, String blockClipDest, String entityDest, String entityClipDest, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        super(pEntityType, pLevel);
        this.spell = spell;
        this.timeout = timeout;
        this.blockDest = blockDest;
        this.blockClipDest = blockClipDest;
        this.entityDest = entityDest;
        this.entityClipDest = entityClipDest;
        this.blockHitActivation = blockHitActivation;
        this.entityHitActivation = entityHitActivation;
        this.timeoutActivation = timeoutActivation;
    }
    
    @Override
    protected float getInertia()
    {
        return 1F;
    }
    
    @Override
    protected ParticleOptions getTrailParticle()
    {
        return ParticleTypes.POOF;
    }
    
    @Override
    public void tick()
    {
        super.tick();
        
        if(spell != null && !level.isClientSide())
        {
            if(tickCount >= timeout)
            {
                spell.run(level, getPlayerOwner(), timeoutActivation);
                discard();
            }
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        if(spell != null && !level.isClientSide())
        {
            spell.run(level, getPlayerOwner(), entityHitActivation, (ctx) ->
            {
                ctx.getOrCreateTargetGroup(entityDest).addTargets(Target.of(entityHitResult.getEntity()));
                ctx.getOrCreateTargetGroup(entityClipDest).addTargets(Target.of(level, entityHitResult.getLocation()));
            });
            
            discard();
        }
    }
    
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        if(spell != null && !level.isClientSide())
        {
            spell.run(level, getPlayerOwner(), entityHitActivation, (ctx) ->
            {
                ctx.getOrCreateTargetGroup(blockDest).addTargets(Target.of(level, blockHitResult.getBlockPos()));
                ctx.getOrCreateTargetGroup(blockClipDest).addTargets(Target.of(level, blockHitResult.getLocation()));
            });
            
            discard();
        }
    }
    
    @Override
    public boolean shouldBurn()
    {
        return false;
    }
    
    @Nullable
    public SpellInstance getSpell()
    {
        return this.spell;
    }
    
    public void setSpell(SpellInstance spell)
    {
        this.spell = spell;
    }
    
    @Nullable
    public Player getPlayerOwner()
    {
        if(getOwner() instanceof ServerPlayer player && !player.hasDisconnected())
        {
            return player;
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        
        if(spell != null)
        {
            CompoundTag tag = new CompoundTag();
            spell.getNodeId().toNbt(tag);
            nbt.put("Spell", tag);
        }
        else
        {
            discard();
        }
        
        nbt.putInt("Timeout", timeout);
        nbt.putString("BlockDest", blockDest);
        nbt.putString("BlockClipDest", blockClipDest);
        nbt.putString("EntityDest", entityDest);
        nbt.putString("EntityClipDest", entityClipDest);
        nbt.putString("BlockHitActivation", blockHitActivation);
        nbt.putString("EntityHitActivation", entityHitActivation);
        nbt.putString("TimeoutActivation", timeoutActivation);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        
        if(nbt.contains("Spell", CompoundTag.TAG_COMPOUND))
        {
            SpellNodeId spellNodeId = SpellNodeId.fromNbt(nbt.getCompound("Spell"));
            
            if(spellNodeId != null)
            {
                this.spell = spellNodeId.getSpellInstance(SpellTrees.getRegistry(this.level));
            }
        }
        
        if(spell == null)
        {
            discard();
            return;
        }
        
        timeout = nbt.getInt("Timeout");
        blockDest = nbt.getString("BlockDest");
        blockClipDest = nbt.getString("BlockClipDest");
        entityDest = nbt.getString("EntityDest");
        entityClipDest = nbt.getString("EntityClipDest");
        blockHitActivation = nbt.getString("BlockHitActivation");
        entityHitActivation = nbt.getString("EntityHitActivation");
        timeoutActivation = nbt.getString("TimeoutActivation");
    }
    
    public static void shoot(Vec3 position, Vec3 direction, @Nullable Entity source, SpellInstance spell, float velocity, float inaccuracy, int timeout, String blockDest, String blockClipDest, String entityDest, String entityClipDest, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        if(source.level instanceof ServerLevel level)
        {
            SpellProjectile projectile = new SpellProjectile(BuiltinRegistries.SPELL_PROJECTILE.get(), level, spell, timeout, blockDest, blockClipDest, entityDest, entityClipDest, blockHitActivation, entityHitActivation, timeoutActivation);
            projectile.setOwner(source);
            
            projectile.moveTo(position.x, position.y, position.z, 0F, 0F);
            projectile.shoot(direction.x, direction.y, direction.z, velocity, inaccuracy);
            
            level.addFreshEntity(projectile);
        }
    }
    
    public static void shoot(Entity source, SpellInstance spell, float velocity, float inaccuracy, int timeout, String blockDest, String blockClipDest, String entityDest, String entityClipDest, String blockHitActivation, String entityHitActivation, String timeoutActivation)
    {
        shoot(source.getEyePosition(), source.getLookAngle().normalize(), source, spell, velocity, inaccuracy, timeout, blockDest, blockClipDest, entityDest, entityClipDest, blockHitActivation, entityHitActivation, timeoutActivation);
    }
}

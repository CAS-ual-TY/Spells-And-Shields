package de.cas_ual_ty.spells.spell.base;

import de.cas_ual_ty.spells.registers.BuiltinRegistries;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
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
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class SpellProjectile extends AbstractHurtingProjectile implements IEntityAdditionalSpawnData
{
    protected Spell spell;
    
    protected CompoundTag spellDataTag;
    
    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level)
    {
        super(entityType, level);
        this.spellDataTag = new CompoundTag();
    }
    
    public SpellProjectile(EntityType<? extends SpellProjectile> entityType, Level level, Spell spell)
    {
        this(entityType, level);
        this.spell = spell;
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
        
        if(spell != null)
        {
            // TODO Timeout
        }
    }
    
    @NotNull
    public CompoundTag getSpellDataTag()
    {
        return this.spellDataTag;
    }
    
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        if(spell != null)
        {
            // TODO
        }
    }
    
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        if(spell != null)
        {
            // TODO
        }
    }
    
    @Override
    public boolean shouldBurn()
    {
        return false;
    }
    
    public Spell getSpell()
    {
        return this.spell;
    }
    
    public void setSpell(Spell spell)
    {
        this.spell = spell;
    }
    
    public static void shoot(Entity source, Spell spell, float velocity, float inaccuracy, BiConsumer<SpellProjectile, ServerLevel> followUp)
    {
        if(source.level instanceof ServerLevel level)
        {
            Vec3 position = source.getEyePosition();
            Vec3 direction = source.getViewVector(1F).normalize();
            
            SpellProjectile projectile = new SpellProjectile(BuiltinRegistries.SPELL_PROJECTILE.get(), level, spell);
            
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
        boolean exist = spell != null;
        
        buf.writeBoolean(exist);
        if(exist)
        {
            buf.writeResourceLocation(Spells.getRegistry(level).getKey(spell));
        }
    }
    
    @Override
    public void readSpawnData(FriendlyByteBuf buf)
    {
        boolean exist = buf.readBoolean();
        
        if(exist)
        {
            this.spell = Spells.getRegistry(level).get(buf.readResourceLocation());
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.put("SpellData", this.spellDataTag);
        
        if(spell != null)
        {
            nbt.putString("Spell", Spells.getRegistry(level).getKey(spell).toString());
        }
        else
        {
            discard();
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.spellDataTag = nbt.contains("SpellData") ? nbt.getCompound("SpellData") : new CompoundTag();
        
        if(nbt.contains("Spell", StringTag.TAG_STRING))
        {
            String key = nbt.getString("Spell");
            this.spell = Spells.getRegistry(level).get(new ResourceLocation(key));
        }
        
        if(spell == null)
        {
            discard();
        }
    }
}

package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IProjectileSpell;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class FireBallSpell extends BaseIngredientsSpell implements IProjectileSpell
{
    public final int defaultFireSeconds;
    public final float defaultDamage;
    
    protected int fireSeconds;
    protected float damage;
    
    public FireBallSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, int defaultFireSeconds, float defaultDamage)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        this.defaultFireSeconds = defaultFireSeconds;
        this.defaultDamage = defaultDamage;
    }
    
    public FireBallSpell(float manaCost, ItemStack ingredient, int fireSeconds, float damage)
    {
        super(manaCost, ingredient);
        this.defaultFireSeconds = fireSeconds;
        this.defaultDamage = damage;
    }
    
    public FireBallSpell(float manaCost)
    {
        this(manaCost, new ItemStack(Items.BLAZE_POWDER), 2, 2F);
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
                    entity.level.addParticle(ParticleTypes.LARGE_SMOKE, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
                }
            }
            
            if(entity.tickCount % 4 == 0)
            {
                entity.level.addParticle(ParticleTypes.LAVA, pos.x, pos.y, pos.z, 0, 0, 0);
            }
            
            final double spread = 0.1D;
            
            for(int i = 0; i < 2; ++i)
            {
                entity.level.addParticle(ParticleTypes.SMOKE, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
                entity.level.addParticle(ParticleTypes.FLAME, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
            }
        }
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        shootStraight(manaHolder, 3F, (projectile, level) -> level.playSound(null, manaHolder.getPlayer(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F));
    }
    
    @Override
    public void onEntityHit(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        Entity hit = entityHitResult.getEntity();
        if(hit instanceof LivingEntity livingEntity)
        {
            livingEntity.hurt(DamageSource.indirectMagic(entity, entity.getOwner()), this.damage);
            livingEntity.setSecondsOnFire(this.fireSeconds);
        }
        IProjectileSpell.super.onEntityHit(entity, entityHitResult);
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("fireSeconds", this.defaultFireSeconds);
        json.addProperty("damage", this.defaultDamage);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        this.fireSeconds = SpellsFileUtil.jsonInt(json, "fireSeconds");
        this.damage = SpellsFileUtil.jsonFloat(json, "damage");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        this.fireSeconds = defaultFireSeconds;
        this.damage = defaultDamage;
    }
}

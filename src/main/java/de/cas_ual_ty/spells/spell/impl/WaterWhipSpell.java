package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IReturnProjectileSpell;
import de.cas_ual_ty.spells.spell.base.HandIngredientSpell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class WaterWhipSpell extends HandIngredientSpell implements IReturnProjectileSpell
{
    public final float defaultDamage;
    protected float damage;
    
    public WaterWhipSpell(float manaCost)
    {
        this(manaCost, 10F);
    }
    
    public WaterWhipSpell(float manaCost, float damage)
    {
        super(manaCost);
        defaultDamage = damage;
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        shootStraight(manaHolder, (projectile, level) ->
        {
            level.playSound(null, manaHolder.getPlayer().blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        });
    }
    
    @Override
    public boolean checkHandIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return itemStack.getItem() == Items.WATER_BUCKET;
    }
    
    @Override
    public void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack)
    {
        if(manaHolder.getPlayer().getMainHandItem() == itemStack)
        {
            manaHolder.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        }
        else if(manaHolder.getPlayer().getOffhandItem() == itemStack)
        {
            manaHolder.getPlayer().setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BUCKET));
        }
    }
    
    @Override
    public void tick(SpellProjectile entity)
    {
        if(entity.level.isClientSide)
        {
            Vec3 position = entity.position();
            Random random = new Random();
            
            if(entity.tickCount % 2 == 0)
            {
                final double spread = entity.getDeltaMovement().length() * 0.1D;
                
                for(int i = 0; i < 10; ++i)
                {
                    Vec3 pos = position.add(entity.getDeltaMovement().scale(random.nextGaussian()));
                    entity.level.addParticle(ParticleTypes.FALLING_WATER, pos.x + random.nextGaussian() * spread, pos.y + random.nextGaussian() * spread, pos.z + random.nextGaussian() * spread, 0, 0, 0);
                }
            }
        }
    }
    
    @Override
    public ParticleOptions getTrailParticle()
    {
        return ParticleTypes.FALLING_WATER;
    }
    
    @Override
    public boolean onEntityHitDeparture(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        if(entityHitResult.getEntity() instanceof LivingEntity hit)
        {
            hit.hurt(DamageSource.indirectMagic(entity, entity.getOwner()), 10F);
        }
        
        entity.discard();
        return true;
    }
    
    @Override
    public void onEntityHitReturn(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        if(entity.getOwner() instanceof LivingEntity player && entityHitResult.getEntity() == player)
        {
            if(player.getMainHandItem().getItem() == Items.BUCKET)
            {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
                player.level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            else if(player.getOffhandItem().getItem() == Items.BUCKET)
            {
                player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.WATER_BUCKET));
                player.level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            
            entity.discard();
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("damage", this.defaultDamage);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        this.damage = SpellsFileUtil.jsonFloat(json, "damage");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        this.damage = defaultDamage;
    }
}

package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IClientSpell;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LeapSpell extends BaseIngredientsSpell implements IClientSpell
{
    public final double defaultSpeed;
    protected double speed;
    
    public LeapSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, double speed)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        defaultSpeed = speed;
    }
    
    public LeapSpell(float manaCost, ItemStack handIngredient, double speed)
    {
        super(manaCost, handIngredient);
        defaultSpeed = speed;
    }
    
    public LeapSpell(float manaCost, double speed)
    {
        super(manaCost);
        defaultSpeed = speed;
    }
    
    public LeapSpell()
    {
        this(6F, 2.5D);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        LivingEntity entity = manaHolder.getPlayer();
        
        Vec3 direction = entity.getViewVector(1F).normalize();
        double y = direction.y;
        direction = direction.add(0, -direction.y, 0).scale(this.speed);
        
        entity.setDeltaMovement(direction.x, Math.max(0.5D, y + 0.5D), direction.z);
        entity.fallDistance = 0F;
        
        if(entity.level instanceof ServerLevel serverLevel)
        {
            final int count = 4;
            final double spread = 0.1D;
            Vec3 position = entity.position();
            serverLevel.sendParticles(ParticleTypes.POOF, position.x, position.y, position.z, count, entity.getRandom().nextGaussian() * spread, entity.getRandom().nextGaussian() * spread, entity.getRandom().nextGaussian() * spread, 0D);
            serverLevel.playSound(null, manaHolder.getPlayer(), getJumpSound(), SoundSource.PLAYERS, 1F, 1F);
        }
    }
    
    protected SoundEvent getJumpSound()
    {
        return SoundEvents.ENDER_DRAGON_FLAP;
    }
    
    @Override
    public void performOnClient(ManaHolder manaHolder)
    {
        perform(manaHolder);
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("speed", this.defaultSpeed);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        this.speed = SpellsFileUtil.jsonDouble(json, "speed");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        this.speed = this.defaultSpeed;
    }
}

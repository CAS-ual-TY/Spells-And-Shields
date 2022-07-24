package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class JumpSpell extends BaseIngredientsSpell
{
    public final double defaultSpeed;
    
    protected double speed;
    
    public JumpSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, double speed)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        this.defaultSpeed = speed;
    }
    
    public JumpSpell(float manaCost, double speed)
    {
        super(manaCost);
        this.defaultSpeed = speed;
    }
    
    public JumpSpell(float manaCost)
    {
        this(manaCost, 1.5D);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        LivingEntity entity = manaHolder.getPlayer();
        
        Vec3 direction = entity.getDeltaMovement().add(0, this.speed, 0);
        entity.setDeltaMovement(direction);
        
        entity.fallDistance = 0F;
        
        if(entity.level instanceof ServerLevel serverLevel)
        {
            final int count = 4;
            final double spread = 0.1D;
            Vec3 position = entity.position();
            serverLevel.sendParticles(ParticleTypes.POOF, position.x, position.y, position.z, count, entity.getRandom().nextGaussian() * spread, entity.getRandom().nextGaussian() * spread, entity.getRandom().nextGaussian() * spread, 0.0D);
        }
    }
    
    @Override
    public boolean performOnClient()
    {
        return true;
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

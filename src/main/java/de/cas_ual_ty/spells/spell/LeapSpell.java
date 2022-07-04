package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LeapSpell extends BaseIngredientsSpell
{
    public final double distance;
    
    public LeapSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, double distance)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        this.distance = distance;
    }
    
    public LeapSpell(float manaCost, double distance)
    {
        super(manaCost);
        this.distance = distance;
    }
    
    public LeapSpell(float manaCost)
    {
        this(manaCost, 2.5D);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        LivingEntity entity = manaHolder.getPlayer();
        
        Vec3 direction = entity.getViewVector(1.0F).normalize();
        double y = direction.y;
        direction = direction.add(0, -direction.y, 0).scale(this.distance);
        
        entity.setDeltaMovement(direction.x, Math.max(0.5D, y + 0.5D), direction.z);
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
}

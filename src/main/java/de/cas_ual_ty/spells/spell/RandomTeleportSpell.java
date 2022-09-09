package de.cas_ual_ty.spells.spell;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RandomTeleportSpell extends BaseIngredientsSpell
{
    protected int attempts = 5;
    protected int range = 32;
    
    public RandomTeleportSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        super(manaCost, handIngredients, inventoryIngredients);
    }
    
    public RandomTeleportSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public RandomTeleportSpell(float manaCost, ItemStack handIngredient)
    {
        super(manaCost, handIngredient);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        if(!manaHolder.getPlayer().level.isClientSide && manaHolder.getPlayer().level instanceof ServerLevel level)
        {
            LivingEntity entity = manaHolder.getPlayer();
            RandomSource random = entity.getRandom();
            
            Vec3 pos = entity.position();
            
            double x = 0;
            double y = 0;
            double z = 0;
            
            boolean success = false;
            int i = 0;
            while(!success && i < attempts)
            {
                x = entity.getX() + (random.nextDouble() - 0.5D) * range * 2;
                y = entity.getY() + (double) (random.nextInt(range * 2) - range);
                z = entity.getZ() + (random.nextDouble() - 0.5D) * range * 2;
                
                success = entity.randomTeleport(x, y, z, true);
                i++;
            }
            
            if(success)
            {
                level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                level.playSound(null, entity, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            else
            {
                level.playSound(null, entity, SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}

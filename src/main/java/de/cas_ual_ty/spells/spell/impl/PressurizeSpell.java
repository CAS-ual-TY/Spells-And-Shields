package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PressurizeSpell extends BaseIngredientsSpell
{
    public final int removeFluidRadius = 6;
    public final double knockBackRadius = 6;
    public final double knockBackStrength = 3D;
    
    public PressurizeSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        super(manaCost, handIngredients, inventoryIngredients);
    }
    
    public PressurizeSpell(float manaCost, ItemStack handIngredient)
    {
        super(manaCost, handIngredient);
    }
    
    public PressurizeSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public PressurizeSpell()
    {
        this(4F);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        LivingEntity caster = manaHolder.getPlayer();
        
        if(caster.level instanceof ServerLevel level)
        {
            double r = knockBackRadius * 2;
            AABB area = AABB.ofSize(caster.getEyePosition(), r, r, r);
            
            double strength = 2D;
            
            caster.level.playSound(null, caster, SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 1F, 1F);
            
            List<Entity> entities = caster.level.getEntities(caster, area);
            
            for(int i = 0; i < 18; i++)
            {
                Vec3 dir = Vec3.directionFromRotation(90F, i * 20F).normalize();
                Vec3 pos = manaHolder.getPlayer().getEyePosition().add(dir);
                level.sendParticles(ParticleTypes.POOF, pos.x, pos.y, pos.z, 2, dir.x, 0, dir.z, 1D);
            }
            
            for(Entity entity : entities)
            {
                if(entity instanceof LivingEntity target)
                {
                    Vec3 dir = target.position().subtract(caster.position());
                    target.knockback(strength, -dir.x, -dir.z);
                    
                    Vec3 pos = target.getEyePosition();
                    level.sendParticles(ParticleTypes.POOF, pos.x, pos.y, pos.z, 3, 0, 0, 0, 0D);
                }
            }
            
            /* // TODO remove fluid
            for(int x = -removeFluidRadius; x <= removeFluidRadius; x++)
            {
                for(int y = -removeFluidRadius; y <= removeFluidRadius; y++)
                {
                    for(int z = -removeFluidRadius; z <= removeFluidRadius; z++)
                    {
                        BlockPos pos = new BlockPos(x, y, z);
    
                        FluidState fluidState = level.getFluidState(pos);
                        
                        if(!fluidState.isEmpty())
                        {
                            LevelChunk levelchunk = level.getChunkAt(pos);
                            //levelchunk.fl
                            
                            BlockState blockState = levelchunk.getBlockState(pos);
                            blockState.getBlock().
                        }
                    }
                }
            }
            */
        }
    }
}

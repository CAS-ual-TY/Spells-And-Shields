package de.cas_ual_ty.spells.spell.target;

import de.cas_ual_ty.spells.registers.TargetTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class Target
{
    public final ITargetType<?> type;
    
    public Target(ITargetType<?> type)
    {
        this.type = type;
    }
    
    public abstract Level getLevel();
    
    public static EntityTarget of(Entity entity)
    {
        if(entity instanceof Player player)
        {
            return new PlayerTarget(TargetTypes.PLAYER.get(), player);
        }
        else if(entity instanceof LivingEntity livingEntity)
        {
            return new LivingEntityTarget(TargetTypes.LIVING_ENTITY.get(), livingEntity);
        }
        else
        {
            return new EntityTarget(TargetTypes.ENTITY.get(), entity);
        }
    }
    
    public static StaticTarget of(Level level, BlockPos blockPos)
    {
        return new StaticTarget(TargetTypes.STATIC.get(), level, blockPos);
    }
    
    public static StaticTarget of(Level level, Vec3 position)
    {
        return new StaticTarget(TargetTypes.STATIC.get(), level, position);
    }
}

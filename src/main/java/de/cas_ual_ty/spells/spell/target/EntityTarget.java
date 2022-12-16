package de.cas_ual_ty.spells.spell.target;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityTarget extends PositionTarget
{
    protected Entity entity;
    
    public EntityTarget(ITargetType<?> type, Entity entity)
    {
        super(type, entity.getLevel());
        this.entity = entity;
    }
    
    public Entity getEntity()
    {
        return entity;
    }
    
    @Override
    public Vec3 getPosition()
    {
        return entity.getEyePosition();
    }
    
    @Override
    public BlockPos getBlockPos()
    {
        return entity.blockPosition();
    }
}

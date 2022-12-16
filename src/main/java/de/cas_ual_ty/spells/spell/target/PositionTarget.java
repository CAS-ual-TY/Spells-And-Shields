package de.cas_ual_ty.spells.spell.target;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class PositionTarget extends Target
{
    protected Level level;
    
    public PositionTarget(ITargetType<?> type, Level level)
    {
        super(type);
        this.level = level;
    }
    
    @Override
    public Level getLevel()
    {
        return level;
    }
    
    public abstract Vec3 getPosition();
    
    public abstract BlockPos getBlockPos();
}

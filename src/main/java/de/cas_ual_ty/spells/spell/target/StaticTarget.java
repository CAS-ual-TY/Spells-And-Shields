package de.cas_ual_ty.spells.spell.target;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class StaticTarget extends PositionTarget
{
    protected Vec3 position;
    protected BlockPos blockPos;
    
    protected StaticTarget(ITargetType<?> type, Level level, Vec3 position, BlockPos blockPos)
    {
        super(type, level);
        this.position = position;
        this.blockPos = blockPos;
    }
    
    public StaticTarget(ITargetType<?> type, Level level, Vec3 position)
    {
        this(type, level, position, new BlockPos((int) position.x, (int) position.y, (int) position.z));
    }
    
    public StaticTarget(ITargetType<?> type, Level level, BlockPos blockPos)
    {
        this(type, level, Vec3.atCenterOf(blockPos), blockPos);
    }
    
    @Override
    public Vec3 getPosition()
    {
        return position;
    }
    
    @Override
    public BlockPos getBlockPos()
    {
        return blockPos;
    }
}

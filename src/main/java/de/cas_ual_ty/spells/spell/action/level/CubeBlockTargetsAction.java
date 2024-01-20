package de.cas_ual_ty.spells.spell.action.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PositionTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class CubeBlockTargetsAction extends AffectSingleTypeAction<PositionTarget>
{
    public static Codec<CubeBlockTargetsAction> makeCodec(SpellActionType<CubeBlockTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("targets")).forGetter(CubeBlockTargetsAction::getTargets),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("corner1")).forGetter(CubeBlockTargetsAction::getCorner1),
                CtxVarTypes.VEC3.get().refCodec().fieldOf(ParamNames.paramVec3("corner2")).forGetter(CubeBlockTargetsAction::getCorner2)
        ).apply(instance, (activation, source, targets, corner1, corner2) -> new CubeBlockTargetsAction(type, activation, source, targets, corner1, corner2)));
    }
    
    public static CubeBlockTargetsAction make(Object activation, Object source, String targets, DynamicCtxVar<Vec3> corner1, DynamicCtxVar<Vec3> corner2)
    {
        return new CubeBlockTargetsAction(SpellActionTypes.CUBE_BLOCK_TARGETS.get(), activation.toString(), source.toString(), targets, corner1, corner2);
    }
    
    protected String targets;
    protected DynamicCtxVar<Vec3> corner1;
    protected DynamicCtxVar<Vec3> corner2;
    
    public CubeBlockTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public CubeBlockTargetsAction(SpellActionType<?> type, String activation, String source, String targets, DynamicCtxVar<Vec3> corner1, DynamicCtxVar<Vec3> corner2)
    {
        super(type, activation, source);
        this.targets = targets;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }
    
    public String getTargets()
    {
        return targets;
    }
    
    public DynamicCtxVar<Vec3> getCorner1()
    {
        return corner1;
    }
    
    public DynamicCtxVar<Vec3> getCorner2()
    {
        return corner2;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PositionTarget positionTarget)
    {
        TargetGroup destination = ctx.getOrCreateTargetGroup(targets);
        
        corner1.getValue(ctx).ifPresent(corner1 ->
        {
            corner2.getValue(ctx).ifPresent(corner2 ->
            {
                BlockPos p1 = new BlockPos(positionTarget.getPosition().add(corner1));
                BlockPos p2 = new BlockPos(positionTarget.getPosition().add(corner2));
                
                int minX = Math.min(p1.getX(), p2.getX());
                int maxX = Math.max(p1.getX(), p2.getX());
                int minY = Math.min(p1.getY(), p2.getY());
                int maxY = Math.max(p1.getY(), p2.getY());
                int minZ = Math.min(p1.getZ(), p2.getZ());
                int maxZ = Math.max(p1.getZ(), p2.getZ());
                
                for(int x = minX; x <= maxX; x++)
                {
                    for(int y = minY; y <= maxY; y++)
                    {
                        for(int z = minZ; z <= maxZ; z++)
                        {
                            destination.addTargets(Target.of(positionTarget.getLevel(), new BlockPos(x, y, z)));
                        }
                    }
                }
            });
        });
    }
    
    @Override
    public ITargetType<PositionTarget> getAffectedType()
    {
        return TargetTypes.POSITION.get();
    }
}

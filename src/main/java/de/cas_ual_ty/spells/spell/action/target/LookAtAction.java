package de.cas_ual_ty.spells.spell.action.target;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LookAtAction extends SpellAction
{
    protected String src;
    protected double range;
    protected float bbInflation;
    protected ClipContext.Block block;
    protected ClipContext.Fluid fluid;
    protected String blockDest;
    protected String missDest;
    protected String entityDest;
    protected String entityClipDest;
    
    public LookAtAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public LookAtAction(SpellActionType<?> type, String activation, String src, double range, float bbInflation, ClipContext.Block block, ClipContext.Fluid fluid, String blockDest, String missDest, String entityDest, String entityClipDest)
    {
        super(type, activation);
        this.src = src;
        this.range = range;
        this.bbInflation = bbInflation;
        this.block = block;
        this.fluid = fluid;
        this.blockDest = blockDest;
        this.missDest = missDest;
        this.entityDest = entityDest;
        this.entityClipDest = entityClipDest;
    }
    
    @Override
    protected void wasActivated(SpellContext ctx)
    {
        TargetGroup source = ctx.getTargetGroup(src);
        
        source.forEachType(SpellsRegistries.ENTITY_TARGET.get(), (entityTarget) ->
        {
            HitResult hitResult = SpellsUtil.rayTrace(entityTarget.getLevel(), entityTarget.getEntity(), range, e -> true, bbInflation, block, fluid);
            
            if(hitResult.getType() == HitResult.Type.BLOCK)
            {
                ctx.getOrCreateTargetGroup(blockDest).addTargets(Target.of(entityTarget.getLevel(), hitResult.getLocation()));
            }
            if(hitResult.getType() == HitResult.Type.MISS)
            {
                ctx.getOrCreateTargetGroup(missDest).addTargets(Target.of(entityTarget.getLevel(), hitResult.getLocation()));
            }
            else if(hitResult instanceof EntityHitResult entityHitResult)
            {
                ctx.getOrCreateTargetGroup(entityDest).addTargets(Target.of(entityHitResult.getEntity()));
                ctx.getOrCreateTargetGroup(entityClipDest).addTargets(Target.of(entityTarget.getLevel(), hitResult.getLocation()));
            }
        });
    }
}

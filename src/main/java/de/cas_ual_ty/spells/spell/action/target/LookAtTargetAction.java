package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.effect.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LookAtTargetAction extends AffectSingleTypeAction<EntityTarget>
{
    public static Codec<LookAtTargetAction> makeCodec(SpellActionType<LookAtTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetsCodec(),
                Codec.DOUBLE.fieldOf(ParamNames.paramDoubleImm("range")).forGetter(LookAtTargetAction::getRange),
                Codec.FLOAT.fieldOf(ParamNames.paramDoubleImm("bbInflation")).forGetter(LookAtTargetAction::getBbInflation),
                SpellsUtil.namedEnumCodec(ClipContext.Block::valueOf).fieldOf("blockClipContext").forGetter(LookAtTargetAction::getBlock),
                SpellsUtil.namedEnumCodec(ClipContext.Fluid::valueOf).fieldOf("fluidClipContext").forGetter(LookAtTargetAction::getFluid),
                Codec.STRING.fieldOf(ParamNames.singleTarget("blockDestination")).forGetter(LookAtTargetAction::getBlockDest),
                Codec.STRING.fieldOf(ParamNames.singleTarget("missDestination")).forGetter(LookAtTargetAction::getMissDest),
                Codec.STRING.fieldOf(ParamNames.singleTarget("entityDestination")).forGetter(LookAtTargetAction::getEntityDest),
                Codec.STRING.fieldOf(ParamNames.singleTarget("entityClipDestination")).forGetter(LookAtTargetAction::getEntityClipDest)
        ).apply(instance, (activation, targets, range, bbInflation, block, fluid, blockDest, missDest, entityDest, entityClipDest) -> new LookAtTargetAction(type, activation, targets, range, bbInflation, block, fluid, blockDest, missDest, entityDest, entityClipDest)));
    }
    
    protected double range;
    protected float bbInflation;
    protected ClipContext.Block block;
    protected ClipContext.Fluid fluid;
    protected String blockDest;
    protected String missDest;
    protected String entityDest;
    protected String entityClipDest;
    
    public LookAtTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public LookAtTargetAction(SpellActionType<?> type, String activation, String targets, double range, float bbInflation, ClipContext.Block block, ClipContext.Fluid fluid, String blockDest, String missDest, String entityDest, String entityClipDest)
    {
        super(type, activation, targets);
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
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    public double getRange()
    {
        return range;
    }
    
    public float getBbInflation()
    {
        return bbInflation;
    }
    
    public ClipContext.Block getBlock()
    {
        return block;
    }
    
    public ClipContext.Fluid getFluid()
    {
        return fluid;
    }
    
    public String getBlockDest()
    {
        return blockDest;
    }
    
    public String getMissDest()
    {
        return missDest;
    }
    
    public String getEntityDest()
    {
        return entityDest;
    }
    
    public String getEntityClipDest()
    {
        return entityClipDest;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, EntityTarget entityTarget)
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
    }
}

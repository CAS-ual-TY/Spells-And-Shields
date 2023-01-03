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
import net.minecraft.world.phys.BlockHitResult;
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
                Codec.STRING.fieldOf(ParamNames.singleTarget("blockClipDestination")).forGetter(LookAtTargetAction::getBlockClipDest),
                Codec.STRING.fieldOf(ParamNames.singleTarget("missDestination")).forGetter(LookAtTargetAction::getMissDest),
                Codec.STRING.fieldOf(ParamNames.singleTarget("entityDestination")).forGetter(LookAtTargetAction::getEntityDest),
                Codec.STRING.fieldOf(ParamNames.singleTarget("entityClipDestination")).forGetter(LookAtTargetAction::getEntityClipDest),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("blockHit")).forGetter(LookAtTargetAction::getBlockHitActivation),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("entityHit")).forGetter(LookAtTargetAction::getEntityHitActivation),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("miss")).forGetter(LookAtTargetAction::getMissActivation)
        ).apply(instance, (activation, targets, range, bbInflation, block, fluid, blockDest, blockClipDest, missDest, entityDest, entityClipDest, blockHitActivation, entityHitActivation, missActivation) -> new LookAtTargetAction(type, activation, targets, range, bbInflation, block, fluid, blockDest, blockClipDest, missDest, entityDest, entityClipDest, blockHitActivation, entityHitActivation, missActivation)));
    }
    
    protected double range;
    protected float bbInflation;
    protected ClipContext.Block block;
    protected ClipContext.Fluid fluid;
    
    protected String blockDest;
    protected String blockClipDest;
    protected String entityDest;
    protected String entityClipDest;
    protected String missDest;
    
    protected String blockHitActivation;
    protected String entityHitActivation;
    protected String missActivation;
    
    public LookAtTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public LookAtTargetAction(SpellActionType<?> type, String activation, String targets, double range, float bbInflation, ClipContext.Block block, ClipContext.Fluid fluid, String blockDest, String blockClipDest, String missDest, String entityDest, String entityClipDest, String blockHitActivation, String entityHitActivation, String missActivation)
    {
        super(type, activation, targets);
        this.range = range;
        this.bbInflation = bbInflation;
        this.block = block;
        this.fluid = fluid;
        this.blockDest = blockDest;
        this.blockClipDest = blockClipDest;
        this.missDest = missDest;
        this.entityDest = entityDest;
        this.entityClipDest = entityClipDest;
        this.blockHitActivation = blockHitActivation;
        this.entityHitActivation = entityHitActivation;
        this.missActivation = missActivation;
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
    
    public String getBlockClipDest()
    {
        return blockClipDest;
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
    
    public String getBlockHitActivation()
    {
        return blockHitActivation;
    }
    
    public String getEntityHitActivation()
    {
        return entityHitActivation;
    }
    
    public String getMissActivation()
    {
        return missActivation;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, EntityTarget entityTarget)
    {
        HitResult hitResult = SpellsUtil.rayTrace(entityTarget.getLevel(), entityTarget.getEntity(), range, e -> true, bbInflation, block, fluid);
        
        if(hitResult instanceof BlockHitResult blockHitResult)
        {
            ctx.activate(blockHitActivation);
            ctx.getOrCreateTargetGroup(blockDest).addTargets(Target.of(entityTarget.getLevel(), blockHitResult.getBlockPos()));
            ctx.getOrCreateTargetGroup(blockClipDest).addTargets(Target.of(entityTarget.getLevel(), hitResult.getLocation()));
        }
        else if(hitResult instanceof EntityHitResult entityHitResult)
        {
            ctx.activate(entityHitActivation);
            ctx.getOrCreateTargetGroup(entityDest).addTargets(Target.of(entityHitResult.getEntity()));
            ctx.getOrCreateTargetGroup(entityClipDest).addTargets(Target.of(entityTarget.getLevel(), hitResult.getLocation()));
        }
        else
        {
            ctx.activate(missActivation);
            ctx.getOrCreateTargetGroup(missDest).addTargets(Target.of(entityTarget.getLevel(), hitResult.getLocation()));
        }
    }
}

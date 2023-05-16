package de.cas_ual_ty.spells.spell.action.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.SrcDstTargetAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class RangedEntityTargetsAction extends SrcDstTargetAction
{
    public static Codec<RangedEntityTargetsAction> makeCodec(SpellActionType<RangedEntityTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf("range").forGetter(RangedEntityTargetsAction::getRange)
        ).apply(instance, (activation, dst, src, range) -> new RangedEntityTargetsAction(type, activation, dst, src, range)));
    }
    
    public static RangedEntityTargetsAction make(Object activation, Object dst, Object src, DynamicCtxVar<Double> range)
    {
        return new RangedEntityTargetsAction(SpellActionTypes.RANGED_ENTITY_TARGETS.get(), activation.toString(), dst.toString(), src.toString(), range);
    }
    
    protected DynamicCtxVar<Double> range;
    
    public RangedEntityTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public RangedEntityTargetsAction(SpellActionType<?> type, String activation, String dst, String src, DynamicCtxVar<Double> range)
    {
        super(type, activation, dst, src);
        this.range = range;
    }
    
    public DynamicCtxVar<Double> getRange()
    {
        return range;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        source.getSingleType(TargetTypes.POSITION.get(), position ->
        {
            range.getValue(ctx).ifPresent(range ->
            {
                if(ctx.level instanceof ServerLevel level)
                {
                    range = range * 2;
                    Entity entity = TargetTypes.ENTITY.get().ifType(position).map(EntityTarget::getEntity).orElse(null);
                    level.getEntities(entity, AABB.ofSize(position.getPosition(), range, range, range)).stream()
                            .map(Target::of)
                            .forEach(destination::addTargets);
                }
            });
        });
    }
}

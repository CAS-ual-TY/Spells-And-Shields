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
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class RangedTargetsAction extends SrcDstTargetAction
{
    public static Codec<RangedTargetsAction> makeCodec(SpellActionType<RangedTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec(),
                srcCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf("range").forGetter(RangedTargetsAction::getRange)
        ).apply(instance, (activation, dst, src, range) -> new RangedTargetsAction(type, activation, dst, src, range)));
    }
    
    public static RangedTargetsAction make(String activation, String dest, String src, DynamicCtxVar<Double> range)
    {
        return new RangedTargetsAction(SpellActionTypes.RANGED_TARGETS.get(), activation, dest, src, range);
    }
    
    protected DynamicCtxVar<Double> range;
    
    public RangedTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public RangedTargetsAction(SpellActionType<?> type, String activation, String dest, String src, DynamicCtxVar<Double> range)
    {
        super(type, activation, dest, src);
        this.range = range;
    }
    
    public DynamicCtxVar<Double> getRange()
    {
        return range;
    }
    
    @Override
    public void findTargets(SpellContext ctx, TargetGroup source, TargetGroup destination)
    {
        source.getSingleTarget(target -> TargetTypes.POSITION.get().ifType(target, position ->
        {
            range.getValue(ctx).ifPresent(range ->
            {
                if(ctx.level instanceof ServerLevel level)
                {
                    range = range * 2;
                    Entity entity = TargetTypes.ENTITY.get().ifType(target).map(EntityTarget::getEntity).orElse(null);
                    level.getEntities(entity, AABB.ofSize(position.getPosition(), range, range, range));
                }
            });
        }));
    }
}

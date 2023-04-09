package de.cas_ual_ty.spells.spell.action.ai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.target.Target;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.Mob;

public class GetTargetAction extends GetTargetAttributeAction<LivingEntityTarget>
{
    public static Codec<GetTargetAction> makeCodec(SpellActionType<GetTargetAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.destinationTarget("target")).forGetter(GetTargetAction::getTarget),
                Codec.STRING.fieldOf(ParamNames.var("has_target")).forGetter(GetTargetAction::getHasTarget)
        ).apply(instance, (activation, source, target, hasTarget) -> new GetTargetAction(type, activation, source, target, hasTarget)));
    }
    
    public static GetTargetAction make(String activation, String source, String target, String hasTarget)
    {
        return new GetTargetAction(SpellActionTypes.GET_ENTITY_TAG.get(), activation, source, target, hasTarget);
    }
    
    protected String target;
    protected String hasTarget;
    
    public GetTargetAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetTargetAction(SpellActionType<?> type, String activation, String source, String target, String hasTarget)
    {
        super(type, activation, source);
        this.target = target;
        this.hasTarget = hasTarget;
        
        if(!target.isEmpty())
        {
            addTargetAttribute(e -> e.getLivingEntity() instanceof Mob mob ? Target.of(mob.getTarget()) : null, target);
        }
        
        if(!hasTarget.isEmpty())
        {
            addVariableAttribute(e -> e.getLivingEntity() instanceof Mob mob ? (mob.getTarget() != null) : false, CtxVarTypes.BOOLEAN.get(), hasTarget);
        }
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public String getTarget()
    {
        return target;
    }
    
    public String getHasTarget()
    {
        return hasTarget;
    }
}

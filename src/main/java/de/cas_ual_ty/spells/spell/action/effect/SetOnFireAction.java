package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;

public class SetOnFireAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<SetOnFireAction> makeCodec(SpellActionType<SetOnFireAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("fire_seconds")).forGetter(SetOnFireAction::getFireSeconds)
        ).apply(instance, (activation, multiTargets, fireSeconds) -> new SetOnFireAction(type, activation, multiTargets, fireSeconds)));
    }
    
    public static SetOnFireAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> fireSeconds)
    {
        return new SetOnFireAction(SpellActionTypes.SET_ON_FIRE.get(), activation.toString(), multiTargets.toString(), fireSeconds);
    }
    
    protected DynamicCtxVar<Integer> fireSeconds;
    
    public SetOnFireAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SetOnFireAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> fireSeconds)
    {
        super(type, activation, multiTargets);
        this.fireSeconds = fireSeconds;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public DynamicCtxVar<Integer> getFireSeconds()
    {
        return fireSeconds;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        fireSeconds.getValue(ctx).ifPresent(fireTicks ->
        {
            if(fireTicks > 0)
            {
                // affected by fire protection enchantment
                target.getEntity().setSecondsOnFire(fireTicks);
            }
        });
    }
}

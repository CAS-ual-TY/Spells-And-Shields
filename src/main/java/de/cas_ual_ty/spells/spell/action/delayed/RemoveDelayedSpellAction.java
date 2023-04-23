package de.cas_ual_ty.spells.spell.action.delayed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.DelayedSpellHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;

public class RemoveDelayedSpellAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<RemoveDelayedSpellAction> makeCodec(SpellActionType<RemoveDelayedSpellAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("uuid")).forGetter(RemoveDelayedSpellAction::getUuid),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("force_activate")).forGetter(RemoveDelayedSpellAction::getForceActivate)
        ).apply(instance, (activation, multiTargets, uuid, forceActivate) -> new RemoveDelayedSpellAction(type, activation, multiTargets, uuid, forceActivate)));
    }
    
    public static RemoveDelayedSpellAction make(Object activation, Object multiTargets, DynamicCtxVar<String> uuid, DynamicCtxVar<Boolean> forceActivate)
    {
        return new RemoveDelayedSpellAction(SpellActionTypes.REMOVE_DELAYED_SPELL.get(), activation.toString(), multiTargets.toString(), uuid, forceActivate);
    }
    
    protected DynamicCtxVar<String> uuid;
    protected DynamicCtxVar<Boolean> forceActivate;
    
    public RemoveDelayedSpellAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public RemoveDelayedSpellAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> uuid, DynamicCtxVar<Boolean> forceActivate)
    {
        super(type, activation, multiTargets);
        this.uuid = uuid;
        this.forceActivate = forceActivate;
    }
    
    public DynamicCtxVar<String> getUuid()
    {
        return uuid;
    }
    
    public DynamicCtxVar<Boolean> getForceActivate()
    {
        return forceActivate;
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget target)
    {
        DelayedSpellHolder.getHolder(target.getEntity()).ifPresent(holder ->
        {
            this.uuid.getValue(ctx).map(SpellsUtil::uuidFromString).ifPresent(uuid1 ->
            {
                boolean force = forceActivate.getValue(ctx).orElse(false);
                holder.removeDelayedSpell(uuid1, force);
            });
        });
    }
}

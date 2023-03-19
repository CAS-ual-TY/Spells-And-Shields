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

import java.util.UUID;

public class CheckHasDelayedSpellAction extends AffectTypeAction<EntityTarget>
{
    public static Codec<CheckHasDelayedSpellAction> makeCodec(SpellActionType<CheckHasDelayedSpellAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                targetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("uuid")).forGetter(CheckHasDelayedSpellAction::getUuid),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("to_activate")).forGetter(CheckHasDelayedSpellAction::getToActivate)
        ).apply(instance, (activation, targets, uuid, toActivate) -> new CheckHasDelayedSpellAction(type, activation, targets, uuid, toActivate)));
    }
    
    public static CheckHasDelayedSpellAction make(String activation, String targets, DynamicCtxVar<String> uuid, String toActivate)
    {
        return new CheckHasDelayedSpellAction(SpellActionTypes.CHECK_HAS_DELAYED_SPELL.get(), activation, targets, uuid, toActivate);
    }
    
    protected DynamicCtxVar<String> uuid;
    protected String toActivate;
    
    public CheckHasDelayedSpellAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public CheckHasDelayedSpellAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<String> uuid, String toActivate)
    {
        super(type, activation, targets);
        this.uuid = uuid;
        this.toActivate = toActivate;
    }
    
    public DynamicCtxVar<String> getUuid()
    {
        return uuid;
    }
    
    public String getToActivate()
    {
        return toActivate;
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, EntityTarget target)
    {
        if(!toActivate.isEmpty())
        {
            DelayedSpellHolder.getHolder(target.getEntity()).ifPresent(holder ->
            {
                this.uuid.getValue(ctx).map(s ->
                {
                    try
                    {
                        return UUID.fromString(s);
                    }
                    catch(IllegalArgumentException e)
                    {
                        return null;
                    }
                }).ifPresent(uuid1 ->
                {
                    if(holder.hasDelayedSpell(uuid1))
                    {
                        ctx.activate(toActivate);
                    }
                });
            });
        }
    }
}

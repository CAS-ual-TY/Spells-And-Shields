package de.cas_ual_ty.spells.spell.action.delayed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.DelayedSpellHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;

public class CheckHasDelayedSpellAction extends AffectSingleTypeAction<EntityTarget>
{
    public static Codec<CheckHasDelayedSpellAction> makeCodec(SpellActionType<CheckHasDelayedSpellAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("uuid")).forGetter(CheckHasDelayedSpellAction::getUuid)
        ).apply(instance, (activation, singleTarget, uuid) -> new CheckHasDelayedSpellAction(type, activation, singleTarget, uuid)));
    }
    
    public static CheckHasDelayedSpellAction make(Object activation, Object singleTarget, DynamicCtxVar<String> uuid)
    {
        return new CheckHasDelayedSpellAction(SpellActionTypes.CHECK_HAS_DELAYED_SPELL.get(), activation.toString(), singleTarget.toString(), uuid);
    }
    
    protected DynamicCtxVar<String> uuid;
    
    public CheckHasDelayedSpellAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public CheckHasDelayedSpellAction(SpellActionType<?> type, String activation, String singleTarget, DynamicCtxVar<String> uuid)
    {
        super(type, activation, singleTarget);
        this.uuid = uuid;
    }
    
    public DynamicCtxVar<String> getUuid()
    {
        return uuid;
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, EntityTarget target)
    {
        DelayedSpellHolder.getHolder(target.getEntity()).ifPresent(holder ->
        {
            uuid.getValue(ctx).map(SpellsUtil::uuidFromString).ifPresent(uuid1 ->
            {
                if(!holder.hasDelayedSpell(uuid1))
                {
                    ctx.deactivate(activation);
                }
            });
        });
    }
}

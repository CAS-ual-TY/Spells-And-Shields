package de.cas_ual_ty.spells.spell.action.cooldown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.BuiltinVariables;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class TryCooldownAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<TryCooldownAction> makeCodec(SpellActionType<TryCooldownAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("duration")).forGetter(TryCooldownAction::getDuration)
        ).apply(instance, (activation, duration) -> new TryCooldownAction(type, activation, duration)));
    }

    public static TryCooldownAction make(Object activation, DynamicCtxVar<Integer> duration)
    {
        return new TryCooldownAction(SpellActionTypes.TRY_COOLDOWN.get(), activation.toString(), duration);
    }

    protected DynamicCtxVar<Integer> duration;

    public TryCooldownAction(SpellActionType<?> type)
    {
        super(type);
    }

    public TryCooldownAction(SpellActionType<?> type, String activation, DynamicCtxVar<Integer> duration)
    {
        super(type, activation, BuiltinTargetGroups.OWNER.targetGroup);
        this.duration = duration;
    }

    public DynamicCtxVar<Integer> getDuration()
    {
        return duration;
    }

    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }

    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget target)
    {
        duration.getValue(ctx).ifPresent(duration ->
        {
            ctx.getCtxVar(CtxVarTypes.INT.get(), BuiltinVariables.SPELL_SLOT.name).ifPresent(slot ->
            {
                if(slot >= 0)
                {
                    SpellHolder.getSpellHolder(target.getPlayer()).ifPresent(spellHolder -> spellHolder.setCooldown(slot, duration));
                }
            });
        });
    }
}

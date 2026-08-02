package de.cas_ual_ty.spells.spell.action.cooldown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class SetCooldownAction extends CooldownAction
{
    public static Codec<SetCooldownAction> makeCodec(SpellActionType<SetCooldownAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                slotCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("duration")).forGetter(SetCooldownAction::getDuration)
        ).apply(instance, (activation, target, slot, duration) -> new SetCooldownAction(type, activation, target, slot, duration)));
    }

    public static SetCooldownAction make(Object activation, Object target, DynamicCtxVar<Integer> slot, DynamicCtxVar<Integer> duration)
    {
        return new SetCooldownAction(SpellActionTypes.SET_COOLDOWN.get(), activation.toString(), target.toString(), slot, duration);
    }

    protected DynamicCtxVar<Integer> duration;

    public SetCooldownAction(SpellActionType<?> type)
    {
        super(type);
    }

    public SetCooldownAction(SpellActionType<?> type, String activation, String target, DynamicCtxVar<Integer> slot, DynamicCtxVar<Integer> duration)
    {
        super(type, activation, target, slot);
        this.duration = duration;
    }

    public DynamicCtxVar<Integer> getDuration()
    {
        return duration;
    }

    @Override
    protected void affectCooldown(SpellContext ctx, SpellHolder spellHolder, int slot)
    {
        duration.getValue(ctx).ifPresent(duration -> spellHolder.setCooldown(slot, duration));
    }
}

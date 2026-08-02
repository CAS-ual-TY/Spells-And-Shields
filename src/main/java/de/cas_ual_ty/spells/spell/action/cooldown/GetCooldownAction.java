package de.cas_ual_ty.spells.spell.action.cooldown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class GetCooldownAction extends GetTargetAttributeAction<PlayerTarget>
{
    public static Codec<GetCooldownAction> makeCodec(SpellActionType<GetCooldownAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("slot")).forGetter(GetCooldownAction::getSlot),
                Codec.STRING.fieldOf(ParamNames.var("cooldown")).forGetter(GetCooldownAction::getCooldown)
        ).apply(instance, (activation, source, slot, cooldown) -> new GetCooldownAction(type, activation, source, slot, cooldown)));
    }

    public static GetCooldownAction make(Object activation, Object source, DynamicCtxVar<Integer> slot, String cooldown)
    {
        return new GetCooldownAction(SpellActionTypes.GET_COOLDOWN.get(), activation.toString(), source.toString(), slot, cooldown);
    }

    protected DynamicCtxVar<Integer> slot;
    protected String cooldown;

    public GetCooldownAction(SpellActionType<?> type)
    {
        super(type);
    }

    public GetCooldownAction(SpellActionType<?> type, String activation, String source, DynamicCtxVar<Integer> slot, String cooldown)
    {
        super(type, activation, source);
        this.slot = slot;
        this.cooldown = cooldown;

        if(!cooldown.isEmpty())
        {
            addVariableAttribute((ctx, target) -> slot.getValue(ctx)
                    .filter(s -> s >= 0 && s < SpellHolder.SPELL_SLOTS)
                    .flatMap(s -> SpellHolder.getSpellHolder(target.getPlayer()).map(holder -> holder.getCooldown(s)))
                    .orElse(null), CtxVarTypes.INT.get(), cooldown);
        }
    }

    public DynamicCtxVar<Integer> getSlot()
    {
        return slot;
    }

    public String getCooldown()
    {
        return cooldown;
    }

    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
}

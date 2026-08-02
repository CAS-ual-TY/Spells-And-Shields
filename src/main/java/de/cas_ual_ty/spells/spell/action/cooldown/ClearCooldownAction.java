package de.cas_ual_ty.spells.spell.action.cooldown;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class ClearCooldownAction extends CooldownAction
{
    public static Codec<ClearCooldownAction> makeCodec(SpellActionType<ClearCooldownAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                slotCodec()
        ).apply(instance, (activation, target, slot) -> new ClearCooldownAction(type, activation, target, slot)));
    }

    public static ClearCooldownAction make(Object activation, Object target, DynamicCtxVar<Integer> slot)
    {
        return new ClearCooldownAction(SpellActionTypes.CLEAR_COOLDOWN.get(), activation.toString(), target.toString(), slot);
    }

    public ClearCooldownAction(SpellActionType<?> type)
    {
        super(type);
    }

    public ClearCooldownAction(SpellActionType<?> type, String activation, String target, DynamicCtxVar<Integer> slot)
    {
        super(type, activation, target, slot);
    }

    @Override
    protected void affectCooldown(SpellContext ctx, SpellHolder spellHolder, int slot)
    {
        spellHolder.setCooldown(slot, 0);
    }
}

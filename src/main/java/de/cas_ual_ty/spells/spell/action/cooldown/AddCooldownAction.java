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

public class AddCooldownAction extends CooldownAction
{
    public static Codec<AddCooldownAction> makeCodec(SpellActionType<AddCooldownAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                slotCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(AddCooldownAction::getAmount)
        ).apply(instance, (activation, target, slot, amount) -> new AddCooldownAction(type, activation, target, slot, amount)));
    }

    public static AddCooldownAction make(Object activation, Object target, DynamicCtxVar<Integer> slot, DynamicCtxVar<Integer> amount)
    {
        return new AddCooldownAction(SpellActionTypes.ADD_COOLDOWN.get(), activation.toString(), target.toString(), slot, amount);
    }

    protected DynamicCtxVar<Integer> amount;

    public AddCooldownAction(SpellActionType<?> type)
    {
        super(type);
    }

    public AddCooldownAction(SpellActionType<?> type, String activation, String target, DynamicCtxVar<Integer> slot, DynamicCtxVar<Integer> amount)
    {
        super(type, activation, target, slot);
        this.amount = amount;
    }

    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }

    @Override
    protected void affectCooldown(SpellContext ctx, SpellHolder spellHolder, int slot)
    {
        amount.getValue(ctx).ifPresent(amount -> spellHolder.setCooldown(slot, spellHolder.getCooldown(slot) + amount));
    }
}

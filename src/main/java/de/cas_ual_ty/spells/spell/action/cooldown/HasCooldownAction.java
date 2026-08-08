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
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public class HasCooldownAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<HasCooldownAction> makeCodec(SpellActionType<HasCooldownAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("slot")).forGetter(HasCooldownAction::getSlot)
        ).apply(instance, (activation, source, slot) -> new HasCooldownAction(type, activation, source, slot)));
    }

    public static HasCooldownAction make(Object activation, Object source, DynamicCtxVar<Integer> slot)
    {
        return new HasCooldownAction(SpellActionTypes.HAS_COOLDOWN.get(), activation.toString(), source.toString(), slot);
    }

    protected DynamicCtxVar<Integer> slot;

    public HasCooldownAction(SpellActionType<?> type)
    {
        super(type);
    }

    public HasCooldownAction(SpellActionType<?> type, String activation, String source, DynamicCtxVar<Integer> slot)
    {
        super(type, activation, source);
        this.slot = slot;
    }

    public DynamicCtxVar<Integer> getSlot()
    {
        return slot;
    }

    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }


    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        slot.getValue(ctx).filter(s -> s >= 0 && s < SpellHolder.SPELL_SLOTS).ifPresent(slot1 ->
        {
            SpellHolder.getSpellHolder(playerTarget.getPlayer()).ifPresent(spellHolder ->
            {
                if(spellHolder.getCooldown(slot1) > 0)
                {
                    ctx.deactivate(activation);
                }
            });
        });
    }
}

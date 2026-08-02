package de.cas_ual_ty.spells.spell.action.cooldown;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;

public abstract class CooldownAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static <T extends CooldownAction> RecordCodecBuilder<T, DynamicCtxVar<Integer>> slotCodec()
    {
        return CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("slot")).forGetter(CooldownAction::getSlot);
    }

    protected DynamicCtxVar<Integer> slot;

    public CooldownAction(SpellActionType<?> type)
    {
        super(type);
    }

    public CooldownAction(SpellActionType<?> type, String activation, String target, DynamicCtxVar<Integer> slot)
    {
        super(type, activation, target);
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
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget target)
    {
        slot.getValue(ctx).ifPresent(slot ->
        {
            if(slot >= 0 && slot < SpellHolder.SPELL_SLOTS)
            {
                SpellHolder.getSpellHolder(target.getPlayer()).ifPresent(spellHolder -> affectCooldown(ctx, spellHolder, slot));
            }
        });
    }

    protected abstract void affectCooldown(SpellContext ctx, SpellHolder spellHolder, int slot);
}

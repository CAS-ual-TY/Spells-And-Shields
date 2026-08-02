package de.cas_ual_ty.spells.spell.action.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;

public class GetManaAction extends GetTargetAttributeAction<LivingEntityTarget>
{
    public static Codec<GetManaAction> makeCodec(SpellActionType<GetManaAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.var("mana")).forGetter(GetManaAction::getMana),
                Codec.STRING.fieldOf(ParamNames.var("extra_mana")).forGetter(GetManaAction::getExtraMana),
                Codec.STRING.fieldOf(ParamNames.var("max_mana")).forGetter(GetManaAction::getMaxMana)
        ).apply(instance, (activation, singleTarget, mana, extraMana, maxMana) -> new GetManaAction(type, activation, singleTarget, mana, extraMana, maxMana)));
    }

    public static GetManaAction make(Object activation, Object singleTarget, String mana, String extraMana, String maxMana)
    {
        return new GetManaAction(SpellActionTypes.GET_MANA.get(), activation.toString(), singleTarget.toString(), mana, extraMana, maxMana);
    }

    protected String mana;
    protected String extraMana;
    protected String maxMana;

    public GetManaAction(SpellActionType<?> type)
    {
        super(type);
    }

    public GetManaAction(SpellActionType<?> type, String activation, String singleTarget, String mana, String extraMana, String maxMana)
    {
        super(type, activation, singleTarget);
        this.mana = mana;
        this.extraMana = extraMana;
        this.maxMana = maxMana;

        if(!mana.isEmpty())
        {
            addVariableAttribute(e -> ManaHolder.getManaHolder(e.getLivingEntity()).map(mh -> (double) mh.getUsableMana()).orElse(null), CtxVarTypes.DOUBLE.get(), mana);
        }

        if(!extraMana.isEmpty())
        {
            addVariableAttribute(e -> ManaHolder.getManaHolder(e.getLivingEntity()).map(mh -> (double) mh.getExtraMana()).orElse(null), CtxVarTypes.DOUBLE.get(), extraMana);
        }

        if(!maxMana.isEmpty())
        {
            addVariableAttribute(e -> ManaHolder.getManaHolder(e.getLivingEntity()).map(mh -> (double) mh.getMaxMana()).orElse(null), CtxVarTypes.DOUBLE.get(), maxMana);
        }
    }

    public String getMana()
    {
        return mana;
    }

    public String getExtraMana()
    {
        return extraMana;
    }

    public String getMaxMana()
    {
        return maxMana;
    }

    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
}

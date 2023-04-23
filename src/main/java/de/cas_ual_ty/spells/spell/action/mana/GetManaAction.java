package de.cas_ual_ty.spells.spell.action.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetManaAction extends GetTargetAttributeAction<LivingEntityTarget>
{
    public static Codec<GetManaAction> makeCodec(SpellActionType<GetManaAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.varResult()).forGetter(GetManaAction::getMana)
        ).apply(instance, (activation, singleTarget, mana) -> new GetManaAction(type, activation, singleTarget, mana)));
    }
    
    public static GetManaAction make(Object activation, Object singleTarget, String mana)
    {
        return new GetManaAction(SpellActionTypes.GET_MANA.get(), activation.toString(), singleTarget.toString(), mana);
    }
    
    protected String mana;
    
    public GetManaAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetManaAction(SpellActionType<?> type, String activation, String singleTarget, String mana)
    {
        super(type, activation, singleTarget);
        this.mana = mana;
        
        if(!mana.isEmpty())
        {
            addVariableAttribute(e -> ManaHolder.getManaHolder(e.getLivingEntity()).map(mh -> (double) mh.getMana()).orElse(null), CtxVarTypes.DOUBLE.get(), mana);
        }
    }
    
    public String getMana()
    {
        return mana;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
}

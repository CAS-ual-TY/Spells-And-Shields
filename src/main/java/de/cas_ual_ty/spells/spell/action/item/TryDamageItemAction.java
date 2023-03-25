package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;

public class TryDamageItemAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<TryDamageItemAction> makeCodec(SpellActionType<TryDamageItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("damage")).forGetter(TryDamageItemAction::getDamage),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("success")).forGetter(TryDamageItemAction::getSuccess)
        ).apply(instance, (activation, singleTarget, damage, success) -> new TryDamageItemAction(type, activation, singleTarget, damage, success)));
    }
    
    public static TryDamageItemAction make(String activation, String singleTarget, DynamicCtxVar<Integer> damage, String success)
    {
        return new TryDamageItemAction(SpellActionTypes.TRY_DAMAGE_ITEM.get(), activation, singleTarget, damage, success);
    }
    
    protected DynamicCtxVar<Integer> damage;
    protected String success;
    
    public TryDamageItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TryDamageItemAction(SpellActionType<?> type, String activation, String singleTarget, DynamicCtxVar<Integer> damage, String success)
    {
        super(type, activation, singleTarget);
        this.damage = damage;
        this.success = success;
    }
    
    public DynamicCtxVar<Integer> getDamage()
    {
        return damage;
    }
    
    public String getSuccess()
    {
        return success;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        damage.getValue(ctx).ifPresent(damage ->
        {
            if(itemTarget.getItem().getMaxDamage() - itemTarget.getItem().getDamageValue() < damage)
            {
                return;
            }
            
            ctx.activate(success);
            
            if(!itemTarget.isCreative())
            {
                itemTarget.getItem().hurt(damage, SpellsUtil.RANDOM, null);
            }
        });
        
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
}

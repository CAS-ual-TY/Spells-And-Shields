package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;

public class DamageItemAction extends AffectTypeAction<ItemTarget>
{
    public static Codec<DamageItemAction> makeCodec(SpellActionType<DamageItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("damage")).forGetter(DamageItemAction::getDamage)
        ).apply(instance, (activation, multiTargets, damage) -> new DamageItemAction(type, activation, multiTargets, damage)));
    }
    
    public static DamageItemAction make(String activation, String multiTargets, DynamicCtxVar<Integer> damage)
    {
        return new DamageItemAction(SpellActionTypes.DAMAGE_ITEM.get(), activation, multiTargets, damage);
    }
    
    protected DynamicCtxVar<Integer> damage;
    
    public DamageItemAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public DamageItemAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> damage)
    {
        super(type, activation, multiTargets);
        this.damage = damage;
    }
    
    public DynamicCtxVar<Integer> getDamage()
    {
        return damage;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        damage.getValue(ctx).ifPresent(damage ->
        {
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

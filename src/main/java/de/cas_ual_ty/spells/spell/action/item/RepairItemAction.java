package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class RepairItemAction extends AffectTypeAction<ItemTarget>
{
    public static Codec<RepairItemAction> makeCodec(SpellActionType<RepairItemAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(RepairItemAction::getAmount)
        ).apply(instance, (activation, multiTargets, amount) -> new RepairItemAction(type, activation, multiTargets, amount)));
    }

    public static RepairItemAction make(Object activation, Object multiTargets, DynamicCtxVar<Integer> amount)
    {
        return new RepairItemAction(SpellActionTypes.REPAIR_ITEM.get(), activation.toString(), multiTargets.toString(), amount);
    }

    protected DynamicCtxVar<Integer> amount;

    public RepairItemAction(SpellActionType<?> type)
    {
        super(type);
    }

    public RepairItemAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<Integer> amount)
    {
        super(type, activation, multiTargets);
        this.amount = amount;
    }

    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        amount.getValue(ctx).ifPresent(amount ->
        {
            if(!itemTarget.isCreative())
            {
                ItemStack item = itemTarget.getItem();
                item.setDamageValue(Mth.clamp(item.getDamageValue() - amount, 0, item.getMaxDamage()));
            }
        });
    }

    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
}

package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.player.Player;

public class ReplenishManaAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<ReplenishManaAction> makeCodec(SpellActionType<ReplenishManaAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("mana_amount")).forGetter(ReplenishManaAction::getAmount)
        ).apply(instance, (activation, targets, amount) -> new ReplenishManaAction(type, activation, targets, amount)));
    }
    
    protected DynamicCtxVar<Double> amount;
    
    public ReplenishManaAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ReplenishManaAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Double> amount)
    {
        super(type, activation, targets);
        this.amount = amount;
    }
    
    public ReplenishManaAction(SpellActionType<?> type, BuiltinActivations activation, BuiltinTargetGroups targets, double amount)
    {
        this(type, activation.activation, targets.targetGroup, CtxVarTypes.DOUBLE.get().refImm(amount));
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public DynamicCtxVar<Double> getAmount()
    {
        return amount;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        if(!(target.getLivingEntity() instanceof Player player && player.isCreative()))
        {
            amount.getValue(ctx).ifPresent(amount ->
            {
                ManaHolder.getManaHolder(target.getLivingEntity()).ifPresent(manaHolder ->
                {
                    manaHolder.replenish(amount.floatValue());
                });
            });
        }
    }
}

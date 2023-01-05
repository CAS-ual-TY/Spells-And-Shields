package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.player.Player;

public class BurnManaAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<BurnManaAction> makeCodec(SpellActionType<BurnManaAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("mana_amount")).forGetter(BurnManaAction::getAmount)
        ).apply(instance, (activation, targets, amount) -> new BurnManaAction(type, activation, targets, amount)));
    }
    
    public static BurnManaAction make(String activation, String targets, DynamicCtxVar<Double> amount)
    {
        return new BurnManaAction(SpellActionTypes.BURN_MANA.get(), activation, targets, amount);
    }
    
    protected DynamicCtxVar<Double> amount;
    
    public BurnManaAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public BurnManaAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Double> amount)
    {
        super(type, activation, targets);
        this.amount = amount;
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
                    manaHolder.burn(amount.floatValue());
                });
            });
        }
    }
}

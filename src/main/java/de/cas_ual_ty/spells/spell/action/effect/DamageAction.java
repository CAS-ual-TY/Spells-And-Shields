package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.damagesource.DamageSource;

public class DamageAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<DamageAction> makeCodec(SpellActionType<DamageAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("damage")).forGetter(DamageAction::getDamage)
        ).apply(instance, (activation, targets, damage) -> new DamageAction(type, activation, targets, damage)));
    }
    
    public static DamageAction make(String activation, String targets, DynamicCtxVar<Double> damage)
    {
        return new DamageAction(SpellActionTypes.DAMAGE.get(), activation, targets, damage);
    }
    
    protected DynamicCtxVar<Double> damage;
    
    public DamageAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public DamageAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Double> damage)
    {
        super(type, activation, targets);
        this.damage = damage;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public DynamicCtxVar<Double> getDamage()
    {
        return damage;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        damage.getValue(ctx).ifPresent(damage ->
        {
            target.getLivingEntity().hurt(ctx.owner != null ? DamageSource.indirectMagic(ctx.owner, null) : DamageSource.MAGIC, damage.floatValue());
        });
    }
}

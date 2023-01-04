package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.damagesource.DamageSource;

public class SourcedDamageAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<SourcedDamageAction> makeCodec(SpellActionType<SourcedDamageAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramDouble("damage")).forGetter(SourcedDamageAction::getDamage),
                Codec.STRING.fieldOf(ParamNames.singleTarget("source")).forGetter(SourcedDamageAction::getSource)
        ).apply(instance, (activation, targets, damage, source) -> new SourcedDamageAction(type, activation, targets, damage, source)));
    }
    
    protected DynamicCtxVar<Double> damage;
    protected String source;
    
    public SourcedDamageAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SourcedDamageAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Double> damage, String source)
    {
        super(type, activation, targets);
        this.damage = damage;
        this.source = source;
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
    
    public String getSource()
    {
        return source;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        damage.getValue(ctx).ifPresent(damage ->
        {
            ctx.getTargetGroup(source).getSingleTarget(source ->
            {
                TargetTypes.ENTITY.get().ifType(source, entityTarget ->
                {
                    target.getLivingEntity().hurt(ctx.owner != null ? DamageSource.indirectMagic(ctx.owner, entityTarget.getEntity()) : DamageSource.MAGIC, damage.floatValue());
                });
            });
        });
    }
}

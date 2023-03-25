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
import net.minecraft.world.phys.Vec3;

public class SourcedKnockbackAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<SourcedKnockbackAction> makeCodec(SpellActionType<SourcedKnockbackAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                CtxVarTypes.DOUBLE.get().refCodec().fieldOf(ParamNames.paramVec3("strength")).forGetter(SourcedKnockbackAction::getStrength),
                Codec.STRING.fieldOf(ParamNames.singleTarget("source")).forGetter(SourcedKnockbackAction::getSource)
        ).apply(instance, (activation, targets, strength, source) -> new SourcedKnockbackAction(type, activation, targets, strength, source)));
    }
    
    public static SourcedKnockbackAction make(String activation, String targets, DynamicCtxVar<Double> strength, String source)
    {
        return new SourcedKnockbackAction(SpellActionTypes.SOURCED_KNOCKBACK.get(), activation, targets, strength, source);
    }
    
    protected DynamicCtxVar<Double> strength;
    protected String source;
    
    public SourcedKnockbackAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public SourcedKnockbackAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<Double> strength, String source)
    {
        super(type, activation, targets);
        this.strength = strength;
        this.source = source;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    public DynamicCtxVar<Double> getStrength()
    {
        return strength;
    }
    
    public String getSource()
    {
        return source;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        strength.getValue(ctx).ifPresent(strength ->
        {
            ctx.getTargetGroup(source).getSingleTarget(t ->
            {
                TargetTypes.POSITION.get().ifType(t, source ->
                {
                    Vec3 vec = target.getPosition().subtract(source.getPosition()).multiply(1, 0, 1);
                    target.getLivingEntity().knockback(strength, vec.x, vec.z);
                });
            });
        });
    }
}

package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.CtxVarRef;
import net.minecraft.world.damagesource.DamageSource;

public class DamageAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<DamageAction> makeCodec(SpellActionType<DamageAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.makeActivation(),
                AffectTypeAction.makeTargetsCodec(),
                SpellsRegistries.DOUBLE_CTX_VAR.get().refCodec().fieldOf("damage").forGetter(DamageAction::getDamage)
        ).apply(instance, (activation, targets, damage) -> new DamageAction(type, activation, targets, damage)));
    }
    
    protected CtxVarRef<Double> damage;
    
    public DamageAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public DamageAction(SpellActionType<?> type, String activation, String targets, CtxVarRef<Double> damage)
    {
        super(type, activation, targets, SpellsRegistries.LIVING_ENTITY_TARGET.get());
        this.damage = damage;
    }
    
    public DamageAction(SpellActionType<?> type, BuiltinActivations activation, BuiltinTargetGroups targets, double damage)
    {
        this(type, activation.activation, targets.targetGroup, SpellsRegistries.DOUBLE_CTX_VAR.get().ref(damage));
    }
    
    public CtxVarRef<Double> getDamage()
    {
        return damage;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, LivingEntityTarget target)
    {
        damage.getValue(ctx).ifPresent(damage ->
        {
            target.getLivingEntity().hurt(ctx.spellHolder != null ? DamageSource.indirectMagic(ctx.spellHolder.getPlayer(), null) : DamageSource.MAGIC, damage.floatValue());
        });
    }
}

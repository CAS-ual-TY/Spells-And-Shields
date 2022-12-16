package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import net.minecraft.world.damagesource.DamageSource;

public class DamageAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<DamageAction> makeCodec(SpellActionType<DamageAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("activation").forGetter(DamageAction::getActivations),
                Codec.STRING.fieldOf("targets").forGetter(DamageAction::getTargets),
                Codec.DOUBLE.fieldOf("damage").forGetter(DamageAction::getDamage)
        ).apply(instance, (activation, targets, damage) -> new DamageAction(type, activation, targets, damage)));
    }
    
    protected double damage;
    
    public DamageAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public DamageAction(SpellActionType<?> type, String activation, String targets, double damage)
    {
        super(type, activation, targets, SpellsRegistries.LIVING_ENTITY_TARGET.get());
        this.damage = damage;
    }
    
    public double getDamage()
    {
        return damage;
    }
    
    @Override
    public void affectTarget(SpellContext ctx, LivingEntityTarget target)
    {
        target.getLivingEntity().hurt(ctx.spellHolder != null ? DamageSource.indirectMagic(ctx.spellHolder.getPlayer(), null) : DamageSource.MAGIC, (float) damage);
    }
}

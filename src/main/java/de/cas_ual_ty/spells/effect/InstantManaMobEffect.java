package de.cas_ual_ty.spells.effect;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class InstantManaMobEffect extends MobEffect
{
    public InstantManaMobEffect(MobEffectCategory mobEffectCategory, int color)
    {
        super(mobEffectCategory, color);
    }

    @Override
    public boolean isInstantenous()
    {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier)
    {
        return duration >= 1;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity directSource, @Nullable Entity indirectSource, LivingEntity entity, int level, double strength)
    {
        ManaHolder.getManaHolder(entity).ifPresent(manaHolder ->
        {
            if(this == BuiltInRegisters.INSTANT_MANA_EFFECT.get())
            {
                int health = (int) (strength * (double) (4 << level) + 0.5D);
                manaHolder.replenish(health);
            }
            else if(this == BuiltInRegisters.MANA_BOMB_EFFECT.get())
            {
                int damage = (int) (strength * (double) (6 << level) + 0.5D);
                manaHolder.burn(damage);
            }
            else
            {
                applyEffectTick(entity, level);
            }
        });
    }
}

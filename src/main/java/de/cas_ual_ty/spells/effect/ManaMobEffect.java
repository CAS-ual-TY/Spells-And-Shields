package de.cas_ual_ty.spells.effect;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.BuiltInRegisters;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ManaMobEffect extends MobEffect
{
    public ManaMobEffect(MobEffectCategory mobEffectCategory, int color)
    {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int level)
    {
        ManaHolder.getManaHolder(entity).ifPresent(manaHolder ->
        {
            if(this == BuiltInRegisters.REPLENISHMENT_EFFECT.get())
            {
                manaHolder.replenish(1F);
            }
            else if(this == BuiltInRegisters.LEAKING_MOB_EFFECT.get())
            {
                manaHolder.burn(1F);
            }
        });
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier)
    {
        if(this == BuiltInRegisters.REPLENISHMENT_EFFECT.get())
        {
            int k = 50 >> amplifier;
            return k > 0 ? duration % k == 0 : true;
        }
        else if(this == BuiltInRegisters.LEAKING_MOB_EFFECT.get())
        {
            int j = 25 >> amplifier;
            return j > 0 ? duration % j == 0 : true;
        }
        else
        {
            return false;
        }
    }
}

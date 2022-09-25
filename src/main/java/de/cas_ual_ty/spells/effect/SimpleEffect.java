package de.cas_ual_ty.spells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SimpleEffect extends MobEffect
{
    public SimpleEffect(MobEffectCategory mobEffectCategory, int color)
    {
        super(mobEffectCategory, color);
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier)
    {
        return false;
    }
}
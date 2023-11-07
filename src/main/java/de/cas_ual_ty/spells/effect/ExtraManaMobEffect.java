package de.cas_ual_ty.spells.effect;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ExtraManaMobEffect extends MobEffect
{
    public ExtraManaMobEffect(MobEffectCategory mobEffectCategory, int color)
    {
        super(mobEffectCategory, color);
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier)
    {
        return true;
    }
    
    @Override
    public void onEffectStarted(LivingEntity livingEntity, int amplifier)
    {
        super.onEffectStarted(livingEntity, amplifier);
        ManaHolder.getManaHolder(livingEntity).ifPresent(manaHolder -> manaHolder.setExtraMana(manaHolder.getExtraMana() + (float) (4 * (amplifier + 1))));
    }
}

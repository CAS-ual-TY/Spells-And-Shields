package de.cas_ual_ty.spells.effect;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.registers.BuiltinRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.LazyOptional;

public class ManaMobEffect extends MobEffect
{
    public ManaMobEffect(MobEffectCategory mobEffectCategory, int color)
    {
        super(mobEffectCategory, color);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int level)
    {
        LazyOptional<ManaHolder> manaHolder = ManaHolder.getManaHolder(entity);
        
        if(!manaHolder.isPresent())
        {
            return;
        }
        
        ManaHolder.getManaHolder(entity).ifPresent(manaHolder1 ->
        {
            if(this == BuiltinRegistries.REPLENISHMENT_EFFECT.get())
            {
                manaHolder1.replenish(1F);
            }
            else if(this == BuiltinRegistries.LEAKING_MOB_EFFECT.get())
            {
                manaHolder1.burn(1F);
            }
        });
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier)
    {
        if(this == BuiltinRegistries.REPLENISHMENT_EFFECT.get())
        {
            int k = 50 >> amplifier;
            if(k > 0)
            {
                return duration % k == 0;
            }
            else
            {
                return true;
            }
        }
        else if(this == BuiltinRegistries.LEAKING_MOB_EFFECT.get())
        {
            int j = 25 >> amplifier;
            if(j > 0)
            {
                return duration % j == 0;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
}

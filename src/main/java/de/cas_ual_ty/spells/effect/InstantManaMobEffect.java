package de.cas_ual_ty.spells.effect;

import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;

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
    public boolean isDurationEffectTick(int duration, int amplifier)
    {
        return duration >= 1;
    }
    
    @Override
    public void applyInstantenousEffect(@Nullable Entity directSource, @Nullable Entity indirectSource, LivingEntity entity, int level, double strength)
    {
        // directSource = eg. area of effect cloud
        // indirectSource = eg. player who threw potion
        // 0 < strength <= 1.0D
        
        LazyOptional<ManaHolder> manaHolder = ManaHolder.getManaHolder(entity);
        
        if(!manaHolder.isPresent())
        {
            this.applyEffectTick(entity, level);
        }
        
        ManaHolder.getManaHolder(entity).ifPresent(manaHolder1 ->
        {
            if(this == SpellsRegistries.INSTANT_MANA_EFFECT.get())
            {
                int health = (int) (strength * (double) (4 << level) + 0.5D);
                manaHolder1.replenish(health);
            }
            else if(this == SpellsRegistries.MANA_BOMB_EFFECT.get())
            {
                int damage = (int) (strength * (double) (6 << level) + 0.5D);
                manaHolder1.burn(damage);
            }
            else
            {
                this.applyEffectTick(entity, level);
            }
        });
    }
}

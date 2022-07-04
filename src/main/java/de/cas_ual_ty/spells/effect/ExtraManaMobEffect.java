package de.cas_ual_ty.spells.effect;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class ExtraManaMobEffect extends MobEffect
{
    public ExtraManaMobEffect(MobEffectCategory mobEffectCategory, int color)
    {
        super(mobEffectCategory, color);
    }
    
    @Override
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier)
    {
        ManaHolder.getManaHolder(livingEntity).ifPresent(manaHolder -> manaHolder.setExtraMana(manaHolder.getExtraMana() - (float) (4 * (amplifier + 1))));
        super.removeAttributeModifiers(livingEntity, attributeMap, amplifier);
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier)
    {
        ManaHolder.getManaHolder(livingEntity).ifPresent(manaHolder -> manaHolder.setExtraMana(manaHolder.getExtraMana() + (float) (4 * (amplifier + 1))));
        super.addAttributeModifiers(livingEntity, attributeMap, amplifier);
    }
}

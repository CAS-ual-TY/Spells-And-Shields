package de.cas_ual_ty.spells.enchantment;

import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;

public class ManaBladeEnchantment extends Enchantment
{
    private static final int MIN_COST = 5;
    private static final int LEVEL_COST = 8;
    private static final int LEVEL_COST_SPAN = 20;
    
    public ManaBladeEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots)
    {
        super(rarity, EnchantmentCategory.WEAPON, equipmentSlots);
    }
    
    @Override
    public int getMinCost(int level)
    {
        return MIN_COST + (level - 1) * LEVEL_COST;
    }
    
    @Override
    public int getMaxCost(int level)
    {
        return this.getMinCost(level) + LEVEL_COST_SPAN;
    }
    
    @Override
    public int getMaxLevel()
    {
        return 5;
    }
    
    @Override
    public boolean checkCompatibility(Enchantment enchantment)
    {
        return !(enchantment instanceof DamageEnchantment);
    }
    
    @Override
    public boolean canEnchant(ItemStack itemStack)
    {
        return itemStack.getItem() instanceof AxeItem ? true : super.canEnchant(itemStack);
    }
    
    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level)
    {
        if(target instanceof LivingEntity livingEntity && level > 0)
        {
            ManaHolder.getManaHolder(user).ifPresent(manaHolder ->
            {
                if(manaHolder.getMana() > 2F)
                {
                    float damage = Math.min(manaHolder.getMana(), (float) level * 2.5F);
                    
                    manaHolder.burn(damage);
                    livingEntity.hurt(DamageSource.indirectMagic(user, null), damage);
                    
                    RandomSource random = user.getRandom();
                    
                    int i = 20 + random.nextInt(10 * level);
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, i, 3));
                    
                    Vec3 eyes = livingEntity.getEyePosition();
                    Vec3 spread = new Vec3(0.5, 0.5, 0.5);
                    
                    for(i = 0; i < 10; i++)
                    {
                        livingEntity.level.addParticle(ParticleTypes.ENCHANTED_HIT, eyes.x + random.nextGaussian() * spread.x, eyes.y + random.nextGaussian() * spread.y, eyes.z + random.nextGaussian() * spread.z, 0, 0, 0);
                    }
                }
            });
        }
        
    }
}
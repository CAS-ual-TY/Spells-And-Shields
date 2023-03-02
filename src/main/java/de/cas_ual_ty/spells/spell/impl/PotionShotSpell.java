package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IProjectileSpell;
import de.cas_ual_ty.spells.spell.base.HandIngredientSpell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class PotionShotSpell extends HandIngredientSpell implements IProjectileSpell
{
    public static final String KEY_EFFECTS = "Effects";
    
    public PotionShotSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public PotionShotSpell()
    {
        super(2F);
    }
    
    @Override
    public void perform(ManaHolder manaHolder, ItemStack itemStack)
    {
        shootStraight(manaHolder, (projectile, level) ->
        {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(itemStack);
            
            ListTag tags = new ListTag();
            
            for(MobEffectInstance effect : effects)
            {
                tags.add(effect.save(new CompoundTag()));
            }
            
            projectile.getSpellDataTag().put(KEY_EFFECTS, tags);
        });
    }
    
    @Override
    public void projectileHitEntity(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        if(entityHitResult.getEntity() instanceof LivingEntity target)
        {
            if(entity.getSpellDataTag().get(KEY_EFFECTS) instanceof ListTag tags)
            {
                tags.stream()
                        .filter(tag -> tag instanceof CompoundTag)
                        .map(tag -> MobEffectInstance.load((CompoundTag) tag))
                        .filter(e -> e != null)
                        .forEach(target::addEffect);
            }
            
            IProjectileSpell.super.projectileHitEntity(entity, entityHitResult);
        }
    }
    
    @Override
    public boolean checkHandIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return itemStack.getItem() == Items.POTION;
    }
    
    @Override
    public void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack)
    {
        if(manaHolder.getPlayer().getMainHandItem() == itemStack)
        {
            manaHolder.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        }
        else if(manaHolder.getPlayer().getOffhandItem() == itemStack)
        {
            manaHolder.getPlayer().setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLASS_BOTTLE));
        }
    }
    
    @Override
    public void projectileTick(SpellProjectile entity)
    {
    
    }
}

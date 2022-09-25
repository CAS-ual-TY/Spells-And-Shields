package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class FireChargeSpell extends BaseIngredientsSpell
{
    public FireChargeSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        super(manaCost, handIngredients, inventoryIngredients);
    }
    
    public FireChargeSpell(float manaCost, ItemStack handIngredient)
    {
        super(manaCost, handIngredient);
    }
    
    public FireChargeSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public FireChargeSpell()
    {
        this(4F, new ItemStack(Items.FIRE_CHARGE));
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        Level level = manaHolder.getPlayer().level;
        
        Fireball fireball = new LargeFireball(EntityType.FIREBALL, level);
        fireball.setPos(manaHolder.getPlayer().getEyePosition()
                .add(manaHolder.getPlayer().getViewVector(1.0F).scale(2D))
                .add(manaHolder.getPlayer().getDeltaMovement()));
        fireball.shootFromRotation(manaHolder.getPlayer(), manaHolder.getPlayer().getXRot(), manaHolder.getPlayer().getYRot(), 0.0F, 3.0F, 1.0F);
        fireball.setOwner(manaHolder.getPlayer());
        
        level.playSound(null, manaHolder.getPlayer(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        level.addFreshEntity(fireball);
    }
}

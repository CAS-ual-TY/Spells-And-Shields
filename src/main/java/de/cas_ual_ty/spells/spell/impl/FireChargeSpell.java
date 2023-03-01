package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
        LivingEntity entity = manaHolder.getPlayer();
        
        Vec3 view = entity.getViewVector(1F).scale(2D);
        Fireball fireball = new LargeFireball(level, entity, view.x, view.y, view.z, 1);
        fireball.setPos(entity.getEyePosition()
                .add(view)
                .add(entity.getDeltaMovement()));
        
        level.playSound(null, manaHolder.getPlayer(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1F, 1F);
        
        level.addFreshEntity(fireball);
    }
}

package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.HandIngredientSpell;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BlowArrowSpell extends HandIngredientSpell
{
    public BlowArrowSpell(float manaCost)
    {
        super(manaCost);
    }
    
    @Override
    public void perform(ManaHolder manaHolder, ItemStack itemStack)
    {
        Level level = manaHolder.getPlayer().level;
        
        ArrowItem arrowItem = (ArrowItem) (itemStack.getItem() instanceof ArrowItem ? itemStack.getItem() : Items.ARROW);
        
        AbstractArrow arrow = arrowItem.createArrow(level, itemStack, manaHolder.getPlayer());
        arrow.shootFromRotation(manaHolder.getPlayer(), manaHolder.getPlayer().getXRot(), manaHolder.getPlayer().getYRot(), 0.0F, 3.0F, 1.0F);
        arrow.setCritArrow(true);
        
        if(manaHolder.getPlayer() instanceof Player player)
        {
            if(player.getAbilities().instabuild && (itemStack.is(Items.SPECTRAL_ARROW) || itemStack.is(Items.TIPPED_ARROW)))
            {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
        }
        else
        {
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
        
        level.addFreshEntity(arrow);
    }
    
    @Override
    public boolean checkHandIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return BowItem.ARROW_ONLY.test(itemStack);
    }
    
    @Override
    public void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack)
    {
        itemStack.shrink(1);
    }
}

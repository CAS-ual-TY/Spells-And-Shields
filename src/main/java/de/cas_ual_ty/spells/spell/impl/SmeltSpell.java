package de.cas_ual_ty.spells.spell.impl;

import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.HandIngredientSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SmeltSpell extends HandIngredientSpell
{
    public SmeltSpell(float manaCost)
    {
        super(manaCost);
    }
    
    public SmeltSpell()
    {
        super(4F);
    }
    
    @Override
    public void perform(ManaHolder manaHolder, ItemStack itemStack)
    {
        Level level = manaHolder.getPlayer().level;
        Optional<BlastingRecipe> blastingRecipe = getBlastingRecipe(manaHolder.getPlayer().level, itemStack);
        
        if(manaHolder.getPlayer() instanceof Player player && blastingRecipe.isPresent())
        {
            player.getInventory().placeItemBackInInventory(blastingRecipe.get().getResultItem().copy());
        }
        
        Vec3 position = manaHolder.getPlayer().position().add(0D, manaHolder.getPlayer().getEyeHeight() * 0.5D, 0D);
        
        if(level instanceof ServerLevel serverLevel)
        {
            final int count = 3;
            final double spread = 0.5D;
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, position.x, position.y, position.z, count, manaHolder.getPlayer().getRandom().nextGaussian() * spread, manaHolder.getPlayer().getRandom().nextGaussian() * spread, manaHolder.getPlayer().getRandom().nextGaussian() * spread, 0D);
        }
    }
    
    @Override
    public boolean checkHandIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return getBlastingRecipe(manaHolder.getPlayer().level, itemStack).isPresent();
    }
    
    public int getRequiredCount(ItemStack itemStack)
    {
        return 8;
    }
    
    @Override
    public void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack)
    {
        itemStack.setCount(itemStack.getCount() - 1);
    }
    
    public static Optional<BlastingRecipe> getBlastingRecipe(Level level, ItemStack itemStack)
    {
        return level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(itemStack), level);
    }
}

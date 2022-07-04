package de.cas_ual_ty.spells.recipe;

import de.cas_ual_ty.spells.SpellsRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TippedSpearRecipe extends CustomRecipe
{
    public TippedSpearRecipe(ResourceLocation resourceLocation) //TODO
    {
        super(resourceLocation);
    }
    
    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level)
    {
        boolean spearFound = false;
        boolean potionFound = false;
        
        if(craftingContainer.getWidth() * craftingContainer.getHeight() == 2)
        {
            for(int i = 0; i < craftingContainer.getWidth(); ++i)
            {
                for(int j = 0; j < craftingContainer.getHeight(); ++j)
                {
                    ItemStack itemstack = craftingContainer.getItem(i + j * craftingContainer.getWidth());
                    
                    if(itemstack.isEmpty())
                    {
                        return false;
                    }
                    
                    if(i == 1 && j == 1)
                    {
                        if(!itemstack.is(Items.LINGERING_POTION))
                        {
                            return false;
                        }
                    }
                    else if(!itemstack.is(Items.ARROW))
                    {
                        return false;
                    }
                }
            }
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public ItemStack assemble(CraftingContainer craftingContainer)
    {
        ItemStack itemstack = craftingContainer.getItem(1 + craftingContainer.getWidth());
        
        if(!itemstack.is(Items.LINGERING_POTION))
        {
            return ItemStack.EMPTY;
        }
        else
        {
            ItemStack itemstack1 = new ItemStack(Items.TIPPED_ARROW, 8);
            PotionUtils.setPotion(itemstack1, PotionUtils.getPotion(itemstack));
            PotionUtils.setCustomEffects(itemstack1, PotionUtils.getCustomEffects(itemstack));
            return itemstack1;
        }
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width >= 2 && height >= 2;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SpellsRegistries.TIPPED_SPEAR.get();
    }
}

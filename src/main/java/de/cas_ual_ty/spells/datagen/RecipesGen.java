package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellsRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;

import java.util.function.Consumer;

public class RecipesGen extends RecipeProvider
{
    public RecipesGen(DataGenerator dataGen)
    {
        super(dataGen);
    }
    
    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
    {
        SpecialRecipeBuilder.special(SpellsRegistries.TIPPED_SPEAR.get()).save(consumer, "tipped_spear");
    }
}

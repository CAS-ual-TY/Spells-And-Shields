package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellsAndShields;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStatesGen extends BlockStateProvider
{
    public BlockStatesGen(DataGenerator dataGen, ExistingFileHelper fileHelper)
    {
        super(dataGen, SpellsAndShields.MOD_ID, fileHelper);
    }
    
    @Override
    protected void registerStatesAndModels()
    {
    
    }
}

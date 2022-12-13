package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.spelltree.SpellTrees;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpellTreesGen implements DataProvider
{
    protected Map<ResourceLocation, SpellTree> spellTrees;
    
    protected DataGenerator gen;
    protected String modid;
    protected ExistingFileHelper exFileHelper;
    
    public SpellTreesGen(DataGenerator gen, String modid, ExistingFileHelper exFileHelper)
    {
        this.gen = gen;
        this.modid = modid;
        this.exFileHelper = exFileHelper;
        spellTrees = new HashMap<>();
    }
    
    public void addSpellTree(String key, SpellTree spellTree)
    {
        addSpellTree(new ResourceLocation(modid, key), spellTree);
    }
    
    public void addSpellTree(ResourceLocation key, SpellTree spellTree)
    {
        spellTrees.put(key, spellTree);
    }
    
    public void addSpellTrees()
    {
        addSpellTree("nether", SpellTrees.fireTree());
        addSpellTree("ocean", SpellTrees.waterTree());
        addSpellTree("mining", SpellTrees.earthTree());
        addSpellTree("movement", SpellTrees.airTree());
        addSpellTree("end", SpellTrees.enderTree());
    }
    
    @Override
    public void run(CachedOutput pOutput) throws IOException
    {
        DataGenerator generator = gen;
        ExistingFileHelper existingFileHelper = exFileHelper;
        RegistryAccess registryAccess = RegistryAccess.builtinCopy();
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        
        addSpellTrees();
        
        JsonCodecProvider<SpellTree> provider = JsonCodecProvider.forDatapackRegistry(gen, existingFileHelper, SpellsAndShields.MOD_ID, registryOps, SpellTrees.SPELL_TREES_REGISTRY_KEY, spellTrees);
        provider.run(pOutput);
    }
    
    @Override
    public String getName()
    {
        return "Spells & Shields Spell Trees Files";
    }
}

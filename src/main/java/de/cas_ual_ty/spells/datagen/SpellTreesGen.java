package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import de.cas_ual_ty.spells.util.SpellsDowngrade;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SpellTreesGen implements DataProvider
{
    
    protected Map<ResourceLocation, SpellTree> spellTrees;
    
    protected DataGenerator gen;
    protected String modId;
    protected ExistingFileHelper exFileHelper;
    protected RegistryAccess registryAccess;
    protected RegistryOps<JsonElement> registryOps;
    
    protected Registry<Spell> registry;
    
    public SpellTreesGen(DataGenerator gen, String modId, ExistingFileHelper exFileHelper)
    {
        this.gen = gen;
        this.modId = modId;
        this.exFileHelper = exFileHelper;
        registryAccess = RegistryAccess.builtinCopy();
        registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        
        spellTrees = new HashMap<>();
    }
    
    public void addSpellTree(String key, SpellTree spellTree)
    {
        addSpellTree(new ResourceLocation(modId, key), spellTree);
    }
    
    public void addSpellTree(ResourceLocation key, SpellTree spellTree)
    {
        spellTrees.put(key, spellTree);
    }
    
    public void addSpellTrees()
    {
        addSpellTree("nether", SpellTrees.fireTree(this::spellRef));
        addSpellTree("ocean", SpellTrees.waterTree(this::spellRef));
        addSpellTree("mining", SpellTrees.earthTree(this::spellRef));
        addSpellTree("movement", SpellTrees.airTree(this::spellRef));
        addSpellTree("end", SpellTrees.enderTree(this::spellRef));
    }
    
    protected Holder<Spell> spellRef(ResourceLocation spell)
    {
        return Holder.Reference.createStandAlone(registry, ResourceKey.create(registry.key(), spell));
    }
    
    @Override
    public void run(HashCache pOutput) throws IOException
    {
        registry = registryOps.registry(Spells.REGISTRY_KEY).orElseThrow();
        addSpellTrees();
        save(pOutput);
    }
    
    private void save(HashCache pOutput)
    {
        Path path = gen.getOutputFolder();
        spellTrees.forEach((rl, spellTree) -> {
            Path path1 = createPath(path, rl);
            try
            {
                DataProvider.save(SpellsDowngrade.GSON, pOutput, SpellsCodecs.SPELL_TREE_CONTENTS.encodeStart(JsonOps.INSTANCE, spellTree).getOrThrow(false, SpellsAndShields.LOGGER::error), path1);
            }
            catch(IOException ioexception)
            {
                SpellsAndShields.LOGGER.error("Couldn't save spell tree {}", path1, ioexception);
            }
        });
    }
    
    private static Path createPath(Path pPath, ResourceLocation pId)
    {
        return pPath.resolve("data/" + pId.getNamespace() + "/" + SpellsAndShields.MOD_ID + "/spell_trees/" + pId.getPath() + ".json");
    }
    
    @Override
    public String getName()
    {
        return "Spells & Shields Spell Trees Files";
    }
}

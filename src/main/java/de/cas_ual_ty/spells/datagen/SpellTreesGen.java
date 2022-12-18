package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
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
        this.registryAccess = RegistryAccess.builtinCopy();
        this.registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        
        spellTrees = new HashMap<>();
        registry = registryOps.registry(Spells.REGISTRY_KEY).orElseThrow();
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
        addSpellTree("debug", SpellTrees.debugTree(spellRef(Spells.TEST)));
        
        /*addSpellTree("nether", SpellTrees.fireTree());
        addSpellTree("ocean", SpellTrees.waterTree());
        addSpellTree("mining", SpellTrees.earthTree());
        addSpellTree("movement", SpellTrees.airTree());
        addSpellTree("end", SpellTrees.enderTree());*/
    }
    
    protected Holder<Spell> spellRef(ResourceLocation spell)
    {
        return Holder.Reference.createStandAlone(registry, ResourceKey.create(registry.key(), spell));
    }
    
    @Override
    public void run(CachedOutput pOutput) throws IOException
    {
        addSpellTrees();
        JsonCodecProvider<SpellTree> provider = JsonCodecProvider.forDatapackRegistry(gen, exFileHelper, modId, registryOps, SpellTrees.REGISTRY_KEY, spellTrees);
        provider.run(pOutput);
    }
    
    @Override
    public String getName()
    {
        return "Spells & Shields Spell Trees Files";
    }
}

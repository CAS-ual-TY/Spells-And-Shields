package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.registers.SpellTrees;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class SpellTreesGen
{
    protected String modId;
    protected final BootstapContext<SpellTree> context;
    
    protected final HolderGetter<Spell> spellGetter;
    
    public SpellTreesGen(String modId, BootstapContext<SpellTree> context)
    {
        this.modId = modId;
        this.context = context;
        this.spellGetter = context.lookup(Spells.REGISTRY_KEY);
    }
    
    public void addSpellTree(String key, SpellTree spellTree)
    {
        addSpellTree(new ResourceLocation(modId, key), spellTree);
    }
    
    public void addSpellTree(ResourceLocation key, SpellTree spellTree)
    {
        context.register(ResourceKey.create(SpellTrees.REGISTRY_KEY, key), spellTree);
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
        return spellGetter.getOrThrow(ResourceKey.create(Spells.REGISTRY_KEY, spell));
    }
    
    public String getName()
    {
        return "Spells & Shields Spell Trees Files";
    }
}

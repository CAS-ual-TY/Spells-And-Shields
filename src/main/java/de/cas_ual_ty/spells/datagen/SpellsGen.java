package de.cas_ual_ty.spells.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cas_ual_ty.spells.NewSpells;
import de.cas_ual_ty.spells.SpellsRegistries;
import de.cas_ual_ty.spells.spell.NewSpell;
import de.cas_ual_ty.spells.spell.action.effect.DamageAction;
import de.cas_ual_ty.spells.spell.context.BuiltinActivations;
import de.cas_ual_ty.spells.spell.context.BuiltinTargetGroups;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpellsGen implements DataProvider
{
    protected Map<ResourceLocation, NewSpell> spells;
    
    protected DataGenerator gen;
    protected String modId;
    protected ExistingFileHelper exFileHelper;
    protected RegistryAccess registryAccess;
    protected RegistryOps<JsonElement> registryOps;
    
    public SpellsGen(DataGenerator gen, String modId, ExistingFileHelper exFileHelper)
    {
        this.gen = gen;
        this.modId = modId;
        this.exFileHelper = exFileHelper;
        this.registryAccess = RegistryAccess.builtinCopy();
        this.registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        
        spells = new HashMap<>();
    }
    
    public void addSpell(String key, NewSpell spell)
    {
        addSpell(new ResourceLocation(modId, key), spell);
    }
    
    public void addSpell(ResourceLocation key, NewSpell spell)
    {
        spells.put(key, spell);
    }
    
    protected void addSpells()
    {
        addSpell(NewSpells.TEST, new NewSpell(modId, "textures/spell/default_fallback.png", NewSpells.KEY_TEST, 2F)
                .addTooltip(Component.literal("Description here // this is a debug spell"))
                .addTooltip(Component.literal("You damage yourself using it"))
                .addTooltip(Component.translatable("translated.description.key.line1"))
                .addTooltip(Component.translatable("translated.description.key.line2"))
                .addAction(new DamageAction(SpellsRegistries.DAMAGE_ACTION.get(), BuiltinActivations.ACTIVE.activation, BuiltinTargetGroups.OWNER.targetGroup, 5D))
        );
    }
    
    @Override
    public void run(CachedOutput pOutput) throws IOException
    {
        addSpells();
        JsonCodecProvider<NewSpell> provider = JsonCodecProvider.forDatapackRegistry(gen, exFileHelper, modId, registryOps, NewSpells.SPELLS_REGISTRY_KEY, spells);
        provider.run(pOutput);
    }
    
    @Override
    public String getName()
    {
        return "Spells & Shields Spells Files";
    }
}
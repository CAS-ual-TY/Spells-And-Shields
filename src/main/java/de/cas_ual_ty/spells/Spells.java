package de.cas_ual_ty.spells;

import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class Spells
{
    private static Supplier<IForgeRegistry<Spell>> SPELLS_REGISTRY;
    public static ResourceKey<Registry<Spell>> SPELLS_REGISTRY_KEY;
    
    public static Registry<Spell> getRegistry2(Level level)
    {
        return level.registryAccess().registryOrThrow(SPELLS_REGISTRY_KEY);
    }
    
    public static final ResourceLocation TEST = new ResourceLocation(SpellsAndShields.MOD_ID, "test");
    public static final String KEY_TEST = key(TEST);
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Spells::newRegistry);
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        SPELLS_REGISTRY = event.create(new RegistryBuilder<Spell>().setMaxID(2048).dataPackRegistry(SpellsCodecs.SPELL_CONTENTS).setName(new ResourceLocation(SpellsAndShields.MOD_ID, "spells")).onCreate((registry, stage) -> SPELLS_REGISTRY_KEY = registry.getRegistryKey()));
    }
    
    public static String key(ResourceLocation rl)
    {
        return "spell." + rl.getNamespace() + "." + rl.getPath();
    }
}

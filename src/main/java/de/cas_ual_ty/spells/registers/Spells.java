package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class Spells
{
    private static Supplier<IForgeRegistry<Spell>> REGISTRY;
    public static ResourceKey<Registry<Spell>> REGISTRY_KEY;
    
    public static Registry<Spell> getRegistry(LevelAccessor level)
    {
        return level.registryAccess().registryOrThrow(REGISTRY_KEY);
    }
    
    public static final ResourceLocation TEST = rl("test");
    public static final String KEY_TEST = key(TEST);
    
    public static final ResourceLocation TEST2 = rl("test2");
    public static final String KEY_TEST2 = key(TEST2);
    
    public static final ResourceLocation LEAP = rl("leap");
    public static final String KEY_LEAP = key(LEAP);
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Spells::newRegistry);
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<Spell>().setMaxID(2048).dataPackRegistry(SpellsCodecs.SPELL_CONTENTS).setName(new ResourceLocation(SpellsAndShields.MOD_ID, "spells"))
                .onCreate((registry, stage) -> REGISTRY_KEY = registry.getRegistryKey())
        );
    }
    
    private static ResourceLocation rl(String path)
    {
        return new ResourceLocation(SpellsAndShields.MOD_ID, path);
    }
    
    public static String key(ResourceLocation rl)
    {
        return "spell." + rl.getNamespace() + "." + rl.getPath();
    }
}

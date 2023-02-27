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
    
    public static final ResourceLocation LEAP = rl("leap");
    public static final String KEY_LEAP = key(LEAP);
    public static final String KEY_LEAP_DESC = descKey(LEAP);
    
    public static final ResourceLocation SUMMON_ANIMAL = rl("summon_animal");
    public static final String KEY_SUMMON_ANIMAL = key(SUMMON_ANIMAL);
    public static final String KEY_SUMMON_ANIMAL_DESC = descKey(SUMMON_ANIMAL);
    
    // TODO ingredients, mid-flight particles
    public static final ResourceLocation FIRE_BALL = rl("fire_ball");
    public static final String KEY_FIRE_BALL = key(FIRE_BALL);
    public static final String KEY_FIRE_BALL_DESC = descKey(FIRE_BALL);
    
    // TODO all
    public static final ResourceLocation BLAST_SMELT = rl("blast_smelt");
    public static final String KEY_BLAST_SMELT = key(BLAST_SMELT);
    public static final String KEY_BLAST_SMELT_DESC = descKey(BLAST_SMELT);
    
    public static final ResourceLocation TRANSFER_MANA = rl("transfer_mana");
    public static final String KEY_TRANSFER_MANA = key(TRANSFER_MANA);
    public static final String KEY_TRANSFER_MANA_DESC = descKey(TRANSFER_MANA);
    
    public static final ResourceLocation BLOW_ARROW = rl("blow_arrow");
    public static final String KEY_BLOW_ARROW = key(BLOW_ARROW);
    public static final String KEY_BLOW_ARROW_DESC = descKey(BLOW_ARROW);
    
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
    
    public static String key(ResourceLocation rl, String suffix)
    {
        return "spell." + rl.getNamespace() + "." + rl.getPath() + (!suffix.isEmpty() ? "." + suffix : "");
    }
    
    public static String key(ResourceLocation rl)
    {
        return key(rl, "");
    }
    
    public static String descKey(ResourceLocation rl)
    {
        return key(rl, "desc");
    }
    
    public static String descKey(ResourceLocation rl, int index)
    {
        return key(rl, "desc_" + index);
    }
}

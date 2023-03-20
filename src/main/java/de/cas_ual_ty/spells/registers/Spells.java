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
    
    public static final ResourceLocation DUMMY = rl("dummy");
    public static final String KEY_DUMMY = key(DUMMY);
    public static final String KEY_DUMMY_DESC = descKey(DUMMY);
    
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
    
    public static final ResourceLocation HEALTH_BOOST = rl("health_boost");
    public static final String KEY_HEALTH_BOOST = key(HEALTH_BOOST);
    public static final String KEY_HEALTH_BOOST_DESC = descKey(HEALTH_BOOST);
    
    public static final ResourceLocation MANA_BOOST = rl("mana_boost");
    public static final String KEY_MANA_BOOST = key(MANA_BOOST);
    public static final String KEY_MANA_BOOST_DESC = descKey(MANA_BOOST);
    
    public static final ResourceLocation WATER_LEAP = rl("water_leap");
    public static final String KEY_WATER_LEAP = key(WATER_LEAP);
    public static final String KEY_WATER_LEAP_DESC = descKey(WATER_LEAP);
    
    public static final ResourceLocation AQUA_AFFINITY = rl("aqua_affinity");
    public static final String KEY_AQUA_AFFINITY = key(AQUA_AFFINITY);
    public static final String KEY_AQUA_AFFINITY_DESC = descKey(AQUA_AFFINITY);
    
    //TODO fx
    public static final ResourceLocation WATER_WHIP = rl("water_whip");
    public static final String KEY_WATER_WHIP = key(WATER_WHIP);
    public static final String KEY_WATER_WHIP_DESC = descKey(WATER_WHIP);
    
    public static final ResourceLocation POTION_SHOT = rl("potion_shot");
    public static final String KEY_POTION_SHOT = key(POTION_SHOT);
    public static final String KEY_POTION_SHOT_DESC = descKey(POTION_SHOT);
    
    public static final ResourceLocation FROST_WALKER = rl("frost_walker");
    public static final String KEY_FROST_WALKER = key(FROST_WALKER);
    public static final String KEY_FROST_WALKER_DESC = descKey(FROST_WALKER);
    
    public static final ResourceLocation JUMP = rl("jump");
    public static final String KEY_JUMP = key(JUMP);
    public static final String KEY_JUMP_DESC = descKey(JUMP);
    
    public static final ResourceLocation MANA_SOLES = rl("mana_soles");
    public static final String KEY_MANA_SOLES = key(MANA_SOLES);
    public static final String KEY_MANA_SOLES_DESC = descKey(MANA_SOLES);
    
    public static final ResourceLocation FIRE_CHARGE = rl("fire_charge");
    public static final String KEY_FIRE_CHARGE = key(FIRE_CHARGE);
    public static final String KEY_FIRE_CHARGE_DESC = descKey(FIRE_CHARGE);
    
    public static final ResourceLocation PRESSURIZE = rl("pressurize");
    public static final String KEY_PRESSURIZE = key(PRESSURIZE);
    public static final String KEY_PRESSURIZE_DESC = descKey(PRESSURIZE);
    
    public static final ResourceLocation INSTANT_MINE = rl("instant_mine");
    public static final String KEY_INSTANT_MINE = key(INSTANT_MINE);
    public static final String KEY_INSTANT_MINE_DESC = descKey(INSTANT_MINE);
    
    public static final ResourceLocation SPIT_METAL = rl("spit_metal");
    public static final String KEY_SPIT_METAL = key(SPIT_METAL);
    public static final String KEY_SPIT_METAL_DESC = descKey(SPIT_METAL);
    
    public static final ResourceLocation FLAMETHROWER = rl("flamethrower");
    public static final String KEY_FLAMETHROWER = key(FLAMETHROWER);
    public static final String KEY_FLAMETHROWER_DESC = descKey(FLAMETHROWER);
    
    public static final ResourceLocation LAVA_WALKER = rl("lava_walker");
    public static final String KEY_LAVA_WALKER = key(LAVA_WALKER);
    public static final String KEY_LAVA_WALKER_DESC = descKey(LAVA_WALKER);
    
    public static final ResourceLocation SILENCE_TARGET = rl("silence_target");
    public static final String KEY_SILENCE_TARGET = key(SILENCE_TARGET);
    public static final String KEY_SILENCE_TARGET_DESC = descKey(SILENCE_TARGET);
    
    public static final ResourceLocation RANDOM_TELEPORT = rl("random_teleport");
    public static final String KEY_RANDOM_TELEPORT = key(RANDOM_TELEPORT);
    public static final String KEY_RANDOM_TELEPORT_DESC = descKey(RANDOM_TELEPORT);
    
    public static final ResourceLocation FORCED_TELEPORT = rl("forced_teleport");
    public static final String KEY_FORCED_TELEPORT = key(FORCED_TELEPORT);
    public static final String KEY_FORCED_TELEPORT_DESC = descKey(FORCED_TELEPORT);
    
    public static final ResourceLocation TELEPORT = rl("teleport");
    public static final String KEY_TELEPORT = key(TELEPORT);
    public static final String KEY_TELEPORT_DESC = descKey(TELEPORT);
    
    public static final ResourceLocation LIGHTNING_STRIKE = rl("lightning_strike");
    public static final String KEY_LIGHTNING_STRIKE = key(LIGHTNING_STRIKE);
    public static final String KEY_LIGHTNING_STRIKE_DESC = descKey(LIGHTNING_STRIKE);
    
    public static final ResourceLocation DRAIN_FLAME = rl("drain_flame");
    public static final String KEY_DRAIN_FLAME = key(DRAIN_FLAME);
    public static final String KEY_DRAIN_FLAME_DESC = descKey(DRAIN_FLAME);
    
    public static final ResourceLocation GROWTH = rl("growth");
    public static final String KEY_GROWTH = key(GROWTH);
    public static final String KEY_GROWTH_DESC = descKey(GROWTH);
    
    public static final ResourceLocation GHAST = rl("ghast");
    public static final String KEY_GHAST = key(GHAST);
    public static final String KEY_GHAST_DESC = descKey(GHAST);
    
    public static final ResourceLocation ENDER_ARMY = rl("ender_army");
    public static final String KEY_ENDER_ARMY = key(ENDER_ARMY);
    public static final String KEY_ENDER_ARMY_DESC = descKey(ENDER_ARMY);
    
    public static final ResourceLocation PERMANENT_SPEED = rl("permanent_speed");
    public static final String KEY_PERMANENT_SPEED = key(PERMANENT_SPEED);
    public static final String KEY_PERMANENT_SPEED_DESC = descKey(PERMANENT_SPEED);
    
    public static final ResourceLocation TEMPORARY_SPEED = rl("temporary_speed");
    public static final String KEY_TEMPORARY_SPEED = key(TEMPORARY_SPEED);
    public static final String KEY_TEMPORARY_SPEED_DESC = descKey(TEMPORARY_SPEED);
    
    public static final ResourceLocation TOGGLE_SPEED = rl("toggle_speed");
    public static final String KEY_TOGGLE_SPEED = key(TOGGLE_SPEED);
    public static final String KEY_TOGGLE_SPEED_DESC = descKey(TOGGLE_SPEED);
    
    public static final ResourceLocation PERMANENT_REGENERATION = rl("permanent_regeneration");
    public static final String KEY_PERMANENT_REGENERATION = key(PERMANENT_REGENERATION);
    public static final String KEY_PERMANENT_REGENERATION_DESC = descKey(PERMANENT_REGENERATION);
    
    public static final ResourceLocation TEMPORARY_REGENERATION = rl("temporary_regeneration");
    public static final String KEY_TEMPORARY_REGENERATION = key(TEMPORARY_REGENERATION);
    public static final String KEY_TEMPORARY_REGENERATION_DESC = descKey(TEMPORARY_REGENERATION);
    
    public static final ResourceLocation TOGGLE_REGENERATION = rl("toggle_regeneration");
    public static final String KEY_TOGGLE_REGENERATION = key(TOGGLE_REGENERATION);
    public static final String KEY_TOGGLE_REGENERATION_DESC = descKey(TOGGLE_REGENERATION);
    
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

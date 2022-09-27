package de.cas_ual_ty.spells;

import com.google.gson.JsonElement;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.*;
import de.cas_ual_ty.spells.spell.base.AttributeSpell;
import de.cas_ual_ty.spells.spell.base.PermanentMobEffectSpell;
import de.cas_ual_ty.spells.spell.base.SpellIcon;
import de.cas_ual_ty.spells.spell.base.TemporaryMobEffectSpell;
import de.cas_ual_ty.spells.spell.impl.*;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class Spells
{
    public static Supplier<IForgeRegistry<ISpell>> SPELLS_REGISTRY;
    private static final DeferredRegister<ISpell> SPELLS = DeferredRegister.create(new ResourceLocation(MOD_ID, "spells"), MOD_ID);
    
    public static final RegistryObject<ISpell> LEAP = SPELLS.register("leap", LeapSpell::new);
    public static final RegistryObject<ISpell> SUMMON_ANIMAL = SPELLS.register("summon_animal", SummonAnimalSpell::new);
    public static final RegistryObject<ISpell> FIRE_BALL = SPELLS.register("fire_ball", FireBallSpell::new);
    public static final RegistryObject<ISpell> BLAST_SMELT = SPELLS.register("blast_smelt", SmeltSpell::new);
    public static final RegistryObject<ISpell> TRANSFER_MANA = SPELLS.register("transfer_mana", TransferManaSpell::new);
    public static final RegistryObject<ISpell> BLOW_ARROW = SPELLS.register("blow_arrow", BlowArrowSpell::new);
    public static final RegistryObject<ISpell> HEALTH_BOOST = SPELLS.register("health_boost", () -> new AttributeSpell(() -> Attributes.MAX_HEALTH, 4.0D, AttributeModifier.Operation.ADDITION).setIcon(new ResourceLocation("textures/mob_effect/health_boost.png")));
    public static final RegistryObject<ISpell> MANA_BOOST = SPELLS.register("mana_boost", () -> new AttributeSpell(SpellsRegistries.MAX_MANA_ATTRIBUTE::get, 4.0D, AttributeModifier.Operation.ADDITION).setIcon(new ResourceLocation(MOD_ID, "textures/mob_effect/mana_boost.png")));
    public static final RegistryObject<ISpell> SPEED = SPELLS.register("speed", () -> new PermanentMobEffectSpell(MobEffects.MOVEMENT_SPEED));
    public static final RegistryObject<ISpell> JUMP_BOOST = SPELLS.register("jump_boost", () -> new PermanentMobEffectSpell(MobEffects.JUMP));
    public static final RegistryObject<ISpell> DOLPHINS_GRACE = SPELLS.register("dolphins_grace", () -> new PermanentMobEffectSpell(MobEffects.DOLPHINS_GRACE));
    public static final RegistryObject<ISpell> WATER_LEAP = SPELLS.register("water_leap", WaterLeapSpell::new);
    public static final RegistryObject<ISpell> AQUA_AFFINITY = SPELLS.register("aqua_affinity", () -> new AquaAffinitySpell().setSmallIcon(new ResourceLocation("textures/item/enchanted_book.png")));
    public static final RegistryObject<ISpell> WATER_BREATHING = SPELLS.register("water_breathing", () -> new PermanentMobEffectSpell(MobEffects.WATER_BREATHING));
    public static final RegistryObject<ISpell> SLOW_FALLING = SPELLS.register("slow_falling", () -> new PermanentMobEffectSpell(MobEffects.SLOW_FALLING));
    public static final RegistryObject<ISpell> HASTE = SPELLS.register("haste", () -> new PermanentMobEffectSpell(MobEffects.DIG_SPEED));
    public static final RegistryObject<ISpell> REGENERATION = SPELLS.register("regeneration", () -> new PermanentMobEffectSpell(MobEffects.REGENERATION, 50));
    public static final RegistryObject<ISpell> REPLENISHMENT = SPELLS.register("replenishment", () -> new PermanentMobEffectSpell(SpellsRegistries.REPLENISHMENT_EFFECT.get(), 50));
    public static final RegistryObject<ISpell> WATER_WHIP = SPELLS.register("water_whip", WaterWhipSpell::new);
    public static final RegistryObject<ISpell> POTION_SHOT = SPELLS.register("potion_shot", PotionShotSpell::new);
    public static final RegistryObject<ISpell> FROST_WALKER = SPELLS.register("frost_walker", () -> new WalkerSpell(() -> Blocks.WATER, () -> Material.WATER, Blocks.FROSTED_ICE::defaultBlockState));
    public static final RegistryObject<ISpell> JUMP = SPELLS.register("jump", JumpSpell::new);
    public static final RegistryObject<ISpell> MANA_SOLES = SPELLS.register("mana_soles", ManaSolesSpell::new);
    public static final RegistryObject<ISpell> FIRE_CHARGE = SPELLS.register("fire_charge", () -> new FireChargeSpell().setSmallIcon(new ResourceLocation("textures/item/fire_charge.png")));
    public static final RegistryObject<ISpell> PRESSURIZE = SPELLS.register("pressurize", PressurizeSpell::new);
    public static final RegistryObject<ISpell> INSTANT_MINE = SPELLS.register("instant_mine", InstantMineSpell::new);
    public static final RegistryObject<ISpell> FIRE_RESISTANCE = SPELLS.register("fire_resistance", () -> new PermanentMobEffectSpell(MobEffects.FIRE_RESISTANCE));
    public static final RegistryObject<ISpell> SPIT_METAL = SPELLS.register("spit_metal", SpitMetalSpell::new);
    public static final RegistryObject<ISpell> NIGHT_VISION = SPELLS.register("night_vision", () -> new PermanentMobEffectSpell(MobEffects.NIGHT_VISION));
    public static final RegistryObject<ISpell> STRENGTH = SPELLS.register("strength", () -> new PermanentMobEffectSpell(MobEffects.DAMAGE_BOOST));
    public static final RegistryObject<ISpell> RESISTANCE = SPELLS.register("resistance", () -> new PermanentMobEffectSpell(MobEffects.DAMAGE_RESISTANCE));
    public static final RegistryObject<ISpell> INVISIBILITY = SPELLS.register("invisibility", () -> new PermanentMobEffectSpell(MobEffects.INVISIBILITY));
    public static final RegistryObject<ISpell> GLOWING = SPELLS.register("glowing", () -> new PermanentMobEffectSpell(MobEffects.GLOWING));
    public static final RegistryObject<ISpell> LUCK = SPELLS.register("luck", () -> new PermanentMobEffectSpell(MobEffects.LUCK));
    public static final RegistryObject<ISpell> CONDUIT_POWER = SPELLS.register("conduit_power", () -> new PermanentMobEffectSpell(MobEffects.CONDUIT_POWER));
    public static final RegistryObject<ISpell> FLAMETHROWER = SPELLS.register("flamethrower", FlamethrowerSpell::new);
    public static final RegistryObject<ISpell> LAVA_WALKER = SPELLS.register("lava_walker", () -> new WalkerSpell(() -> Blocks.LAVA, () -> Material.LAVA, Blocks.OBSIDIAN::defaultBlockState));
    public static final RegistryObject<ISpell> SILENCE_TARGET = SPELLS.register("silence_target", () -> new SilenceTargetSpell().setIcon(new ResourceLocation(MOD_ID, "textures/mob_effect/silence.png")));
    public static final RegistryObject<ISpell> RANDOM_TELEPORT = SPELLS.register("random_teleport", RandomTeleportSpell::new);
    public static final RegistryObject<ISpell> FORCED_TELEPORT = SPELLS.register("forced_teleport", ForcedTeleportSpell::new);
    public static final RegistryObject<ISpell> TELEPORT = SPELLS.register("teleport", TeleportSpell::new);
    public static final RegistryObject<ISpell> LIGHTNING_STRIKE = SPELLS.register("lightning_strike", LightningStrikeSpell::new);
    public static final RegistryObject<ISpell> DRAIN_FLAME = SPELLS.register("drain_flame", DrainFlameSpell::new);
    public static final RegistryObject<ISpell> GROWTH = SPELLS.register("growth", GrowthSpell::new);
    public static final RegistryObject<ISpell> MAGIC_IMMUNE_SELF = SPELLS.register("magic_immune_self", () -> new TemporaryMobEffectSpell(7F, new ItemStack(Items.GLASS_PANE), SpellsRegistries.MAGIC_IMMUNE_EFFECT.get(), 20 * 20, 0, false, true, true));
    public static final RegistryObject<ISpell> GHAST = SPELLS.register("ghast", () -> new GhastSpell().setIcon(new SpellIcon(new ResourceLocation("textures/entity/ghast/ghast.png"), 16, 16, 16, 16, 64, 32)));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Spells::newRegistry);
        SPELLS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        SPELLS_REGISTRY = event.create(new RegistryBuilder<ISpell>().setMaxID(1024).setName(new ResourceLocation(MOD_ID, "spells")));
    }
    
    public static void spellsConfigs()
    {
        Path p = SpellsFileUtil.getOrCreateSubConfigDir("spells");
        
        File folder = p.toFile();
        
        if(!folder.isDirectory() || folder.listFiles() == null)
        {
            SpellsAndShields.LOGGER.error("Can not read or write spell config files in {} (is it a folder?).", p);
            SPELLS_REGISTRY.get().getValues().stream().filter(s -> s instanceof IConfigurableSpell).map(s -> (IConfigurableSpell) s).forEach(IConfigurableSpell::applyDefaultConfig);
            return;
        }
        
        SPELLS_REGISTRY.get().getEntries().stream().filter(e -> e.getValue() instanceof IConfigurableSpell).forEach(entry ->
        {
            IConfigurableSpell spell = (IConfigurableSpell) entry.getValue();
            ResourceLocation key = entry.getKey().location();
            
            File f = p.resolve(spell.getFileName(key) + ".json").toFile();
            
            if(SpellsConfig.CREATE_SPELLS_CONFIGS.get())
            {
                try
                {
                    SpellsFileUtil.writeJsonToFile(f, spell.makeDefaultConfig());
                    SpellsAndShields.LOGGER.info("Successfully wrote default config of spell {} to file {}.", key.toString(), f.toPath());
                }
                catch(Exception e)
                {
                    SpellsAndShields.LOGGER.error("Failed writing default config of spell {} to file {}.", key.toString(), f.toPath(), e);
                    e.printStackTrace();
                }
                
                spell.applyDefaultConfig();
            }
            else if(SpellsConfig.LOAD_SPELLS_CONFIGS.get())
            {
                if(!f.exists())
                {
                    SpellsAndShields.LOGGER.info("Can not find config of spell {} at {}, applying default config.", key.toString(), f.toPath());
                    spell.applyDefaultConfig();
                }
                else
                {
                    boolean failed = false;
                    JsonElement json = null;
                    
                    try
                    {
                        json = SpellsFileUtil.readJsonFromFile(f);
                    }
                    catch(Exception e)
                    {
                        failed = true;
                        SpellsAndShields.LOGGER.error("Failed reading config of spell {} from file {}, applying default config.", key.toString(), f.toPath(), e);
                        e.printStackTrace();
                        spell.applyDefaultConfig();
                    }
                    
                    if(json != null && json.isJsonObject())
                    {
                        try
                        {
                            spell.readFromConfig(json.getAsJsonObject());
                            SpellsAndShields.LOGGER.info("Successfully read config of spell {} from file {}.", key.toString(), f.toPath());
                        }
                        catch(IllegalStateException e)
                        {
                            SpellsAndShields.LOGGER.error("Failed reading config of spell {} from file {}, applying default config.", key.toString(), f.toPath(), e);
                            e.printStackTrace();
                            spell.applyDefaultConfig();
                        }
                    }
                    else if(!failed)
                    {
                        SpellsAndShields.LOGGER.error("Failed reading config of spell {} from file {}, applying default config.", key.toString(), f.toPath());
                        spell.applyDefaultConfig();
                    }
                }
            }
            else
            {
                spell.applyDefaultConfig();
            }
        });
        
        if(SpellsConfig.CREATE_SPELLS_CONFIGS.get())
        {
            SpellsConfig.CREATE_SPELLS_CONFIGS.set(false);
            SpellsConfig.CREATE_SPELLS_CONFIGS.save();
        }
    }
    
    private static void playerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            SpellHolder.getSpellHolder(event.player).ifPresent(spellHolder ->
            {
                LinkedList<Integer> idx = new LinkedList<>();
                
                for(int i = 0; i < SpellHolder.SPELL_SLOTS; i++)
                {
                    idx.addLast(i);
                }
                
                while(!idx.isEmpty())
                {
                    int i = idx.removeFirst();
                    
                    if(spellHolder.getSpell(i) instanceof ITickSpell spell)
                    {
                        int amount = 1;
                        
                        for(int j = i + 1; j < SpellHolder.SPELL_SLOTS; j++)
                        {
                            if(spellHolder.getSpell(j) == spell)
                            {
                                idx.removeFirstOccurrence(j);
                                amount++;
                            }
                        }
                        
                        spell.tick(spellHolder, amount);
                    }
                }
                
                for(int i = 0; i < SpellHolder.SPELL_SLOTS; i++)
                {
                    if(spellHolder.getSpell(i) instanceof IStackedTickSpell spell)
                    {
                        spell.tick(spellHolder, i);
                    }
                }
            });
        }
    }
    
    private static void tick(TickEvent.LevelTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Spells.SPELLS_REGISTRY.get().forEach(s ->
            {
                if(s instanceof ISingletonTickSpell spell)
                {
                    spell.tickSingleton();
                }
            });
        }
    }
    
    public static void registerEventSpells()
    {
        SPELLS_REGISTRY.get().forEach(spell ->
        {
            if(spell instanceof IEventSpell eventSpell)
            {
                eventSpell.registerEvents();
            }
        });
    }
    
    public static void registerEvents()
    {
        MinecraftForge.EVENT_BUS.addListener(Spells::playerTick);
        MinecraftForge.EVENT_BUS.addListener(Spells::tick);
    }
}

package de.cas_ual_ty.spells;

import com.google.gson.JsonElement;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.spell.*;
import de.cas_ual_ty.spells.spell.base.AttributeSpell;
import de.cas_ual_ty.spells.spell.base.MobEffectSpell;
import de.cas_ual_ty.spells.spell.impl.*;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    
    public static final RegistryObject<ISpell> SPIT_METAL = SPELLS.register("spit_metal", () -> new SpitMetalSpell(4F).setSmallIcon(new ResourceLocation("textures/item/iron_nugget.png")));
    public static final RegistryObject<ISpell> FIRE_RESISTANCE = SPELLS.register("fire_resistance", () -> new MobEffectSpell(MobEffects.FIRE_RESISTANCE));
    public static final RegistryObject<ISpell> INSTANT_MINE = SPELLS.register("instant_mine", () -> new InstantMineSpell(5F).setSmallIcon(new ResourceLocation("textures/item/diamond_pickaxe.png")));
    public static final RegistryObject<ISpell> PRESSURIZE = SPELLS.register("pressurize", () -> new PressurizeSpell(4F));
    public static final RegistryObject<ISpell> FIRE_CHARGE = SPELLS.register("fire_charge", () -> new FireChargeSpell(4F, new ItemStack(Items.FIRE_CHARGE)).setSmallIcon(new ResourceLocation("textures/item/fire_charge.png")));
    public static final RegistryObject<ISpell> MANA_SOLES = SPELLS.register("mana_soles", () -> new ManaSolesSpell().setSmallIcon(new ResourceLocation("textures/item/diamond_boots.png")));
    public static final RegistryObject<ISpell> JUMP = SPELLS.register("jump", () -> new JumpSpell(5F).setSmallIcon(new ResourceLocation("textures/item/feather.png")));
    public static final RegistryObject<ISpell> FROST_WALKER = SPELLS.register("frost_walker", () -> new WalkerSpell().setSmallIcon(new ResourceLocation("textures/item/golden_boots.png")));
    public static final RegistryObject<ISpell> POTION_SHOT = SPELLS.register("potion_shot", () -> new PotionShotSpell(2.0F).setSmallIcon(new ResourceLocation("textures/item/potion.png")));
    public static final RegistryObject<ISpell> WATER_WHIP = SPELLS.register("water_whip", () -> new WaterWhipSpell(2.0F));
    public static final RegistryObject<ISpell> REPLENISHMENT = SPELLS.register("replenishment", () -> new MobEffectSpell(SpellsRegistries.REPLENISHMENT_EFFECT.get(), 50));
    public static final RegistryObject<ISpell> REGENERATION = SPELLS.register("regeneration", () -> new MobEffectSpell(MobEffects.REGENERATION, 50));
    public static final RegistryObject<ISpell> HASTE = SPELLS.register("haste", () -> new MobEffectSpell(MobEffects.DIG_SPEED));
    public static final RegistryObject<ISpell> SLOW_FALLING = SPELLS.register("slow_falling", () -> new MobEffectSpell(MobEffects.SLOW_FALLING));
    public static final RegistryObject<ISpell> WATER_BREATHING = SPELLS.register("water_breathing", () -> new MobEffectSpell(MobEffects.WATER_BREATHING));
    public static final RegistryObject<ISpell> AQUA_AFFINITY = SPELLS.register("aqua_affinity", () -> new AquaAffinitySpell().setSmallIcon(new ResourceLocation("textures/item/enchanted_book.png")));
    public static final RegistryObject<ISpell> WATER_LEAP = SPELLS.register("water_leap", () -> new WaterLeapSpell(7F));
    public static final RegistryObject<ISpell> DOLPHINS_GRACE = SPELLS.register("dolphins_grace", () -> new MobEffectSpell(MobEffects.DOLPHINS_GRACE));
    public static final RegistryObject<ISpell> JUMP_BOOST = SPELLS.register("jump_boost", () -> new MobEffectSpell(MobEffects.JUMP));
    public static final RegistryObject<ISpell> SPEED = SPELLS.register("speed", () -> new MobEffectSpell(MobEffects.MOVEMENT_SPEED));
    public static final RegistryObject<ISpell> MANA_BOOST = SPELLS.register("mana_boost", () -> new AttributeSpell(SpellsRegistries.MAX_MANA::get, 4.0D, AttributeModifier.Operation.ADDITION).setIcon(new ResourceLocation(MOD_ID, "textures/mob_effect/mana_boost.png")));
    public static final RegistryObject<ISpell> HEALTH_BOOST = SPELLS.register("health_boost", () -> new AttributeSpell(() -> Attributes.MAX_HEALTH, 4.0D, AttributeModifier.Operation.ADDITION).setIcon(new ResourceLocation("textures/mob_effect/health_boost.png")));
    public static final RegistryObject<ISpell> BLOW_ARROW = SPELLS.register("blow_arrow", () -> new BlowArrowSpell(7F).setSmallIcon(new ResourceLocation("textures/item/bow_pulling_0.png")));
    public static final RegistryObject<ISpell> TRANSFER_MANA = SPELLS.register("transfer_mana", () -> new TransferManaSpell(4F));
    public static final RegistryObject<ISpell> BLAST_SMELT = SPELLS.register("blast_smelt", () -> new SmeltSpell(6F).setSmallIcon(new ResourceLocation("textures/item/coal.png")));
    public static final RegistryObject<ISpell> FIRE_BALL = SPELLS.register("fire_ball", () -> new FireBallSpell(5F));
    public static final RegistryObject<ISpell> SUMMON_ANIMAL = SPELLS.register("summon_animal", () -> new SummonAnimalSpell(18F));
    public static final RegistryObject<ISpell> LEAP = SPELLS.register("leap", () -> new LeapSpell(7F));
    
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
        
        if(SpellsConfig.LOAD_SPELLS_CONFIGS.get())
        {
            SPELLS_REGISTRY.get().getEntries().stream().filter(e -> e.getValue() instanceof IConfigurableSpell).forEach(entry ->
            {
                IConfigurableSpell spell = (IConfigurableSpell) entry.getValue();
                ResourceLocation key = entry.getKey().location();
                
                File f = p.resolve(spell.getFileName(key) + ".json").toFile();
                
                if(!f.exists())
                {
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
                    }
                    
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
            });
        }
        else
        {
            SPELLS_REGISTRY.get().getValues().stream().filter(s -> s instanceof IConfigurableSpell).map(s -> (IConfigurableSpell) s).forEach(IConfigurableSpell::applyDefaultConfig);
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
                        int amount = 0;
                        
                        for(int j = 0; j < SpellHolder.SPELL_SLOTS; j++)
                        {
                            if(j > i)
                            {
                                idx.removeFirstOccurrence(j);
                            }
                            
                            amount++;
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

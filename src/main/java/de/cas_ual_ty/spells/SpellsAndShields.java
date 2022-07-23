package de.cas_ual_ty.spells;

import de.cas_ual_ty.spells.capability.SpellsCapabilities;
import de.cas_ual_ty.spells.network.*;
import de.cas_ual_ty.spells.spell.tree.SpellTrees;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SpellsAndShields.MOD_ID)
public class SpellsAndShields
{
    public static final String MOD_ID = "spells_and_shields";
    
    public static final Logger LOGGER = LogManager.getLogger();
    
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    
    public SpellsAndShields()
    {
        Spells.register();
        SpellsRegistries.register();
        
        SpellsFileUtil.getOrCreateConfigDir();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpellsConfig.GENERAL_SPEC, MOD_ID + "/common" + ".toml");
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        
        Spells.registerEvents();
        SpellsRegistries.registerEvents();
        SpellsCapabilities.registerEvents();
        SpellTrees.registerEvents();
        
        CHANNEL.registerMessage(0, ManaSyncMessage.class, ManaSyncMessage::encode, ManaSyncMessage::decode, ManaSyncMessage::handle);
        CHANNEL.registerMessage(1, SpellsSyncMessage.class, SpellsSyncMessage::encode, SpellsSyncMessage::decode, SpellsSyncMessage::handle);
        CHANNEL.registerMessage(2, FireSpellMessage.class, FireSpellMessage::encode, FireSpellMessage::decode, FireSpellMessage::handle);
        CHANNEL.registerMessage(3, RequestSpellProgressionMenuMessage.class, RequestSpellProgressionMenuMessage::encode, RequestSpellProgressionMenuMessage::decode, RequestSpellProgressionMenuMessage::handle);
        CHANNEL.registerMessage(4, SpellProgressionSyncMessage.class, SpellProgressionSyncMessage::encode, SpellProgressionSyncMessage::decode, SpellProgressionSyncMessage::handle);
        CHANNEL.registerMessage(5, RequestLearnSpellMessage.class, RequestLearnSpellMessage::encode, RequestLearnSpellMessage::decode, RequestLearnSpellMessage::handle);
        CHANNEL.registerMessage(6, RequestEquipSpellMessage.class, RequestEquipSpellMessage::encode, RequestEquipSpellMessage::decode, RequestEquipSpellMessage::handle);
        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> de.cas_ual_ty.spells.client.SpellsClientUtil::onModConstruct);
    }
    
    private void setup(FMLCommonSetupEvent event)
    {
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, SpellsRegistries.INSTANT_MANA.get(), SpellsRegistries.STRONG_INSTANT_MANA.get(), null, Items.LAPIS_LAZULI, SpellsRegistries.MANA_BOMB.get(), SpellsRegistries.STRONG_MANA_BOMB.get(), null, Items.FERMENTED_SPIDER_EYE);
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, SpellsRegistries.REPLENISHMENT.get(), SpellsRegistries.STRONG_REPLENISHMENT.get(), SpellsRegistries.LONG_REPLENISHMENT.get(), Items.TUBE_CORAL_FAN, null, null, null, null);
        SpellsUtil.addPotionRecipes(Potions.AWKWARD, SpellsRegistries.LEAKING.get(), SpellsRegistries.STRONG_LEAKING.get(), SpellsRegistries.LONG_LEAKING.get(), Items.DEAD_TUBE_CORAL_FAN, null, null, null, null);
        SpellTrees.readOrWriteSpellTreeConfigs();
        Spells.spellsConfigs();
        Spells.registerEventSpells();
    }
}

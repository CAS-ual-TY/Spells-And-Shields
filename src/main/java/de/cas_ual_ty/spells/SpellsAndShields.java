package de.cas_ual_ty.spells;

import de.cas_ual_ty.spells.capability.SpellsCapabilities;
import de.cas_ual_ty.spells.client.SpellsClientUtil;
import de.cas_ual_ty.spells.command.SpellCommand;
import de.cas_ual_ty.spells.network.*;
import de.cas_ual_ty.spells.spell.tree.SpellTrees;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
    
    public static final int SPELL_SLOTS = 5;
    
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    
    public SpellsAndShields()
    {
        SpellsRegistries.register();
        
        SpellsFileUtil.getOrCreateConfigDir();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpellsConfig.GENERAL_SPEC, MOD_ID + "/common" + ".toml");
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::entityAttributeModification);
        
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        
        SpellsCapabilities.registerEvents();
        SpellTrees.registerEvents();
        
        CHANNEL.registerMessage(0, ManaSyncMessage.class, ManaSyncMessage::encode, ManaSyncMessage::decode, ManaSyncMessage::handle);
        CHANNEL.registerMessage(1, SpellsSyncMessage.class, SpellsSyncMessage::encode, SpellsSyncMessage::decode, SpellsSyncMessage::handle);
        CHANNEL.registerMessage(2, FireSpellMessage.class, FireSpellMessage::encode, FireSpellMessage::decode, FireSpellMessage::handle);
        CHANNEL.registerMessage(3, RequestSpellProgressionMenuMessage.class, RequestSpellProgressionMenuMessage::encode, RequestSpellProgressionMenuMessage::decode, RequestSpellProgressionMenuMessage::handle);
        CHANNEL.registerMessage(4, SpellProgressionSyncMessage.class, SpellProgressionSyncMessage::encode, SpellProgressionSyncMessage::decode, SpellProgressionSyncMessage::handle);
        CHANNEL.registerMessage(5, RequestLearnSpellMessage.class, RequestLearnSpellMessage::encode, RequestLearnSpellMessage::decode, RequestLearnSpellMessage::handle);
        CHANNEL.registerMessage(6, RequestEquipSpellMessage.class, RequestEquipSpellMessage::encode, RequestEquipSpellMessage::decode, RequestEquipSpellMessage::handle);
        
        SpellsClientUtil.onModConstruct();
    }
    
    private void setup(FMLCommonSetupEvent event)
    {
        SpellTrees.readOrWriteSpellTreeConfigs();
        SpellsRegistries.spellsConfigs();
    }
    
    private void clientSetup(FMLClientSetupEvent event)
    {
        SpellsClientUtil.clientSetup(event);
    }
    
    private void registerCommands(RegisterCommandsEvent event)
    {
        SpellCommand.register(event.getDispatcher());
    }
    
    private void entityAttributeModification(EntityAttributeModificationEvent event)
    {
        event.add(EntityType.PLAYER, SpellsRegistries.MANA_BOOST.get());
    }
}

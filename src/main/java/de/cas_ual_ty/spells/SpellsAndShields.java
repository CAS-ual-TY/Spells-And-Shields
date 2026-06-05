package de.cas_ual_ty.spells;

import de.cas_ual_ty.spells.capability.SpellsCapabilities;
import de.cas_ual_ty.spells.network.*;
import de.cas_ual_ty.spells.registers.*;
import de.cas_ual_ty.spells.spell.compiler.BinaryOperation;
import de.cas_ual_ty.spells.spell.compiler.Compiler;
import de.cas_ual_ty.spells.spell.compiler.TernaryOperation;
import de.cas_ual_ty.spells.spell.compiler.UnaryOperation;
import de.cas_ual_ty.spells.spell.context.SpellsEvents;
import de.cas_ual_ty.spells.util.SpellsCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SpellsAndShields.MOD_ID)
public class SpellsAndShields
{
    public static final String MOD_ID = "spells_and_shields";

    public static final Logger LOGGER = LogManager.getLogger();

    public SpellsAndShields(IEventBus modEventBus, ModContainer modContainer)
    {
        SpellsCodecs.makeCodecs(modEventBus);

        BuiltInRegisters.register(modEventBus);
        CtxVarTypes.register(modEventBus);
        RequirementTypes.register(modEventBus);
        SpellActionTypes.register(modEventBus);
        SpellIconTypes.register(modEventBus);
        Spells.register(modEventBus);
        SpellTrees.register(modEventBus);
        TargetTypes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, SpellsConfig.GENERAL_SPEC, MOD_ID + "/common.toml");

        modEventBus.addListener(SpellsAndShields::registerPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(BuiltInRegisters::addPotionRecipes);
        BuiltInRegisters.registerEvents(modEventBus);
        SpellsCapabilities.registerEvents(modEventBus);

        SpellsEvents.registerEvents();

        Compiler.registerSuppliers();
        SpellsConfig.registerGlobals();
        UnaryOperation.registerToCompiler();
        BinaryOperation.registerToCompiler();
        TernaryOperation.registerToCompiler();

        if(FMLEnvironment.dist.isClient())
        {
            de.cas_ual_ty.spells.client.SpellsClientUtil.onModConstruct(modEventBus, modContainer);
        }
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event)
    {
        IPayloadRegistrar registrar = event.registrar(MOD_ID);
        // server → client
        registrar.playToClient(ManaSyncMessage.TYPE, ManaSyncMessage.STREAM_CODEC, ManaSyncMessage::handle);
        registrar.playToClient(SpellsSyncMessage.TYPE, SpellsSyncMessage.STREAM_CODEC, SpellsSyncMessage::handle);
        registrar.playToClient(SpellProgressionSyncMessage.TYPE, SpellProgressionSyncMessage.STREAM_CODEC, SpellProgressionSyncMessage::handle);
        registrar.playToClient(RunActionOnClientMessage.TYPE, RunActionOnClientMessage.STREAM_CODEC, RunActionOnClientMessage::handle);
        registrar.playToClient(ParticleEmitterSyncMessage.TYPE, ParticleEmitterSyncMessage.STREAM_CODEC, ParticleEmitterSyncMessage::handle);
        // client → server
        registrar.playToServer(FireSpellMessage.TYPE, FireSpellMessage.STREAM_CODEC, FireSpellMessage::handle);
        registrar.playToServer(RequestSpellProgressionMenuMessage.TYPE, RequestSpellProgressionMenuMessage.STREAM_CODEC, RequestSpellProgressionMenuMessage::handle);
        registrar.playToServer(RequestLearnSpellMessage.TYPE, RequestLearnSpellMessage.STREAM_CODEC, RequestLearnSpellMessage::handle);
        registrar.playToServer(RequestEquipSpellMessage.TYPE, RequestEquipSpellMessage.STREAM_CODEC, RequestEquipSpellMessage::handle);
    }

}

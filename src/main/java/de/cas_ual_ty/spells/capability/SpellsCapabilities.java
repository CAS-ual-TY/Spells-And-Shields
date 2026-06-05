package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.context.BuiltinEvents;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

public class SpellsCapabilities
{
    static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SpellsAndShields.MOD_ID);

    public static final Supplier<AttachmentType<ManaHolder>> MANA_HOLDER =
            ATTACHMENT_TYPES.register("mana_holder", () -> AttachmentType.serializable(ManaHolder::new).build());

    public static final Supplier<AttachmentType<SpellHolder>> SPELL_HOLDER =
            ATTACHMENT_TYPES.register("spell_holder", () -> AttachmentType.serializable(SpellHolder::new).build());

    public static final Supplier<AttachmentType<SpellProgressionHolder>> SPELL_PROGRESSION_HOLDER =
            ATTACHMENT_TYPES.register("spell_progression_holder", () -> AttachmentType.serializable(SpellProgressionHolder::new).build());

    public static final Supplier<AttachmentType<ExtraTagHolder>> EXTRA_TAG_HOLDER =
            ATTACHMENT_TYPES.register("extra_tag_holder", () -> AttachmentType.serializable(ExtraTagHolder::new).build());

    public static final Supplier<AttachmentType<DelayedSpellHolder>> DELAYED_SPELL_HOLDER =
            ATTACHMENT_TYPES.register("delayed_spell_holder", () -> AttachmentType.serializable(DelayedSpellHolder::new).build());

    public static final Supplier<AttachmentType<ParticleEmitterHolder>> PARTICLE_EMITTER_HOLDER =
            ATTACHMENT_TYPES.register("particle_emitter_holder", () -> AttachmentType.serializable(ParticleEmitterHolder::new).build());

    private static void playerClone(PlayerEvent.Clone event)
    {
        if(!event.isWasDeath())
        {
            SpellProgressionHolder.getSpellProgressionHolder(event.getEntity()).ifPresent(current ->
                    SpellProgressionHolder.getSpellProgressionHolder(event.getOriginal()).ifPresent(original ->
                            current.deserializeNBT(original.serializeNBT())));

            ManaHolder.getManaHolder(event.getEntity()).ifPresent(current ->
            {
                ManaHolder.getManaHolder(event.getOriginal()).ifPresent(original ->
                        current.deserializeNBT(original.serializeNBT()));
                current.sendSync();
            });

            SpellHolder.getSpellHolder(event.getEntity()).ifPresent(current ->
            {
                SpellHolder.getSpellHolder(event.getOriginal()).ifPresent(original ->
                        current.deserializeNBT(original.serializeNBT()));
                current.sendSync();
            });

            ExtraTagHolder.getHolder(event.getEntity()).ifPresent(current ->
                    ExtraTagHolder.getHolder(event.getOriginal()).ifPresent(original ->
                            current.deserializeNBT(original.serializeNBT())));

            DelayedSpellHolder.getHolder(event.getEntity()).ifPresent(current ->
                    DelayedSpellHolder.getHolder(event.getOriginal()).ifPresent(original ->
                            current.deserializeNBT(original.serializeNBT())));

            ParticleEmitterHolder.getHolder(event.getEntity()).ifPresent(current ->
                    ParticleEmitterHolder.getHolder(event.getOriginal()).ifPresent(original ->
                            current.deserializeNBT(original.serializeNBT())));
        }
        else
        {
            SpellProgressionHolder.getSpellProgressionHolder(event.getEntity()).ifPresent(current ->
            {
                SpellProgressionHolder.getSpellProgressionHolder(event.getOriginal()).ifPresent(original ->
                        current.deserializeNBT(original.serializeNBT()));

                if(SpellsConfig.FORGET_SPELLS_ON_DEATH.get())
                {
                    for(SpellNodeId key : current.getProgression().keySet())
                    {
                        if(current.getSpellStatus(key) == SpellStatus.LEARNED)
                        {
                            current.setSpellStatus(key, SpellStatus.FORGOTTEN);
                        }
                    }
                }
            });

            ManaHolder.getManaHolder(event.getEntity()).ifPresent(manaHolder ->
            {
                if(SpellsConfig.RESPAWN_WITH_FULL_MANA.get())
                {
                    manaHolder.replenish(manaHolder.getMaxMana());
                }
                manaHolder.sendSync();
            });

            if(!SpellsConfig.CLEAR_SLOTS_ON_DEATH.get() && !SpellsConfig.FORGET_SPELLS_ON_DEATH.get())
            {
                SpellHolder.getSpellHolder(event.getEntity()).ifPresent(current ->
                {
                    SpellHolder.getSpellHolder(event.getOriginal()).ifPresent(original ->
                            current.deserializeNBT(original.serializeNBT()));
                    current.sendSync();
                    current.activateAll(BuiltinEvents.ON_EQUIP.activation);
                });
            }
        }
    }

    private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer player)
        {
            ManaHolder.getManaHolder(player).ifPresent(ManaHolder::sendSync);
            SpellHolder.getSpellHolder(player).ifPresent(SpellHolder::sendSync);
        }
    }

    private static void playerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer player)
        {
            ManaHolder.getManaHolder(player).ifPresent(ManaHolder::sendSync);
            SpellHolder.getSpellHolder(player).ifPresent(SpellHolder::sendSync);
        }
    }

    private static void playerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer player)
        {
            ManaHolder.getManaHolder(player).ifPresent(ManaHolder::sendSync);
            SpellHolder.getSpellHolder(player).ifPresent(SpellHolder::sendSync);
        }
    }

    private static void startTracking(PlayerEvent.StartTracking event)
    {
        if(event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            ParticleEmitterHolder.getHolder(event.getTarget()).ifPresent(h ->
                    PacketDistributor.sendToPlayer(serverPlayer, h.makeSyncMessage()));

            if(event.getTarget() instanceof LivingEntity livingEntity)
            {
                ManaHolder.getManaHolder(livingEntity).ifPresent(manaHolder ->
                        PacketDistributor.sendToPlayer(serverPlayer, manaHolder.makeSyncMessage()));

                if(livingEntity instanceof Player target)
                {
                    SpellHolder.getSpellHolder(target).ifPresent(spellHolder ->
                            PacketDistributor.sendToPlayer(serverPlayer, spellHolder.makeSyncMessage()));
                }
            }
        }
    }

    private static void levelTick(LevelTickEvent.Post event)
    {
        if(event.getLevel() instanceof ServerLevel level)
        {
            List<Entity> entities = List.copyOf(level.getAllEntities().toList());
            entities.forEach(e ->
            {
                DelayedSpellHolder.getHolder(e).ifPresent(DelayedSpellHolder::tick);
                ParticleEmitterHolder.getHolder(e).ifPresent(h -> h.tick(false));
            });
        }
    }

    private static void playerTick(PlayerTickEvent.Post event)
    {
        if(!event.getEntity().level().isClientSide)
        {
            ManaHolder.getManaHolder(event.getEntity()).ifPresent(ManaHolder::tick);
        }
    }

    public static void registerEvents(IEventBus modEventBus)
    {
        ATTACHMENT_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerClone);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerLoggedIn);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerRespawn);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerChangedDimensions);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::startTracking);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::levelTick);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerTick);
    }
}

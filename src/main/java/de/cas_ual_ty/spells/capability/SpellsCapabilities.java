package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.SpellsConfig;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.spell.context.BuiltinEvents;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.capabilities.*;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedList;
import java.util.List;

public class SpellsCapabilities
{
    public static Capability<ManaHolder> MANA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<SpellHolder> SPELLS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<SpellProgressionHolder> SPELL_PROGRESSION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<ExtraTagHolder> EXTRA_TAG_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<DelayedSpellHolder> DELAYED_SPELL_HOLDER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<ParticleEmitterHolder> PARTICLE_EMITTER_HOLDER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    
    private static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(ManaHolder.class);
        event.register(SpellHolder.class);
        event.register(SpellProgressionHolder.class);
        event.register(ExtraTagHolder.class);
        event.register(DelayedSpellHolder.class);
        event.register(ParticleEmitterHolder.class);
    }
    
    private static void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if(event.getObject() instanceof Player player)
        {
            ManaHolder manaHolder = new ManaHolder(player);
            attachCapability(event, manaHolder, MANA_CAPABILITY, "mana_holder");
            
            SpellHolder spellHolder = new SpellHolder(player);
            attachCapability(event, spellHolder, SPELLS_CAPABILITY, "spell_holder");
            
            SpellProgressionHolder spellProgressionHolder = new SpellProgressionHolder(player);
            attachCapability(event, spellProgressionHolder, SPELL_PROGRESSION_CAPABILITY, "spell_progression_holder");
        }
        
        ExtraTagHolder extraTagHolder = new ExtraTagHolder();
        attachCapability(event, extraTagHolder, EXTRA_TAG_CAPABILITY, "extra_tag_holder");
        
        DelayedSpellHolder delayedSpellHolder = new DelayedSpellHolder(event.getObject());
        attachCapability(event, delayedSpellHolder, DELAYED_SPELL_HOLDER_CAPABILITY, "delayed_spell_holder");
        
        ParticleEmitterHolder particleEmitterHolder = new ParticleEmitterHolder(event.getObject());
        attachCapability(event, particleEmitterHolder, PARTICLE_EMITTER_HOLDER_CAPABILITY, "particle_emitter_holder");
    }
    
    private static <T extends Tag, C extends INBTSerializable<T>> void attachCapability(AttachCapabilitiesEvent<?> event, C capData, Capability<C> capability, String name)
    {
        LazyOptional<C> optional = LazyOptional.of(() -> capData);
        ICapabilitySerializable<T> provider = new ICapabilitySerializable<>()
        {
            @Override
            public <S> LazyOptional<S> getCapability(Capability<S> cap, Direction side)
            {
                if(cap == capability)
                {
                    return optional.cast();
                }
                
                return LazyOptional.empty();
            }
            
            @Override
            public T serializeNBT()
            {
                return capData.serializeNBT();
            }
            
            @Override
            public void deserializeNBT(T tag)
            {
                capData.deserializeNBT(tag);
            }
        };
        
        event.addCapability(new ResourceLocation(SpellsAndShields.MOD_ID, name), provider);
    }
    
    private static void playerClone(PlayerEvent.Clone event)
    {
        event.getOriginal().reviveCaps();
        
        if(!event.isWasDeath())
        {
            SpellProgressionHolder.getSpellProgressionHolder(event.getEntity()).ifPresent(current ->
            {
                SpellProgressionHolder.getSpellProgressionHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
            });
            
            ManaHolder.getManaHolder(event.getEntity()).ifPresent(current ->
            {
                ManaHolder.getManaHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
                
                current.sendSync();
            });
            
            SpellHolder.getSpellHolder(event.getEntity()).ifPresent(current ->
            {
                SpellHolder.getSpellHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
                
                current.sendSync();
            });
            
            ExtraTagHolder.getHolder(event.getEntity()).ifPresent(current ->
            {
                ExtraTagHolder.getHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
            });
            
            DelayedSpellHolder.getHolder(event.getEntity()).ifPresent(current ->
            {
                DelayedSpellHolder.getHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
            });
            
            ParticleEmitterHolder.getHolder(event.getEntity()).ifPresent(current ->
            {
                ParticleEmitterHolder.getHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
            });
        }
        else
        {
            SpellProgressionHolder.getSpellProgressionHolder(event.getEntity()).ifPresent(current ->
            {
                SpellProgressionHolder.getSpellProgressionHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
                
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
                    {
                        current.deserializeNBT(original.serializeNBT());
                    });
                    
                    current.sendSync();
                    current.activateAll(BuiltinEvents.ON_EQUIP.activation);
                });
            }
        }
        
        event.getOriginal().invalidateCaps();
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
            ParticleEmitterHolder.getHolder(event.getTarget()).ifPresent(particleEmitterHolder -> SpellsAndShields.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), particleEmitterHolder.makeSyncMessage()));
            
            if(event.getTarget() instanceof LivingEntity livingEntity)
            {
                ManaHolder.getManaHolder(livingEntity).ifPresent(manaHolder -> SpellsAndShields.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), manaHolder.makeSyncMessage()));
                
                if(livingEntity instanceof Player target)
                {
                    SpellHolder.getSpellHolder(target).ifPresent((spellHolder -> SpellsAndShields.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), spellHolder.makeSyncMessage())));
                }
            }
        }
    }
    
    private static void levelTick(TickEvent.LevelTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            if(event.level instanceof ServerLevel level)
            {
                List<Entity> entities = new LinkedList<>();
                for(Entity e : level.getAllEntities())
                {
                    if(e != null)
                    {
                        entities.add(e);
                    }
                }
                entities.forEach(e ->
                {
                    DelayedSpellHolder.getHolder(e).ifPresent(DelayedSpellHolder::tick);
                    ParticleEmitterHolder.getHolder(e).ifPresent(h -> h.tick(false));
                });
            }
        }
    }
    
    private static void playerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END && !event.player.level().isClientSide)
        {
            ManaHolder.getManaHolder(event.player).ifPresent(ManaHolder::tick);
        }
    }
    
    public static void registerEvents()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellsCapabilities::registerCapabilities);
        NeoForge.EVENT_BUS.addGenericListener(Entity.class, SpellsCapabilities::attachCapabilities);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerClone);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerLoggedIn);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerRespawn);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerChangedDimensions);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::startTracking);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::levelTick);
        NeoForge.EVENT_BUS.addListener(SpellsCapabilities::playerTick);
    }
}

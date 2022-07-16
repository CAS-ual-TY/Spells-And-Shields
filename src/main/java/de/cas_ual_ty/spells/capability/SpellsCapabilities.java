package de.cas_ual_ty.spells.capability;

import de.cas_ual_ty.spells.SpellsAndShields;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

public class SpellsCapabilities
{
    public static Capability<IManaHolder> MANA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<ISpellHolder> SPELLS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<ISpellProgressionHolder> SPELL_PROGRESSION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    
    private static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(IManaHolder.class);
        event.register(ISpellHolder.class);
        event.register(ISpellProgressionHolder.class);
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
    }
    
    private static void playerClone(PlayerEvent.Clone event)
    {
        event.getOriginal().reviveCaps();
        
        if(!event.isWasDeath())
        {
            SpellProgressionHolder.getSpellProgressionHolder(event.getPlayer()).ifPresent(current ->
            {
                SpellProgressionHolder.getSpellProgressionHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
            });
            
            ManaHolder.getManaHolder(event.getPlayer()).ifPresent(current ->
            {
                ManaHolder.getManaHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
                
                current.sendSync();
            });
            
            SpellHolder.getSpellHolder(event.getPlayer()).ifPresent(current ->
            {
                SpellHolder.getSpellHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
                
                current.sendSync();
            });
        }
        else
        {
            SpellProgressionHolder.getSpellProgressionHolder(event.getPlayer()).ifPresent(current ->
            {
                SpellProgressionHolder.getSpellProgressionHolder(event.getOriginal()).ifPresent(original ->
                {
                    current.deserializeNBT(original.serializeNBT());
                });
            });
        }
        
        event.getOriginal().invalidateCaps();
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
    
    private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getPlayer() instanceof ServerPlayer player)
        {
            ManaHolder.getManaHolder(player).ifPresent(ManaHolder::sendSync);
            SpellHolder.getSpellHolder(player).ifPresent(SpellHolder::sendSync);
        }
    }
    
    private static void playerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if(event.getPlayer() instanceof ServerPlayer player)
        {
            ManaHolder.getManaHolder(player).ifPresent(ManaHolder::sendSync);
            SpellHolder.getSpellHolder(player).ifPresent(SpellHolder::sendSync);
        }
    }
    
    private static void playerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if(event.getPlayer() instanceof ServerPlayer player)
        {
            ManaHolder.getManaHolder(player).ifPresent(ManaHolder::sendSync);
            SpellHolder.getSpellHolder(player).ifPresent(SpellHolder::sendSync);
        }
    }
    
    private static void startTracking(PlayerEvent.StartTracking event)
    {
        if(event.getPlayer() instanceof ServerPlayer serverPlayer && event.getTarget() instanceof LivingEntity livingEntity)
        {
            ManaHolder.getManaHolder(livingEntity).ifPresent(manaHolder -> SpellsAndShields.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), manaHolder.makeSyncMessage()));
            
            if(livingEntity instanceof Player target)
            {
                SpellHolder.getSpellHolder(target).ifPresent((spellHolder -> SpellsAndShields.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), spellHolder.makeSyncMessage())));
            }
        }
    }
    
    private static void playerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END && !event.player.level.isClientSide)
        {
            ManaHolder.getManaHolder(event.player).ifPresent(ManaHolder::tick);
        }
    }
    
    public static void registerEvents()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellsCapabilities::registerCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, SpellsCapabilities::attachCapabilities);
        MinecraftForge.EVENT_BUS.addListener(SpellsCapabilities::playerClone);
        MinecraftForge.EVENT_BUS.addListener(SpellsCapabilities::playerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(SpellsCapabilities::playerRespawn);
        MinecraftForge.EVENT_BUS.addListener(SpellsCapabilities::playerChangedDimensions);
        MinecraftForge.EVENT_BUS.addListener(SpellsCapabilities::startTracking);
        MinecraftForge.EVENT_BUS.addListener(SpellsCapabilities::playerTick);
    }
}

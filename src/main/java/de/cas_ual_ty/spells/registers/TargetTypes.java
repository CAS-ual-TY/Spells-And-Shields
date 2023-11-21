package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.spell.target.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class TargetTypes
{
    public static final ResourceKey<Registry<ITargetType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "targets"));
    private static final DeferredRegister<ITargetType<?>> DEFERRED_REGISTER = DeferredRegister.create(REGISTRY_KEY, MOD_ID);
    public static final Registry<ITargetType<?>> REGISTRY = DEFERRED_REGISTER.makeRegistry(builder -> builder.maxId(256));
    
    public static final DeferredHolder<ITargetType<?>, ITargetType<EntityTarget>> ENTITY = DEFERRED_REGISTER.register("entity", () -> (t -> t instanceof EntityTarget));
    public static final DeferredHolder<ITargetType<?>, ITargetType<LivingEntityTarget>> LIVING_ENTITY = DEFERRED_REGISTER.register("living_entity", () -> (t -> t instanceof LivingEntityTarget));
    public static final DeferredHolder<ITargetType<?>, ITargetType<PlayerTarget>> PLAYER = DEFERRED_REGISTER.register("player", () -> (t -> t instanceof PlayerTarget));
    public static final DeferredHolder<ITargetType<?>, ITargetType<ItemTarget>> ITEM = DEFERRED_REGISTER.register("item", () -> (t -> t instanceof ItemTarget));
    public static final DeferredHolder<ITargetType<?>, ITargetType<PositionTarget>> POSITION = DEFERRED_REGISTER.register("position", () -> (t -> t instanceof PositionTarget));
    public static final DeferredHolder<ITargetType<?>, ITargetType<StaticTarget>> STATIC = DEFERRED_REGISTER.register("static", () -> (t -> t instanceof StaticTarget));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TargetTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        //REGISTRY = event.create(new RegistryBuilder<ITargetType<?>>().setMaxID(256).setName(new ResourceLocation(MOD_ID, "targets")));
    }
}

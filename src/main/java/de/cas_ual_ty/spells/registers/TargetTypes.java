package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.spell.target.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class TargetTypes
{
    public static Supplier<IForgeRegistry<ITargetType<?>>> REGISTRY;
    private static final DeferredRegister<ITargetType<?>> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(MOD_ID, "targets"), MOD_ID);
    public static final RegistryObject<ITargetType<EntityTarget>> ENTITY_TARGET = DEFERRED_REGISTER.register("entity", () -> (t -> t instanceof EntityTarget));
    public static final RegistryObject<ITargetType<LivingEntityTarget>> LIVING_ENTITY_TARGET = DEFERRED_REGISTER.register("living_entity", () -> (t -> t instanceof LivingEntityTarget));
    public static final RegistryObject<ITargetType<PlayerTarget>> PLAYER_TARGET = DEFERRED_REGISTER.register("player", () -> (t -> t instanceof PlayerTarget));
    public static final RegistryObject<ITargetType<ItemTarget>> ITEM_TARGET = DEFERRED_REGISTER.register("item", () -> (t -> t instanceof ItemTarget));
    public static final RegistryObject<ITargetType<PositionTarget>> POSITION_TARGET = DEFERRED_REGISTER.register("position", () -> (t -> t instanceof PositionTarget));
    public static final RegistryObject<ITargetType<StaticTarget>> STATIC_TARGET = DEFERRED_REGISTER.register("static", () -> (t -> t instanceof StaticTarget));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TargetTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<ITargetType<?>>().setMaxID(256).setName(new ResourceLocation(MOD_ID, "targets")));
    }
}

package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.requirement.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class RequirementTypes
{
    public static final ResourceKey<Registry<RequirementType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "requirements"));
    private static final DeferredRegister<RequirementType<?>> DEFERRED_REGISTER = DeferredRegister.create(REGISTRY_KEY, MOD_ID);
    public static final Registry<RequirementType<?>> REGISTRY = DEFERRED_REGISTER.makeRegistry(builder -> builder.maxId(256));
    
    public static final DeferredHolder<RequirementType<?>, RequirementType<WrappedRequirement>> WRAPPED = DEFERRED_REGISTER.register("client_wrap", () -> new RequirementType<>(WrappedRequirement::new, (type) -> WrappedRequirement.CODEC));
    public static final DeferredHolder<RequirementType<?>, RequirementType<BookshelvesRequirement>> BOOKSHELVES = DEFERRED_REGISTER.register("bookshelves", () -> new RequirementType<>(BookshelvesRequirement::new, BookshelvesRequirement::makeCodec));
    public static final DeferredHolder<RequirementType<?>, RequirementType<AdvancementRequirement>> ADVANCEMENT = DEFERRED_REGISTER.register("advancement", () -> new RequirementType<>(AdvancementRequirement::new, AdvancementRequirement::makeCodec));
    public static final DeferredHolder<RequirementType<?>, RequirementType<ItemRequirement>> ITEM = DEFERRED_REGISTER.register("item", () -> new RequirementType<>(ItemRequirement::new, ItemRequirement::makeCodec));
    public static final DeferredHolder<RequirementType<?>, RequirementType<ConfigRequirement>> CONFIG = DEFERRED_REGISTER.register("config", () -> new RequirementType<>(ConfigRequirement::new, ConfigRequirement::makeCodec));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(RequirementTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        //REGISTRY = event.create(new RegistryBuilder<RequirementType<?>>().setMaxID(256).setName(new ResourceLocation(MOD_ID, "requirements")));
    }
}

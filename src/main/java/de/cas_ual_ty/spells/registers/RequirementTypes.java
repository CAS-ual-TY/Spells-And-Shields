package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.requirement.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class RequirementTypes
{
    public static Supplier<IForgeRegistry<RequirementType<?>>> REGISTRY;
    private static final DeferredRegister<RequirementType<?>> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(MOD_ID, "requirements"), MOD_ID);
    
    public static final RegistryObject<RequirementType<WrappedRequirement>> WRAPPED = DEFERRED_REGISTER.register("client_wrap", () -> new RequirementType<>(WrappedRequirement::new, (type) -> WrappedRequirement.CODEC));
    public static final RegistryObject<RequirementType<BookshelvesRequirement>> BOOKSHELVES = DEFERRED_REGISTER.register("bookshelves", () -> new RequirementType<>(BookshelvesRequirement::new, BookshelvesRequirement::makeCodec));
    public static final RegistryObject<RequirementType<AdvancementRequirement>> ADVANCEMENT = DEFERRED_REGISTER.register("advancement", () -> new RequirementType<>(AdvancementRequirement::new, AdvancementRequirement::makeCodec));
    public static final RegistryObject<RequirementType<ItemRequirement>> ITEM = DEFERRED_REGISTER.register("item", () -> new RequirementType<>(ItemRequirement::new, ItemRequirement::makeCodec));
    public static final RegistryObject<RequirementType<ConfigRequirement>> CONFIG = DEFERRED_REGISTER.register("config", () -> new RequirementType<>(ConfigRequirement::new, ConfigRequirement::makeCodec));
    public static final RegistryObject<RequirementType<MinRequirement>> MIN = DEFERRED_REGISTER.register("min", () -> new RequirementType<>(MinRequirement::new, MinRequirement::makeCodec));
    public static final RegistryObject<RequirementType<MaxRequirement>> MAX = DEFERRED_REGISTER.register("max", () -> new RequirementType<>(MaxRequirement::new, MaxRequirement::makeCodec));
    public static final RegistryObject<RequirementType<LearnedRequirement>> LEARNED = DEFERRED_REGISTER.register("learned", () -> new RequirementType<>(LearnedRequirement::new, LearnedRequirement::makeCodec));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(RequirementTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        RequirementType<Requirement> classObj = new RequirementType<>();
        REGISTRY = event.create(new RegistryBuilder<RequirementType<?>>().setType((Class<RequirementType<?>>) classObj.getClass()).setMaxID(256).setName(new ResourceLocation(MOD_ID, "requirements")));
    }
}

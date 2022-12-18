package de.cas_ual_ty.spells.registers;

import de.cas_ual_ty.spells.spell.icon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static de.cas_ual_ty.spells.SpellsAndShields.MOD_ID;

public class SpellIconTypes
{
    public static Supplier<IForgeRegistry<SpellIconType<?>>> REGISTRY;
    private static final DeferredRegister<SpellIconType<?>> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(MOD_ID, "spell_icons"), MOD_ID);
    public static final RegistryObject<SpellIconType<DefaultSpellIcon>> DEFAULT_SPELL_ICON = DEFERRED_REGISTER.register("default", () -> new SpellIconType<>(DefaultSpellIcon::new, DefaultSpellIcon::makeCodec));
    public static final RegistryObject<SpellIconType<SizedSpellIcon>> SIZED_SPELL_ICON = DEFERRED_REGISTER.register("sized", () -> new SpellIconType<>(SizedSpellIcon::new, SizedSpellIcon::makeCodec));
    public static final RegistryObject<SpellIconType<AdvancedSpellIcon>> ADVANCED_SPELL_ICON = DEFERRED_REGISTER.register("advanced", () -> new SpellIconType<>(AdvancedSpellIcon::new, AdvancedSpellIcon::makeCodec));
    public static final RegistryObject<SpellIconType<ItemSpellIcon>> ITEM_SPELL_ICON = DEFERRED_REGISTER.register("item", () -> new SpellIconType<>(ItemSpellIcon::new, ItemSpellIcon::makeCodec));
    
    public static void register()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpellIconTypes::newRegistry);
        DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static void newRegistry(NewRegistryEvent event)
    {
        REGISTRY = event.create(new RegistryBuilder<SpellIconType<?>>().setMaxID(1024).setName(new ResourceLocation(MOD_ID, "spell_icons")));
    }
}

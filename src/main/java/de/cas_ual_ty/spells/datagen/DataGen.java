package de.cas_ual_ty.spells.datagen;

import de.cas_ual_ty.spells.SpellsAndShields;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        event.getGenerator().addProvider(event.includeClient(), new LangGen(event.getGenerator(), "en_us"));
        event.getGenerator().addProvider(event.includeServer(), new SpellsGen(event.getGenerator(), SpellsAndShields.MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeServer(), new SpellTreesGen(event.getGenerator(), SpellsAndShields.MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(event.includeServer(), new DocsGen(event.getGenerator(), SpellsAndShields.MOD_ID, event.getExistingFileHelper()));
    }
}

package de.cas_ual_ty.spells.datagen;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        event.getGenerator().addProvider(new LangGen(event.getGenerator(), "en_us"));
    }
}

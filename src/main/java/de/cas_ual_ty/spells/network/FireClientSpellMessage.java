package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.client.ClientMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record FireClientSpellMessage(ResourceLocation spell)
{
    public static void encode(FireClientSpellMessage msg, FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(msg.spell());
    }
    
    public static FireClientSpellMessage decode(FriendlyByteBuf buf)
    {
        return new FireClientSpellMessage(buf.readResourceLocation());
    }
    
    public static void handle(FireClientSpellMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientMessageHandler.handleFireSpell(msg));
        context.get().setPacketHandled(true);
    }
}

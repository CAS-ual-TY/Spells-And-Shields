package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.client.ClientMessageHandler;
import de.cas_ual_ty.spells.spell.IClientSpell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record FireClientSpellMessage(IClientSpell spell)
{
    public static void encode(FireClientSpellMessage msg, FriendlyByteBuf buf)
    {
        buf.writeRegistryId(msg.spell());
    }
    
    public static FireClientSpellMessage decode(FriendlyByteBuf buf)
    {
        return new FireClientSpellMessage((IClientSpell) buf.readRegistryId());
    }
    
    public static void handle(FireClientSpellMessage msg, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() -> ClientMessageHandler.handleFireSpell(msg));
        context.get().setPacketHandled(true);
    }
}
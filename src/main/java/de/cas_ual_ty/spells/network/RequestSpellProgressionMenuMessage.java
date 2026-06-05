package de.cas_ual_ty.spells.network;

import de.cas_ual_ty.spells.SpellsAndShields;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.progression.SpellProgressionMenu;
import de.cas_ual_ty.spells.progression.SpellStatus;
import de.cas_ual_ty.spells.registers.Spells;
import de.cas_ual_ty.spells.spell.Spell;
import de.cas_ual_ty.spells.spelltree.SpellNodeId;
import de.cas_ual_ty.spells.spelltree.SpellTree;
import de.cas_ual_ty.spells.util.ProgressionHelper;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;

public record RequestSpellProgressionMenuMessage(BlockPos pos) implements CustomPacketPayload
{
    public static final Type<RequestSpellProgressionMenuMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpellsAndShields.MOD_ID, "request_progression_menu"));
    public static final StreamCodec<FriendlyByteBuf, RequestSpellProgressionMenuMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> buf.writeBlockPos(msg.pos()),
            buf -> new RequestSpellProgressionMenuMessage(buf.readBlockPos())
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(RequestSpellProgressionMenuMessage msg, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            ServerPlayer player = (ServerPlayer) context.player();

            try
            {
                if(player.hasContainerOpen() && SpellsUtil.isEnchantingTable(player.level().getBlockState(msg.pos()).getBlock()))
                {
                    ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), msg.pos());

                    SpellProgressionHolder.getSpellProgressionHolder(player).ifPresent(spellProgressionHolder ->
                    {
                        access.execute((level, blockPos) ->
                        {
                            List<SpellTree> availableSpellTrees = ProgressionHelper.getStrippedSpellTrees(spellProgressionHolder, access);
                            HashMap<SpellNodeId, SpellStatus> progression = spellProgressionHolder.getProgression();

                            player.openMenu(new MenuProvider()
                            {
                                @Override
                                public Component getDisplayName()
                                {
                                    return SpellProgressionMenu.TITLE;
                                }

                                @Override
                                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player)
                                {
                                    return new SpellProgressionMenu(id, inventory, access, availableSpellTrees, progression);
                                }
                            }, buf -> SpellProgressionSyncMessage.STREAM_CODEC.encode(buf, new SpellProgressionSyncMessage(blockPos, availableSpellTrees, progression)));
                        });
                    });
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        });
    }
}

package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.scores.PlayerTeam;

import java.util.List;

public class TeamColorRequirement extends Requirement
{
    public static Codec<TeamColorRequirement> makeCodec(RequirementType<TeamColorRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                ChatFormatting.CODEC.fieldOf("color").forGetter(TeamColorRequirement::getColor)
        ).apply(instance, (color) -> new TeamColorRequirement(type, color)));
    }

    protected ChatFormatting color;

    public TeamColorRequirement(RequirementType<?> type)
    {
        super(type);
    }

    public TeamColorRequirement(RequirementType<?> type, ChatFormatting color)
    {
        this(type);
        this.color = color;
    }

    public ChatFormatting getColor()
    {
        return color;
    }

    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        PlayerTeam team = spellProgressionHolder.getPlayer().getTeam();
        return team != null && team.getColor() == color;
    }

    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId, color.getName())));
    }

    @Override
    public void writeToBuf(RegistryFriendlyByteBuf buf)
    {
        buf.writeEnum(color);
    }

    @Override
    public void readFromBuf(RegistryFriendlyByteBuf buf)
    {
        color = buf.readEnum(ChatFormatting.class);
    }
}

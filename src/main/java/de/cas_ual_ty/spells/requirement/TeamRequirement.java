package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.scores.PlayerTeam;

import java.util.List;

public class TeamRequirement extends Requirement
{
    public static Codec<TeamRequirement> makeCodec(RequirementType<TeamRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("team").forGetter(TeamRequirement::getTeam)
        ).apply(instance, (team) -> new TeamRequirement(type, team)));
    }

    protected String team;

    public TeamRequirement(RequirementType<?> type)
    {
        super(type);
    }

    public TeamRequirement(RequirementType<?> type, String team)
    {
        this(type);
        this.team = team;
    }

    public String getTeam()
    {
        return team;
    }

    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        PlayerTeam playerTeam = spellProgressionHolder.getPlayer().getTeam();
        return playerTeam != null && playerTeam.getName().equals(team);
    }

    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId, team)));
    }

    @Override
    public void writeToBuf(RegistryFriendlyByteBuf buf)
    {
        buf.writeUtf(team);
    }

    @Override
    public void readFromBuf(RegistryFriendlyByteBuf buf)
    {
        team = buf.readUtf();
    }
}

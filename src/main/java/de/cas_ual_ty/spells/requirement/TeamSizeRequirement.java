package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.scores.PlayerTeam;

import java.util.List;

public class TeamSizeRequirement extends Requirement
{
    public static Codec<TeamSizeRequirement> makeCodec(RequirementType<TeamSizeRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("minimum").forGetter(TeamSizeRequirement::getMinimum),
                Codec.BOOL.fieldOf("online_only").forGetter(TeamSizeRequirement::getOnlineOnly)
        ).apply(instance, (minimum, onlineOnly) -> new TeamSizeRequirement(type, minimum, onlineOnly)));
    }

    protected int minimum;
    protected boolean onlineOnly;

    public TeamSizeRequirement(RequirementType<?> type)
    {
        super(type);
    }

    public TeamSizeRequirement(RequirementType<?> type, int minimum, boolean onlineOnly)
    {
        this(type);
        this.minimum = minimum;
        this.onlineOnly = onlineOnly;
    }

    public int getMinimum()
    {
        return minimum;
    }

    public boolean getOnlineOnly()
    {
        return onlineOnly;
    }

    protected int getTeamSize(SpellProgressionHolder spellProgressionHolder)
    {
        PlayerTeam team = spellProgressionHolder.getPlayer().getTeam();

        if(team == null)
        {
            return 0;
        }

        if(!onlineOnly)
        {
            return team.getPlayers().size();
        }

        if(!(spellProgressionHolder.getPlayer().level() instanceof ServerLevel level))
        {
            return 0;
        }

        int count = 0;

        for(String playerName : team.getPlayers())
        {
            if(level.getServer().getPlayerList().getPlayerByName(playerName) != null)
            {
                count++;
            }
        }

        return count;
    }

    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return getTeamSize(spellProgressionHolder) >= minimum;
    }

    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId, getTeamSize(spellProgressionHolder), minimum)));
    }

    @Override
    public void writeToBuf(RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(minimum);
        buf.writeBoolean(onlineOnly);
    }

    @Override
    public void readFromBuf(RegistryFriendlyByteBuf buf)
    {
        minimum = buf.readInt();
        onlineOnly = buf.readBoolean();
    }
}

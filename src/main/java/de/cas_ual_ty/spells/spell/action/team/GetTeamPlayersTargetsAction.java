package de.cas_ual_ty.spells.spell.action.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.DstTargetAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class GetTeamPlayersTargetsAction extends DstTargetAction
{
    public static Codec<GetTeamPlayersTargetsAction> makeCodec(SpellActionType<GetTeamPlayersTargetsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                dstCodec("players"),
                Codec.STRING.fieldOf(ParamNames.singleTarget("reference")).forGetter(GetTeamPlayersTargetsAction::getReference)
        ).apply(instance, (activation, dst, reference) -> new GetTeamPlayersTargetsAction(type, activation, dst, reference)));
    }

    public static GetTeamPlayersTargetsAction make(Object activation, Object dst, Object reference)
    {
        return new GetTeamPlayersTargetsAction(SpellActionTypes.GET_TEAM_PLAYERS_TARGETS.get(), activation.toString(), dst.toString(), reference.toString());
    }

    protected String reference;

    public GetTeamPlayersTargetsAction(SpellActionType<?> type)
    {
        super(type);
    }

    public GetTeamPlayersTargetsAction(SpellActionType<?> type, String activation, String dst, String reference)
    {
        super(type, activation, dst);
        this.reference = reference;
    }

    public String getReference()
    {
        return reference;
    }

    @Override
    public void findTargets(SpellContext ctx, TargetGroup destination)
    {
        if(!(ctx.level instanceof ServerLevel level))
        {
            return;
        }

        TargetGroup referenceGroup = ctx.getTargetGroup(reference);

        if(referenceGroup == null)
        {
            return;
        }

        referenceGroup.getSingleTarget()
                .filter(t -> t instanceof EntityTarget)
                .map(t -> ((EntityTarget) t).getEntity().getTeam())
                .ifPresent(team ->
                {
                    for(String playerName : team.getPlayers())
                    {
                        ServerPlayer player = level.getServer().getPlayerList().getPlayerByName(playerName);

                        if(player != null)
                        {
                            destination.addTargets(new PlayerTarget(TargetTypes.PLAYER.get(), player));
                        }
                    }
                });
    }
}

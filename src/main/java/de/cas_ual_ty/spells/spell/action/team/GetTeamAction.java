package de.cas_ual_ty.spells.spell.action.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import net.minecraft.world.scores.PlayerTeam;

public class GetTeamAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetTeamAction> makeCodec(SpellActionType<GetTeamAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("team")).forGetter(GetTeamAction::getTeam),
                Codec.STRING.fieldOf(ParamNames.var("color")).forGetter(GetTeamAction::getColor),
                Codec.STRING.fieldOf(ParamNames.var("allow_friendly_fire")).forGetter(GetTeamAction::getAllowFriendlyFire),
                Codec.STRING.fieldOf(ParamNames.var("see_friendly_invisibles")).forGetter(GetTeamAction::getSeeFriendlyInvisibles),
                Codec.STRING.fieldOf(ParamNames.var("collision_rule")).forGetter(GetTeamAction::getCollisionRule),
                Codec.STRING.fieldOf(ParamNames.var("name_tag_visibility")).forGetter(GetTeamAction::getNameTagVisibility),
                Codec.STRING.fieldOf(ParamNames.var("death_message_visibility")).forGetter(GetTeamAction::getDeathMessageVisibility)
        ).apply(instance, (activation, source, team, color, allowFriendlyFire, seeFriendlyInvisibles, collisionRule, nameTagVisibility, deathMessageVisibility) ->
                new GetTeamAction(type, activation, source, team, color, allowFriendlyFire, seeFriendlyInvisibles, collisionRule, nameTagVisibility, deathMessageVisibility)));
    }

    public static GetTeamAction make(Object activation, Object source, String team, String color, String allowFriendlyFire, String seeFriendlyInvisibles, String collisionRule, String nameTagVisibility, String deathMessageVisibility)
    {
        return new GetTeamAction(SpellActionTypes.GET_TEAM.get(), activation.toString(), source.toString(), team, color, allowFriendlyFire, seeFriendlyInvisibles, collisionRule, nameTagVisibility, deathMessageVisibility);
    }

    protected String team;
    protected String color;
    protected String allowFriendlyFire;
    protected String seeFriendlyInvisibles;
    protected String collisionRule;
    protected String nameTagVisibility;
    protected String deathMessageVisibility;

    public GetTeamAction(SpellActionType<?> type)
    {
        super(type);
    }

    public GetTeamAction(SpellActionType<?> type, String activation, String source, String team, String color, String allowFriendlyFire, String seeFriendlyInvisibles, String collisionRule, String nameTagVisibility, String deathMessageVisibility)
    {
        super(type, activation, source);
        this.team = team;
        this.color = color;
        this.allowFriendlyFire = allowFriendlyFire;
        this.seeFriendlyInvisibles = seeFriendlyInvisibles;
        this.collisionRule = collisionRule;
        this.nameTagVisibility = nameTagVisibility;
        this.deathMessageVisibility = deathMessageVisibility;

        if(!team.isEmpty())
        {
            addVariableAttribute(e -> { PlayerTeam t = getPlayerTeam(e); return t == null ? null : t.getName(); }, CtxVarTypes.STRING.get(), team);
        }

        if(!color.isEmpty())
        {
            addVariableAttribute(e -> { PlayerTeam t = getPlayerTeam(e); return t == null ? null : t.getColor().getName(); }, CtxVarTypes.STRING.get(), color);
        }

        if(!allowFriendlyFire.isEmpty())
        {
            addVariableAttribute(e -> { PlayerTeam t = getPlayerTeam(e); return t == null ? null : t.isAllowFriendlyFire(); }, CtxVarTypes.BOOLEAN.get(), allowFriendlyFire);
        }

        if(!seeFriendlyInvisibles.isEmpty())
        {
            addVariableAttribute(e -> { PlayerTeam t = getPlayerTeam(e); return t == null ? null : t.canSeeFriendlyInvisibles(); }, CtxVarTypes.BOOLEAN.get(), seeFriendlyInvisibles);
        }

        if(!collisionRule.isEmpty())
        {
            addVariableAttribute(e -> { PlayerTeam t = getPlayerTeam(e); return t == null ? null : t.getCollisionRule().name; }, CtxVarTypes.STRING.get(), collisionRule);
        }

        if(!nameTagVisibility.isEmpty())
        {
            addVariableAttribute(e -> { PlayerTeam t = getPlayerTeam(e); return t == null ? null : t.getNameTagVisibility().name; }, CtxVarTypes.STRING.get(), nameTagVisibility);
        }

        if(!deathMessageVisibility.isEmpty())
        {
            addVariableAttribute(e -> { PlayerTeam t = getPlayerTeam(e); return t == null ? null : t.getDeathMessageVisibility().name; }, CtxVarTypes.STRING.get(), deathMessageVisibility);
        }
    }

    private static PlayerTeam getPlayerTeam(EntityTarget target)
    {
        return target.getEntity().getTeam();
    }

    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }

    public String getTeam()
    {
        return team;
    }

    public String getColor()
    {
        return color;
    }

    public String getAllowFriendlyFire()
    {
        return allowFriendlyFire;
    }

    public String getSeeFriendlyInvisibles()
    {
        return seeFriendlyInvisibles;
    }

    public String getCollisionRule()
    {
        return collisionRule;
    }

    public String getNameTagVisibility()
    {
        return nameTagVisibility;
    }

    public String getDeathMessageVisibility()
    {
        return deathMessageVisibility;
    }
}

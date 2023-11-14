package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetPlayerNameAction extends GetTargetAttributeAction<PlayerTarget>
{
    public static Codec<GetPlayerNameAction> makeCodec(SpellActionType<GetPlayerNameAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("player_name")).forGetter(GetPlayerNameAction::getPlayerName)
        ).apply(instance, (activation, source, playerName) -> new GetPlayerNameAction(type, activation, source, playerName)));
    }
    
    public static GetPlayerNameAction make(Object activation, Object source, String playerName)
    {
        return new GetPlayerNameAction(SpellActionTypes.GET_PLAYER_NAME.get(), activation.toString(), source.toString(), playerName);
    }
    
    protected String playerName;
    
    public GetPlayerNameAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetPlayerNameAction(SpellActionType<?> type, String activation, String source, String playerName)
    {
        super(type, activation, source);
        this.playerName = playerName;
        
        if(!playerName.isEmpty())
        {
            addVariableAttribute(e -> e.getPlayer().getScoreboardName(), CtxVarTypes.STRING.get(), playerName);
        }
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
    
    public String getPlayerName()
    {
        return playerName;
    }
}
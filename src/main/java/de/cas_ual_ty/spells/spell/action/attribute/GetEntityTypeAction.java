package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.GetTargetAttributeAction;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

public class GetEntityTypeAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityTypeAction> makeCodec(SpellActionType<GetEntityTypeAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("entity_type")).forGetter(GetEntityTypeAction::getEntityType),
                Codec.STRING.fieldOf(ParamNames.var("is_living")).forGetter(GetEntityTypeAction::isLiving),
                Codec.STRING.fieldOf(ParamNames.var("is_player")).forGetter(GetEntityTypeAction::isPlayer)
        ).apply(instance, (activation, source, entityType, isLiving, isPlayer) -> new GetEntityTypeAction(type, activation, source, entityType, isLiving, isPlayer)));
    }
    
    public static GetEntityTypeAction make(Object activation, Object source, String entityType, String isLiving, String isPlayer)
    {
        return new GetEntityTypeAction(SpellActionTypes.GET_ENTITY_TYPE.get(), activation.toString(), source.toString(), entityType, isLiving, isPlayer);
    }
    
    protected String entityType;
    protected String isLiving;
    protected String isPlayer;
    
    public GetEntityTypeAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetEntityTypeAction(SpellActionType<?> type, String activation, String source, String entityType, String isLiving, String isPlayer)
    {
        super(type, activation, source);
        this.entityType = entityType;
        this.isLiving = isLiving;
        this.isPlayer = isPlayer;
        
        if(!entityType.isEmpty())
        {
            addVariableAttribute(e -> ForgeRegistries.ENTITY_TYPES.getKey(e.getEntity().getType()).toString(), CtxVarTypes.STRING.get(), entityType);
        }
        
        if(!isLiving.isEmpty())
        {
            addVariableAttribute(e -> e.getEntity() instanceof LivingEntity, CtxVarTypes.BOOLEAN.get(), isLiving);
        }
        
        if(!isPlayer.isEmpty())
        {
            addVariableAttribute(e -> e.getEntity() instanceof Player, CtxVarTypes.BOOLEAN.get(), isPlayer);
        }
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    public String getEntityType()
    {
        return entityType;
    }
    
    public String isLiving()
    {
        return isLiving;
    }
    
    public String isPlayer()
    {
        return isPlayer;
    }
}

package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.target.EntityTarget;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.util.ParamNames;

public class GetEntityUUIDAction extends GetTargetAttributeAction<EntityTarget>
{
    public static Codec<GetEntityUUIDAction> makeCodec(SpellActionType<GetEntityUUIDAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                Codec.STRING.fieldOf(ParamNames.var("uuid")).forGetter(GetEntityUUIDAction::getUuid)
        ).apply(instance, (activation, target, uuid) -> new GetEntityUUIDAction(type, activation, target, uuid)));
    }
    
    public static GetEntityUUIDAction make(String activation, String target, String uuid)
    {
        return new GetEntityUUIDAction(SpellActionTypes.GET_ENTITY_UUID.get(), activation, target, uuid);
    }
    
    protected String uuid;
    
    public GetEntityUUIDAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetEntityUUIDAction(SpellActionType<?> type, String activation, String target, String uuid)
    {
        super(type, activation, target);
        this.uuid = uuid;
        
        if(!uuid.isEmpty())
        {
            addVariableAttribute(e -> e.getEntity().getStringUUID(), CtxVarTypes.STRING.get(), uuid);
        }
    }
    
    @Override
    public ITargetType<EntityTarget> getAffectedType()
    {
        return TargetTypes.ENTITY.get();
    }
    
    public String getUuid()
    {
        return uuid;
    }
}

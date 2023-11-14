package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.registries.ForgeRegistries;

public class RemoveAttributeModifierAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<RemoveAttributeModifierAction> makeCodec(SpellActionType<RemoveAttributeModifierAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("attribute")).forGetter(RemoveAttributeModifierAction::getAttribute),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("uuid")).forGetter(RemoveAttributeModifierAction::getUuid)
        ).apply(instance, (activation, multiTargets, attribute, uuid) -> new RemoveAttributeModifierAction(type, activation, multiTargets, attribute, uuid)));
    }
    
    public static RemoveAttributeModifierAction make(Object activation, Object multiTargets, DynamicCtxVar<String> attribute, DynamicCtxVar<String> uuid)
    {
        return new RemoveAttributeModifierAction(SpellActionTypes.REMOVE_ATTRIBUTE_MODIFIER.get(), activation.toString(), multiTargets.toString(), attribute, uuid);
    }
    
    protected DynamicCtxVar<String> attribute;
    protected DynamicCtxVar<String> uuid;
    
    public RemoveAttributeModifierAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public RemoveAttributeModifierAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> attribute, DynamicCtxVar<String> uuid)
    {
        super(type, activation, multiTargets);
        this.attribute = attribute;
        this.uuid = uuid;
    }
    
    public DynamicCtxVar<String> getAttribute()
    {
        return attribute;
    }
    
    public DynamicCtxVar<String> getUuid()
    {
        return uuid;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        SpellsUtil.stringToObject(ctx, attribute, ForgeRegistries.ATTRIBUTES).ifPresent(attribute ->
        {
            AttributeInstance a = target.getLivingEntity().getAttribute(attribute);
            
            if(a != null)
            {
                uuid.getValue(ctx).map(SpellsUtil::uuidFromString).ifPresent(uuid ->
                {
                    a.removePermanentModifier(uuid);
                });
            }
        });
    }
}

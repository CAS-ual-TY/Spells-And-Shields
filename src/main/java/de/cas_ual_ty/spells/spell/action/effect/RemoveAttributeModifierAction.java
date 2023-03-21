package de.cas_ual_ty.spells.spell.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellAction;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class RemoveAttributeModifierAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<RemoveAttributeModifierAction> makeCodec(SpellActionType<RemoveAttributeModifierAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                SpellAction.activationCodec(),
                AffectTypeAction.targetsCodec(),
                ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(RemoveAttributeModifierAction::getAttribute),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramInt("uuid")).forGetter(RemoveAttributeModifierAction::getUuid)
        ).apply(instance, (activation, targets, attribute, uuid) -> new RemoveAttributeModifierAction(type, activation, targets, attribute, uuid)));
    }
    
    public static RemoveAttributeModifierAction make(String activation, String targets, Attribute attribute, DynamicCtxVar<String> uuid)
    {
        return new RemoveAttributeModifierAction(SpellActionTypes.REMOVE_ATTRIBUTE_MODIFIER.get(), activation, targets, attribute, uuid);
    }
    
    protected Attribute attribute;
    protected DynamicCtxVar<String> uuid;
    
    public RemoveAttributeModifierAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public RemoveAttributeModifierAction(SpellActionType<?> type, String activation, String targets, Attribute attribute, DynamicCtxVar<String> uuid)
    {
        super(type, activation, targets);
        this.attribute = attribute;
        this.uuid = uuid;
    }
    
    public Attribute getAttribute()
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
        if(attribute != null)
        {
            AttributeInstance a = target.getLivingEntity().getAttribute(attribute);
            
            if(a != null)
            {
                this.uuid.getValue(ctx).map(UUID::fromString).ifPresent(uuid ->
                {
                    a.removeModifier(uuid);
                });
            }
        }
    }
}

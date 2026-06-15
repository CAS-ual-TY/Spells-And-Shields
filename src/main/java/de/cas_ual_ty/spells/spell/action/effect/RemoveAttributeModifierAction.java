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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class RemoveAttributeModifierAction extends AffectTypeAction<LivingEntityTarget>
{
    public static Codec<RemoveAttributeModifierAction> makeCodec(SpellActionType<RemoveAttributeModifierAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                multiTargetsCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("attribute")).forGetter(RemoveAttributeModifierAction::getAttribute),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("id")).forGetter(RemoveAttributeModifierAction::getId)
        ).apply(instance, (activation, multiTargets, attribute, id) -> new RemoveAttributeModifierAction(type, activation, multiTargets, attribute, id)));
    }

    public static RemoveAttributeModifierAction make(Object activation, Object multiTargets, DynamicCtxVar<String> attribute, DynamicCtxVar<String> id)
    {
        return new RemoveAttributeModifierAction(SpellActionTypes.REMOVE_ATTRIBUTE_MODIFIER.get(), activation.toString(), multiTargets.toString(), attribute, id);
    }

    protected DynamicCtxVar<String> attribute;
    protected DynamicCtxVar<String> id;

    public RemoveAttributeModifierAction(SpellActionType<?> type)
    {
        super(type);
    }

    public RemoveAttributeModifierAction(SpellActionType<?> type, String activation, String multiTargets, DynamicCtxVar<String> attribute, DynamicCtxVar<String> id)
    {
        super(type, activation, multiTargets);
        this.attribute = attribute;
        this.id = id;
    }

    public DynamicCtxVar<String> getAttribute()
    {
        return attribute;
    }

    public DynamicCtxVar<String> getId()
    {
        return id;
    }

    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }

    @Override
    public void affectTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget target)
    {
        SpellsUtil.stringToHolder(ctx, attribute, BuiltInRegistries.ATTRIBUTE).ifPresent(attribute ->
        {
            AttributeInstance a = target.getLivingEntity().getAttribute(attribute);

            if(a != null)
            {
                id.getValue(ctx).ifPresent(id ->
                {
                    a.removeModifier(ResourceLocation.parse(id));
                });
            }
        });
    }
}

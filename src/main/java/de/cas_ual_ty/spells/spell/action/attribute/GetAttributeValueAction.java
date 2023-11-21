package de.cas_ual_ty.spells.spell.action.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.LivingEntityTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class GetAttributeValueAction extends AffectSingleTypeAction<LivingEntityTarget>
{
    public static Codec<GetAttributeValueAction> makeCodec(SpellActionType<GetAttributeValueAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("attribute")).forGetter(GetAttributeValueAction::getAttribute),
                Codec.STRING.fieldOf(ParamNames.var("amount")).forGetter(GetAttributeValueAction::getAmount),
                Codec.STRING.fieldOf(ParamNames.var("default_amount")).forGetter(GetAttributeValueAction::getDefaultAmount)
        ).apply(instance, (activation, targets, attribute, amount, defaultAmount) -> new GetAttributeValueAction(type, activation, targets, attribute, amount, defaultAmount)));
    }
    
    public static GetAttributeValueAction make(Object activation, Object targets, DynamicCtxVar<String> attribute, String amount, String defaultAmount)
    {
        return new GetAttributeValueAction(SpellActionTypes.GET_ATTRIBUTE_VALUE.get(), activation.toString(), targets.toString(), attribute, amount, defaultAmount);
    }
    
    protected DynamicCtxVar<String> attribute;
    protected String amount;
    protected String defaultAmount;
    
    public GetAttributeValueAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public GetAttributeValueAction(SpellActionType<?> type, String activation, String targets, DynamicCtxVar<String> attribute, String amount, String defaultAmount)
    {
        super(type, activation, targets);
        this.attribute = attribute;
        this.amount = amount;
        this.defaultAmount = defaultAmount;
    }
    
    public DynamicCtxVar<String> getAttribute()
    {
        return attribute;
    }
    
    public String getAmount()
    {
        return amount;
    }
    
    public String getDefaultAmount()
    {
        return defaultAmount;
    }
    
    @Override
    public ITargetType<LivingEntityTarget> getAffectedType()
    {
        return TargetTypes.LIVING_ENTITY.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, LivingEntityTarget livingEntityTarget)
    {
        SpellsUtil.stringToObject(ctx, attribute, BuiltInRegistries.ATTRIBUTE).ifPresent(attribute ->
        {
            AttributeInstance a = livingEntityTarget.getLivingEntity().getAttribute(attribute);
            
            if(a != null)
            {
                ctx.setCtxVar(CtxVarTypes.DOUBLE.get(), amount, a.getValue());
                ctx.setCtxVar(CtxVarTypes.DOUBLE.get(), defaultAmount, a.getBaseValue());
            }
        });
    }
}

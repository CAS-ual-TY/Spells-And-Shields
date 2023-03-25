package de.cas_ual_ty.spells.spell.action.item;

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
import de.cas_ual_ty.spells.spell.target.ItemTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import net.minecraft.world.item.ItemStack;

public class ItemEqualsActivationAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<ItemEqualsActivationAction> makeCodec(SpellActionType<ItemEqualsActivationAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("to_activate")).forGetter(ItemEqualsActivationAction::getToActivate),
                ItemStack.CODEC.fieldOf("item").forGetter(ItemEqualsActivationAction::getItem),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("ignore_tag")).forGetter(ItemEqualsActivationAction::getIgnoreTag),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("minimum_count")).forGetter(ItemEqualsActivationAction::getMinimumCount),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("minimum_durability")).forGetter(ItemEqualsActivationAction::getMinimumDurability)
        ).apply(instance, (activation, singleTarget, toActivate, item, ignoreTag, minimumCount, minimumDurability) -> new ItemEqualsActivationAction(type, activation, singleTarget, toActivate, item, ignoreTag, minimumCount, minimumDurability)));
    }
    
    public static ItemEqualsActivationAction make(String activation, String singleTarget, String toActivate, ItemStack item, DynamicCtxVar<Boolean> ignoreTag, DynamicCtxVar<Integer> minimumCount, DynamicCtxVar<Integer> minimumDurability)
    {
        return new ItemEqualsActivationAction(SpellActionTypes.ITEM_EQUALS_ACTIVATION.get(), activation, singleTarget, toActivate, item, ignoreTag, minimumCount, minimumDurability);
    }
    
    protected String toActivate;
    
    protected ItemStack item;
    protected DynamicCtxVar<Boolean> ignoreTag;
    protected DynamicCtxVar<Integer> minimumCount;
    protected DynamicCtxVar<Integer> minimumDurability;
    
    public ItemEqualsActivationAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ItemEqualsActivationAction(SpellActionType<?> type, String activation, String singleTarget, String toActivate, ItemStack item, DynamicCtxVar<Boolean> ignoreTag, DynamicCtxVar<Integer> minimumCount, DynamicCtxVar<Integer> minimumDurability)
    {
        super(type, activation, singleTarget);
        this.toActivate = toActivate;
        this.item = item;
        this.ignoreTag = ignoreTag;
        this.minimumCount = minimumCount;
        this.minimumDurability = minimumDurability;
    }
    
    public String getToActivate()
    {
        return toActivate;
    }
    
    public ItemStack getItem()
    {
        return item;
    }
    
    public DynamicCtxVar<Boolean> getIgnoreTag()
    {
        return ignoreTag;
    }
    
    public DynamicCtxVar<Integer> getMinimumCount()
    {
        return minimumCount;
    }
    
    public DynamicCtxVar<Integer> getMinimumDurability()
    {
        return minimumDurability;
    }
    
    @Override
    public ITargetType<ItemTarget> getAffectedType()
    {
        return TargetTypes.ITEM.get();
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, ItemTarget itemTarget)
    {
        minimumCount.getValue(ctx).ifPresent(minimumCount ->
        {
            minimumDurability.getValue(ctx).ifPresent(minimumDurability ->
            {
                ignoreTag.getValue(ctx).ifPresent(ignoreTag ->
                {
                    if(!itemTarget.getItem().is(this.item.getItem()))
                    {
                        return;
                    }
                    
                    if(minimumCount >= 0 && itemTarget.getItem().getCount() < minimumCount)
                    {
                        return;
                    }
                    
                    if(minimumDurability >= 0 && itemTarget.getItem().getMaxDamage() - itemTarget.getItem().getDamageValue() < minimumDurability)
                    {
                        return;
                    }
                    
                    if(!ignoreTag && !ItemStack.tagMatches(item, itemTarget.getItem()))
                    {
                        return;
                    }
                    
                    ctx.activate(toActivate);
                });
            });
        });
    }
}

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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemTagEqualsActivationAction extends AffectSingleTypeAction<ItemTarget>
{
    public static Codec<ItemTagEqualsActivationAction> makeCodec(SpellActionType<ItemTagEqualsActivationAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                singleTargetCodec(),
                Codec.STRING.fieldOf(ParamNames.interactedActivation("to_activate")).forGetter(ItemTagEqualsActivationAction::getToActivate),
                TagKey.codec(ForgeRegistries.ITEMS.getRegistryKey()).fieldOf("item_tag").forGetter(ItemTagEqualsActivationAction::getItemTag),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("minimum_count")).forGetter(ItemTagEqualsActivationAction::getMinimumCount),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("minimum_durability")).forGetter(ItemTagEqualsActivationAction::getMinimumDurability)
        ).apply(instance, (activation, singleTarget, toActivate, itemTag, minimumCount, minimumDurability) -> new ItemTagEqualsActivationAction(type, activation, singleTarget, toActivate, itemTag, minimumCount, minimumDurability)));
    }
    
    public static ItemTagEqualsActivationAction make(String activation, String singleTarget, String toActivate, TagKey<Item> itemTag, DynamicCtxVar<Integer> minimumCount, DynamicCtxVar<Integer> minimumDurability)
    {
        return new ItemTagEqualsActivationAction(SpellActionTypes.ITEM_TAG_EQUALS_ACTIVATION.get(), activation, singleTarget, toActivate, itemTag, minimumCount, minimumDurability);
    }
    
    protected String toActivate;
    
    protected TagKey<Item> itemTag;
    protected DynamicCtxVar<Integer> minimumCount;
    protected DynamicCtxVar<Integer> minimumDurability;
    
    public ItemTagEqualsActivationAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ItemTagEqualsActivationAction(SpellActionType<?> type, String activation, String singleTarget, String toActivate, TagKey<Item> itemTag, DynamicCtxVar<Integer> minimumCount, DynamicCtxVar<Integer> minimumDurability)
    {
        super(type, activation, singleTarget);
        this.toActivate = toActivate;
        this.itemTag = itemTag;
        this.minimumCount = minimumCount;
        this.minimumDurability = minimumDurability;
    }
    
    public String getToActivate()
    {
        return toActivate;
    }
    
    public TagKey<Item> getItemTag()
    {
        return itemTag;
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
                if(!itemTarget.getItem().is(this.itemTag))
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
                
                ctx.activate(toActivate);
            });
        });
    }
}

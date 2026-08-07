package de.cas_ual_ty.spells.spell.action.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.SpellsUtil;
import de.cas_ual_ty.spells.registers.CtxVarTypes;
import de.cas_ual_ty.spells.registers.SpellActionTypes;
import de.cas_ual_ty.spells.registers.TargetTypes;
import de.cas_ual_ty.spells.spell.action.ParamNames;
import de.cas_ual_ty.spells.spell.action.SpellActionType;
import de.cas_ual_ty.spells.spell.action.base.AffectSingleTypeAction;
import de.cas_ual_ty.spells.spell.context.SpellContext;
import de.cas_ual_ty.spells.spell.context.TargetGroup;
import de.cas_ual_ty.spells.spell.target.ITargetType;
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TryConsumePlayerItemsAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<TryConsumePlayerItemsAction> makeCodec(SpellActionType<TryConsumePlayerItemsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("item")).forGetter(TryConsumePlayerItemsAction::getItem),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(TryConsumePlayerItemsAction::getAmount),
                CtxVarTypes.TAG.get().optionalRefCodec(ParamNames.paramCompoundTag("tag")).forGetter(TryConsumePlayerItemsAction::getTag),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("must_be_in_hand")).forGetter(TryConsumePlayerItemsAction::getMustBeInHand)
        ).apply(instance, (activation, source, item, amount, tag, mustBeInHand) -> new TryConsumePlayerItemsAction(type, activation, source, item, amount, tag, mustBeInHand)));
    }
    
    public static TryConsumePlayerItemsAction make(Object activation, Object source, DynamicCtxVar<String> item, DynamicCtxVar<Integer> amount, @Nullable DynamicCtxVar<CompoundTag> tag, DynamicCtxVar<Boolean> mustBeInHand)
    {
        return new TryConsumePlayerItemsAction(SpellActionTypes.TRY_CONSUME_PLAYER_ITEMS.get(), activation.toString(), source.toString(), item, amount, tag, mustBeInHand);
    }
    
    protected DynamicCtxVar<String> item;
    protected DynamicCtxVar<Integer> amount;
    protected DynamicCtxVar<CompoundTag> tag;
    protected DynamicCtxVar<Boolean> mustBeInHand;
    
    public TryConsumePlayerItemsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public TryConsumePlayerItemsAction(SpellActionType<?> type, String activation, String source, DynamicCtxVar<String> item, DynamicCtxVar<Integer> amount, DynamicCtxVar<CompoundTag> tag, DynamicCtxVar<Boolean> mustBeInHand)
    {
        super(type, activation, source);
        this.item = item;
        this.amount = amount;
        this.tag = tag;
        this.mustBeInHand = mustBeInHand;
    }
    
    public DynamicCtxVar<String> getItem()
    {
        return item;
    }
    
    public DynamicCtxVar<Integer> getAmount()
    {
        return amount;
    }
    
    public DynamicCtxVar<CompoundTag> getTag()
    {
        return tag;
    }
    
    public DynamicCtxVar<Boolean> getMustBeInHand()
    {
        return mustBeInHand;
    }
    
    @Override
    public void affectSingleTarget(SpellContext ctx, TargetGroup group, PlayerTarget playerTarget)
    {
        Player player = playerTarget.getPlayer();
        
        if(player.isCreative())
        {
            return;
        }
        
        SpellsUtil.stringToObject(ctx, item, BuiltInRegistries.ITEM).ifPresent(item ->
        {
            amount.getValue(ctx).ifPresent(amount ->
            {
                mustBeInHand.getValue(ctx).ifPresent(mustBeInHand ->
                {
                    List<ItemStack> items;
                    if(mustBeInHand)
                    {
                        items = List.of(player.getMainHandItem(), player.getOffhandItem());
                    }
                    else
                    {
                        items = new ArrayList<>(player.getInventory().items.size());
                        items.addAll(player.getInventory().items);
                        items.addAll(player.getInventory().offhand);
                    }
                    
                    CompoundTag tag = this.tag.getValue(ctx).orElse(null);

                    int available = 0;

                    for(ItemStack i : items)
                    {
                        if(i.getItem() == item && (tag == null || tag.isEmpty() || (i.get(DataComponents.CUSTOM_DATA) != null && tag.equals(i.get(DataComponents.CUSTOM_DATA).copyTag()))))
                        {
                            available += i.getCount();

                            if(available >= amount)
                            {
                                break;
                            }
                        }
                    }

                    if(available < amount)
                    {
                        ctx.deactivate(activation);
                        return;
                    }

                    int count = amount;

                    for(ItemStack i : items)
                    {
                        if(i.getItem() == item && (tag == null || tag.isEmpty() || (i.get(DataComponents.CUSTOM_DATA) != null && tag.equals(i.get(DataComponents.CUSTOM_DATA).copyTag()))))
                        {
                            int c = Math.min(count, i.getCount());
                            i.shrink(c);
                            count -= c;
                        }

                        if(count <= 0)
                        {
                            break;
                        }
                    }
                });
            });
        });
    }
    
    @Override
    public ITargetType<PlayerTarget> getAffectedType()
    {
        return TargetTypes.PLAYER.get();
    }
}

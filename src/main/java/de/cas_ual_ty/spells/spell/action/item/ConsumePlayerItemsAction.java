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
import de.cas_ual_ty.spells.spell.target.PlayerTarget;
import de.cas_ual_ty.spells.spell.variable.DynamicCtxVar;
import de.cas_ual_ty.spells.util.ParamNames;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class ConsumePlayerItemsAction extends AffectSingleTypeAction<PlayerTarget>
{
    public static Codec<ConsumePlayerItemsAction> makeCodec(SpellActionType<ConsumePlayerItemsAction> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                activationCodec(),
                sourceCodec(),
                CtxVarTypes.STRING.get().refCodec().fieldOf(ParamNames.paramString("item")).forGetter(ConsumePlayerItemsAction::getItem),
                CtxVarTypes.INT.get().refCodec().fieldOf(ParamNames.paramInt("amount")).forGetter(ConsumePlayerItemsAction::getAmount),
                CtxVarTypes.COMPOUND_TAG.get().refCodec().fieldOf(ParamNames.paramCompoundTag("tag")).forGetter(ConsumePlayerItemsAction::getTag),
                CtxVarTypes.BOOLEAN.get().refCodec().fieldOf(ParamNames.paramBoolean("must_be_in_hand")).forGetter(ConsumePlayerItemsAction::getMustBeInHand)
        ).apply(instance, (activation, source, item, amount, tag, mustBeInHand) -> new ConsumePlayerItemsAction(type, activation, source, item, amount, tag, mustBeInHand)));
    }
    
    public static ConsumePlayerItemsAction make(String activation, String source, DynamicCtxVar<String> item, DynamicCtxVar<Integer> amount, DynamicCtxVar<CompoundTag> tag, DynamicCtxVar<Boolean> mustBeInHand)
    {
        return new ConsumePlayerItemsAction(SpellActionTypes.CONSUME_PLAYER_ITEMS.get(), activation, source, item, amount, tag, mustBeInHand);
    }
    
    protected DynamicCtxVar<String> item;
    protected DynamicCtxVar<Integer> amount;
    protected DynamicCtxVar<CompoundTag> tag;
    protected DynamicCtxVar<Boolean> mustBeInHand;
    
    public ConsumePlayerItemsAction(SpellActionType<?> type)
    {
        super(type);
    }
    
    public ConsumePlayerItemsAction(SpellActionType<?> type, String activation, String source, DynamicCtxVar<String> item, DynamicCtxVar<Integer> amount, DynamicCtxVar<CompoundTag> tag, DynamicCtxVar<Boolean> mustBeInHand)
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
        
        SpellsUtil.stringToObject(ctx, item, ForgeRegistries.ITEMS).ifPresent(item ->
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
                    
                    int count = amount;
                    
                    for(ItemStack i : items)
                    {
                        if(i.getItem() == item && (tag == null || (i.getTag() != null && tag.equals(i.getTag()))))
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

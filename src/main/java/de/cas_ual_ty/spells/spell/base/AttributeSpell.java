package de.cas_ual_ty.spells.spell.base;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.SpellHolder;
import de.cas_ual_ty.spells.util.SpellsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class AttributeSpell extends PassiveSpell implements IEquipSpell, IConfigurableSpell
{
    public final Supplier<Attribute> defaultAttribute;
    public final AttributeModifier defaultAttributeModifier;
    
    protected Attribute attribute;
    protected AttributeModifier attributeModifier;
    
    public AttributeSpell(Supplier<Attribute> attribute, Supplier<String> name, double amount, AttributeModifier.Operation operation)
    {
        this.defaultAttribute = attribute;
        this.defaultAttributeModifier = new AttributeModifier(UUID.nameUUIDFromBytes("".getBytes(StandardCharsets.UTF_8)), name, amount, operation);
    }
    
    public AttributeSpell(Supplier<Attribute> attribute, double amount, AttributeModifier.Operation operation)
    {
        this(attribute, () -> Util.makeDescriptionId("effect", ForgeRegistries.ATTRIBUTES.getKey(attribute.get())), amount, operation);
    }
    
    @Override
    public void onEquip(SpellHolder spellHolder, int slot)
    {
        if(spellHolder.getPlayer().level.isClientSide)
        {
            return;
        }
        
        Player player = spellHolder.getPlayer();
        AttributeInstance ai = player.getAttribute(this.attribute);
        
        if(ai != null)
        {
            UUID uuid = SpellsUtil.generateUUIDForSlotAttribute(attribute, slot);
            AttributeModifier modifier = new AttributeModifier(uuid, this.attributeModifier.getName(), this.attributeModifier.getAmount(), this.attributeModifier.getOperation());
            
            if(ai.hasModifier(modifier))
            {
                ai.removeModifier(uuid);
            }
            
            ai.addPermanentModifier(modifier);
        }
    }
    
    @Override
    public void onUnequip(SpellHolder spellHolder, int slot)
    {
        if(spellHolder.getPlayer().level.isClientSide)
        {
            return;
        }
        
        Player player = spellHolder.getPlayer();
        AttributeInstance ai = player.getAttribute(this.attribute);
        
        if(ai != null)
        {
            UUID uuid = SpellsUtil.generateUUIDForSlotAttribute(attribute, slot);
            ai.removePermanentModifier(uuid);
        }
    }
    
    @Override
    public void addSpellDesc(List<Component> list)
    {
        list.add(Component.translatable(getDescKey()));
        
        if(attribute != null && attributeModifier != null)
        {
            list.add(Component.empty());
            list.add(whenAppliedComponent());
            
            addTooltip(list, attribute, attributeModifier);
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        return new JsonObject();
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        this.attribute = this.defaultAttribute.get();
        this.attributeModifier = this.defaultAttributeModifier;
    }
    
    @Override
    public void applyDefaultConfig()
    {
        this.attribute = this.defaultAttribute.get();
        this.attributeModifier = this.defaultAttributeModifier;
    }
    
    public static void addTooltip(List<Component> list, Attribute attribute, AttributeModifier attributeModifier)
    {
        double amount = attributeModifier.getAmount();
        double renderedAmount;
        
        if(attributeModifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributeModifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL)
        {
            renderedAmount = attributeModifier.getAmount();
        }
        else
        {
            renderedAmount = attributeModifier.getAmount() * 100D;
        }
        
        if(amount > 0D)
        {
            list.add((Component.translatable("attribute.modifier.plus." + attributeModifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(renderedAmount), Component.translatable(attribute.getDescriptionId()))).withStyle(ChatFormatting.BLUE));
        }
        else if(amount < 0D)
        {
            renderedAmount *= -1D;
            list.add((Component.translatable("attribute.modifier.take." + attributeModifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(renderedAmount), Component.translatable(attribute.getDescriptionId()))).withStyle(ChatFormatting.RED));
        }
    }
}

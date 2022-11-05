package de.cas_ual_ty.spells.spell.base;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BaseIngredientsSpell extends MultiIngredientSpell
{
    public final List<ItemStack> defaultHandIngredients;
    public final List<ItemStack> defaultInventoryIngredients;
    
    protected List<ItemStack> handIngredients;
    protected List<ItemStack> inventoryIngredients;
    
    public BaseIngredientsSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        super(manaCost);
        this.defaultHandIngredients = ImmutableList.copyOf(handIngredients);
        this.defaultInventoryIngredients = ImmutableList.copyOf(inventoryIngredients);
    }
    
    public BaseIngredientsSpell(float manaCost, ItemStack handIngredient)
    {
        super(manaCost);
        this.defaultHandIngredients = ImmutableList.of(handIngredient);
        this.defaultInventoryIngredients = ImmutableList.of();
    }
    
    public BaseIngredientsSpell(float manaCost)
    {
        super(manaCost);
        this.defaultHandIngredients = ImmutableList.of();
        this.defaultInventoryIngredients = ImmutableList.of();
    }
    
    @Override
    public List<ItemStack> getRequiredHandIngredients()
    {
        return handIngredients;
    }
    
    @Override
    public List<ItemStack> getRequiredInventoryIngredients()
    {
        return inventoryIngredients;
    }
    
    @Override
    public void consumeItemStacks(ManaHolder manaHolder, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients)
    {
        for(ItemStack required : getRequiredHandIngredients())
        {
            handIngredients.stream().filter(itemStack -> itemStack.getItem() == required.getItem()).forEach(itemStack -> itemStack.shrink(required.getCount()));
        }
        
        for(ItemStack required : getRequiredInventoryIngredients())
        {
            inventoryIngredients.stream().filter(itemStack -> itemStack.getItem() == required.getItem()).forEach(itemStack -> itemStack.shrink(required.getCount()));
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        
        JsonArray handIngredients = new JsonArray();
        JsonArray inventoryIngredients = new JsonArray();
        
        for(ItemStack ingredient : this.defaultHandIngredients)
        {
            JsonObject o = new JsonObject();
            SpellsFileUtil.jsonItemStack(o, ingredient);
            handIngredients.add(o);
        }
        
        for(ItemStack ingredient : this.defaultInventoryIngredients)
        {
            JsonObject o = new JsonObject();
            SpellsFileUtil.jsonItemStack(o, ingredient);
            inventoryIngredients.add(o);
        }
        
        json.add("hand_ingredients", handIngredients);
        json.add("inventory_ingredients", inventoryIngredients);
        
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        
        JsonArray handIngredients = SpellsFileUtil.jsonArray(json, "hand_ingredients");
        JsonArray inventoryIngredients = SpellsFileUtil.jsonArray(json, "inventory_ingredients");
        
        if(handIngredients.size() > 2)
        {
            throw new IllegalStateException();
        }
        
        // -1 as one item is in main hand
        if(inventoryIngredients.size() > Inventory.INVENTORY_SIZE - 1)
        {
            throw new IllegalStateException();
        }
        
        this.handIngredients = new ArrayList<>(handIngredients.size());
        this.inventoryIngredients = new ArrayList<>(inventoryIngredients.size());
        
        for(JsonElement e : handIngredients)
        {
            if(!e.isJsonObject())
            {
                throw new IllegalStateException();
            }
            
            JsonObject o = e.getAsJsonObject();
            ItemStack ingredient = SpellsFileUtil.jsonItemStack(o);
            this.handIngredients.add(ingredient);
        }
        
        for(JsonElement e : inventoryIngredients)
        {
            if(!e.isJsonObject())
            {
                throw new IllegalStateException();
            }
            
            JsonObject o = e.getAsJsonObject();
            ItemStack ingredient = SpellsFileUtil.jsonItemStack(o);
            this.inventoryIngredients.add(ingredient);
        }
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        this.handIngredients = ImmutableList.copyOf(defaultHandIngredients);
        this.inventoryIngredients = ImmutableList.copyOf(defaultInventoryIngredients);
    }
}

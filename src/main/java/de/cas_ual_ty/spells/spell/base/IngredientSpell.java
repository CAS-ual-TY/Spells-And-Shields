package de.cas_ual_ty.spells.spell.base;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.SpellsFileUtil;
import de.cas_ual_ty.spells.capability.ManaHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class IngredientSpell extends Spell
{
    public final ItemStack defaultIngredient;
    
    protected ItemStack ingredient;
    
    public IngredientSpell(float manaCost, ItemStack ingredient)
    {
        super(manaCost);
        this.defaultIngredient = ingredient;
    }
    
    public ItemStack getIngredient()
    {
        return this.ingredient;
    }
    
    public abstract void perform(ManaHolder manaHolder, ItemStack itemStack);
    
    @Override
    public final void perform(ManaHolder manaHolder)
    {
    }
    
    @Override
    public boolean activate(ManaHolder manaHolder)
    {
        if(this.canActivate(manaHolder))
        {
            ItemStack itemStack = this.hasIngredient(manaHolder);
            
            boolean creative = false;
            
            if((manaHolder.getPlayer() instanceof Player player && (creative = player.isCreative())) || this.ingredient.isEmpty() || itemStack.getItem() == this.ingredient.getItem())
            {
                this.perform(manaHolder, itemStack);
                
                if(!creative)
                {
                    this.burnMana(manaHolder);
                    this.consumeItemStack(manaHolder, itemStack);
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    public ItemStack hasIngredient(ManaHolder manaHolder)
    {
        if(this.ingredient.isEmpty() && this.ingredient.getCount() == 0)
        {
            return ItemStack.EMPTY;
        }
        
        for(ItemStack itemStack : manaHolder.getPlayer().getAllSlots())
        {
            if(this.isItemStackIngredient(manaHolder, itemStack))
            {
                return itemStack;
            }
        }
        
        return ItemStack.EMPTY;
    }
    
    public boolean isItemStackIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return itemStack.getItem() == this.ingredient.getItem() && itemStack.getCount() >= this.ingredient.getCount();
    }
    
    public void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack)
    {
        itemStack.setCount(itemStack.getCount() - this.ingredient.getCount());
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        
        if(this.defaultIngredient.isEmpty())
        {
            json.addProperty("ingredient", "null");
            json.addProperty("count", 0);
        }
        else
        {
            json.addProperty("ingredient", this.defaultIngredient.getItem().getRegistryName().toString());
            json.addProperty("count", this.defaultIngredient.getCount());
        }
        
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        
        Item ingredient = SpellsFileUtil.jsonItem(json, "ingredient", true);
        int count = SpellsFileUtil.jsonInt(json, "count");
        
        this.ingredient = ingredient == null || count == 0 ? ItemStack.EMPTY : new ItemStack(ingredient, count);
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        ingredient = defaultIngredient;
    }
}

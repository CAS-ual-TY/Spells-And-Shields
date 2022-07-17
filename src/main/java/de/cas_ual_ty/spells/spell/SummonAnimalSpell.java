package de.cas_ual_ty.spells.spell;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.HandIngredientSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class SummonAnimalSpell extends HandIngredientSpell
{
    public final Map<Item, Ingredient> defaultIngredientMap;
    
    protected Map<Item, Ingredient> ingredientMap;
    
    public SummonAnimalSpell(float manaCost, Map<Item, Ingredient> ingredientMap)
    {
        super(manaCost);
        this.defaultIngredientMap = ImmutableMap.copyOf(ingredientMap);
        this.ingredientMap = new HashMap<>();
    }
    
    public SummonAnimalSpell(float manaCost)
    {
        super(manaCost);
        
        this.ingredientMap = new HashMap<>();
        this.addIngredient(Items.BEEF, 8, EntityType.COW);
        this.addIngredient(Items.CHICKEN, 8, EntityType.CHICKEN);
        this.addIngredient(Items.MUTTON, 8, EntityType.SHEEP);
        this.addIngredient(Items.PORKCHOP, 8, EntityType.PIG);
        this.defaultIngredientMap = ImmutableMap.copyOf(ingredientMap);
        
        this.ingredientMap.clear();
    }
    
    public void addIngredient(ItemStack ingredient, EntityType<?> entity)
    {
        this.ingredientMap.put(ingredient.getItem(), new Ingredient(ingredient, entity));
    }
    
    public void addIngredient(Item ingredient, int count, EntityType<?> entity)
    {
        this.ingredientMap.put(ingredient, new Ingredient(new ItemStack(ingredient, count), entity));
    }
    
    @Override
    public void perform(ManaHolder manaHolder, ItemStack itemStack)
    {
        Level level = manaHolder.getPlayer().level;
        
        Ingredient ingredient = ingredientMap.getOrDefault(itemStack.getItem(), null);
        
        if(ingredient == null || ingredient.entity() == null)
        {
            // TODO debug log: error
            return;
        }
        
        EntityType<?> entityType = ingredient.entity();
        
        Entity entity = entityType.create(manaHolder.getPlayer().level);
        
        if(entity instanceof LivingEntity livingEntity)
        {
            Vec3 position = manaHolder.getPlayer().position();
            
            livingEntity.moveTo(position.x, position.y, position.z, 0.0F, manaHolder.getPlayer().getYRot() - 180F);
            
            if(livingEntity instanceof AgeableMob ageableMob)
            {
                ageableMob.setBaby(true);
            }
            
            level.addFreshEntity(livingEntity);
            
            if(level instanceof ServerLevel serverLevel)
            {
                RandomSource random = livingEntity.getRandom();
                final int count = 3;
                final double spread = 0.4D;
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, position.x, position.y, position.z, count, random.nextGaussian() * spread, random.nextGaussian() * spread, random.nextGaussian() * spread, 0.0D);
            }
        }
    }
    
    @Override
    public boolean checkHandIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return itemStack.getCount() >= this.getRequiredCount(itemStack) && ingredientMap.containsKey(itemStack.getItem());
    }
    
    @Override
    public boolean checkInventoryIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return false;
    }
    
    public int getRequiredCount(ItemStack itemStack)
    {
        return ingredientMap.get(itemStack.getItem()).count();
    }
    
    @Override
    public void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack)
    {
        itemStack.shrink(ingredientMap.get(itemStack.getItem()).count());
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        
        JsonArray ingredients = new JsonArray();
        defaultIngredientMap.forEach((item, ingredient) ->
        {
            JsonObject o = new JsonObject();
            SpellsFileUtil.jsonItemStack(o, ingredient.ingredient(), "item", "count");
            o.addProperty("entity", ForgeRegistries.ENTITY_TYPES.getKey(ingredient.entity()).toString());
            ingredients.add(o);
        });
        json.add("ingredients", ingredients);
        
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        
        this.ingredientMap.clear();
        
        JsonArray ingredients = SpellsFileUtil.jsonArray(json, "ingredients");
        
        for(JsonElement e : ingredients)
        {
            if(!e.isJsonObject())
            {
                throw new IllegalStateException();
            }
            
            JsonObject ingredient = e.getAsJsonObject();
            
            ItemStack itemStack = SpellsFileUtil.jsonItemStack(ingredient, "item", "count");
            EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(SpellsFileUtil.jsonString(ingredient, "entity")));
            
            if(itemStack.isEmpty())
            {
                throw new IllegalStateException();
            }
            
            if(entity == null)
            {
                throw new IllegalStateException(new NullPointerException());
            }
            
            this.addIngredient(itemStack, entity);
        }
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        this.ingredientMap.clear();
        this.ingredientMap.putAll(this.defaultIngredientMap);
    }
    
    private static record Ingredient(ItemStack ingredient, EntityType<?> entity)
    {
        public Item item()
        {
            return ingredient().getItem();
        }
        
        public int count()
        {
            return ingredient().getCount();
        }
    }
}

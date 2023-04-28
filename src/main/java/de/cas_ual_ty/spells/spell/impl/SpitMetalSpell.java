package de.cas_ual_ty.spells.spell.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.IProjectileSpell;
import de.cas_ual_ty.spells.spell.base.HandIngredientSpell;
import de.cas_ual_ty.spells.spell.base.SpellProjectile;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.EntityHitResult;

import java.util.HashMap;
import java.util.Map;

public class SpitMetalSpell extends HandIngredientSpell implements IProjectileSpell
{
    public final float defaultBaseDamage;
    public final Map<Item, Float> defaultIngredientMap;
    
    protected float baseDamage;
    protected Map<Item, Float> ingredientMap;
    
    public SpitMetalSpell(float manaCost, float baseDamage, Map<Item, Float> ingredientMap)
    {
        super(manaCost);
        defaultBaseDamage = baseDamage;
        defaultIngredientMap = ingredientMap;
        
        this.ingredientMap = new HashMap<>();
    }
    
    public SpitMetalSpell()
    {
        super(4F);
        defaultBaseDamage = 8F;
        
        ingredientMap = new HashMap<>();
        ingredientMap.put(Items.IRON_NUGGET, Tiers.IRON.getAttackDamageBonus());
        ingredientMap.put(Items.GOLD_NUGGET, Tiers.GOLD.getAttackDamageBonus());
        defaultIngredientMap = ImmutableMap.copyOf(ingredientMap);
        ingredientMap.clear();
    }
    
    @Override
    public void perform(ManaHolder manaHolder, ItemStack itemStack)
    {
        float damage = ingredientMap.getOrDefault(itemStack.getItem(), 0F);
        shootStraight(manaHolder, (projectile, level) ->
        {
            level.playSound(null, manaHolder.getPlayer(), SoundEvents.LLAMA_SPIT, SoundSource.PLAYERS, 1F, 1F);
            projectile.getSpellDataTag().putFloat("materialDamage", damage + baseDamage);
        });
    }
    
    @Override
    public void projectileHitEntity(SpellProjectile entity, EntityHitResult entityHitResult)
    {
        Entity hit = entityHitResult.getEntity();
        if(hit instanceof LivingEntity livingEntity)
        {
            livingEntity.hurt(livingEntity.damageSources().indirectMagic(entity, entity.getOwner()), entity.getSpellDataTag().getFloat("materialDamage"));
        }
        IProjectileSpell.super.projectileHitEntity(entity, entityHitResult);
    }
    
    @Override
    public boolean checkHandIngredient(ManaHolder manaHolder, ItemStack itemStack)
    {
        return ingredientMap.containsKey(itemStack.getItem());
    }
    
    @Override
    public void consumeItemStack(ManaHolder manaHolder, ItemStack itemStack)
    {
        itemStack.shrink(1);
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("baseDamage", defaultBaseDamage);
        
        JsonArray materials = new JsonArray();
        defaultIngredientMap.forEach((item, damage) ->
        {
            JsonObject material = new JsonObject();
            SpellsFileUtil.jsonItem(material, "item", item);
            material.addProperty("bonusDamage", damage);
            materials.add(material);
        });
        json.add("materials", materials);
        
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        baseDamage = SpellsFileUtil.jsonFloat(json, "baseDamage");
        
        JsonArray materials = SpellsFileUtil.jsonArray(json, "materials");
        
        for(JsonElement e : materials)
        {
            if(!e.isJsonObject())
            {
                throw new IllegalStateException();
            }
            
            JsonObject material = e.getAsJsonObject();
            
            Item item = SpellsFileUtil.jsonItem(material, "item");
            float damage = SpellsFileUtil.jsonFloat(material, "bonusDamage");
            
            if(item == null)
            {
                throw new IllegalStateException();
            }
            
            this.ingredientMap.put(item, damage);
        }
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        baseDamage = defaultBaseDamage;
        ingredientMap.putAll(defaultIngredientMap);
    }
}

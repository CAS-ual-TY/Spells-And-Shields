package de.cas_ual_ty.spells.spell.impl;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.ManaHolder;
import de.cas_ual_ty.spells.spell.base.BaseIngredientsSpell;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GrowthSpell extends BaseIngredientsSpell
{
    public final int defaultRange;
    protected int range;
    
    public GrowthSpell(float manaCost, List<ItemStack> handIngredients, List<ItemStack> inventoryIngredients, int range)
    {
        super(manaCost, handIngredients, inventoryIngredients);
        defaultRange = range;
    }
    
    public GrowthSpell(float manaCost, ItemStack handIngredient, int range)
    {
        super(manaCost, handIngredient);
        defaultRange = range;
    }
    
    public GrowthSpell(float manaCost, int range)
    {
        super(manaCost);
        defaultRange = range;
    }
    
    public GrowthSpell()
    {
        this(4F, new ItemStack(Items.BONE_MEAL), 3);
    }
    
    @Override
    public void perform(ManaHolder manaHolder)
    {
        if(manaHolder.getPlayer() instanceof Player player && player.level instanceof ServerLevel level)
        {
            BlockPos pos0 = manaHolder.getPlayer().getOnPos();
            
            for(int x = -range; x <= range; x++)
            {
                for(int z = -range; z <= range; z++)
                {
                    for(int y = 0; y <= 2; y++)
                    {
                        BlockPos pos = pos0.offset(x, y, z);
                        Vec3 pos1 = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                        
                        BoneMealItem.applyBonemeal(ItemStack.EMPTY, player.level, pos, player);
                        level.sendParticles(ParticleTypes.POOF, pos1.x, pos1.y, pos1.z, 1, 0, 0, 0, 0.0D);
                    }
                }
            }
            
            level.playSound(null, manaHolder.getPlayer(), SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1F, 1F);
        }
    }
    
    @Override
    public JsonObject makeDefaultConfig()
    {
        JsonObject json = super.makeDefaultConfig();
        json.addProperty("range", defaultRange);
        return json;
    }
    
    @Override
    public void readFromConfig(JsonObject json)
    {
        super.readFromConfig(json);
        range = SpellsFileUtil.jsonInt(json, "range");
    }
    
    @Override
    public void applyDefaultConfig()
    {
        super.applyDefaultConfig();
        range = defaultRange;
    }
}

package de.cas_ual_ty.spells.requirement;

import com.google.gson.JsonObject;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import de.cas_ual_ty.spells.util.SpellsFileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

public class BookshelvesRequirement extends Requirement
{
    protected int bookshelves;
    
    public BookshelvesRequirement(IRequirementType<?> type)
    {
        super(type);
    }
    
    public BookshelvesRequirement(IRequirementType<?> type, int bookshelves)
    {
        super(type);
        this.bookshelves = Mth.clamp(bookshelves, 0, 32);
    }
    
    @Override
    public boolean passes(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return access.evaluate(BookshelvesRequirement::getSurroundingEnchantingPower).orElse(0) >= this.bookshelves;
    }
    
    @Override
    public MutableComponent makeDescription(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        int amount = access.evaluate(BookshelvesRequirement::getSurroundingEnchantingPower).orElse(0);
        return Component.translatable(descriptionId, amount, bookshelves);
    }
    
    @Override
    public void writeToJson(JsonObject json)
    {
        json.addProperty("bookshelves", this.bookshelves);
    }
    
    @Override
    public void readFromJson(JsonObject json)
    {
        this.bookshelves = SpellsFileUtil.jsonInt(json, "bookshelves");
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeInt(this.bookshelves);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        this.bookshelves = buf.readInt();
    }
    
    public static float getEnchantingPower(Level level, BlockPos pos)
    {
        return level.getBlockState(pos).getEnchantPowerBonus(level, pos);
    }
    
    public static int getSurroundingEnchantingPower(Level level, BlockPos blockPos)
    {
        int sum = 0;
        
        for(int z = -1; z <= 1; ++z)
        {
            for(int x = -1; x <= 1; ++x)
            {
                if((z != 0 || x != 0) && level.isEmptyBlock(blockPos.offset(x, 0, z)) && level.isEmptyBlock(blockPos.offset(x, 1, z)))
                {
                    sum += getEnchantingPower(level, blockPos.offset(x * 2, 0, z * 2));
                    sum += getEnchantingPower(level, blockPos.offset(x * 2, 1, z * 2));
                    
                    if(x != 0 && z != 0)
                    {
                        sum += getEnchantingPower(level, blockPos.offset(x * 2, 0, z));
                        sum += getEnchantingPower(level, blockPos.offset(x * 2, 1, z));
                        sum += getEnchantingPower(level, blockPos.offset(x, 0, z * 2));
                        sum += getEnchantingPower(level, blockPos.offset(x, 1, z * 2));
                    }
                }
            }
        }
        
        return sum;
    }
}

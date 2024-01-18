package de.cas_ual_ty.spells.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cas_ual_ty.spells.capability.SpellProgressionHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

import java.util.List;

public class BookshelvesRequirement extends Requirement
{
    public static Codec<BookshelvesRequirement> makeCodec(RequirementType<BookshelvesRequirement> type)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("bookshelves").forGetter(BookshelvesRequirement::getBookshelves)
        ).apply(instance, (bookshelves) -> new BookshelvesRequirement(type, bookshelves)));
    }
    
    protected int bookshelves;
    
    public BookshelvesRequirement(RequirementType<?> type)
    {
        super(type);
    }
    
    public BookshelvesRequirement(RequirementType<?> type, int bookshelves)
    {
        this(type);
        this.bookshelves = Mth.clamp(bookshelves, 0, 32);
    }
    
    public int getBookshelves()
    {
        return bookshelves;
    }
    
    @Override
    protected boolean doesPlayerPass(SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        return access.evaluate(BookshelvesRequirement::getSurroundingEnchantingPower).orElse(0) >= bookshelves;
    }
    
    @Override
    public void makeDescription(List<Component> tooltip, SpellProgressionHolder spellProgressionHolder, ContainerLevelAccess access)
    {
        int amount = access.evaluate(BookshelvesRequirement::getSurroundingEnchantingPower).orElse(0);
        tooltip.add(formatComponent(spellProgressionHolder, access, Component.translatable(descriptionId, amount, bookshelves)));
    }
    
    @Override
    public void writeToBuf(FriendlyByteBuf buf)
    {
        buf.writeInt(bookshelves);
    }
    
    @Override
    public void readFromBuf(FriendlyByteBuf buf)
    {
        bookshelves = buf.readInt();
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

package de.cas_ual_ty.spells.util;

import de.cas_ual_ty.spells.SpellsAndShields;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class SpellsFileUtil
{
    private static Path configDir = null;
    
    public static Path getOrCreateConfigDir()
    {
        return configDir != null ? configDir : (configDir = FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(SpellsAndShields.MOD_ID), SpellsAndShields.MOD_ID));
    }
    
    public static Path getOrCreateSubConfigDir(String name)
    {
        return FileUtils.getOrCreateDirectory(getOrCreateConfigDir().resolve(name), SpellsAndShields.MOD_ID + "/" + name);
    }
    
    public static boolean doesSubConfigDirExist(String name)
    {
        return Files.isDirectory(getOrCreateConfigDir().resolve(name));
    }
}

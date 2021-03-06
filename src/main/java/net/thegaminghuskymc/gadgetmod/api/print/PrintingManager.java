package net.thegaminghuskymc.gadgetmod.api.print;

import com.google.common.collect.HashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.thegaminghuskymc.gadgetmod.HuskyGadgetMod;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class PrintingManager
{
    private static HashBiMap<String, Class<? extends IPrint>> registeredPrints = HashBiMap.create();

    @SideOnly(Side.CLIENT)
    private static Map<String, IPrint.Renderer> registeredRenders;

    public static void registerPrint(ResourceLocation identifier, Class<? extends IPrint> classPrint)
    {
        try
        {
            classPrint.getConstructor().newInstance();
            if(HuskyGadgetMod.proxy.registerPrint(identifier, classPrint))
            {
                HuskyGadgetMod.getLogger().info("Registering print '" + classPrint.getName() + "'");
                registeredPrints.put(identifier.toString(), classPrint);
            }
            else
            {
                HuskyGadgetMod.getLogger().error("The print '" + classPrint.getName() + "' could not be registered due to a critical error!");
            }
        }
        catch(NoSuchMethodException e)
        {
            HuskyGadgetMod.getLogger().error("The print '" + classPrint.getName() + "' is missing an empty constructor and could not be registered!");
        }
        catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            HuskyGadgetMod.getLogger().error("The print '" + classPrint.getName() + "' could not be registered due to a critical error!");
        }
    }

    public static boolean isRegisteredPrint(Class<? extends IPrint> clazz)
    {
        return registeredPrints.containsValue(clazz);
    }

    @Nullable
    public static IPrint getPrint(String identifier)
    {
        Class<? extends IPrint> clazz = registeredPrints.get(identifier);
        if(clazz != null)
        {
            try
            {
                return clazz.newInstance();
            }
            catch(InstantiationException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static IPrint.Renderer getRenderer(IPrint print)
    {
        String id = getPrintIdentifier(print);
        return registeredRenders.get(id);
    }

    @SideOnly(Side.CLIENT)
    public static IPrint.Renderer getRenderer(String identifier)
    {
        return registeredRenders.get(identifier);
    }

    public static String getPrintIdentifier(IPrint print)
    {
        return registeredPrints.inverse().get(print.getClass());
    }
}

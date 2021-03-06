package net.thegaminghuskymc.gadgetmod.core.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.thegaminghuskymc.gadgetmod.core.images.Monitor;
import net.thegaminghuskymc.gadgetmod.tileentity.TileEntityDevice;
import net.thegaminghuskymc.gadgetmod.tileentity.TileEntityMonitor;
import net.thegaminghuskymc.gadgetmod.tileentity.TileEntityMonitorOld;
import net.thegaminghuskymc.gadgetmod.tileentity.TileEntityRouter;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class Connection
{
    private UUID routerId;
    private BlockPos routerPos;

    private Connection() {}

    public Connection(Router router)
    {
        this.routerId = router.getId();
        this.routerPos = router.getPos();
    }

    public UUID getRouterId()
    {
        return routerId;
    }

    @Nullable
    public BlockPos getRouterPos()
    {
        return routerPos;
    }

    public void setRouterPos(BlockPos routerPos)
    {
        this.routerPos = routerPos;
    }

    @Nullable
    public Router getRouter(World world)
    {
        if(routerPos == null)
            return null;

        TileEntity tileEntity = world.getTileEntity(routerPos);
        if(tileEntity instanceof TileEntityRouter)
        {
            TileEntityRouter router = (TileEntityRouter) tileEntity;
            if(router.getRouter().getId().equals(routerId))
            {
                return router.getRouter();
            }
        }
        return null;
    }

    public boolean isConnected()
    {
        return routerPos != null;
    }

    public NBTTagCompound toTag()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", routerId.toString());
        return tag;
    }

    public static Connection fromTag(TileEntityDevice device, NBTTagCompound tag)
    {
        Connection connection = new Connection();
        connection.routerId = UUID.fromString(tag.getString("id"));

        return connection;
    }

}
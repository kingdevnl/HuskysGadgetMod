package net.thegaminghuskymc.gadgetmod.network.task;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.thegaminghuskymc.gadgetmod.DeviceConfig;

/**
 * Author: MrCrayfish
 */
public class MessageSyncConfig implements IMessage, IMessageHandler<MessageSyncConfig, MessageSyncConfig>
{
    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, DeviceConfig.writeSyncTag());
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        NBTTagCompound syncTag = ByteBufUtils.readTag(buf);
        DeviceConfig.readSyncTag(syncTag);
    }

    @Override
    public MessageSyncConfig onMessage(MessageSyncConfig message, MessageContext ctx)
    {
        return null;
    }
}

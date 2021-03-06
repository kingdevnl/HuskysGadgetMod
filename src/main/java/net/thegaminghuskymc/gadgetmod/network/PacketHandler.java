package net.thegaminghuskymc.gadgetmod.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.thegaminghuskymc.gadgetmod.Reference;
import net.thegaminghuskymc.gadgetmod.network.task.*;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    public static void init() {
        INSTANCE.registerMessage(MessageRequest.class, MessageRequest.class, 1, Side.SERVER);
        INSTANCE.registerMessage(MessageResponse.class, MessageResponse.class, 2, Side.CLIENT);
        INSTANCE.registerMessage(MessageSyncApplications.class, MessageSyncApplications.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(MessageSyncConfig.class, MessageSyncConfig.class, 4, Side.CLIENT);
        INSTANCE.registerMessage(MessageSyncBlock.class, MessageSyncBlock.class, 5, Side.SERVER);
    }
}

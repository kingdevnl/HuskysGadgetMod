package net.thegaminghuskymc.gadgetmod.core;

import net.thegaminghuskymc.gadgetmod.api.app.Layout;

/**
 * Created by Casey on 07-Aug-17.
 */
public interface System {
    void openContext(Layout layout, int x, int y);

    boolean hasContext();

    void closeContext();
}
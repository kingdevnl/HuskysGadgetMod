package net.thegaminghuskymc.gadgetmod.object.tools;

import net.thegaminghuskymc.gadgetmod.object.Canvas;
import net.thegaminghuskymc.gadgetmod.object.Tool;

public class ToolEyeDropper extends Tool {

    @Override
    public void handleClick(Canvas canvas, int x, int y) {
        canvas.setColour(canvas.getPixel(x, y));
    }

    @Override
    public void handleRelease(Canvas canvas, int x, int y) {
    }

    @Override
    public void handleDrag(Canvas canvas, int x, int y) {
    }

}

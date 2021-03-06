package net.thegaminghuskymc.gadgetmod.core;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.thegaminghuskymc.gadgetmod.HuskyGadgetMod;
import net.thegaminghuskymc.gadgetmod.Reference;
import net.thegaminghuskymc.gadgetmod.api.app.Application;
import net.thegaminghuskymc.gadgetmod.api.app.Dialog;
import net.thegaminghuskymc.gadgetmod.api.app.Layout;
import net.thegaminghuskymc.gadgetmod.api.io.Drive;
import net.thegaminghuskymc.gadgetmod.api.task.TaskManager;
import net.thegaminghuskymc.gadgetmod.api.utils.RenderUtil;
import net.thegaminghuskymc.gadgetmod.core.client.LaptopFontRenderer;
import net.thegaminghuskymc.gadgetmod.programs.system.SystemApplication;
import net.thegaminghuskymc.gadgetmod.programs.system.component.FileBrowser;
import net.thegaminghuskymc.gadgetmod.programs.system.task.TaskUpdateApplicationData;
import net.thegaminghuskymc.gadgetmod.programs.system.task.TaskUpdateSystemData;
import net.thegaminghuskymc.gadgetmod.tileentity.TileEntityLaptop;
import net.thegaminghuskymc.gadgetmod.util.GuiHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Laptop extends GuiScreen implements System {

    public static final int ID = 1;
    public static final ResourceLocation ICON_TEXTURES = new ResourceLocation(Reference.MOD_ID, "textures/gui/app_icons.png");
    public static final FontRenderer fontRenderer = new LaptopFontRenderer(Minecraft.getMinecraft());
    private static final ResourceLocation LAPTOP_GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/laptop.png");
    private static final List<Application> APPLICATIONS = new ArrayList<>();
    private static final List<ResourceLocation> WALLPAPERS = new ArrayList<>();
    private static final int BORDER = 10;
    public static final int DEVICE_WIDTH = 464;
    static final int SCREEN_WIDTH = DEVICE_WIDTH - BORDER * 2;
    public static final int DEVICE_HEIGHT = 246;
    static final int SCREEN_HEIGHT = DEVICE_HEIGHT - BORDER * 2;

    private static System system;
    private static BlockPos pos;
    private static Drive mainDrive;
    private String OSName;
    private String OSVersion;
    private String username;
    private String password;
    private Settings settings;
    private TaskBar bar;
    private Window[] windows;
    private Layout context = null;
    private NBTTagCompound appData;
    private NBTTagCompound systemData;
    private int currentWallpaper;
    private int lastMouseX, lastMouseY;
    private boolean dragging = false;

    public Laptop(TileEntityLaptop laptop) {
        this.appData = laptop.getApplicationData();
        this.systemData = laptop.getSystemData();
        this.windows = new Window[5];
        this.settings = Settings.fromTag(systemData.getCompoundTag("Settings"));
        this.bar = new TaskBar(APPLICATIONS);
        this.currentWallpaper = systemData.getInteger("CurrentWallpaper");
        if (currentWallpaper < 0 || currentWallpaper >= WALLPAPERS.size()) {
            this.currentWallpaper = 0;
        }
        Laptop.system = this;
        Laptop.pos = laptop.getPos();
        this.username = "admin";
        this.password = "password";
    }

    @Nullable
    public static BlockPos getPos() {
        return pos;
    }

    public static void addWallpaper(ResourceLocation wallpaper) {
        if (wallpaper != null) {
            WALLPAPERS.add(wallpaper);
        }
    }

    public static System getSystem() {
        return system;
    }

    @Nullable
    public static Drive getMainDrive() {
        return mainDrive;
    }

    public static void setMainDrive(Drive mainDrive) {
        if (Laptop.mainDrive == null) {
            Laptop.mainDrive = mainDrive;
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int posX = (width - DEVICE_WIDTH) / 2;
        int posY = (height - DEVICE_HEIGHT) / 2;
        bar.init(posX + BORDER, posY + DEVICE_HEIGHT - 236);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        for (Window window : windows) {
            if (window != null)
                window.close();
        }

        /* Send system data */
        NBTTagCompound systemData = new NBTTagCompound();
        systemData.setInteger("CurrentWallpaper", currentWallpaper);
        systemData.setTag("Settings", settings.toTag());
        TaskManager.sendTask(new TaskUpdateSystemData(pos, systemData));

        Laptop.pos = null;
        Laptop.system = null;
        Laptop.mainDrive = null;
    }

    @Override
    public void onResize(Minecraft mcIn, int width, int height) {
        super.onResize(mcIn, width, height);
        for (Window window : windows) {
            if (window != null)
                window.content.markForLayoutUpdate();
        }
    }

    @Override
    public void updateScreen() {
        bar.onTick();

        for (Window window : windows) {
            if (window != null) {
                window.onTick();
            }
        }

        FileBrowser.refreshList = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(LAPTOP_GUI);

        /* Physical Screen */
        int posX = (width - DEVICE_WIDTH) / 2;
        int posY = (height - DEVICE_HEIGHT) / 2;

        /* Corners */
        this.drawTexturedModalRect(posX, posY, 0, 0, BORDER, BORDER); // TOP-LEFT
        this.drawTexturedModalRect(posX + DEVICE_WIDTH - BORDER, posY, 11, 0, BORDER, BORDER); // TOP-RIGHT
        this.drawTexturedModalRect(posX + DEVICE_WIDTH - BORDER, posY + DEVICE_HEIGHT - BORDER, 11, 11, BORDER, BORDER); // BOTTOM-RIGHT
        this.drawTexturedModalRect(posX, posY + DEVICE_HEIGHT - BORDER, 0, 11, BORDER, BORDER); // BOTTOM-LEFT

        /* Edges */
        RenderUtil.drawRectWithTexture(posX + BORDER, posY, 10, 0, SCREEN_WIDTH, BORDER, 1, BORDER); // TOP
        RenderUtil.drawRectWithTexture(posX + DEVICE_WIDTH - BORDER, posY + BORDER, 11, 10, BORDER, SCREEN_HEIGHT, BORDER, 1); // RIGHT
        RenderUtil.drawRectWithTexture(posX + BORDER, posY + DEVICE_HEIGHT - BORDER, 10, 11, SCREEN_WIDTH, BORDER, 1, BORDER); // BOTTOM
        RenderUtil.drawRectWithTexture(posX, posY + BORDER, 0, 11, BORDER, SCREEN_HEIGHT, BORDER, 1); // LEFT

        /* Center */
        RenderUtil.drawRectWithTexture(posX + BORDER, posY + BORDER, 10, 10, SCREEN_WIDTH, SCREEN_HEIGHT, 1, 1);

        /* Wallpaper */
        this.mc.getTextureManager().bindTexture(WALLPAPERS.get(currentWallpaper));
        RenderUtil.drawRectWithFullTexture(posX + 10, posY + 10, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        boolean insideContext = false;
        if (context != null) {
            insideContext = GuiHelper.isMouseInside(mouseX, mouseY, context.xPosition, context.yPosition, context.xPosition + context.width, context.yPosition + context.height);
        }

        /* Window */
        for (int i = windows.length - 1; i >= 0; i--) {
            Window window = windows[i];
            if (window != null) {
                window.render(this, mc, posX + BORDER, posY + BORDER, mouseX, mouseY, i == 0 && !insideContext, partialTicks);
            }
        }

        /* Application Bar */
        bar.render(this, mc, posX + 10, posY + DEVICE_HEIGHT - 236, mouseX, mouseY, partialTicks);

        if (context != null) {
            context.render(this, mc, context.xPosition, context.yPosition, mouseX, mouseY, true, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        int posX = (width - SCREEN_WIDTH) / 2;
        int posY = (height - SCREEN_HEIGHT) / 2;

        if (this.context != null) {
            int dropdownX = context.xPosition;
            int dropdownY = context.yPosition;
            if (GuiHelper.isMouseInside(mouseX, mouseY, dropdownX, dropdownY, dropdownX + context.width, dropdownY + context.height)) {
                this.context.handleMouseClick(mouseX, mouseY, mouseButton);
                this.dragging = true;
                return;
            } else {
                this.context = null;
            }
        }

        this.bar.handleClick(this, posX, posY + SCREEN_HEIGHT - 226, mouseX, mouseY, mouseButton);

        for (int i = 0; i < windows.length; i++) {
            Window<Application> window = windows[i];
            if (window != null) {
                Window<net.thegaminghuskymc.gadgetmod.api.app.Dialog> dialogWindow = window.getContent().getActiveDialog();
                if (isMouseWithinWindow(mouseX, mouseY, window) || isMouseWithinWindow(mouseX, mouseY, dialogWindow)) {
                    windows[i] = null;
                    updateWindowStack();
                    windows[0] = window;

                    windows[0].handleMouseClick(this, posX, posY, mouseX, mouseY, mouseButton);

                    if (isMouseWithinWindowBar(mouseX, mouseY, dialogWindow)) {
                        this.dragging = true;
                        return;
                    }

                    if (isMouseWithinWindowBar(mouseX, mouseY, window) && dialogWindow == null) {
                        this.dragging = true;
                        return;
                    }
                    break;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.dragging = false;
        if (this.context != null) {
            int dropdownX = context.xPosition;
            int dropdownY = context.yPosition;
            if (GuiHelper.isMouseInside(mouseX, mouseY, dropdownX, dropdownY, dropdownX + context.width, dropdownY + context.height)) {
                this.context.handleMouseRelease(mouseX, mouseY, state);
            }
        } else if (windows[0] != null) {
            windows[0].handleMouseRelease(mouseX, mouseY, state);
        }
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        if (Keyboard.getEventKeyState()) {
            char pressed = Keyboard.getEventCharacter();
            int code = Keyboard.getEventKey();

            if (windows[0] != null) {
                windows[0].handleKeyTyped(pressed, code);
            }

            super.keyTyped(pressed, code);
        } else {
            if (windows[0] != null) {
                windows[0].handleKeyReleased(Keyboard.getEventCharacter(), Keyboard.getEventKey());
            }
        }

        this.mc.dispatchKeypresses();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        int posX = (width - SCREEN_WIDTH) / 2;
        int posY = (height - SCREEN_HEIGHT) / 2;

        if (this.context != null) {
            if (dragging) {
                int dropdownX = context.xPosition;
                int dropdownY = context.yPosition;
                if (GuiHelper.isMouseInside(mouseX, mouseY, dropdownX, dropdownY, dropdownX + context.width, dropdownY + context.height)) {
                    this.context.handleMouseDrag(mouseX, mouseY, clickedMouseButton);
                }
            }
            return;
        }

        if (windows[0] != null) {
            Window<Application> window = windows[0];
            Window<Dialog> dialogWindow = window.getContent().getActiveDialog();
            if (dragging) {
                if (isMouseOnScreen(mouseX, mouseY)) {
                    if (dialogWindow == null) {
                        window.handleWindowMove(posX, posY, -(lastMouseX - mouseX), -(lastMouseY - mouseY));
                    } else {
                        dialogWindow.handleWindowMove(posX, posY, -(lastMouseX - mouseX), -(lastMouseY - mouseY));
                    }
                } else {
                    dragging = false;
                }
            } else {
                if (isMouseWithinWindow(mouseX, mouseY, window) || isMouseWithinWindow(mouseX, mouseY, dialogWindow)) {
                    window.handleMouseDrag(mouseX, mouseY, clickedMouseButton);
                }
            }
        }
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            if (windows[0] != null) {
                windows[0].handleMouseScroll(mouseX, mouseY, scroll >= 0);
            }
        }
    }

    @Override
    public void drawHoveringText(List<String> textLines, int x, int y) {
        super.drawHoveringText(textLines, x, y);
    }

    public void open(Application app) {
        if (HuskyGadgetMod.proxy.hasAllowedApplications()) {
            if (!HuskyGadgetMod.proxy.getAllowedApplications().contains(app.getInfo())) {
                return;
            }
        }

        for (int i = 0; i < windows.length; i++) {
            Window<Application> window = windows[i];
            if (window != null && window.content.getInfo().getFormattedId().equals(app.getInfo().getFormattedId())) {
                windows[i] = null;
                updateWindowStack();
                windows[0] = window;
                return;
            }
        }

        app.setLaptopPosition(pos);

        Window<Application> window = new Window<>(app, this);
        window.init((width - SCREEN_WIDTH) / 2, (height - SCREEN_HEIGHT) / 2);

        if (appData.hasKey(app.getInfo().getFormattedId())) {
            app.load(appData.getCompoundTag(app.getInfo().getFormattedId()));
        }

        if (app instanceof SystemApplication) {
            ((SystemApplication) app).setLaptop(this);
        }

        if (app.getCurrentLayout() == null) {
            app.restoreDefaultLayout();
        }

        addWindow(window);

        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public void close(Application app) {
        for (int i = 0; i < windows.length; i++) {
            Window<Application> window = windows[i];
            if (window != null) {
                if (window.content.getInfo().equals(app.getInfo())) {
                    if (app.isDirty()) {
                        NBTTagCompound container = new NBTTagCompound();
                        app.save(container);
                        app.clean();
                        appData.setTag(app.getInfo().getFormattedId(), container);
                        TaskManager.sendTask(new TaskUpdateApplicationData(pos.getX(), pos.getY(), pos.getZ(), app.getInfo().getFormattedId(), container));
                    }

                    if (app instanceof SystemApplication) {
                        ((SystemApplication) app).setLaptop(null);
                    }

                    window.handleClose();
                    windows[i] = null;
                    return;
                }
            }
        }
    }

    private void addWindow(Window<Application> window) {
        if (hasReachedWindowLimit())
            return;

        updateWindowStack();
        windows[0] = window;
    }

    private void updateWindowStack() {
        for (int i = windows.length - 1; i >= 0; i--) {
            if (windows[i] != null) {
                if (i + 1 < windows.length) {
                    if (i == 0 || windows[i - 1] != null) {
                        if (windows[i + 1] == null) {
                            windows[i + 1] = windows[i];
                            windows[i] = null;
                        }
                    }
                }
            }
        }
    }

    private boolean hasReachedWindowLimit() {
        for (Window window : windows) {
            if (window == null) return false;
        }
        return true;
    }

    private boolean isMouseOnScreen(int mouseX, int mouseY) {
        int posX = (width - SCREEN_WIDTH) / 2;
        int posY = (height - SCREEN_HEIGHT) / 2;
        return GuiHelper.isMouseInside(mouseX, mouseY, posX, posY, posX + SCREEN_WIDTH, posY + SCREEN_HEIGHT);
    }

    private boolean isMouseWithinWindowBar(int mouseX, int mouseY, Window window) {
        if (window == null) return false;
        int posX = (width - SCREEN_WIDTH) / 2;
        int posY = (height - SCREEN_HEIGHT) / 2;
        return GuiHelper.isMouseInside(mouseX, mouseY, posX + window.offsetX + 1, posY + window.offsetY + 1, posX + window.offsetX + window.width - 13, posY + window.offsetY + 11);
    }

    private boolean isMouseWithinWindow(int mouseX, int mouseY, Window window) {
        if (window == null) return false;
        int posX = (width - SCREEN_WIDTH) / 2;
        int posY = (height - SCREEN_HEIGHT) / 2;
        return GuiHelper.isMouseInside(mouseX, mouseY, posX + window.offsetX, posY + window.offsetY, posX + window.offsetX + window.width, posY + window.offsetY + window.height);
    }

    public boolean isApplicationRunning(String appId) {
        for (Window window : windows) {
            if (window != null && ((Application) window.content).getInfo().getFormattedId().equals(appId)) {
                return true;
            }
        }
        return false;
    }

    public void nextWallpaper() {
        if (currentWallpaper + 1 < WALLPAPERS.size()) {
            currentWallpaper++;
        }
    }

    public void prevWallpaper() {
        if (currentWallpaper - 1 >= 0) {
            currentWallpaper--;
        }
    }

    public int getCurrentWallpaper() {
        return currentWallpaper;
    }

    public List<ResourceLocation> getWallapapers() {
        return ImmutableList.copyOf(WALLPAPERS);
    }

    @Nullable
    public Application getApplication(String appId) {
        return APPLICATIONS.stream().filter(app -> app.getInfo().getFormattedId().equals(appId)).findFirst().orElse(null);
    }

    public List<Application> getApplications() {
        return APPLICATIONS;
    }

    public Settings getSettings() {
        return settings;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void openContext(Layout layout, int x, int y) {
        layout.updateComponents(x, y);
        context = layout;
        layout.init();
    }

    @Override
    public boolean hasContext() {
        return context != null;
    }

    @Override
    public void closeContext() {
        context = null;
    }
}
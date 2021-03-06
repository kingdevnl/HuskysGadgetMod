package net.thegaminghuskymc.gadgetmod.programs;

import net.minecraft.nbt.NBTTagCompound;
import net.thegaminghuskymc.gadgetmod.api.app.Application;
import net.thegaminghuskymc.gadgetmod.api.app.Component;
import net.thegaminghuskymc.gadgetmod.api.app.Dialog;
import net.thegaminghuskymc.gadgetmod.api.app.Icons;
import net.thegaminghuskymc.gadgetmod.api.app.component.Button;
import net.thegaminghuskymc.gadgetmod.api.app.component.ItemList;
import net.thegaminghuskymc.gadgetmod.api.io.File;
import net.thegaminghuskymc.gadgetmod.programs.bluej.AddFileDialog;
import net.thegaminghuskymc.gadgetmod.programs.bluej.Project;
import net.thegaminghuskymc.gadgetmod.programs.bluej.ProjectFile;
import net.thegaminghuskymc.gadgetmod.programs.bluej.components.BlueJCodeEditor;
import net.thegaminghuskymc.gadgetmod.programs.bluej.resources.BlueJResourceLocation;

import java.util.ArrayList;

public class ApplicationBlueJ extends Application {

    private static final int WIDTH = 362, HEIGHT = 164;

    private Button newProject, openProject, saveFile, exportProject;
    private Button newFile, deleteFile;
    private Button copyAll, paste;
    // TODO Import and export resources
    private Button run, stop;
    private Button settings;

    private BlueJCodeEditor codeEditor;
    private ItemList<String> files;
    // TODO add resources
    // TODO add terminal

    private Project currentProject;
    private ProjectFile openedFile;
    private int openedFileHash;

    private int leftPanelWidth;
    private int middlePanelWidth;
//	private int rightPanelWidth;

    private int x;

    private void resetLayout() {
        x = 1;
        leftPanelWidth = 80;
        middlePanelWidth = 280;
//		rightPanelWidth = 0;
    }

    private int getNextBtnPos() {
        int curr = x;
        x += 16;
        return curr;
    }

    private void addSeperator() {
        x += 2;
    }

    @Override
    public void init() {
        currentProject = null;
        openedFile = null;

        setDefaultWidth(WIDTH);
        setDefaultHeight(HEIGHT);

        // setup buttons

        resetLayout();

        newProject = new Button(getNextBtnPos(), 1, Icons.NEW_FOLDER);
        newProject.setToolTip("New Project", "Create new project");
        newProject.setClickListener(this::createProjectHandler);
        openProject = new Button(getNextBtnPos(), 1, Icons.LOAD);
        openProject.setToolTip("Open Project", "Open an exsting project");
        openProject.setClickListener(this::loadProjectHandler);
        exportProject = new Button(getNextBtnPos(), 1, Icons.EXPORT);
        exportProject.setToolTip("Export Project", "Export the project as a runnable");
        exportProject.setClickListener(this::archiveProjectHandler);
        exportProject.setEnabled(false);

        addComponent(newProject);
        addComponent(openProject);
        addComponent(exportProject);

        addSeperator();

        newFile = new Button(getNextBtnPos(), 1, Icons.NEW_FILE);
        newFile.setToolTip("New File", "Create new file");
        newFile.setClickListener(this::newFileHandler);
        deleteFile = new Button(getNextBtnPos(), 1, Icons.TRASH);
        deleteFile.setToolTip("Delete File", "Delete selected file");
        deleteFile.setClickListener(this::deleteFileHandler);
        deleteFile.setEnabled(false);
        saveFile = new Button(getNextBtnPos(), 1, Icons.SAVE);
        saveFile.setClickListener(this::saveFileHandler);
        saveFile.setToolTip("Save File", "Save current file");
        saveFile.setEnabled(false);

        addComponent(newFile);
        addComponent(deleteFile);
        addComponent(saveFile);

        addSeperator();

        copyAll = new Button(getNextBtnPos(), 1, Icons.COPY);
        copyAll.setToolTip("Copy All", "Copy all the contents of the current file to the clipboard");
        copyAll.setEnabled(false);
        paste = new Button(getNextBtnPos(), 1, Icons.CLIPBOARD);
        paste.setToolTip("Paste", "Paste the contents of the clipboard to the current file");
        paste.setEnabled(false);

        addComponent(copyAll);
        addComponent(paste);

        addSeperator();

        run = new Button(getNextBtnPos(), 1, Icons.PLAY);
        run.setToolTip("Run", "Run code");
        stop = new Button(getNextBtnPos(), 1, Icons.STOP);
        stop.setToolTip("Stop", "Stop running code");

        addComponent(run);
        addComponent(stop);

        addSeperator();

        settings = new Button(getNextBtnPos(), 1, Icons.WRENCH);
        settings.setToolTip("Settings", "Open and edit settings");

        addComponent(settings);

        // setup layout

        files = new ItemList<>(1, 18, leftPanelWidth, (HEIGHT - 18) / 15 + 1);
        files.setItemClickListener(this::fileBrowserClickHandler);
        codeEditor = new BlueJCodeEditor(1 + leftPanelWidth, 18, middlePanelWidth, HEIGHT - 23, null);

        addComponent(files);
        addComponent(codeEditor);

        // set project buttons to disabled

        setProjectButtons(false);
    }

    ////////////////////////// Project Buttons Handlers //////////////////////////

    private void createProjectHandler(int mouseX, int mouseY, int mouseButton) {
        currentProject = new Project();
        Dialog.SaveFile input = new Dialog.SaveFile(this, new NBTTagCompound());
        input.setResponseHandler((success, file) -> {
            if (success) {
                unloadProject(() -> {
                    BlueJResourceLocation resloc = new BlueJResourceLocation("files", "root", file.getPath());
                    currentProject = Project.loadProject(resloc);
                });
            }
            return true;
        });
        openDialog(input);
    }

    private void loadProjectHandler(int mouseX, int mouseY, int mouseButton) {
        unloadProject(() -> {
            Dialog.OpenFile open = new Dialog.OpenFile(this);
            open.setResponseHandler((success, file) -> {
                if (success && file != null) {
                    handleFile(file);
                }
                return true;
            });
            openDialog(open);
        });
    }

    private void archiveProjectHandler(int mouseX, int mouseY, int mouseButton) {
        NBTTagCompound tag = currentProject.archive();
        // TODO: Change from Runner to a newer once we have newer one
        Dialog.SaveFile file = new Dialog.SaveFile(this, new File("", "idemodthingy:dynamicapp", tag));
        file.setResponseHandler((success, f) -> {
            return true;
        });
        openDialog(file);
    }

    ////////////////////////// File Buttons Handlers //////////////////////////

    private void newFileHandler(int mouseX, int mouseY, int mouseButton) {
        AddFileDialog input = new AddFileDialog("File name");
        input.setResponseHandler((success, file) -> {
            if (success) {
                files.addItem(file.getName());
                currentProject.addFile(file);
            }
            return true;
        });
        openDialog(input);
    }

    private void deleteFileHandler(int mouseX, int mouseY, int mouseButton) {
        files.removeItem(files.getSelectedIndex());
        deleteFile.setEnabled(false);
        codeEditor.setEditable(false);
        codeEditor.setText("");
        files.setSelectedIndex(-1);
        currentProject.removeFile(openedFile.getName());
        openedFile = null;
    }

    private void saveFileHandler(int mouseX, int mouseY, int mouseButton) {
        saveOpenedFile();
    }

    ////////////////////////// Other Handlers //////////////////////////

    private void fileBrowserClickHandler(String item, int index, int button) {
        unloadFile(() -> {
            if (openedFile != null) {
                openedFile.setCode(codeEditor.getText());
            }
            openedFile = currentProject.getFile(item);
            codeEditor.setText(openedFile.getCode());
//			codeEditor.setLanguage(file.getLanguage().getHighlight());

            openedFileHash = codeEditor.getText().hashCode();

            saveFile.setEnabled(true);
            codeEditor.setEditable(true);
            deleteFile.setEnabled(true);
        });
    }

    ////////////////////////// Utility Methods //////////////////////////

    @Override
    public boolean handleFile(File file) {
        unloadProject(() -> {
            currentProject = Project.loadProject(new BlueJResourceLocation("files", "root", file.getParent().getPath()));

            for (String fileName : currentProject.getAllFileNames()) {
                files.addItem(fileName);
            }

            setProjectButtons(true);
        });
        return true;
    }

    public void unloadFile(Runnable runnable) {
        if (openedFile != null && codeEditor.getText().hashCode() != openedFileHash) {
            Dialog.Confirmation shouldSave = new Dialog.Confirmation("Do you want to save before exiting?");
            shouldSave.setPositiveListener((mouseX, mouseY, mouseButton) -> {
                saveOpenedFile();
                runnable.run();
            });
            shouldSave.setNegativeListener((mouseX, mouseY, mouseButton) -> {
                runnable.run();
            });
            openDialog(shouldSave);
        } else {
            runnable.run();
        }
    }

    public void unloadProject(Runnable runnable) {
        unloadFile(() -> {
            currentProject = null;
            openedFile = null;

            files.setItems(new ArrayList<String>());
            codeEditor.setText("");

            setProjectButtons(false);
            codeEditor.setEditable(false);
            deleteFile.setEnabled(false);

            runnable.run();
        });
    }

    public void saveOpenedFile() {
        if (openedFile != null && codeEditor.getText().hashCode() != openedFileHash) {
            openedFile.setCode(codeEditor.getText());
            currentProject.getResolvedResourceLocation().getFile(openedFile.getName()).setData(openedFile.toNBT());
            openedFileHash = codeEditor.getText().hashCode();
        }
    }

    public void setProjectButtons(boolean enabled) {
        newFile.setEnabled(enabled);
        files.setEnabled(enabled);
        run.setEnabled(enabled);
        stop.setEnabled(enabled);
        exportProject.setEnabled(enabled);
    }

    // TODO load layout settings

    public void load(NBTTagCompound arg0) {
    }

    public void save(NBTTagCompound arg0) {
    }

}

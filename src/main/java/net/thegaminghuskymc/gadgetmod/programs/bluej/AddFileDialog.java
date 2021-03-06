package net.thegaminghuskymc.gadgetmod.programs.bluej;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.thegaminghuskymc.gadgetmod.api.app.Dialog;
import net.thegaminghuskymc.gadgetmod.api.app.component.Button;
import net.thegaminghuskymc.gadgetmod.api.app.component.ComboBox;
import net.thegaminghuskymc.gadgetmod.api.app.component.Text;
import net.thegaminghuskymc.gadgetmod.api.app.component.TextField;
import net.thegaminghuskymc.gadgetmod.programs.bluej.api.BlueJLanguageManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class AddFileDialog extends Dialog {

    private String messageText = null;
    private String inputText = "";
    private String positiveText = "Okay";
    private String negativeText = "Cancel";

    private ResponseHandler<ProjectFile> responseListener;

    private TextField textFieldInput;
    private ComboBox.List<String> languages;
    private Button buttonPositive;
    private Button buttonNegative;

    public AddFileDialog() {
    }

    public AddFileDialog(String messageText) {
        this.messageText = messageText;
    }

    @Override
    public void init() {
        super.init();

        int offset = 0;

        if (messageText != null) {
            int lines = Minecraft.getMinecraft().fontRenderer
                    .listFormattedStringToWidth(messageText, getWidth() - 10).size();
            defaultLayout.height += lines * 9 + 20 + 20;
            offset += lines * 9 + 5;
        }

        super.init();

        String[] items = BlueJLanguageManager.getHighlighters().keySet().toArray(new String[BlueJLanguageManager.getHighlighters().keySet().size() + 1]);
        items[items.length - 1] = "Text";
        languages = new ComboBox.List<>(5, 5 + offset, getWidth() - 10, items);
        addComponent(languages);

        defaultLayout.setBackground((gui, mc, x, y, width, height, mouseX, mouseY, windowActive) -> {
            Gui.drawRect(x, y, x + width, y + height, Color.LIGHT_GRAY.getRGB());
        });

        if (messageText != null) {
            Text message = new Text(messageText, 5, 5, getWidth() - 10);
            this.addComponent(message);
        }

        textFieldInput = new TextField(5, 5 + offset + 20, getWidth() - 10);
        textFieldInput.setText(inputText);
        textFieldInput.setFocused(true);
        this.addComponent(textFieldInput);

        int positiveWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(positiveText);
        buttonPositive = new Button(getWidth() - positiveWidth - 15, getHeight() - 20, positiveText);
        buttonPositive.setSize(positiveWidth + 10, 15);
        buttonPositive.setClickListener((mouseX, mouseY, mouseButton) -> {
            if (!textFieldInput.getText().isEmpty()) {
                boolean close = true;
                if (responseListener != null) {
                    close = responseListener.onResponse(true, new ProjectFile(textFieldInput.getText().trim(), "", BlueJLanguageManager.getHighlighters().get(languages.getSelectedItem())));
                }
                if (close)
                    close();
            }
        });
        this.addComponent(buttonPositive);

        int negativeWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(negativeText);
        buttonNegative = new Button(getWidth() - positiveWidth - negativeWidth - 15 - 15, getHeight() - 20,
                negativeText);
        buttonNegative.setSize(negativeWidth + 10, 15);
        buttonNegative.setClickListener((mouseX, mouseY, mouseButton) -> close());
        this.addComponent(buttonNegative);
    }

    /**
     * Sets the initial text for the input text field
     *
     * @param inputText
     */
    public void setInputText(@Nonnull String inputText) {
        this.inputText = inputText;
    }

    /**
     * Gets the input text field. This will be null if has not been
     *
     * @return
     */
    @Nullable
    public TextField getTextFieldInput() {
        return textFieldInput;
    }

    /**
     * Sets the positive button text
     *
     * @param positiveText
     */
    public void setPositiveText(@Nonnull String positiveText) {
        this.positiveText = positiveText;
    }

    /**
     * Sets the negative button text
     *
     * @param negativeText
     */
    public void setNegativeText(@Nonnull String negativeText) {
        this.negativeText = negativeText;
    }

    /**
     * Sets the response handler. The handler is called when the positive button
     * is pressed and returns the value in the input text field. Returning true
     * in the handler indicates that the dialog should close.
     *
     * @param responseListener
     */
    public void setResponseHandler(ResponseHandler<ProjectFile> responseListener) {
        this.responseListener = responseListener;
    }
}
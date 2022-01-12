package bot.Tools;

import net.dv8tion.jda.api.interactions.components.Button;

/**
 * @author  Wil Aquino
 * Date:    January 10, 2022
 * Project: LaunchPoint Bot
 * Module:  Draft.java
 * Purpose: Conveniently builds a button..
 */
public class BuiltButton {

    /** Field for the built button. */
    private final Button button;

    /**
     * Builds the button, given a numbered button type.
     * @param buttonID the ID of the button.
     * @param buttonLabel the label of the button.
     * @param buttonType 0 for a primary type button.
     *                   1 for a success type button.
     *                   2 for a secondary type button.
     *                   3 for a destructive type button.
     */
    public BuiltButton(String buttonID, String buttonLabel,
                       int buttonType) {
        switch (buttonType) {
            case 1:
                button = Button.success(buttonID, buttonLabel);
                break;
            case 2:
                button = Button.secondary(buttonID, buttonLabel);
                break;
            case 3:
                button = Button.danger(buttonID, buttonLabel);
                break;
            default:
                button = Button.primary(buttonID, buttonLabel);
                break;
        }
    }

    /**
     * Retrieves the built button.
     * @return said button.
     */
    public Button getButton() {
        return button;
    }
}

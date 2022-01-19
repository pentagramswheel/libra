package bot.Tools;

import net.dv8tion.jda.api.interactions.components.Button;

/**
 * @author  Wil Aquino
 * Date:    January 10, 2022
 * Project: Libra
 * Module:  Draft.java
 * Purpose: Conveniently builds a button.
 */
public class ButtonBuilder {

    /** Field for the built button. */
    private final Button button;

    /**
     * Builds a button, given a numbered button type.
     * @param buttonID the ID of the button.
     * @param buttonLabel the label of the button.
     * @param url the url to link to, if any.
     * @param buttonType 0 for a primary type button.
     *                   1 for a success type button.
     *                   2 for a secondary type button.
     *                   3 for a destructive type button.
     *                   4 for a link type button.
     */
    public ButtonBuilder(String buttonID, String buttonLabel, String url,
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
            case 4:
                button = Button.link(url, buttonLabel);
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

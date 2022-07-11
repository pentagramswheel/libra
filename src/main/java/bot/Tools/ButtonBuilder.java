package bot.Tools;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

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
     * Retrieves a button style.
     * @param type 0 for a primary type button.
     *             1 for a success type button.
     *             2 for a secondary type button.
     *             3 for a destructive type button.
     *             4 for a link type button.
     */
    private ButtonStyle getButtonStyle(int type) {
        switch (type) {
            case 1:
                return ButtonStyle.SUCCESS;
            case 2:
                return ButtonStyle.SECONDARY;
            case 3:
                return ButtonStyle.DANGER;
            case 4:
                return ButtonStyle.LINK;
            default:
                return ButtonStyle.PRIMARY;
        }
    }

    /**
     * Builds a button, given a numbered button type.
     * @param buttonID the ID of the button.
     * @param buttonLabel the label of the button.
     * @param url the url to link to, if any.
     * @param buttonType the type of button to build.
     */
    public ButtonBuilder(String buttonID, String buttonLabel, String url,
                         int buttonType) {
        if (getButtonStyle(buttonType).equals(ButtonStyle.LINK)) {
            button = Button.of(getButtonStyle(buttonType), url, buttonLabel);
        } else {
            button = Button.of(getButtonStyle(buttonType), buttonID, buttonLabel);
        }
    }

    /**
     * Builds a button, given a numbered button type.
     * @param buttonID the ID of the button.
     * @param emojiName the name of an emoji on Discord.
     * @param emojiID the Discord ID of the emoji.
     * @param url the url to link to, if any.
     * @param buttonType the type of button to build.
     */
    public ButtonBuilder(String buttonID, String emojiName, String emojiID,
                         String url, int buttonType) {
        if (getButtonStyle(buttonType).equals(ButtonStyle.LINK)) {
            button = Button.of(getButtonStyle(buttonType), url,
                    Emoji.fromEmote(emojiName, Long.parseLong(emojiID), false));
        } else {
            button = Button.of(getButtonStyle(buttonType), buttonID,
                    Emoji.fromEmote(emojiName, Long.parseLong(emojiID), false));
        }
    }

    /**
     * Retrieves the built button.
     */
    public Button getButton() {
        return button;
    }
}

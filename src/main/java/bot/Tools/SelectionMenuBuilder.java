package bot.Tools;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    January 17, 2022
 * Project: Libra
 * Module:  Draft.java
 * Purpose: Conveniently builds a selection menu.
 */
public class SelectionMenuBuilder {

    /** Field for the built button. */
    private final SelectionMenu menu;

    /**
     * Builds a selection menu.
     * @param menuID the ID of the menu.
     * @param labels the labels of the menu.
     * @param values the values to each of the labels in the menu.
     * @param emojis the emoji values to each of the labels in the menu.
     */
    public SelectionMenuBuilder(String menuID, List<String> labels,
                                List<String> values, List<Emoji> emojis) {
        SelectionMenu.Builder menuBuilder = SelectionMenu.create(menuID);
        for (int i = 0; i < labels.size(); i++) {
            if (emojis == null) {
                menuBuilder.addOption(labels.get(i), values.get(i));
            } else {
                menuBuilder.addOption(labels.get(i), values.get(i), emojis.get(i));
            }
        }

        menu = menuBuilder.build();
    }

    /**
     * Retrieves the built selection menu.
     * @return said menu.
     */
    public SelectionMenu getMenu() {
        return menu;
    }
}

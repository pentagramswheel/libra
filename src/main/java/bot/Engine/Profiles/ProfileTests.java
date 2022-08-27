package bot.Engine.Profiles;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author  Slate, Wil Aquino
 * Date:    August 26, 2022
 * Project: Libra
 * Module:  PlayerTests.java
 * Purpose: Dedicated class for unit testing profile info.
 */
public class ProfileTests {

    /** Builds a sample player to use throughout the tests. */
    private PlayerInfo samplePlayer() {
        List<Object> info = Arrays.asList(
                "Libra#9209", "Libra", "0000-0000-0000", "she/her",
                "Skirmisher", "Sloshers", "X (2100-2200)",
                "Tech @ MullowayIT");
        return new PlayerInfo(null, 1, info);
    }

    /** Tests if player info is being read correctly. */
    @Test
    public void testPlayerInfo() {
        PlayerInfo player = samplePlayer();

        assertEquals(player.getTag(), "Libra#9209");
        assertEquals(player.getNickname(), "Libra");
        assertEquals(player.getFC(), "0000-0000-0000");
        assertEquals(player.getPronouns(), "she/her");
        assertEquals(player.getPlaystyle(), "Skirmisher");
        assertEquals(player.getWeaponPool(), "Sloshers");
        assertEquals(player.getRank(), "X (2100-2200)");
        assertEquals(player.getTeam(), "Tech @ MullowayIT");
    }

    /** Tests if pronouns are being read correctly. */
    @Test
    public void testPronouns() {
        String testMsg = "my pronouns are they/she/it and I am a squid-kid";

        PronounsBuilder builder = new PronounsBuilder(testMsg);
        for (int i = 0; i < 100; i++) {
            String chosenSubject = builder.getSubjectTitleCase();
            String chosenObject = builder.getObjectTitleCase();
            String chosenPosPro = builder.getPossessivePronounTitleCase();
            String chosenPosAdj = builder.getPossessiveAdjectiveTitleCase();

            assertNotEquals("He", chosenSubject);
            assertTrue(chosenSubject.equals("They") || chosenSubject.equals("She")
                    || chosenSubject.equals("It"));

            assertNotEquals("Him", chosenObject);
            assertTrue(chosenObject.equals("Them") || chosenObject.equals("Her")
                    || chosenObject.equals("It"));

            assertNotEquals("His", chosenPosPro);
            assertTrue(chosenPosPro.equals("Theirs") || chosenPosPro.equals("Hers")
                    || chosenPosPro.equals("Its own"));

            assertNotEquals("His", chosenPosAdj);
            assertTrue(chosenPosAdj.equals("Their") || chosenPosAdj.equals("Her")
                    || chosenPosAdj.equals("Its"));
        }

        builder = new PronounsBuilder(samplePlayer());
        for (int i = 0; i < 10; i++) {
            String chosenSubject = builder.getSubjectTitleCase();
            String chosenObject = builder.getObjectTitleCase();
            String chosenPosPro = builder.getPossessivePronounTitleCase();
            String chosenPosAdj = builder.getPossessiveAdjectiveTitleCase();

            assertEquals("She", chosenSubject);
            assertEquals("Her", chosenObject);
            assertEquals("Hers", chosenPosPro);
            assertEquals("Her", chosenPosAdj);
        }
    }
}

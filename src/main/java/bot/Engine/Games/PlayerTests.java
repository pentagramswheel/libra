package bot.Engine.Games;

import bot.Engine.Games.Drafts.DraftPlayer;
import bot.Engine.Games.Drafts.DraftTeam;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author  Wil Aquino
 * Date:    August 17, 2022
 * Project: Libra
 * Module:  PlayerTests.java
 * Purpose: Dedicated class for unit testing teams and players.
 */
public class PlayerTests {

    /** Tests if Players are working properly. */
    @Test
    public void testInitialPlayers() {
        Player p1 = new Player("123456789", false);
        Player p2 = new Player("123456789", false);
        Player p3 = p1;

        assertEquals(p1, p2);
        assertEquals(p2, p3);
        assertEquals(p1, p3);
        assertNotSame(p2, p3);
    }

    /** Tests if basic functions of teams are working. */
    @Test
    public void testTeamsBasic() {
        Player p1 = new Player("a", false);
        Player p2 = new Player("b", false);
        Player p3 = new Player("c", false);
        Player p4 = new Player("d", false);

        Team<Player> team = new Team<>(4, 4);
        assertTrue(team.isEmpty());
        assertTrue(team.hasMinimumScore());
        assertTrue(team.needsPlayers());

        team.add("1", p1);
        team.add("2", p2);
        team.add("3", p3);
        team.add("4", p4);

        assertFalse(team.needsPlayers());
        assertTrue(team.contains("2"));
        assertTrue(team.getPlayers().get("3").isActive());
        assertFalse(team.getPlayers().get("1").isSub());

        team.remove("2");
        assertTrue(team.needsPlayers());
        assertFalse(team.contains("2"));

        team.clear();
        assertTrue(team.isEmpty());
    }

    /** Tests if heavy usage of teams are working. */
    @Test
    public void testTeamsFull() {
        Team<Player> team = new Team<>(4, 4);
        for (int i = 0; i < 4; i++) {
            String currName = String.valueOf((char) ('a' + i));
            team.add(String.valueOf(i + 1), new Player(currName, false));
        }

        assertEquals(team.getPlayers().get("3").getName(), "c");
        assertTrue(team.hasMinimumScore());

        team.decrementScore();
        assertTrue(team.hasMinimumScore());

        team.incrementScore();
        team.incrementScore();
        team.incrementScore();
        team.incrementScore();
        assertTrue(team.hasMaximumScore());

        team.incrementScore();
        assertTrue(team.hasMaximumScore());

        team.decrementScore();
        team.decrementScore();
        assertEquals(2, team.getScore());

        team.requestSub();
        team.getPlayers().get("3").subOut();
        assertTrue(team.needsPlayers());
        assertTrue(team.getPlayers().get("3").isSub());
        assertFalse(team.getPlayers().get("3").isActive());
        assertEquals(1, team.getPlayers().get("3").getSubAmount());

        team.requestSub();
        team.getPlayers().get("2").subOut();
        assertEquals(4, team.getPlayers().size());

        int subs = 0;
        for (Player player : team.getPlayers().values()) {
            if (player.isSub()) {
                subs++;
            }
        }
        assertEquals(2, subs);
    }

    /** Tests if DraftPlayers are working properly. */
    @Test
    public void testInitialDraftPlayers() {
        DraftPlayer p1 = new DraftPlayer("123456789", 4, false);
        DraftPlayer p2 = new DraftPlayer("123456789", 4, false);
        DraftPlayer p3 = p1;

        assertEquals(p1, p2);
        assertEquals(p2, p3);
        assertEquals(p1, p3);
        assertNotSame(p2, p3);
    }

    /**
     * Creates a draft team to test.
     * @param offset the amount to offset IDs and names by.
     */
    private DraftTeam createDraftTeam(int offset) {
        DraftTeam team = new DraftTeam(4, 4);

        for (int i = 0; i < 4; i++) {
            team.add(String.valueOf(i + 1 + offset), new DraftPlayer(
                    String.valueOf((char) ('a' + i + offset)), 4, false));
        }

        return team;
    }

    /** Tests if usage of draft teams are working. */
    @Test
    public void testDraftTeams() {
        DraftTeam team1 = createDraftTeam(0);
        DraftTeam team2 = createDraftTeam(4);
        team1.setOpponents(team2);
        team2.setOpponents(team1);

        assertEquals(team1, team2.getOpponents());

        assertEquals("g", team2.getPlayers().get("7").getName());
        assertFalse(team1.needsPlayers());
        assertFalse(team2.needsPlayers());

        // score 2-0
        team1.incrementScore();
        team1.incrementScore();
        assertEquals(2, team1.getPlayers().get("1").getWins());
        assertEquals(2, team1.getPlayers().get("4").getWins());

        // score 2-1
        team2.incrementScore();
        assertEquals(1, team2.getPlayers().get("6").getWins());
        assertEquals(1, team2.getPlayers().get("7").getWins());

        assertEquals(1, team1.getPlayers().get("2").getLosses());
        assertEquals(2, team2.getPlayers().get("5").getLosses());
    }

    /** Tests if usage of draft teams are working with subs. */
    @Test
    public void testDraftTeamsWithSubs() {
        DraftTeam team1 = createDraftTeam(0);
        DraftTeam team2 = createDraftTeam(4);
        team1.setOpponents(team2);
        team2.setOpponents(team1);

        assertEquals(team1, team2.getOpponents());
        assertEquals(team2, team1.getOpponents());

        // score 1-2
        team1.incrementScore();
        team2.incrementScore();
        team2.incrementScore();

        // players to sub out (2, 5, 7)
        DraftPlayer player1 = team1.getPlayers().get("2");
        DraftPlayer player2 = team2.getPlayers().get("5");
        DraftPlayer player3 = team2.getPlayers().get("7");

        // players to sub in (9, 10, 11)
        DraftPlayer player4 = new DraftPlayer("i", 4, true);
        DraftPlayer player5 = new DraftPlayer("j", 4, true);
        DraftPlayer player6 = new DraftPlayer("k", 4, true);

        team1.requestSub();
        player1.subOut();

        team1.add("9", player4);

        // score 2-2
        team1.incrementScore();
        assertEquals(2, team1.getScore());
        assertEquals(2, team1.getPlayers().get("3").getWins());
        assertEquals(2, team1.getPlayers().get("1").getLosses());

        assertEquals(1, player1.getWins());
        assertEquals(2, player2.getLosses());

        assertEquals(1, player4.getWins());
        assertEquals(0, player4.getLosses());

        assertFalse(team2.hasMaximumScore());

        team2.requestSub();
        player2.subOut();
        team2.requestSub();
        player3.subOut();

        team2.add("10", player5);
        assertTrue(team2.needsPlayers());
        team2.add("11", player6);
        assertFalse(team2.needsPlayers());

        // score 2-4
        team2.incrementScore();
        team2.incrementScore();

        assertEquals(2, team1.getScore());
        assertEquals(4, team1.getPlayers().get("1").getLosses());
        assertEquals(2, player1.getLosses());

        assertTrue(team2.hasMaximumScore());
        assertEquals(4, team2.getPlayers().get("8").getWins());
        assertEquals(2, team2.getPlayers().get("6").getLosses());
        assertEquals(2, player2.getWins());
        assertEquals(2, player3.getLosses());

        assertEquals(2, player5.getWins());
        assertEquals(0, player6.getLosses());
    }
}

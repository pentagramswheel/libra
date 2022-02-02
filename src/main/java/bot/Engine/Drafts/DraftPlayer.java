package bot.Engine.Drafts;

/**
 * @author  Wil Aquino
 * Date:    January 11, 2022
 * Project: LaunchPoint Bot
 * Module:  DraftPlayer.java
 * Purpose: Represents a player playing within a draft.
 */
public class DraftPlayer {

    /** Flag for checking whether this player is active or not. */
    private boolean active;

    /** The player's amount of won matches during the draft. */
    private int matchWins;

    /** The player's amount of lost matches during the draft. */
    private int matchLosses;

    /** The player's amount of server pings during the draft. */
    private int pings;

    /**
     * Constructs the attributes of the draft player.
     */
    public DraftPlayer() {
        active = true;
        matchWins = matchLosses = 0;
        pings = 0;
    }

    /**
     * Checks whether the player is active within
     * the draft or not.
     * @return True if they are active.
     *         False otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the player to be inactive.
     */
    public void setInactive() {
        active = false;
    }

    /**
     * Retrieves the player's draft wins.
     * @return said wins.
     */
    public int getWins() {
        return matchWins;
    }

    /**
     * Increases the player's amount of wins by one.
     */
    public void incrementWins() {
        matchWins++;
    }

    /** Decreases the player's amount of wins by one. */
    public void decrementWins() {
        matchWins--;
    }

    /**
     * Retrieves the player's draft losses.
     * @return said losses.
     */
    public int getLosses() {
        return matchLosses;
    }

    /**
     * Increases the player's amount of losses by one.
     */
    public void incrementLosses() {
        matchLosses++;
    }

    /** Decreases the player's amount of wins by one. */
    public void decrementLosses() {
        matchLosses--;
    }

    /**
     * Retrieves the amount of times this player has pinged
     * during the draft.
     * @return said pings.
     */
    public int getPings() {
        return pings;
    }

    /** Increases the player's amount of pings by one. */
    public void incrementPings() {
        pings++;
    }

    /** Main method for testing the class. */
    public static void main(String[] args) {
//        DraftPlayer p1 = new DraftPlayer("123456789");
//        DraftPlayer p2 = new DraftPlayer("123456789");
//
//        System.out.println("p1.equals(p2) " + p1.equals(p2));
//        System.out.println("p2.equals(p1) " + p2.equals(p1));
//        System.out.println("p1.equals(p1) " + p1.equals(p1));
//        System.out.println("p1 == p2 " + (p1 == p2));
    }
}

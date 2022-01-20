package bot.Engine.Drafts;

/**
 * @author  Wil Aquino
 * Date:    January 11, 2022
 * Project: LaunchPoint Bot
 * Module:  DraftPlayer.java
 * Purpose: Represents a player playing within a draft.
 */
public class DraftPlayer {

    /** The draft player's Discord ID. */
    private final String id;

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
     * @param playerID the ID of the player to build the attributes for.
     */
    public DraftPlayer(String playerID) {
        id = playerID;
        active = true;
        matchWins = matchLosses = 0;
        pings = 0;
    }

    /**
     * Retrieves the draft player's Discord ID.
     * @return said ID.
     */
    public String getID() {
        return id;
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
     * Sets the player to an inactive.
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

    /** Increases the player's draft wins by one. */
    public void incrementWins() {
        matchWins++;
    }

    /**
     * Retrieves the player's draft losses.
     * @return said losses.
     */
    public int getLosses() {
        return matchLosses;
    }

    /** Increases the player's draft losses by one. */
    public void incrementLosses() {
        matchLosses++;
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

    /**
     * Overrided hashCode() method for draft players.
     * @return the hashcode.
     *
     * @source Josh Bloch's "Effective Java" in Item 8
     */
    @Override
    public int hashCode() {
        int hash = 31;
        hash = (31 * hash) + getID().hashCode();
        hash = (31 * hash) + (isActive() ? 0 : 1);
        hash = (31 * hash) + getWins();
        hash = (31 * hash) + getLosses();
        hash = (31 * hash) + getPings();

        return hash;
    }

    /**
     * Overrided equals() method for draft players.
     * @param o another object to compare.
     * @return True if they are the same draft player.
     *         False otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || (this.getClass() != o.getClass())) {
            return false;
        } else {
            DraftPlayer player = (DraftPlayer) o;

            return this.getID().equals(player.getID())
                    && this.getWins() == player.getWins()
                    && this.getLosses() == player.getLosses()
                    && this.getPings() == player.getPings();
        }
    }

    /** Main method for testing the class. */
    public static void main(String[] args) {
        DraftPlayer p1 = new DraftPlayer("123456789");
        DraftPlayer p2 = new DraftPlayer("123456789");

        System.out.println("p1.equals(p2) " + p1.equals(p2));
        System.out.println("p2.equals(p1) " + p2.equals(p1));
        System.out.println("p1.equals(p1) " + p1.equals(p1));
        System.out.println("p1 == p2 " + (p1 == p2));
    }
}

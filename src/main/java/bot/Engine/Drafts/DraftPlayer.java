package bot.Engine.Drafts;

/**
 * @author  Wil Aquino
 * Date:    January 11, 2022
 * Project: LaunchPoint Bot
 * Module:  DraftPlayer.java
 * Purpose: Represents a player playing within a draft.
 */
public class DraftPlayer {

    /** The name of the player within the server. */
    private final String name;

    /** Flag for checking whether this player is active or not. */
    private boolean active;

    /** Flag for checking whether this player is a captain or not. */
    private boolean captainStatus1, captainStatus2;

    /** Flag for checking whether this player has been assigned a team or not. */
    private boolean teamStatus;

    /** Flag for checking whether this player is a sub or not. */
    private boolean subStatus;

    /** The amount of times this player has subbed out. */
    private int subs;

    /** The player's amount of won matches during the draft. */
    private int matchWins;

    /** The player's amount of lost matches during the draft. */
    private int matchLosses;

    /**
     * Constructs the attributes of the draft player.
     * @param playerName the name of the player.
     * @param isSub a flag for checking if the player is a sub or not.
     */
    public DraftPlayer(String playerName, boolean isSub) {
        name = playerName;

        active = true;
        captainStatus1 = captainStatus2 = false;
        teamStatus = false;

        subStatus = isSub;
        subs = 0;

        matchWins = matchLosses = 0;
    }

    /**
     * Retrieves the name of the player.
     * @return said name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the player as a ping.
     * @param id the Discord ID of the player.
     * @return said ping.
     */
    public String getAsMention(String id) {
        return String.format("<@%s>", id);
    }

    /**
     * Checks if the player is active within the draft.
     * @return True if they are active.
     *         False otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether this player is active or not.
     * @param status their current activity status.
     */
    public void setActiveStatus(boolean status) {
        active = status;
    }

    /**
     * Checks if the player is the first team's captain within the draft.
     * @return True if they are a captain.
     *         False otherwise.
     */
    public boolean isCaptainForTeam1() {
        return captainStatus1;
    }

    /**
     * Sets this player's captain status for the first team.
     * @param status the status to set it to.
     */
    public void setCaptainForTeam1(boolean status) {
        captainStatus1 = status;
    }

    /**
     * Checks if the player is the second team's captain within the draft.
     * @return True if they are a captain.
     *         False otherwise.
     */
    public boolean isCaptainForTeam2() {
        return captainStatus2;
    }

    /**
     * Sets this player's captain status for the second team.
     * @param status the status to set it to.
     */
    public void setCaptainForTeam2(boolean status) {
        captainStatus2 = status;
    }

    /**
     * Checks whether this player is part of a team already or not.
     * @return said status.
     */
    public boolean hasTeam() {
        return teamStatus;
    }

    /**
     * Sets whether this player has joined a team or not.
     * @param status their current team status.
     */
    public void setTeamStatus(boolean status) {
        teamStatus = status;
    }

    /**
     * Checks if the player is a sub.
     * @return True if they are a sub.
     *         False otherwise.
     */
    public boolean isSub() {
        return subStatus;
    }

    /**
     * Sets whether this player is a sub or not.
     * @param status their current sub status.
     */
    public void setSubStatus(boolean status) {
        subStatus = status;
    }

    /**
     * Increases the amount of times this player has subbed out by one.
     */
    public void incrementSubs() {
        subs++;
    }

    /**
     * Retrieves the amount of times this player has subbed out.
     * @return said times.
     */
    public int getSubAmount() {
        return subs;
    }

    /**
     * Retrieves the player's draft wins.
     * @return said wins.
     */
    public int getWins() {
        return matchWins;
    }

    /**
     * Retrieves the player's draft losses.
     * @return said losses.
     */
    public int getLosses() {
        return matchLosses;
    }

    /**
     * Increases the player's amount of wins by one.
     */
    public void incrementWins() {
        if (isActive()) {
            matchWins++;
        }
    }

    /** Decreases the player's amount of wins by one. */
    public void decrementWins() {
        if (isActive()) {
            matchWins--;
        }
    }

    /**
     * Increases the player's amount of losses by one.
     */
    public void incrementLosses() {
        if (isActive()) {
            matchLosses++;
        }
    }

    /** Decreases the player's amount of wins by one. */
    public void decrementLosses() {
        if (isActive()) {
            matchLosses--;
        }
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

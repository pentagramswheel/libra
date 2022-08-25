package bot.Engine.Games;

/**
 * @author  Wil Aquino
 * Date:    July 19, 2022
 * Project: Libra
 * Module:  Player.java
 * Purpose: Represents a player playing within a draft.
 */
public class Player {

    /** The name of the player within the server. */
    private final String name;

    /** Flag for checking whether this player is active or not. */
    private boolean active;

    /** Flag for checking whether this player has been assigned a team or not. */
    private boolean teamStatus;

    /** Flag for checking whether this player is a sub or not. */
    private boolean subStatus;

    /** The amount of times this player has subbed out. */
    private int subs;

    /**
     * Constructs the attributes of the player.
     * @param playerName the name of the player.
     * @param isSub a flag for knowing if the player is a sub or not.
     */
    public Player(String playerName, boolean isSub) {
        name = playerName;

        active = true;
        teamStatus = false;

        subStatus = isSub;
        subs = 0;
    }

    /** Retrieves the name of the player. */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the player as a ping.
     * @param id the Discord ID of the player.
     */
    public String getAsMention(String id) {
        return String.format("<@%s>", id);
    }

    /**
     * Sets whether this player is active or not.
     * @param status their current activity status.
     */
    public void setActiveStatus(boolean status) {
        active = status;
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
     * Sets whether this player has joined a team or not.
     * @param status their current team status.
     */
    public void setTeamStatus(boolean status) {
        teamStatus = status;
    }

    /**
     * Checks whether this player is part of a team already or not.
     * @return said status.
     */
    public boolean hasTeam() {
        return teamStatus;
    }

    /**
     * Sets whether this player is a sub or not.
     * @param status their current sub status.
     */
    public void setSubStatus(boolean status) {
        subStatus = status;
    }

    /**
     * Checks if the player is a sub.
     * @return True if they are a sub.
     *         False otherwise.
     */
    public boolean isSub() {
        return subStatus;
    }

    /** Increases the amount of times this player has subbed out by one. */
    public void incrementSubs() {
        subs++;
    }

    /** Retrieves the amount of times this player has subbed out. */
    public int getSubAmount() {
        return subs;
    }

    /** Overridden hash code for players. */
    @Override
    public int hashCode() {
        int hash = 37;
        hash = 89 * hash + (getName() != null ? getName().hashCode() : 0);
        hash = 89 * hash + (isActive() ? 0 : 1);
        hash = 89 * hash + (hasTeam() ? 0 : 1);
        hash = 89 * hash + (isSub() ? 0 : 1);
        hash = 89 * hash + (getSubAmount() ^ (getSubAmount() >>> 16));

        return hash;
    }

    /** Overridden equals checking for players. */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        } else if (this == o) {
            return true;
        }

        Player p = (Player) o;
        return getName().equals(p.getName())
                && isActive() == p.isActive() && hasTeam() == p.hasTeam()
                && isSub() == p.isSub() && getSubAmount() == p.getSubAmount();
    }
}

package bot.Engine.Games;

/**
 * @author  Wil Aquino
 * Date:    July 18, 2022
 * Project: Libra
 * Module:  GameProperties.java
 * Purpose: Represents the properties of a game.
 */
public class GameProperties {

    /** The type of the game. */
    private final GameType gameType;

    /** The name of the game. */
    private final String name;

    /** The minimum/maximum amount of players needed to start the game. */
    private int minimumPlayersToStart;
    private final int maximumPlayersToStart;

    /** The minimum/maximum amount of players needed to end the game. */
    private final int minimumPlayersToEnd;
    private final int maximumPlayersToEnd;

    /** The maximum amount of matches to play within the game. */
    private final int numMatches;

    /** The score needed to win the game. */
    private final int winningScore;

    /** The number of map generations made for a game. */
    private int mapGens;

    /**
     * Constructs the game's properties.
     * @param type the type of the game.
     */
    public GameProperties(GameType type) {
        gameType = type;

        switch (type) {
            case DRAFT:
                name = "Draft";
                minimumPlayersToStart = maximumPlayersToStart = 8;
                minimumPlayersToEnd = 3;
                maximumPlayersToEnd = 5;
                numMatches = 7;
                winningScore = 4;
                break;
            case RANKED:
                name = "Ranked Modes";
                minimumPlayersToStart = maximumPlayersToStart = 8;
                minimumPlayersToEnd = 3;
                maximumPlayersToEnd = 5;
                numMatches = 7;
                winningScore = 4;
                break;
            case TURF_WAR:
                name = "Turf War Only";
                minimumPlayersToStart = maximumPlayersToStart = 8;
                minimumPlayersToEnd = 3;
                maximumPlayersToEnd = 5;
                numMatches = 7;
                winningScore = 4;
                break;
            case HIDE_AND_SEEK:
                name = "Hide & Seek";
                minimumPlayersToStart = maximumPlayersToStart = 8;
                minimumPlayersToEnd = 3;
                maximumPlayersToEnd = 5;
                numMatches = 6;
                winningScore = 0;
                break;
            case JUGGERNAUT:
                name = "Juggernaut";
                minimumPlayersToStart = maximumPlayersToStart = 6;
                minimumPlayersToEnd = 2;
                maximumPlayersToEnd = 3;
                numMatches = 6;
                winningScore = 0;
                break;
            default:
                name = "Spawn Rush";
                minimumPlayersToStart = 6;
                maximumPlayersToStart = 8;
                minimumPlayersToEnd = 2;
                maximumPlayersToEnd = 3;
                numMatches = 6;
                winningScore = 0;
                break;
        }

        mapGens = 0;
    }

    /** Retrieves the type of the game. */
    public GameType getGameType() {
        return gameType;
    }

    /** Retrieves the name of the game. */
    public String getName() {
        return name;
    }

    /**
     * Stores the minimum players needed to start/continue the game.
     * @param players the minimum amount to store.
     */
    public void setMinimumPlayersToStart(int players) {
        minimumPlayersToStart = players;
    }

    /** Retrieves the minimum players needed to start the game. */
    public int getMinimumPlayersToStart() {
        return minimumPlayersToStart;
    }

    /** Retrieves the maximum players needed to start the game. */
    public int getMaximumPlayersToStart() {
        return maximumPlayersToStart;
    }

    /** Retrieves the minimum players needed to end the game. */
    public int getMinimumPlayersToEnd() {
        return minimumPlayersToEnd;
    }

    /** Retrieves the maximum players needed to end the game. */
    public int getMaximumPlayersToEnd() {
        return maximumPlayersToEnd;
    }

    /** Retrieves the amount of matches meant to be played in the game. */
    public int getTotalMatches() {
        return numMatches;
    }

    /** Retrieves the winning score of the game (0 if none). */
    public int getWinningScore() {
        return winningScore;
    }

    /** Increases this game's amount of map generations by one. */
    public void incrementMapGens() {
        mapGens++;
    }

    /** Retrieves this game's amount of map generations. */
    public int getMapGens() {
        return mapGens;
    }
}

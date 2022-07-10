//package bot.Engine.Games;
//
//import bot.Engine.Drafts.DraftPlayer;
//import bot.Engine.Drafts.DraftProcess;
//import net.dv8tion.jda.api.entities.TextChannel;
//
//import java.util.HashSet;
//import java.util.TreeMap;
//
///**
// * @author  Wil Aquino
// * Date:    July 10, 2022
// * Project: Libra
// * Module:  Game.java
// * Purpose: Formalizes and starts a game.
// */
//public class Game {
//
//    /** Flag for checking whether this game has initialized or not. */
//    private boolean initialized;
//
//    /** The time limit for the request to expire. */
//    private int timeLimit;
//
//    /** The starting time of this draft's initial request. */
//    private final long startTime;
//
//    /** The formal number of this draft. */
//    private final int numDraft;
//
//    /** The formal process for executing this draft. */
//    private DraftProcess draftProcess;
//
//    /** The players of this draft. */
//    private final TreeMap<String, DraftPlayer> players;
//    private final HashSet<String> playerHistory;
//
//    /** The amount of inactive players within the draft. */
//    private int numInactive;
//
//    /** The draft chat channel this draft is occurring in. */
//    private final TextChannel draftChat;
//
//    /** The Discord message ID for this draft's initial interface. */
//    private String messageID;
//
//    /** The number of players required to formally start the draft. */
//    public final static int NUM_PLAYERS_TO_START_DRAFT = 8;
//}

package bot.Engine.Games;

import bot.Engine.Section;
import bot.Engine.Templates.GameReqs;
import bot.Events;
import bot.Engine.Templates.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Arrays;

/**
 * @author  Wil Aquino
 * Date:    January 5, 2022
 * Project: Libra
 * Module:  MapGenerator.java
 * Purpose: Generates a map list for a MIT area.
 *          Optional maplists are shown for both Splatoon 2/3 (S2/3).
 */
public class MapGenerator extends Section implements Command {

    /** The draft associated with this map generation, if any. */
    private final GameReqs foundDraft;

    /** The default minimum/maximum amount of map generations. */
    private final static int MIN_MAPS = 0;
    private final static int MAX_MAPS = 9;

    /** The maximum number of maplists which a draft can generate. */
    private final static int MAX_DRAFT_MAPLISTS = 2;

    /**
     * Loads the map generator's random number generator.
     * @param abbreviation the abbreviation of the section.
     * @param draft a found draft.
     */
    public MapGenerator(String abbreviation, GameReqs draft) {
        super(abbreviation);
        foundDraft = draft;
    }

    /**
     * Resets the generator's available modes list.
     */
    private void resetModes(List<String> modes) {
        modes.add("Splat Zones");
        modes.add("Tower Control");
        modes.add("Rainmaker");
        modes.add("Clam Blitz");
    }

    /** Retrieves the Freshwater Shoals legal maps for each game mode (S2). */
    private TreeMap<String, List<String>> getLegalFSMapsS2() {
        TreeMap<String, List<String>> legalMaps = new TreeMap<>();
        List<String> twMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "Skipper Pavilion", "The Reef",
                "Humpback Pump Track", "Starfish Mainstage", "Wahoo World",
                "Piranha Pit", "Manta Maria", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena"));
        List<String> szMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "Skipper Pavilion", "The Reef",
                "Humpback Pump Track", "Starfish Mainstage", "Wahoo World",
                "Piranha Pit", "Manta Maria", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena"));
        List<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "Ancho-V Games", "Sturgeon Shipyard",
                "Starfish Mainstage", "MakoMart", "The Reef", "Manta Maria",
                "Piranha Pit", "Skipper Pavilion", "Snapper Canal",
                "Humpback Pump Track"));
        List<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Humpback Pump Track", "Starfish Mainstage", "Manta Maria",
                "Sturgeon Shipyard", "Snapper Canal", "Ancho-V Games",
                "MakoMart", "The Reef", "Inkblot Art Academy",
                "Blackbelly Skatepark", "Musselforge Fitness", "Piranha Pit"));
        List<String> cbMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "The Reef", "MakoMart", "Piranha Pit",
                "Snapper Canal", "Humpback Pump Track", "Sturgeon Shipyard",
                "Starfish Mainstage", "Ancho-V Games", "New Albacore Hotel",
                "Manta Maria"));

        List<String> hsMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "Skipper Pavilion", "The Reef",
                "Humpback Pump Track", "Starfish Mainstage", "Wahoo World",
                "Piranha Pit", "Manta Maria", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena"));
        List<String> jnMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "Skipper Pavilion", "The Reef",
                "Humpback Pump Track", "Starfish Mainstage", "Wahoo World",
                "Piranha Pit", "Manta Maria", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena"));
        List<String> srMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "Skipper Pavilion", "The Reef",
                "Humpback Pump Track", "Starfish Mainstage", "Wahoo World",
                "Piranha Pit", "Manta Maria", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena"));

        legalMaps.put("Turf War", twMaps);
        legalMaps.put("Splat Zones", szMaps);
        legalMaps.put("Tower Control", tcMaps);
        legalMaps.put("Rainmaker", rmMaps);
        legalMaps.put("Clam Blitz", cbMaps);

        legalMaps.put("Hide & Seek", hsMaps);
        legalMaps.put("Juggernaut", jnMaps);
        legalMaps.put("Spawn Rush", srMaps);

        return legalMaps;
    }

    /** Retrieves the Freshwater Shoals legal maps for each game mode (S3). */
    private TreeMap<String, List<String>> getLegalFSMapsS3() {
        TreeMap<String, List<String>> legalMaps = new TreeMap<>();
        List<String> twMaps = new ArrayList<>(Arrays.asList(
                "Scorch Gorge", "Eeltail Alley", "Hagglefish Market",
                "Undertow Spillway", "Mincemeat Metalworks",
                "Hammerhead Bridge", "Museum D'Alfonsino", "Mahi-Mahi Resort",
                "Inkblot Art Academy", "Sturgeon Shipyard", "MakoMart",
                "Wahoo World", "Brinewater Springs", "Flounder Heights",
                "Um'ami Ruins", "Manta Maria"));
        List<String> szMaps = new ArrayList<>(Arrays.asList(
                "Scorch Gorge", "Eeltail Alley", "Hagglefish Market",
                "Undertow Spillway", "Mincemeat Metalworks",
                "Hammerhead Bridge", "Museum D'Alfonsino", "Mahi-Mahi Resort",
                "Inkblot Art Academy", "Sturgeon Shipyard", "MakoMart",
                "Wahoo World", "Brinewater Springs", "Flounder Heights",
                "Um'ami Ruins", "Manta Maria"));
        List<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Scorch Gorge", "Eeltail Alley", "Hagglefish Market",
                "Undertow Spillway", "Mincemeat Metalworks",
                "Hammerhead Bridge", "Museum D'Alfonsino", "Mahi-Mahi Resort",
                "Inkblot Art Academy", "Sturgeon Shipyard", "MakoMart",
                "Wahoo World", "Brinewater Springs", "Flounder Heights",
                "Um'ami Ruins", "Manta Maria"));
        List<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Scorch Gorge", "Eeltail Alley", "Hagglefish Market",
                "Undertow Spillway", "Mincemeat Metalworks",
                "Hammerhead Bridge", "Museum D'Alfonsino", "Mahi-Mahi Resort",
                "Inkblot Art Academy", "Sturgeon Shipyard", "MakoMart",
                "Wahoo World", "Brinewater Springs", "Flounder Heights",
                "Um'ami Ruins", "Manta Maria"));
        List<String> cbMaps = new ArrayList<>(Arrays.asList(
                "Scorch Gorge", "Eeltail Alley", "Hagglefish Market",
                "Undertow Spillway", "Mincemeat Metalworks",
                "Hammerhead Bridge", "Museum D'Alfonsino", "Mahi-Mahi Resort",
                "Inkblot Art Academy", "Sturgeon Shipyard", "MakoMart",
                "Wahoo World", "Brinewater Springs", "Flounder Heights",
                "Um'ami Ruins", "Manta Maria"));

        List<String> hsMaps = new ArrayList<>(Arrays.asList(
                "Scorch Gorge", "Eeltail Alley", "Hagglefish Market",
                "Undertow Spillway", "Mincemeat Metalworks",
                "Hammerhead Bridge", "Museum D'Alfonsino", "Mahi-Mahi Resort",
                "Inkblot Art Academy", "Sturgeon Shipyard", "MakoMart",
                "Wahoo World", "Brinewater Springs", "Flounder Heights",
                "Um'ami Ruins", "Manta Maria"));
        List<String> jnMaps = new ArrayList<>(Arrays.asList(
                "Scorch Gorge", "Eeltail Alley", "Hagglefish Market",
                "Undertow Spillway", "Mincemeat Metalworks",
                "Hammerhead Bridge", "Museum D'Alfonsino", "Mahi-Mahi Resort",
                "Inkblot Art Academy", "Sturgeon Shipyard", "MakoMart",
                "Wahoo World", "Brinewater Springs", "Flounder Heights",
                "Um'ami Ruins", "Manta Maria"));
        List<String> srMaps = new ArrayList<>(Arrays.asList(
                "Scorch Gorge", "Eeltail Alley", "Hagglefish Market",
                "Undertow Spillway", "Mincemeat Metalworks",
                "Hammerhead Bridge", "Museum D'Alfonsino", "Mahi-Mahi Resort",
                "Inkblot Art Academy", "Sturgeon Shipyard", "MakoMart",
                "Wahoo World", "Brinewater Springs", "Flounder Heights",
                "Um'ami Ruins", "Manta Maria"));

        legalMaps.put("Turf War", twMaps);
        legalMaps.put("Splat Zones", szMaps);
        legalMaps.put("Tower Control", tcMaps);
        legalMaps.put("Rainmaker", rmMaps);
        legalMaps.put("Clam Blitz", cbMaps);

        legalMaps.put("Hide & Seek", hsMaps);
        legalMaps.put("Juggernaut", jnMaps);
        legalMaps.put("Spawn Rush", srMaps);

        return legalMaps;
    }

    /** Retrieves the LaunchPoint legal maps for each game mode (S2). */
    private TreeMap<String, List<String>> getLegalLPMapsS2() {
        TreeMap<String, List<String>> legalMaps = new TreeMap<>();
        List<String> szMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "Skipper Pavilion", "The Reef",
                "Humpback Pump Track", "Starfish Mainstage", "Wahoo World",
                "Piranha Pit", "Manta Maria", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena"));
        List<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "Ancho-V Games", "Sturgeon Shipyard",
                "Starfish Mainstage", "MakoMart", "The Reef", "Manta Maria",
                "Piranha Pit", "Skipper Pavilion", "Snapper Canal",
                "Humpback Pump Track"));
        List<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Humpback Pump Track", "Starfish Mainstage", "Manta Maria",
                "Sturgeon Shipyard", "Snapper Canal", "Ancho-V Games",
                "MakoMart", "The Reef", "Inkblot Art Academy",
                "Blackbelly Skatepark", "Musselforge Fitness", "Piranha Pit"));
        List<String> cbMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "The Reef", "MakoMart", "Piranha Pit",
                "Snapper Canal", "Humpback Pump Track", "Sturgeon Shipyard",
                "Starfish Mainstage", "Ancho-V Games", "New Albacore Hotel",
                "Manta Maria"));

        legalMaps.put("Splat Zones", szMaps);
        legalMaps.put("Tower Control", tcMaps);
        legalMaps.put("Rainmaker", rmMaps);
        legalMaps.put("Clam Blitz", cbMaps);

        return legalMaps;
    }

    /** Retrieves the LaunchPoint legal maps for each game mode (S3). */
    private TreeMap<String, List<String>> getLegalLPMapsS3() {
        TreeMap<String, List<String>> legalMaps = new TreeMap<>();
        List<String> szMaps = new ArrayList<>(Arrays.asList(
                "Brinewater Springs", "Flounder Heights", "Hagglefish Market",
                "Inkblot Art Academy", "Mahi-Mahi Resort", "MakoMart",
                "Manta Maria", "Museum d'Alfonsino", "Sturgeon Shipyard",
                "Um'ami Ruins", "Wahoo World"));
        List<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Flounder Heights", "Hagglefish Market",
                "Inkblot Art Academy", "MakoMart", "Manta Maria",
                "Museum d'Alfonsino", "Scorch Gorge", "Sturgeon Shipyard",
                "Um'ami Ruins", "Undertow Spillway"));
        List<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Flounder Heights", "Hagglefish Market",
                "Inkblot Art Academy", "MakoMart", "Manta Maria",
                "Museum d'Alfonsino", "Scorch Gorge", "Sturgeon Shipyard",
                "Um'ami Ruins", "Undertow Spillway"));
        List<String> cbMaps = new ArrayList<>(Arrays.asList(
                "Brinewater Springs", "Eeltail Alley", "Flounder Heights",
                "Hagglefish Market", "Inkblot Art Academy", "MakoMart",
                "Manta Maria", "Museum d'Alfonsino", "Scorch Gorge",
                "Sturgeon Shipyard", "Um'ami Ruins"));

        legalMaps.put("Splat Zones", szMaps);
        legalMaps.put("Tower Control", tcMaps);
        legalMaps.put("Rainmaker", rmMaps);
        legalMaps.put("Clam Blitz", cbMaps);

        return legalMaps;
    }

    /** Retrieves the Ink Odyssey legal maps for each game mode (S2). */
    private TreeMap<String, List<String>> getLegalIOMapsS2() {
        TreeMap<String, List<String>> legalMaps = new TreeMap<>();
        List<String> szMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "The Reef", "Wahoo World",
                "Humpback Pump Track", "Piranha Pit", "Starfish Mainstage",
                "Manta Maria", "Skipper Pavilion", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena",
                "Camp Triggerfish"));
        List<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "Sturgeon Shipyard", "Ancho-V Games",
                "MakoMart", "The Reef", "Starfish Mainstage", "Manta Maria",
                "Piranha Pit", "Snapper Canal", "Shellendorf Institute",
                "Musselforge Fitness"));
        List<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Starfish Mainstage", "Manta Maria", "Blackbelly Skatepark",
                "Sturgeon Shipyard", "Humpback Pump Track", "Ancho-V Games",
                "The Reef", "Inkblot Art Academy", "Snapper Canal",
                "MakoMart", "Musselforge Fitness", "Piranha Pit"));
        List<String> cbMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "The Reef", "MakoMart", "Snapper Canal",
                "Piranha Pit", "Sturgeon Shipyard", "Humpback Pump Track",
                "Starfish Mainstage", "New Albacore Hotel", "Manta Maria",
                "Ancho-V Games"));

        legalMaps.put("Splat Zones", szMaps);
        legalMaps.put("Tower Control", tcMaps);
        legalMaps.put("Rainmaker", rmMaps);
        legalMaps.put("Clam Blitz", cbMaps);

        return legalMaps;
    }

    /** Retrieves the Ink Odyssey legal maps for each game mode (S3). */
    private TreeMap<String, List<String>> getLegalIOMapsS3() {
        TreeMap<String, List<String>> legalMaps = new TreeMap<>();
        List<String> szMaps = new ArrayList<>(Arrays.asList(
                "Brinewater Springs", "Flounder Heights", "Hagglefish Market",
                "Inkblot Art Academy", "MakoMart", "Manta Maria",
                "Museum d'Alfonsino", "Sturgeon Shipyard", "Uma'mi Ruins",
                "Wahoo World"));
        List<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Eeltail Alley", "Flounder Heights", "Hagglefish Market",
                "Inkblot Art Academy", "MakoMart", "Manta Maria",
                "Museum d'Alfonsino", "Scorch Gorge", "Sturgeon Shipyard",
                "Undertow Spillway", "Wahoo World"));
        List<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Eeltail Alley", "Hagglefish Market", "Hammerhead Bridge",
                "Inkblot Art Academy", "MakoMart", "Manta Maria",
                "Museum d'Alfonsino", "Scorch Gorge", "Sturgeon Shipyard",
                "Undertow Spillway"));
        List<String> cbMaps = new ArrayList<>(Arrays.asList(
                "Eeltail Alley", "Hagglefish Market", "Inkblot Art Academy",
                "Mahi-Mahi Resort", "MakoMart", "Manta Maria",
                "Museum d'Alfonsino", "Scorch Gorge", "Sturgeon Shipyard",
                "Um'ami Ruins"));

        legalMaps.put("Splat Zones", szMaps);
        legalMaps.put("Tower Control", tcMaps);
        legalMaps.put("Rainmaker", rmMaps);
        legalMaps.put("Clam Blitz", cbMaps);

        return legalMaps;
    }

    /** Retrieves the legal maps for a section. */
    private TreeMap<String, List<String>> getLegalMaps() {
        switch (getPrefix()) {
            case "fs":
                return getLegalFSMapsS3();
            case "lp":
                return getLegalLPMapsS3();
            default:
                return getLegalIOMapsS3();
        }
    }

    /**
     * Locates the URL of a S2 map online.
     * @param map the map to search for.
     */
    private String findS2MapURL(String map) {
        switch (map) {
            case "Humpback Pump Track":
                return "https://static.wikia.nocookie.net/splatoon/images/a/a4/HumpbackPumpTrack.jpg";
            case "Inkblot Art Academy":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/c/c9/S2_Stage_Inkblot_Art_Academy.png";
            case "Moray Towers":
                return "https://static.wikia.nocookie.net/splatoon/images/2/21/200px-MorayTowers.png";
            case "Musselforge Fitness":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/c/cd/S2_Stage_Musselforge_Fitness.png";
            case "Port Mackerel":
                return "https://static.wikia.nocookie.net/splatoon/images/1/1f/PortMackerel.jpg";
            case "Starfish Mainstage":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/3/31/S2_Stage_Starfish_Mainstage.png";
            case "Sturgeon Shipyard":
                return "https://static.wikia.nocookie.net/splatoon/images/3/38/SturgeonShipyard.png";
            case "The Reef":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/f/f7/S2_Stage_The_Reef.png";
            case "Manta Maria":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/7/7e/S2_Stage_Manta_Maria.png";
            case "Kelp Dome":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/f/f0/S2_Stage_Kelp_Dome.png";
            case "Snapper Canal":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/9/91/S2_Stage_Snapper_Canal.png";
            case "Blackbelly Skatepark":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/1/11/S2_Stage_Blackbelly_Skatepark.png";
            case "MakoMart":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/d/d4/S2_Stage_MakoMart.png";
            case "Walleye Warehouse":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/6/6a/S2_Stage_Walleye_Warehouse.png";
            case "Shellendorf Institute":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/6/6c/S2_Stage_Shellendorf_Institute.png";
            case "Arowana Mall":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/f/f5/S2_Stage_Arowana_Mall.png";
            case "Goby Arena":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/d/d0/S2_Stage_Goby_Arena.png";
            case "Piranha Pit":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/8/88/S2_Stage_Piranha_Pit.png";
            case "Camp Triggerfish":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/e/ef/S2_Stage_Camp_Triggerfish.png";
            case "Wahoo World":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/1/14/S2_Stage_Wahoo_World.png";
            case "New Albacore Hotel":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/d/da/S2_Stage_New_Albacore_Hotel.png";
            case "Ancho-V Games":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/2/20/S2_Stage_Ancho-V_Games.png";
            default:
                return "https://cdn.wikimg.net/en/splatoonwiki/images/1/10/S2_Stage_Skipper_Pavilion.png";
        }
    }

    /**
     * Locates the URL of a S3 map online.
     * @param map the map to search for.
     */
    private String findS3MapURL(String map) {
        switch (map) {
            case "Scorch Gorge":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/1/1c/S3_Stage_Scorch_Gorge.png";
            case "Eeltail Alley":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/7/7d/S3_Stage_Eeltail_Alley.png";
            case "Hagglefish Market":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/a/ad/S3_Stage_Hagglefish_Market.png";
            case "Undertow Spillway":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/a/ad/S3_Stage_Undertow_Spillway.png";
            case "Mincemeat Metalworks":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/d/d1/S3_Stage_Mincemeat_Metalworks.png";
            case "Hammerhead Bridge":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/9/94/S3_Stage_Hammerhead_Bridge.png";
            case "Museum d'Alfonsino":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/6/6a/S3_Stage_Museum_d%27Alfonsino.png";
            case "Mahi-Mahi Resort":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/6/6b/S3_Stage_Mahi-Mahi_Resort.png";
            case "Inkblot Art Academy":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/4/45/S3_Stage_Inkblot_Art_Academy.png";
            case "Sturgeon Shipyard":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/4/45/S3_Stage_Sturgeon_Shipyard.png";
            case "MakoMart":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/4/47/S3_Stage_MakoMart.png";
            case "Brinewater Springs":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/f/fc/S3_Stage_Brinewater_Springs.png";
            case "Flounder Heights":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/2/23/S3_Stage_Flounder_Heights.png";
            case "Um'ami Ruins":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/9/95/S3_Stage_Um%27ami_Ruins.png";
            case "Manta Maria":
                return "https://cdn.wikimg.net/en/splatoonwiki/images/8/86/S3_Stage_Manta_Maria.png";
            default:
                return "https://cdn.wikimg.net/en/splatoonwiki/images/7/71/S3_Stage_Wahoo_World.png";
        }
    }

    /**
     * Builds the match format in the form of an embed.
     * @param mode the mode of the match.
     * @param map the map of the match.
     */
    private EmbedBuilder buildMatch(String mode, String map) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(getColor())
                .addField(mode, map, false)
                .setThumbnail(findS3MapURL(map));

        return eb;
    }

    /**
     * Checks if there is currently a problem with running the map
     * generation command or not.
     * @param sc the user's inputted command.
     * @return True if there is a problem.
     *         False otherwise.
     */
    private boolean problemExists(SlashCommandEvent sc, int numMaps) {
        if (numMaps > MAX_MAPS) {
            sendReply(sc, "Too many maps requested.", true);
            return true;
        } else if (numMaps < MIN_MAPS + 1) {
            sendReply(sc, "Why would you request zero or less maps?", true);
            return true;
        } else if (foundDraft == null) {
            return false;
        } else if (!foundDraft.isInitialized()) {
            sendReply(sc, "You cannot generate maps for your "
                    + "draft yet!", true);
            return true;
        } else if (!sc.getTextChannel().equals(
                foundDraft.getDraftChannel())) {
            sendReply(sc, "You can only generate maplists for your draft in "
                    + foundDraft.getDraftChannel().getAsMention() + "!", true);
            return true;
        } else if (numMaps != foundDraft.getProperties().getTotalMatches()) {
            sendReply(sc, "Your draft can only generate `"
                            + foundDraft.getProperties().getTotalMatches()
                            + "` maps.", true);
            return true;
        } else if (foundDraft.getProperties().getMapGens() >= MAX_DRAFT_MAPLISTS) {
            sendReply(sc, "You can only generate two maplists per draft!",
                    true);
            return true;
        } else {
            foundDraft.getProperties().incrementMapGens();
            return false;
        }
    }

    /**
     * Runs the map generation command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        int numMaps = (int) sc.getOptions().remove(0).getAsLong();
        if (problemExists(sc, numMaps)) {
            return;
        }
        sc.deferReply(false).queue();

        int numModes = 0;
        List<String> modes = new ArrayList<>();

        String lastMode = "";
        boolean oneModeFound = false;

        if (foundDraft != null) {
            GameProperties properties = foundDraft.getProperties();
            oneModeFound = !properties.getGameType().equals(GameType.DRAFT)
                    && !properties.getGameType().equals(GameType.RANKED);
            if (oneModeFound) {
                lastMode = properties.getName();
            }
        }

        TreeMap<String, List<String>> legalMaps = getLegalMaps();
        List<String> pastMaps = new ArrayList<>();

        int rIndex;
        List<MessageEmbed> matches = new ArrayList<>();
        for (int i = 0; i < numMaps; i++) {
            String currMode;
            if (!oneModeFound) {
                if (numModes == 0) {
                    resetModes(modes);
                    numModes = 4;
                }

                rIndex = Events.RANDOM_GENERATOR.nextInt(numModes);
                currMode = modes.get(rIndex);
                while (lastMode.equals(currMode)) {
                    rIndex = Events.RANDOM_GENERATOR.nextInt(numModes);
                    currMode = modes.get(rIndex);
                }
                lastMode = modes.remove(rIndex);
                numModes--;
            } else {
                currMode = lastMode;
            }

            List<String> modeMaps = legalMaps.get(currMode);
            rIndex = Events.RANDOM_GENERATOR.nextInt(modeMaps.size());
            String currMap = modeMaps.get(rIndex);
            while (pastMaps.contains(currMap)) {
                rIndex = Events.RANDOM_GENERATOR.nextInt(modeMaps.size());
                currMap = modeMaps.get(rIndex);
            }
            pastMaps.add(modeMaps.remove(rIndex));

            matches.add(buildMatch(currMode, currMap).build());
        }

        if (foundDraft != null) {
            sc.getHook().editOriginalEmbeds(matches).queue(
                    message -> {
                        message.pin().queue();
                        wait(1000);
                    });
        } else {
            sc.getHook().editOriginalEmbeds(matches).queue();
        }

        log("A " + getSection() + " maplist was generated.", false);
    }
}

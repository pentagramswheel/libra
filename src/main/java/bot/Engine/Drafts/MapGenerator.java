package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.Random;

/**
 * @author  Wil Aquino
 * Date:    January 5, 2022
 * Project: Libra
 * Module:  MapGenerator.java
 * Purpose: Generates a map list for a MIT area.
 */
public class MapGenerator extends Section implements Command {

    /** A random number generator. */
    private final Random numGenerator;

    /** The draft associated with this map generation, if any. */
    private final Draft foundDraft;

    /** The maximum number of maplists which a draft can generate. */
    private final static int MAX_DRAFT_MAPLISTS = 2;

    /**
     * Loads the map generator's random number generator.
     * @param abbreviation the abbreviation of the section.
     * @param r the random number generator to load.
     * @param draft a found draft.
     */
    public MapGenerator(String abbreviation, Random r, Draft draft) {
        super(abbreviation);
        numGenerator = r;
        foundDraft = draft;
    }

    /**
     * Checks if the map generation can occur.
     * @return True if the map generation can proceed.
     *         False otherwise.
     */
    private boolean mapGenerationLimitHit() {
        if (foundDraft == null) {
            return false;
        } else if (foundDraft.getMapGens() >= MAX_DRAFT_MAPLISTS) {
            return true;
        } else {
            foundDraft.incrementMapGens();
            return false;
        }
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

    /**
     * Retrieves the LaunchPoint legal maps for each
     * game mode.
     * @return said legal maps.
     */
    private TreeMap<String, ArrayList<String>> getLegalLPMaps() {
        TreeMap<String, ArrayList<String>> legalMaps = new TreeMap<>();
        ArrayList<String> szMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "Skipper Pavilion", "The Reef",
                "Humpback Pump Track", "Starfish Mainstage", "Wahoo World",
                "Piranha Pit", "Manta Maria", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena"));
        ArrayList<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "Ancho-V Games", "Sturgeon Shipyard",
                "Starfish Mainstage", "MakoMart", "The Reef", "Manta Maria",
                "Piranha Pit", "Skipper Pavilion", "Snapper Canal",
                "Humpback Pump Track"));
        ArrayList<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Humpback Pump Track", "Starfish Mainstage", "Manta Maria",
                "Sturgeon Shipyard", "Snapper Canal", "Ancho-V Games",
                "MakoMart", "The Reef", "Inkblot Art Academy",
                "Blackbelly Skatepark", "Musselforge Fitness", "Piranha Pit"));
        ArrayList<String> cbMaps = new ArrayList<>(Arrays.asList(
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

    /**
     * Retrieves the Ink Odyssey legal maps for each
     * game mode.
     * @return said legal maps.
     */
    private TreeMap<String, ArrayList<String>> getLegalIOMaps() {
        TreeMap<String, ArrayList<String>> legalMaps = new TreeMap<>();
        ArrayList<String> szMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "MakoMart", "Ancho-V Games",
                "Sturgeon Shipyard", "The Reef", "Wahoo World",
                "Humpback Pump Track", "Piranha Pit", "Starfish Mainstage",
                "Manta Maria", "Skipper Pavilion", "New Albacore Hotel",
                "Musselforge Fitness", "Snapper Canal", "Goby Arena",
                "Camp Triggerfish"));
        ArrayList<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Inkblot Art Academy", "Sturgeon Shipyard", "Ancho-V Games",
                "MakoMart", "The Reef", "Starfish Mainstage", "Manta Maria",
                "Piranha Pit", "Snapper Canal", "Shellendorf Institute",
                "Musselforge Fitness"));
        ArrayList<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Starfish Mainstage", "Manta Maria", "Blackbelly Skatepark",
                "Sturgeon Shipyard", "Humpback Pump Track", "Ancho-V Games",
                "The Reef", "Inkblot Art Academy", "Snapper Canal",
                "MakoMart", "Musselforge Fitness", "Piranha Pit"));
        ArrayList<String> cbMaps = new ArrayList<>(Arrays.asList(
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

    /**
     * Locates the URL of a map online.
     * @param map the map to search for.
     * @return the map URL.
     */
    private String findMapURL(String map) {
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
     * Builds the match format in the form of an embed.
     * @param mode the mode of the match.
     * @param map the map of the match.
     */
    private EmbedBuilder buildMatch(String mode, String map) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(getColor())
                .addField(mode, map, false)
                .setThumbnail(findMapURL(map));

        return eb;
    }

    /**
     * Runs the map generation command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        if (mapGenerationLimitHit()) {
            sendReply(sc, "You can only generate two maplists per draft!", true);
            return;
        }
        sc.deferReply(true).queue();

        List<OptionMapping> args = sc.getOptions();
        int numMaps = (int) args.get(0).getAsLong();

        int numModes = 0;
        List<String> modes = new ArrayList<>();
        TreeMap<String, ArrayList<String>> legalMaps = getLegalLPMaps();
        if (getSection().equals("io")) {
            legalMaps = getLegalIOMaps();
        }

        String lastMode = "";
        List<String> pastMaps = new ArrayList<>();

        List<EmbedBuilder> matches = new ArrayList<>();
        for (int i = 0; i < numMaps; i++) {
            if (numModes == 0) {
                resetModes(modes);
                numModes = 4;
            }

            int rIndex = numGenerator.nextInt(numModes);
            String currMode = modes.get(rIndex);
            while (lastMode.equals(currMode)) {
                rIndex = numGenerator.nextInt(numModes);
                currMode = modes.get(rIndex);
            }
            lastMode = modes.remove(rIndex);
            numModes--;

            ArrayList<String> modeMaps = legalMaps.get(currMode);
            rIndex = numGenerator.nextInt(modeMaps.size());
            String currMap = modeMaps.get(rIndex);
            while (pastMaps.contains(currMap)) {
                rIndex = numGenerator.nextInt(modeMaps.size());
                currMap = modeMaps.get(rIndex);
            }
            pastMaps.add(modeMaps.remove(rIndex));

            matches.add(buildMatch(currMode, currMap));
        }

        sendEmbeds(sc, matches);
        log("A map list was generated.", false);
    }
}

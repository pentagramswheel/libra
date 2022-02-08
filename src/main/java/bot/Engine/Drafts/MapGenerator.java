package bot.Engine.Drafts;

import bot.Tools.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
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
 * Purpose: Generates a map list.
 */
public class MapGenerator implements Command {

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
     * Retrieve the size of the map list.
     * @param args the arguments of the command.
     * @return said amount.
     */
    private int getListSize(List<OptionMapping> args) {
        return (int) args.get(0).getAsLong();
    }

    /**
     * Retrieves the legal maps for each game mode.
     * @return said legal maps.
     */
    private TreeMap<String, ArrayList<String>> getLegalMaps() {
        TreeMap<String, ArrayList<String>> legalMaps = new TreeMap<>();
        ArrayList<String> szMaps = new ArrayList<>(Arrays.asList(
                "Ancho-V Games", "Goby Arena", "Humpback Pump Track",
                "MakoMart", "Manta Maria", "Musselforge Fitness",
                "New Albacore Hotel", "Piranha Pit", "Skipper Pavilion",
                "Starfish Mainstage", "Sturgeon Shipyard", "The Reef",
                "Wahoo World"));
        ArrayList<String> tcMaps = new ArrayList<>(Arrays.asList(
                "Ancho-V Games", "Inkblot Art Academy", "MakoMart",
                "Manta Maria", "Piranha Pit", "Shellendorf Institute",
                "Starfish Mainstage", "Sturgeon Shipyard", "The Reef"));
        ArrayList<String> rmMaps = new ArrayList<>(Arrays.asList(
                "Ancho-V Games", "Blackbelly Skatepark", "Humpback Pump Track",
                "Inkblot Art Academy", "MakoMart", "Manta Maria",
                "Musselforge Fitness", "Snapper Canal", "Starfish Mainstage",
                "Sturgeon Shipyard"));
        ArrayList<String> cbMaps = new ArrayList<>(Arrays.asList(
                "Humpback Pump Track", "Inkblot Art Academy", "MakoMart",
                "New Albacore Hotel", "Piranha Pit", "Snapper Canal",
                "The Reef"));

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
        eb.setColor(Color.BLUE)
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
        sc.deferReply().queue();
        List<OptionMapping> args = sc.getOptions();

        int numGens = getListSize(args);
        if (numGens > 9) {
            sendResponse(sc,
                    "Too many maps requested. The set would be too long!", true);
            log("A requested map list was too long.", false);
            return;
        } else if (numGens <= 0) {
            sendResponse(sc,
                    "Why would you request zero or less maps?", true);
            log("A requested map list was too short.", false);
            return;
        }

        int numModes = 0;
        ArrayList<String> modes = new ArrayList<>();
        TreeMap<String, ArrayList<String>> legalMaps = getLegalMaps();

        String lastMode = "";
        ArrayList<String> pastMaps = new ArrayList<>();

        ArrayList<EmbedBuilder> matches = new ArrayList<>();
        for (int i = 0; i < numGens; i++) {
            if (numModes == 0) {
                resetModes(modes);
                numModes = 4;
            }

            Random r = new Random();
            int rIndex = r.nextInt(numModes);
            String currMode = modes.get(rIndex);
            while (lastMode.equals(currMode)) {
                rIndex = r.nextInt(numModes);
                currMode = modes.get(rIndex);
            }
            lastMode = modes.remove(rIndex);
            numModes--;

            ArrayList<String> modeMaps = legalMaps.get(currMode);
            rIndex = r.nextInt(modeMaps.size());
            String currMap = modeMaps.get(rIndex);
            while (pastMaps.contains(currMap)) {
                rIndex = r.nextInt(modeMaps.size());
                currMap = modeMaps.get(rIndex);
            }
            pastMaps.add(modeMaps.remove(rIndex));

            matches.add(buildMatch(currMode, currMap));
        }

        sendEmbeds(sc, matches);
        log("A map list was generated.", false);
    }
}

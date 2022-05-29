package bot.Engine;

import bot.Config;
import bot.Tools.Command;

import bot.Tools.GoogleSheetsAPI;
import com.google.api.services.sheets.v4.model.ValueRange;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Turtle#1504
 * Date: May 7, 2022
 * Project: Libra
 * Module: Profile.java
 * Purpose: Manages the profile of the users.
 */
public class Profile implements Command {

    /** Google Sheets ID of the spreadsheet to save to. */
    private final String spreadsheetID;

    /**
     * Sets the spreadsheet ID.
     */
    public Profile() {
        spreadsheetID = Config.mitProfilesSheetID;
    }

    private GoogleSheetsAPI getSpreadsheet() {
        try {
            return new GoogleSheetsAPI(spreadsheetID);
        } catch (GeneralSecurityException | IOException e) {
            log("The MIT profiles spreadsheet could not load.", true);
            return null;
        }
    }

    /**
     * Runs the profile command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        String subCmd = sc.getSubcommandName();
        if (subCmd == null) {
            subCmd = "";
        }

        // tab name of the spreadsheet
        String tab = "'Profiles'";
        List<OptionMapping> args = sc.getOptions();

        switch (subCmd) {
            case "create":
                createProfile(sc, tab);
                break;
            case "delete":
                deleteProfile(sc, tab);
                break;
            case "edit":
                if(args == null){
                    sc.reply("What do you want to change? You didn't specify one. Example: /mit profile edit Slayer").queue();
                }else{
                    editProfile(sc,tab);
                }
                break;
            case "view":
                if(args.size() == 0){
                    viewProfile(sc, sc.getMember().getUser());
                }else{
                    viewProfile(sc, args.get(0).getAsUser());
                }
                break;
        }
    }

    /**
     * Finds the row in the spreadsheet that corresponds to the user id.
     * @param id id of the member
     * @return
     */
    public int findRow(String id) {
        int i = 0;
        try {

            while(true) {

                String response = getSpreadsheet().getSheetValues("Profiles").get(i).get(0).toString();
                if(response.equals(id)){
                    return i;
                }
                if(response.equals("")){
                    return -1;
                }
                i++;
            }
        } catch (IOException e ) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }

        return -1;
    }

    /**
     * Creates a new profile.
     * @param sc the slash command event.
     * @param tab the tab to save to.
     */
    public void createProfile(SlashCommandEvent sc, String tab) {
        String id = sc.getMember().getId();
        String name = sc.getMember().getUser().getAsTag();
        String nickname = sc.getMember().getEffectiveName();
        if(findRow(id) != -1){
            sc.reply("Profile already exists for " + sc.getMember().getAsMention() + ".").queue();
            return;
        }
        GoogleSheetsAPI link = getSpreadsheet();

        ValueRange newRow = link.buildRow(Arrays.asList(
                id, name, nickname, "Unknown", "Unknown", "Unknown"));



        try {
            link.appendRow(tab, newRow);
        } catch (IOException e) {
            log("New profile creation error "
                    + "occurred with " + sc.getMember().getAsMention() + ".", true);
            return;
        }

        sc.reply("Created profile for " + sc.getMember().getAsMention() + ".").queue();
    }

    /**
     * Deletes a profile.
     * @param sc the slash command event.
     * @param tab the tab to save to.
     */
    public void deleteProfile(SlashCommandEvent sc, String tab) {
        String id = sc.getMember().getId();


        GoogleSheetsAPI link = getSpreadsheet();



        int row = findRow(id);
        if(row == -1){
            sc.reply("Profile does not exist for " + sc.getMember().getAsMention() + ".").queue();
            return;
        }
        try {
            String updateRange = link.buildRange(tab,
                    "A", row+1,
                    "G", row+1);
            ValueRange newRow = link.buildRow(Arrays.asList(
                    "", "", "", "", "", ""));
            link.updateRange(updateRange, newRow);

        } catch (IOException e) {
            log("Profile deletion error "
                    + "occurred with " + sc.getMember().getAsMention() + ".", true);
            return;
        }

        sc.reply("Deleted profile for " + sc.getMember().getAsMention() + ".").queue();
    }

    /**
     * Edits a profile.
     * @param sc the slash command event.
     * @param tab the tab to save to.
     */
    public void editProfile(SlashCommandEvent sc, String tab) {
        String id = sc.getMember().getId();
        GoogleSheetsAPI link = getSpreadsheet();
        int row = findRow(id);
        if(row == -1){
            sc.reply("Profile does not exist for " + sc.getMember().getAsMention() + ".").queue();
            return;
        }


        String nickname = null;
        String pronoun = null;
        String playstyle = null;
        String weapon = null;
        try {
            nickname = getSpreadsheet().getSheetValues("Profiles").get(findRow(id)).get(2).toString();
            pronoun = getSpreadsheet().getSheetValues("Profiles").get(findRow(id)).get(3).toString();
            playstyle = getSpreadsheet().getSheetValues("Profiles").get(findRow(id)).get(4).toString();
            weapon = getSpreadsheet().getSheetValues("Profiles").get(findRow(id)).get(5).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
             nickname = sc.getOptionsByName("nickname").get(0).getAsString();

        }catch(IndexOutOfBoundsException e){

        }
        try{
            pronoun = sc.getOptionsByName("pronoun").get(0).getAsString();

        }catch(IndexOutOfBoundsException e){

        }
        try{
            playstyle = sc.getOptionsByName("playstyle").get(0).getAsString();

        }catch(IndexOutOfBoundsException e){

        }
        try{
            weapon = sc.getOptionsByName("weapon").get(0).getAsString();

        }catch(IndexOutOfBoundsException e){

        }





        try {
            String updateRange = link.buildRange(tab,
                    "A", row+1,
                    "G", row+1);
            ValueRange newRow = link.buildRow(Arrays.asList(
                    id, sc.getMember().getUser().getAsTag(), nickname, pronoun, playstyle, weapon));
            link.updateRange(updateRange, newRow);
        } catch (IOException e) {
            log("Profile editing error "
                    + "occurred with " + sc.getMember().getAsMention() + ".", true);
            return;
        }

        sc.reply("Edited profile for " + sc.getMember().getAsMention() + ".").queue();

    }

    /**
     * Sends a embed containing the profile of the member
     * @param sc the slash command event.
     * @param user the user to get the profile of.
     */
    public void viewProfile(SlashCommandEvent sc, User user) {

        String id = user.getId();
        GoogleSheetsAPI link = getSpreadsheet();
        int row = findRow(id);
        if(row == -1){
            sc.reply("Profile does not exist for " + user.getAsMention() + ".").queue();
            return;
        }

        String nickname = null;
        String pronoun = null;
        String playstyle = null;
        String weapon = null;

        try {
            List<Object> userRow =  getSpreadsheet().getSheetValues("Profiles").get(findRow(id));
            nickname = userRow.get(2).toString();
            pronoun = userRow.get(3).toString();
            playstyle = userRow.get(4).toString();
            weapon = userRow.get(5).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }


        sc.reply("**" + user.getAsMention() + "'s Profile**").queue();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Nickname");

        eb.setDescription(nickname);
        eb.setColor(Color.BLUE);
        eb.setThumbnail(user.getEffectiveAvatarUrl());

        EmbedBuilder eb2 = new EmbedBuilder();
        eb2.setTitle("Pronoun(s)");
        eb2.setDescription(pronoun);
        eb2.setColor(Color.BLUE);
        eb2.setThumbnail(user.getEffectiveAvatarUrl());


        EmbedBuilder eb3 = new EmbedBuilder();
        eb3.setTitle("Playstyle");
        eb3.setDescription(playstyle);
        eb3.setColor(Color.BLUE);
        eb3.setThumbnail(user.getEffectiveAvatarUrl());

        EmbedBuilder eb4 = new EmbedBuilder();
        eb4.setTitle("Weapon");
        eb4.setDescription(weapon);
        eb4.setColor(Color.BLUE);
        eb4.setThumbnail(user.getEffectiveAvatarUrl());
        sc.getHook().sendMessageEmbeds(eb.build(), eb2.build(), eb3.build(), eb4.build()).queue();




    }
}

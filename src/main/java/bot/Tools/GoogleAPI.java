package bot.Tools;

import bot.Main;
import bot.Engine.PlayerStats;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    April 12, 2021
 * Project: Libra
 * Module:  GoogleAPI.java
 * Purpose: Establishes a connection with a Google Sheet
 *          through the Google API.
 */
public class GoogleAPI {

    /** Field for Google Sheets SDK. */
    private final Sheets sheetsService;

    /** ID of the Google Sheet being used. */
    private final String spreadsheetID;

    /**
     * Constructs a connection with a spreadsheet based on a provided
     * Google Sheet's ID.
     * @param id the ID of the Google Sheet.
     */
    public GoogleAPI(String id) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();
        spreadsheetID = id;
    }

    /**
     * Creates an OAuth exchange to grant application access to Google Sheets.
     * @param httpTransport the HTTP link to use in the authorization.
     * @return the authorization credential.
     */
    private Credential getCredential(NetHttpTransport httpTransport)
            throws IOException {
        // disable Google API warning
        final java.util.logging.Logger buggyLogger =
                java.util.logging.Logger.getLogger(FileDataStoreFactory.class.getName());
        buggyLogger.setLevel(java.util.logging.Level.SEVERE);

        String resourcesPath = "src/main/resources";     // for local
//        String resourcesPath = "resources";              // for JAR
        String credentialsPath = resourcesPath + "/credentials.json";
        String tokensPath = "tokens";

        List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        InputStream in = new FileInputStream(credentialsPath);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets
                .load(GsonFactory.getDefaultInstance(), new InputStreamReader(in));
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(tokensPath));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, GsonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        AuthorizationCodeInstalledApp oAuth = new AuthorizationCodeInstalledApp(
                flow, receiver);
        Credential credential = oAuth.authorize("user");

        return credential;
    }

    /**
     * Constructs the Google Sheets service link.
     * @return the service link.
     */
    private Sheets getSheetsService()
            throws IOException, GeneralSecurityException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, GsonFactory.getDefaultInstance(), getCredential(httpTransport))
                .setApplicationName(Main.NAME)
                .build();
    }

    /**
     * Retrieves the affiliated spreadsheet.
     * @return said spreadsheet.
     */
    public Sheets getSheet() {
        return sheetsService;
    }

    /**
     * Retrieves the affiliated spreadsheet's ID.
     * @return said ID.
     */
    public String getSpreadsheetID() {
        return spreadsheetID;
    }

    /**
     * Retrieves a table section of the spreadsheet.
     * @param interaction the user interaction calling this method.
     * @param tab the name of the spreadsheet section.
     * @param spreadsheet the values of the spreadsheet section.
     * @return said section as a map, indexed by Discord ID.
     *         null otherwise.
     */
    public TreeMap<Object, PlayerStats> readSection(
            GenericInteractionCreateEvent interaction,
            String tab, Values spreadsheet) {
        try {
            ValueRange spreadSheetTable = spreadsheet.get(
                    getSpreadsheetID(), tab).execute();
            List<List<Object>> values = spreadSheetTable.getValues();

            TreeMap<Object, PlayerStats> data = new TreeMap<>();
            if (values != null && !values.isEmpty()) {
                for (int i = 1; i < values.size(); i++) {
                    List<Object> row = values.get(i);
                    Object id = row.remove(0);
                    PlayerStats rowStats = new PlayerStats(
                            interaction, Integer.toString(i + 1), row);

                    data.put(id, rowStats);
                }

                return data;
            }
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("The data could not load.");
            interaction.getHook().sendMessage(
                    "The data could not load.").setEphemeral(true).queue();
        }

        return null;
    }

    /**
     * Builds a formatted range to edit values at within a spreadsheet.
     * @param tab the name of the spreadsheet section.
     * @param startColumn the column to start the edit range at.
     * @param startRow the row to start the edit range at.
     * @param endColumn the column to end the edit range at.
     * @param endRow the row to end the edit range at.
     * @return the formatted range.
     */
    public String buildRange(String tab, String startColumn, String startRow,
                      String endColumn, String endRow) {
        String updateFormat = "%s" + "!%s%s" + ":%s%s";
        return String.format(updateFormat,
                tab, startColumn, startRow, endColumn, endRow);
    }

    /**
     * Builds the row consisting of items from a list.
     * @param lst the list of items to build the row with.
     * @return the built row.
     */
    public ValueRange buildRow(List<Object> lst) {
        return new ValueRange().setValues(Collections.singletonList(lst));
    }

    /**
     * Appends a row to the end of the spreadsheet section.
     * @param tab the name of the spreadsheet section.
     * @param spreadsheet the values of the spreadsheet section.
     * @param row the row of values to append.
     */
    public void appendRow(String tab, Values spreadsheet, ValueRange row)
        throws IOException {
        spreadsheet.append(getSpreadsheetID(), tab, row)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true).execute();
    }

    /**
     * Updates a row within the spreadsheet section.
     * @param tab the name of the spreadsheet section.
     * @param spreadsheet the values of the spreadsheet section.
     * @param row the row of values to update to.
     */
    public void updateRow(String tab, Values spreadsheet, ValueRange row)
            throws IOException {
        spreadsheet.update(getSpreadsheetID(), tab, row)
                .setValueInputOption("USER_ENTERED")
                .setIncludeValuesInResponse(true).execute();
    }
}

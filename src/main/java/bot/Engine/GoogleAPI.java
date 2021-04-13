package bot.Engine;

import net.dv8tion.jda.api.entities.MessageChannel;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    April 12, 2021
 * Project: LaunchPoint Bot
 * Module:  GoogleAPI.java
 * Purpose: Establishes a connection with a Google Sheet
 *          through the Google API.
 */
public class GoogleAPI {

    /** Field for Google Sheets SDK. */
    private final Sheets sheetsService;

    /** Name of the application. */
    private static final String APPLICATION_NAME = "LaunchPoint Bot";

    /** ID of the Google Sheet being used. */
    private final String spreadsheetID;

    /**
     * Constructs a connection with a spreadsheet based on a provided
     * Google Sheet's ID.
     * @param id the ID of the Google Sheet.
     * @throws IOException ...
     * @throws GeneralSecurityException ...
     */
    public GoogleAPI(String id) throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();
        spreadsheetID = id;
    }

    /**
     * Creates an OAuth exchange to grant application access to Google Sheets.
     * @return the authorization credential.
     * @throws IOException ...
     * @throws GeneralSecurityException ...
     */
    private Credential authorize()
            throws IOException, GeneralSecurityException {
        InputStream in = Graduate.class.getResourceAsStream(
                "/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets
                .load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

        List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();

        AuthorizationCodeInstalledApp oAuth = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver());

        return oAuth.authorize("user");
    }

    /**
     * Constructs the Google Sheets service link.
     * @return the service link.
     * @throws IOException ...
     * @throws GeneralSecurityException ...
     */
    private Sheets getSheetsService()
            throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential).setApplicationName(
                APPLICATION_NAME).build();
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
     * @param inChannel the channel the command was sent in.
     * @param section the name of the spreadsheet section.
     * @param sectionVals the values of the spreadsheet section.
     * @return said section as a map, indexed by Discord tag.
     *         null otherwise.
     */
    public TreeMap<Object, PlayerStats> readSection(
            MessageChannel inChannel, String section, Values sectionVals) {
         try {
             ValueRange response = sectionVals.get(
                     getSpreadsheetID(), section).execute();
             List<List<Object>> values = response.getValues();

             TreeMap<Object, PlayerStats> table = new TreeMap<>();
             if (values != null && !values.isEmpty()) {
                 int i = 1;
                 for (List<Object> row : values) {
                     Object name = row.remove(0);
                     PlayerStats rowStats = new PlayerStats(
                             Integer.toString(i), row);
                     i++;

                     table.put(name, rowStats);
                 }
                 return table;
             }
         } catch (IOException e) {
             inChannel.sendMessage("The spreadsheet could not load.").queue();
         }

         return null;
    }

    /**
     * Appends a row to the end of the spreadsheet section.
     * @param section the name of the spreadsheet section.
     * @param sectionVals the values of the spreadsheet section.
     * @param row the row of values to append.
     */
    public void appendRow(String section, Values sectionVals, ValueRange row)
        throws IOException {
        sectionVals.append(getSpreadsheetID(), section, row)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true).execute();
    }
}

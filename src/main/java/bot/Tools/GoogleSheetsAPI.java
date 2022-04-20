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
import com.google.api.services.sheets.v4.model.*;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.util.ArrayList;
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
public class GoogleSheetsAPI {

    /** Field for a Google Sheets SDK link. */
    private final Sheets sheetsService;

    /** ID of the Google Sheet being used. */
    private final String spreadsheetID;

    /**
     * Constructs a connection with a spreadsheet based on a provided
     * Google Sheet's ID.
     * @param id the ID of the Google Sheet.
     */
    public GoogleSheetsAPI(String id) throws IOException, GeneralSecurityException {
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

//        String resourcesPath = "src/main/resources";     // for local
        String resourcesPath = "resources";              // for JAR
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

//        LocalServerReceiver receiver = new LocalServerReceiver();                                   // for local
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();     // for JAR
        AuthorizationCodeInstalledApp oAuth = new AuthorizationCodeInstalledApp(
                flow, receiver);

        return oAuth.authorize("user");
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
     * Retrieves ALL of the spreadsheets' data (including all tabs).
     * @return said values.
     */
    public Values getSheet() {
        return sheetsService.spreadsheets().values();
    }

    /**
     * Retrieves a tab's data from the spreadsheet.
     * @param tab the tab to retrieve data from.
     * @return said list of values.
     */
    public List<List<Object>> getSheetValues(String tab) throws IOException {
        return getSheet().get(getSpreadsheetID(), tab)
                .setValueRenderOption("UNFORMATTED_VALUE")
                .execute().getValues();
    }

    /**
     * Retrieves the affiliated spreadsheet's ID.
     * @return said ID.
     */
    public String getSpreadsheetID() {
        return spreadsheetID;
    }

    /**
     * Retrieves the sheet ID for the spreadsheet's tab.
     * @param tab the specific tab of the spreadsheet.
     * @return said ID.
     *         -1, otherwise.
     */
    private int getSheetID(String tab) throws IOException {
        List<Sheet> allSheets = sheetsService.spreadsheets()
                .get(getSpreadsheetID()).execute().getSheets();

        for (Sheet sheet : allSheets) {
            SheetProperties properties = sheet.getProperties();
            String title = "'" + properties.getTitle() + "'";
            if (title.equals(tab)) {
                return properties.getSheetId();
            }
        }

        return -1;
    }

    /**
     * Retrieves a specific tab of the spreadsheet, indexing
     * by the first column.
     * @param interaction the user interaction calling this method.
     * @param tab the name of the spreadsheet section.
     * @return said section as a map, indexed by Discord ID.
     *         null otherwise.
     */
    public TreeMap<Object, PlayerStats> readSection(
            GenericInteractionCreateEvent interaction, String tab) {
        try {
            List<List<Object>> values = getSheetValues(tab);

            TreeMap<Object, PlayerStats> data = new TreeMap<>();
            if (values != null && !values.isEmpty()) {
                for (int i = 1; i < values.size(); i++) {
                    List<Object> row = values.get(i);
                    Object id = row.remove(0);
                    PlayerStats rowStats = new PlayerStats(
                            interaction, i + 1, row);

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
     * Sorts a spreadsheet by descending values.
     * @param tab the spreadsheet tab to sort.
     * @param column the column to sort by.
     * @param numRows the number of rows to sort.
     */
    public void sortByDescending(String tab, String column, int numRows)  {
        int numCol = (column.charAt(0)) - 'A';

        try {
            SortSpec ss = new SortSpec();
            ss.setSortOrder("DESCENDING");
            ss.setDimensionIndex(numCol);

            GridRange gr = new GridRange();
            int sheetID = getSheetID(tab);
            if (sheetID == -1) {
                throw new IOException();
            }
            gr.setSheetId(getSheetID(tab));
            gr.setStartRowIndex(1);
            gr.setEndRowIndex(numRows);
            gr.setStartColumnIndex(0);
            gr.setEndColumnIndex(25);

            SortRangeRequest srr = new SortRangeRequest();
            srr.setRange(gr);
            srr.setSortSpecs(Collections.singletonList(ss));

            Request req = new Request();
            req.setSortRange(srr);

            BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
            busReq.setRequests(Collections.singletonList(req));
            sheetsService.spreadsheets().batchUpdate(getSpreadsheetID(), busReq).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public String buildRange(String tab, String startColumn, int startRow,
                      String endColumn, int endRow) {
        String updateFormat = "%s" + "!%s%s" + ":%s%s";
        return String.format(updateFormat,
                tab, startColumn, startRow, endColumn, endRow);
    }

    /**
     * Builds a row consisting of items from a list.
     * @param lst the list of items to populate the row with.
     * @return the built row.
     */
    public ValueRange buildRow(List<Object> lst) {
        return new ValueRange().setValues(Collections.singletonList(lst));
    }

    /**
     * Builds a column consisting of items from a list.
     * @param lst the list of items to populate the column with.
     * @return the built column.
     */
    public ValueRange buildColumn(List<Object> lst) {
        List<List<Object>> values = new ArrayList<>();
        for (Object item : lst) {
            values.add(Collections.singletonList(item));
        }

        return new ValueRange().setValues(values);
    }

    /**
     * Appends a row to the end of a spreadsheet.
     * @param tab the name of the spreadsheet tab to add to.
     * @param row the row of values to append.
     */
    public void appendRow(String tab, ValueRange row)
        throws IOException {
        getSheet().append(getSpreadsheetID(), tab, row)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true).execute();
    }

    /**
     * Updates a range of values within a spreadsheet.
     * @param range the range of values to update.
     * @param values the values to update to.
     */
    public void updateRange(String range, ValueRange values)
            throws IOException {
        getSheet().update(getSpreadsheetID(), range, values)
                .setValueInputOption("USER_ENTERED")
                .setIncludeValuesInResponse(true).execute();
    }
}

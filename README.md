# LaunchPoint Simp Design Documentation
**Contributors:** Wil Aquino

**Date:** February 17, 2021

**Last Updated:** January 10, 2022

**Table of Contents:**
* [Introduction](#introduction)
* [How to Install](#how-to-install)
* [Command Usage](#command-usage)
* [Classes and Data Structures](#classes-and-data-structures)
  - [Main](#main)
  - [Discord](#discord)
  - [Events](#events)
  - [Commands](#commands)
  + [Tools](#tools)
    - [Command](#command)
    - [FileHandler] (#filehandler)
    - [GoogleAPI](#googleapi)
    - [Time](#time)
  + [Engine](#engine)
    - [Add](#add)
    - [Graduate](#graduate)
    - [PlayerStats](#playerstats)
  + [Drafts (Engine)](#drafts-engine)
    - [Log](#log)
    - [MapGenerator] (#mapgenerator)
    - [Undo](#undo)
* [Algorithms](#algorithms)
  - [Main] (#main-1)
  - [Events](#events-1)
  - [Commands](#commands-1)
  + [Tools](#tools-1)
    - [Command](#command-1)
    - [FileHandler] (#filehandler-1)
    - [GoogleAPI](#googleapi-1)
    - [Time](#time-1)
  + [Engine](#engine-1)
    - [Add](#add-1)
    - [Graduate](#graduate-1)
    - [PlayerStats](#playerstats-1)
  + [Drafts (Engine)](#drafts-engine-1)
    - [Log](#log-1)
    - [MapGenerator] (#mapgenerator-1)
    - [Undo](#undo-1)
* [Persistence](#persistence)
* [Licensing and Rights](#licensing-and-rights)




## Introduction
LaunchPoint Simp is an official staff convenience bot (written in Java) for the Splatoon Discord server: LaunchPoint. As its official simp, not only will it simp for everyone, but it will also help everyone to as much of its capacity.




## How to Install
After pulling this project, you can import it using the <medium><a href='https://www.jetbrains.com/help/idea/gradle.html'>JetBrains' official IntelliJ Gradle documentation</a></medium>. All dependencies will be imported upon starting up the project with Gradle (often included with IntelliJ). If another IDE is being used, Gradle must be installed to import the project dependencies.

To start the bot from the console, run the following:
```
javac *
java src/main/java/bot/Main.java
```




## Command Usage
| Command | Usage | Parameters |
| :-------: | ------- | ------- |
| mit status | Checks to see if the bot is online. |
| mit help | Outputs troubleshooting information for the bot. |
| mit profile | Outputs a summary of a player's profile within MIT. |
| mit genmaps | Generates a map list for a draft. | 1. `matches` - the amount of maps needed for the amount of matches going to be played. |
| lp draftstart | Starts an automatic LaunchPoint draft. |
| lp cycle | Manually updates players' LaunchPoint Cycles stats through an affiliated spreadsheet. | 1. `games played` - the amount of games played in a set.<br />2. `score` - the amount of winning games of the set.<br />3. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br /> |
| lp sub | Manually updates subs' LaunchPoint Cycles stats through an affiliated spreadsheet. | 1. `games played` - the amount of games played in a set.<br />2. `score` - the amount of winning games of the set.<br />3. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br /> |
| lp undo | Reverts the previous LP cycle command, *once and only once*. |
| lp add | Gives players the LaunchPoint role. | 1. `users` - Discord users in the form of Discord pings. |
| lp grad | Graduates players from LaunchPoint, logging their status on an affiliated spreadsheet and replacing their "LaunchPoint" role with the "LaunchPoint Graduate" role on the Discord server. | 1. `users` - Discord users in the form of Discord pings. |
| io draftstart | Starts an automatic Ink Odyssey draft. |
| io cycle | Manually updates players' Ink Odyssey Cycles stats through an affiliated spreadsheet. | 1. `games played` - the amount of games played in a set.<br />2. `score` - the amount of winning games of the set.<br />3. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br /> |
| io sub | Manually updates subs' Ink Odyssey stats through an affiliated spreadsheet. | 1. `games played` - the amount of games played in a set.<br />2. `score` - the amount of winning games of the set.<br />3. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br /> |
| io undo | Reverts the previous IO cycle command, *once and only once*. |
| io add | Gives players the Ink Odyssey role. | 1. `users` - Discord users in the form of Discord pings. |
| io grad | Graduates players from Ink Odyssey, logging their status on an affiliated spreadsheet and replacing their "Ink Odyssey" role with the "Ink Odyssey Graduate" role on the Discord server. | 1. `users` - Discord users in the form of Discord pings. |

----




## Classes and Data Structures
#### Main

The entry point of the bot. It constructs the bot and prepares it for processing commands.

###### Instance Variables
1. `String NAME` - the name of the bot.

----

#### Discord

A class consisting of credential-specific information, pertaining to Discord and the bot's persistence. Due to the credentials of this class, the secrets have been redacted. See DiscordExample.java for a template.

----

#### Events

The class which parses through user-inputted commands, as referenced in `Usage`.

###### Instance Variables
1. `JDABuilder BOT` - an object representation of the bot.
2. `Guild SERVER` - an object representation of the Discord server.
3. `InteractionHook INTERACTION` - the original interaction made by the user.
4. `int MAX_LP_DRAFTS` - the maximum number of LaunchPoint drafts.
5. `int MAX_IO_DRAFTS` - the maximum number of Ink Odyssey drafts.
6. `List<StartDraft> lpDrafts` - a list of started LaunchPoint drafts.
7. `List<StartDraft ioDrafts` - a list of started Ink Odyssey drafts.

----

----

### Tools

#### Command

An interface outlining the format of the bot's command implementations.

----

#### FileHandler

A class which handles files (currently only for saving text).

###### Instance Variables
1. `File file` - an object representation for a text file.

----

#### GoogleAPI

A class which navigates a Google Sheet (spreadsheet).

###### Instance Variables
1. `Sheets sheetsService` - an object representation for the Google Sheets SDK.
2. `String APPLICATION_NAME` - the name of the application.
3. `String spreadsheetID` - the credential ID of the spreadsheet.

----

#### Time

A class which retrieves the system's time.

----

### Engine

#### Add

A class which gives roles to users in MIT, processing the command `lp add`.

----

#### Graduate

A class which graduates a user in a section within MIT, processing the command `lp/io grad`, granting the associated roles.

----

#### PlayerStats

A class for storing information about a Discord user.

###### Instance Variables
1. `String draftPosition` - the row position of the user's data within their associated draft spreadsheet.
2. `String name` - the user's Discord tag.
3. `String nickname` - the user's nickname on the server.
4. `int setWins` - the user's amount of won sets.
5. `int setLosses` - the user's amount of lost sets.
6. `int gamesWon` - the user's amount of won games.
7. `int gamesLost` - the user's amount of lost games.

----

#### Section

A class for parenting MIT section-specific commands.

###### Instance Variables
1. `String name` - the name of this section.
2. `String prefix` - the prefix of this section. 
3. `Color color` - the color of this section.
4. `String gradSheetID` - the graduates spreadsheet ID for this section.
5. `String cyclesSheetID` - the graduates spreadsheet ID for this section.

----

### Drafts (Engine)

#### Log

A class which updates the draft stats of a user, processing the `lp/io cycle` and `lp/io sub` commands.

----

#### MapGenerator

A class which generates map lists for drafts.

----

#### StartDraft

A class which starts a draft.

###### Instance Variables
1. `int numLPDrafts` - the number of active LaunchPoint drafts at any time.
2. `int numIODrafts` - the number of active Ink Odyssey drafts at any time.
3. `List<Member> players` - the list of players within the draft.
4. `Role draftRole` - the role to ping for this draft.

----

#### Undo

A class which reverts draft commands, processing the command `lp/io undo`.

----




## Algorithms
#### Main

###### implementSlashCommands

The `implementSlashCommands` method implements the bot's slash commands, given its built object representation `jda`.

###### main

The `main` method is the entry point of the bot's backend.

----

#### Events

###### isSimilar

The `isSimilar` method checks whether a portion of a string `input` can be found in a list of strings `lst` or not.

###### gamesPlayedValid

The `gamesPlayersValid` method checks whether there were more games won than games played (which makes no sense), of the cycle commands, given the command parameters `args`.

###### isStaffCommand

The `isStaffCommand` method checks whether a user `author`'s command `cmd` is a staff command or not.

###### wrongChannelUsed

The `wrongChannelUsed` method checks whether a command `cmd` can be used in the channel the original interaction was sent in or not.

###### printTroubleshootString

The `printTroubleshootString` method prints the script for the `help` command.

###### processDraft

The `processDraft` method starts a draft, if possible, given the inputted command's prefix `prefix`, the user who requested it `author`, and the maximum number of drafts `maxDrafts` for each draft within the running drafts `ongoingDrafts`.

###### processDrafts

The `processDrafts` method starts a draft, if possible, given the inputted command's prefix `prefix` and the user who requested it `author`.

###### findSave

The `findSave` method locates a specific undo file based on the inputted command `cmd`.

###### mentionableFor

The `mentionableFor` method formats a user `om` into a mentionable text ping.

###### saveCycleCall

The `saveCycleCall` method saves the user input `cmd` and `args` to the one of the cycle save files.

###### parseCommands

The `parseCommands` method runs a command based on the full command `cmd`, its prefix `prefix`, and its parameters/options `args`, given the user who inputted it `author` and the object representation of the command itself `sc`.

###### onSlashCommand

The `onSlashCommand` method parses through user input `sc`, checking if a slash command was used, and executing based on the command and its parameters/options.

###### onButtonClick

The `onButtonClick` method parses through clicked buttons `bc`, checking if a specific button was clicked.

----

### Tools

#### Command

###### runCmd

The `runCmd` method runs a command, given the command `cmd` and its parameters/options `args`.

###### getRole (DEFAULT)

The `retrieveRole` method retrieves the role `role` from the server.

###### addRole (DEFAULT)

The `addRole` method adds a role `role` to a user `user`.

###### removeRole (DEFAULT)

The `removeRole` method removes a role `role` to a user `user`.

###### getChannel (DEFAULT)

The `getChannel` method retrieves a text channel `channel` from the server.

###### sendReply (DEFAULT)

The `sendReply` method sends a message `msg` to the user's interaction.

###### sendEmbeds (DEFAULT)

The `sendEmbed` method replies with a list of embeds `ebs` to the user's interaction.

###### sendEmbed (DEFAULT)

The `sendEmbed` method replies with an embed `eb` to the user's interaction.

###### disableButton (DEFAULT)

The `disableButton` method disables a button that was clicked `bc`, changing its label to a new one `newLabel`.

###### sendButtons (DEFAULT)

The `sendButtons` method constructs a group of buttons with reference names `ids`, labels `labels`, and types `types` and links them to the interaction which constructed those buttons, with caption `caption`.

###### sendButton (DEFAULT)

The `sendButton` method constructs a button with reference name `id`, label `label`, and type `type` and link them to the interaction which constructed the button, with caption `caption`.

###### wait (DEFAULT)

The `wait` method pauses the program for `ms` milliseconds.

###### log (DEFAULT)

The `log` method logs a processed command message `msg` onto the console.

----

#### FileHandler

###### FileHandler

The `FileHandler` method, the class's constructor, creates a file for the handler if it does not already exist.

###### writeContents

The `writeContents` method replaces the handler's file content with the new data `contents`.

----

#### GoogleAPI

###### GoogleAPI

The `GoogleAPI` method, the class's constructor, initializes the class's instance variables.

###### authorize

The `authorize` method produces an OAuth exchange with Google for proper user access.

###### getSheetsService

The `getSheetsService` method processes an OAuth exchange and establishes a link with the spreadsheet, via the Google Sheets SDK.

###### getSheet

The `getSheet` method retrieves the object representation of the spreadsheet.

###### getSpreadsheetID

The `getSpreadSheetID` method retrieves the spreadsheet's affiliated ID.

###### readSection

The `readSection` method reads a tab `tab` of the spreadsheet `spreadsheet` and organizes it into a map, in the form of a red-black tree, indexing by Discord user ID.

###### buildRange

The `buildRange` method builds a string detailing a spreadsheet range to analyze, given the tab `tab` of the spreadsheet, the column to begin at `startColumn`, the row to begin at `startRow`, the column to end at `endColumn`, and the row to end at `endRow`.

###### buildRow

The `buildRow` method builds a spreadsheet row consisting of the objects within `lst`, in the same order.

###### appendRow

The `appendRow` method appends a row `row` to at the end of the spreadsheet `spreadsheet` of its section `tab`.

###### updateRow

The `updateRow` method updates a tab `tab` of the spreadsheet `spreadsheet` to be the given row `row`.

----

#### Time

###### currentTime

The `currentTime` method retrieves the current time of the running machine.

----

### Engine

#### Add

###### Add

The `Add` method, the class's constructor, builds the assigned add attributes given its prefix `abbreviation`.

###### enter

The `enter` method adds the section role to a user `user` and retrieves a welcome message.

###### runCmd

The `runCmd` method runs the `lp/io add` commands, given the command `cmd` and its parameters/options `args`.

----

#### Graduate

###### Graduate

The `Graduate` method, the class's constructor, builds the assigned graduation attributes given its prefix `abbreviation`.

###### graduate

The `graduate` method adds a user `user` to a spreadsheet list of MIT graduates and gives them a graduate role.

###### runCmd

The `runCmd` method runs the `lp/io grad` commands, given the command `cmd` and its parameters/options `args`.

----

#### PlayerStats

###### PlayerStats

The `PlayerStats` method, the class's constructor, initializes the class's instance variables. An error may be caught here if a formatting problem is found within the spreadsheet.

###### getDraftPosition

The `getDraftPosition` method retrieves the row number the player is located at within their associated draft spreadsheet.

###### getName

The `getName` method retrieves the user's Discord tag.

###### getNickname

The `getNickname` method retrieves the user's Discord nickname on the server.

###### getSetWins

The `getSetWins` method retrieves the user's amount of won sets.

###### getSetLosses

The `getSetLosses` method retrieves the user's amount of lost sets.

###### getGamesWon

The `getGamesWon` method retrieves the user's amount of won games.

###### getGamesLost

The `getGamesLost` method retrieves the user's amount of lost games.

----

#### Section

###### Section

The `Section` method, the class's constructor, builds the section's attributes given its prefix `abbreviation`.

###### getSection

The `getSection` method retrieves the section's name.

###### getPrefix

The `getPrefix` method retrieves the section's designated prefix.

###### getColor

The `getColor` method retrieves the section's designated color.

###### gradSheetID

The `gradSheetID` method retrieves the graduates sheet ID.

###### cyclesSheetID

The `cyclesSheetID` method retrieves the section's cycle sheet ID.

----

### Drafts (Engine)

#### Log

###### Log

The `Log` method, the class's constructor, builds the assigned cycle log attributes given its prefix `abbreviation`.

###### notSub

The `notSub` method checks if the command `cmd` was a sub command.

###### getGamesPlayed

The `getGamesPlayed` method retrieves the amount of games played, given `args`.

###### getGamesWon

The `getGamesWon` method retrieves the amount of games won, given `args`.

###### cycleSetWon

The `cycleSetWon` method checks if a cycle set was won, given the amount of games won `won` and the total amount of games played `played`.

###### sum

The `sum` method returns the sum of all values within an array `arr`, by using recursion on each index `i`.

###### sendReport

The `sendReport` method sends a summary of all errors during the match report, given the wins/losses `wins`/`losses`, color label `color`, players `players`, whether they were new or existing players `playerTypes`, and which players `errorsFound` resulted in an error.

###### updateUser

This `updateUser` method uses the GoogleAPI `link` to update the stats of a user `user`, within the spreadsheet tab `tab` with values `spreadsheet`, using a map `data`, and given the original user input command `cmd` and parameters/options `args`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### addUser

This `addUser` method uses the GoogleAPI `link` to add the stats of a user `user`, within the spreadsheet tab `tab` with values `spreadsheet`, and given the original user input command `cmd` and parameters/options `args`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### runCmd

The `runCmd` method runs the `lp/io cycle` and `lp/io sub` commands, given the command `cmd` and its parameters/options `args`.

----

#### MapGenerator

###### resetModes

The `resetModes` method resets the modes which can be chosen within the generator.

###### getListSize

The `getListSize` retrieves how many map/mode combinations to generate, given the original user input `args`.

###### getLegalMaps

The `getLegalMaps` method retrieves a list of legal maps in each game mode.

###### findMapURL

The `findMapURL` method finds the URL to an online picture of a given map `map`.

###### buildMatch

The `buildMatch` method builds a match with a map `map` and mode `mode` in the form of an embed.

###### runCmd

The `runCmd` method runs the `mit genmaps` command, given the command `cmd` and its parameters/options `args`.

----

#### StartDraft

###### StartDraft

The `StartDraft` method, the class's constructor, builds the initialized draft using the player who called for it `initialPlayer` and builds the assigned cycle log attributes given its prefix `abbreviation`.

###### getPlayers

The `getPlayers` method retrieves the players of the draft.

###### sendReport

The `sendReport` method sends a summary of the started draft labeled with color `color` and leading the players to communicate through the draft channel `channel`.

###### attemptDraft

The `attemptDraft` method attempts to start the draft after a "join draft" button `bc` was clicked.

###### runCmd

The `runCmd` method runs the `lp/io startdraft` commands, given the command `cmd` and its parameters/options `args`.

----

#### Undo

###### Undo

The `Undo` method, the class's constructor, builds the assigned cycle undo attributes given its prefix `abbreviation`.

##### retrieveLastMessage

The `retrieveLastMessage` method retrieves the previous cycle logging command from the undo file `save`.

###### checkForSub

The `checkForSub` method checks if the included players, given in `args` were subs.

###### getGamesPlayed

The `getGamesPlayed` method retrieves the amount of games played, given `args`.

###### getGamesWon

The `getGamesWon` method retrieves the amount of games won, given `args`.

###### sendReport

The `sendReport` method sends a revert summary of the previous report, given the last command input `lastInput`, the amount of user args `userArgs` within the input, and which players `errorsFound` resulted in an error.

###### undoUser

This `undoUser` method uses the GoogleAPI `link` to revert the stats of a user `user` to its previous state, within the spreadsheet tab `tab` with values `spreadsheet`, using a map `data`, and given the original user input `args`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### runCmd

The `runCmd` method runs the `lp/io undo` commands, given the command `cmd` and its parameters/options `args`.

----




## Persistence

The project saves and loads data from four Google Sheets spreadsheets, two each for the `Log` and `Graduate` classes.

These spreadsheets are connected and interacted with using the Google Sheets API, linked through the Gradle components of this project. Feature summary updates are also sent, through the channel the user originally typed commands in, by the bot using the Discord JDA API, also linked through Gradle.

Additionally, the `lp/io undo` commands allows a user to revert a cycle command, by saving and loading the previous cycle command, saved in text files.

----




## Licensing and Rights

For more info on licensing and copyright, see the information listed under licensing file `LICENSE`.

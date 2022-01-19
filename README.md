# Libra Design Documentation
**Contributors:** Wil Aquino, Turtle#1504

**Date:** February 17, 2021

**Last Updated:** January 18, 2022

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
    - [ArrayHeapMinPQ](#arrayheapminpq)
    - [ButtonBuilder](#buttonbuilder)
    - [Command](#command)
    - [FileHandler](#filehandler)
    - [GoogleAPI](#googleapi)
    - [SelectionMenuBuilder](#selectionmenubuilder)
  + [Engine](#engine)
    - [Add](#add)
    - [Graduate](#graduate)
    - [PlayerStats](#playerstats)
  + [Drafts (Engine)](#drafts-engine)
    - [AutoLog](#autolog)
    - [Draft](#draft)
    - [DraftPlayer](#draftplayer)
    - [Log](#log)
    - [MapGenerator](#mapgenerator)
    - [Undo](#undo)
* [Algorithms](#algorithms)
  - [Main](#main-1)
  - [Events](#events-1)
  - [Commands](#commands-1)
  + [Tools](#tools-1)
    - [ButtonBuilder](#buttonbuilder-1)
    - [Command](#command-1)
    - [FileHandler](#filehandler-1)
    - [GoogleAPI](#googleapi-1)
    - [SelectionMenuBuilder](#selectionmenubuilder-1)
  + [Engine](#engine-1)
    - [Add](#add-1)
    - [Graduate](#graduate-1)
    - [PlayerStats](#playerstats-1)
  + [Drafts (Engine)](#drafts-engine-1)
    - [AutoLog](#autolog-1)
    - [Draft](#draft-1)
    - [DraftPlayer](#draftplayer-1)
    - [Log](#log-1)
    - [MapGenerator](#mapgenerator-1)
    - [Undo](#undo-1)
* [Persistence](#persistence)
* [Licensing and Rights](#licensing-and-rights)




## Introduction
[ TBD ]




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
| lp startdraft | Starts an automatic LaunchPoint draft. |
| lp cycle | Manually updates players' LaunchPoint Cycles stats through an affiliated spreadsheet. | 1. `games played` - the amount of games played in a set.<br />2. `score` - the amount of winning games of the set.<br />3. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br /> |
| lp sub | Manually updates subs' LaunchPoint Cycles stats through an affiliated spreadsheet. | 1. `games played` - the amount of games played in a set.<br />2. `score` - the amount of winning games of the set.<br />3. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br /> |
| lp undo | Reverts the previous LP cycle command, *once and only once*. |
| lp add | Gives players the LaunchPoint role. | 1. `users` - Discord users in the form of Discord pings. |
| lp grad | Graduates players from LaunchPoint, logging their status on an affiliated spreadsheet and replacing their "LaunchPoint" role with the "LaunchPoint Graduate" role on the Discord server. | 1. `users` - Discord users in the form of Discord pings. |
| io startdraft | Starts an automatic Ink Odyssey draft. |
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

#### Config

A class consisting of credential-specific information, pertaining to Discord and the bot's persistence. Due to the credentials of this class, the secrets have been redacted. See ConfigExample.java for a template.

----

#### Events

The class which parses through user-inputted commands, as referenced in `Usage`.

###### Instance Variables
1. `JDABuilder BOT` - an object representation of the bot.
2. `Guild SERVER` - an object representation of the Discord server.
3. `InteractionHook INTERACTION` - the original interaction made by the user.
4. `int MAX_LP_DRAFTS` - the maximum number of LaunchPoint drafts.
5. `int MAX_IO_DRAFTS` - the maximum number of Ink Odyssey drafts.
6. `TreeMap<Integer, Draft> lpDrafts` - a map of LaunchPoint drafts to number draft/button.
7. `TreeMap<Integer, Draft> ioDrafts` - a map of Ink Odyssey drafts to number draft/button.
8. `ArrayHeapMinPQ<Integer> lpQueue` - a queue of numbered LaunchPoint drafts.
9. `ArrayHeapMinPQ<Integer> lpQueue` - a queue of numbered Ink Odyssey drafts.

----

### Tools

#### ArrayHeapMinPQ

A class which builds a heap a minimum priority queue (This class is taken from another project and is therefore not detailed here. See the class itself for more details).

----

#### ButtonBuilder

A class which builds a button quickly.

###### Instance Variables
1. `Button button` - the button that was built.


----

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
2. `String spreadsheetID` - the credential ID of the spreadsheet.

----

#### SelectionMenuBuilder

A class which builds a selection menu quickly.

###### Instance Variables
1. `SelectionMenu menu` - the menu that was built.

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
3. `String emote` - the emote of this section.
4. `Color color` - the color of this section.
5. `String gradSheetID` - the graduates spreadsheet ID for this section.
6. `String cyclesSheetID` - the graduates spreadsheet ID for this section.

----

### Drafts (Engine)

#### AutoLog

A class which manually updates the draft stats of a user.

#### Draft

A class which forms and starts drafts.

###### Instance Variables
1. `boolean started` - a flag for checking whether the draft has started or not.
2. `int numDraft` - the formal number of the draft, with respect to the draft maps in `Events`.
3. `DraftProcess draftProcess` - the formal process for the draft's execution.
4. `List<DraftPlayer> players` - the core (8) players of the draft.
5. `List<DraftPlayer> subs` - the subs of the draft, if any.
6. `int captain1` - the index of team one's captain.
7. `int captain2` - the index of team two's captain.
8. `int subsNeeded` - the amount of subs needed for the draft at any given time.
9. `Role draftRole` - the section of MIT which this draft is occurring in.
10. `TextChannel draftChat` - the draft chat which this draft is linked to.

----

#### DraftPlayer

A class which represents a player within a draft.

###### Instance Variables
1. `String id` - the Discord ID of the player within the draft.
2. `boolean active` - a flag for checking whether a player is active in the draft or not.
3. `int matchWins` - the player's match wins during the draft.
4. `int matchLosses` - the player's match losses during the draft.
5. `int pings` - the player's amount of pings during the draft.

----

#### ManualLog

A class which manually updates the draft stats of a user by processing the `lp/io cycle` and `lp/io sub` commands.

----

#### MapGenerator

A class which generates map lists for drafts.

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

The `gamesPlayersValid` method checks whether there were more games won than games played (which makes no sense), of the cycle commands, given the user inputted slash command `sc`.

###### isStaffCommand

The `isStaffCommand` method checks whether the user's inputted slash command `sc` is a staff command or not.

###### wrongChannelUsed

The `wrongChannelUsed` method checks whether a user's inputted slash command `sc` can be used in the channel the interaction was sent in or not.

###### printTroubleshootString

The `printTroubleshootString` method prints the script for the `help` command, given its slash command `sc`.

###### processDraft

The `processDraft` method starts a draft, if possible, given the inputted command's prefix `prefix` and the user `author` who requested it, logging it into the map of drafts `ongoingDrafts` and queue `queue`, given its slash command `sc`.

###### processDrafts

The `processDrafts` method starts a draft, if possible, given the inputted command's prefix `prefix`, the user who requested it `author`, and its slash command `sc`.

###### findSave

The `findSave` method locates a specific undo file based on the inputted command `cmd`.

###### mentionableFor

The `mentionableFor` method formats a user `om` into a mentionable text ping.

###### saveCycleCall

The `saveCycleCall` method saves the user input `cmd` and `args` to the one of the cycle save files.

###### parseGeneralCommands

The `parseGeneralCommands` method runs a general command based on the user inputted slash command `sc`.

###### parseSectionCommands

The `parseSectionCommands` method runs a section command based on the user inputted slash command `sc`.

###### onSlashCommand

The `onSlashCommand` method analyzes a slash command `sc`, executing based on the command and its parameters/options.

###### onButtonClick

The `onButtonClick` method parses through clicked buttons `bc`.

###### onSelectionMenu

The `onSelectionMenu` method parses through menu selections `sm`.

----

### Tools

#### ButtonBuilder

###### ButtonBuilder

The `ButtonBuilder` method, the class's constructor, builds a button given its implemented id `buttonID`, its label `buttonLabel`, a URL `url` to link if any, and what type of button it is `buttonType`.

###### getButton

The `getButton` method retrieves the built button.

----

#### Command

###### runCmd

The `runCmd` method runs a command, given its slash command `sc`.

###### extractUsers (DEFAULT)

The `extractUsers` method extracts the users of a slash command `sc`.

###### findMember (DEFAULT)

The `findMember` method retrieves a user given their Discord ID, using the interaction `interaction`.

###### getRole (DEFAULT)

The `getRole` method retrieves the role `role` from the server, using the interaction `interaction`.

###### addRole (DEFAULT)

The `addRole` method adds a role `role` to a user with Discord ID `id`, using the interaction `interaction`.

###### removeRole (DEFAULT)

The `removeRole` method removes a role `role` from a user with Discord ID `id`, using the interaction `interaction`.

###### getChannel (DEFAULT)

The `getChannel` method retrieves a text channel `channel` from the server, using the interaction `interaction`.

###### sendResponse (DEFAULT)

The `sendResponse` method responds to the user's acknowledged interaction `interaction` with message `msg`, with a field `ephemeral` to check whether to send it as an ephemeral message or not.

###### sendReply (DEFAULT)

The `sendReply` method replies to the user's interaction `interaction` with message `msg`, with a field `ephemeral` to check whether to send it as an ephemeral message or not.

###### sendMessage (DEFAULT)

The `sendMessage` method sends a message `msg` to the channel where the user's interaction `interaction` was made.

###### editMessage (DEFAULT)

The `editMessage` method replaces the user's acknowledged interaction `interaction`'s message with a new message `msg`

###### sendEmbeds (DEFAULT)

The `sendEmbeds` method edits or sends a list of embedded messages `ebs`, linked with the user's acknowledged interaction `interaction`.

###### sendEmbed (DEFAULT)

The `sendEmbed` method edits or sends an embedded message `eb`, linked with the user's acknowledged interaction `interaction`.

####### sendButtons (DEFAULT)

The `sendButtons` method edits or sends a list of buttons `buttons` with caption `caption`, linked with the user's acknowledged interaction `interaction`.

###### sendButton (DEFAULT)

The `sendButton` method edits or sends a button `button` with caption `caption`, linked with the user's acknowledged interaction `interaction`.
 
###### sendSelectionMenu (DEFAULT)

The `sendSelectionMenu` method edits or sends a selection menu `menu` with caption `caption`, linked with the user's acknowledged interaction `interaction`.

###### wait (DEFAULT)

The `wait` method pauses the program for `ms` milliseconds.

###### log (DEFAULT)

The `log` method logs a processed command message `msg` onto the console, labelling it as an error depending on a flag `isProblem`.

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

The `readSection` method reads a tab `tab` of the spreadsheet `spreadsheet` and organizes it into a map indexing by Discord user ID, given a user's acknowledged interaction `interaction`.

###### buildRange

The `buildRange` method builds a string detailing a spreadsheet range to analyze, given the tab `tab` of the spreadsheet, the column to begin at `startColumn`, the row to begin at `startRow`, the column to end at `endColumn`, and the row to end at `endRow`.

###### buildRow

The `buildRow` method builds a spreadsheet row consisting of the objects within `lst`, in the same order.

###### appendRow

The `appendRow` method appends a row `row` to at the end of the spreadsheet `spreadsheet` of its section `tab`.

###### updateRow

The `updateRow` method updates a tab `tab` of the spreadsheet `spreadsheet` to be the given row `row`.

----

#### SelectionMenuBuilder

###### SelectionMenuBuilder

The `SelectionMenuBuilder` method, the class's constructor, builds a selection menu given its implemented id `menuID`, list of labels `labels`, list of values `values`, and list of emotes `emojis`.

###### getMenu

The `getMenu` method retrieves the built menu.

----

### Engine

#### Add

###### Add

The `Add` method, the class's constructor, builds the assigned add attributes given its prefix `abbreviation`.

###### enter

The `enter` method adds the section role to a user `user` and retrieves a welcome message, given the user inputted slash command `sc`.

###### runCmd

The `runCmd` method runs the `lp/io add` commands, given its slash command `sc`.

----

#### Graduate

###### Graduate

The `Graduate` method, the class's constructor, builds the assigned graduation attributes given its prefix `abbreviation`.

###### graduate

The `graduate` method adds a user `user` to a spreadsheet list of MIT graduates and gives them a graduate role, given the user inputted slash command `sc`.

###### runCmd

The `runCmd` method runs the `lp/io grad` commands, given its slash command `sc`.

----

#### PlayerStats

###### PlayerStats

The `PlayerStats` method, the class's constructor, initializes the player's attributes given a user inputted slash command `sc`, row number `pos`, and row values `vals`, given the user's acknowledged interaction `interaction`.

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

###### getEmote

The `getEmote` method retrieves the section's designated emote.

###### getColor

The `getColor` method retrieves the section's designated color.

###### gradSheetID

The `gradSheetID` method retrieves the graduates sheet ID.

###### cyclesSheetID

The `cyclesSheetID` method retrieves the section's cycle sheet ID.

----

### Drafts (Engine)

#### AutoLog

###### AutoLog

The `AutoLog` method, the class's constructor, builds the assigned cycle log attributes given its prefix `abbreviation`.

###### updateLists

The `updateLists` method builds the stringed player lists `playerList` and `subList` of a draft `draft`, with respect to a team's player `team` and the draft's subs `subs`, given whether they were new or existing players `playerTypes`, which players `errorsFound` resulted in an error, an index `offset` to offset the `playerTypes`/`errorsFound` arrays, and the button `bc` that was clicked to call the method.

###### sendReport

The `sendReport` method sends a summary of the match report of a draft `draft` using a `ManualLog` instance `log`, given the teams `team1` and `team2`, subs `subs`, whether they were new or existing players `playerTypes`, which players `errorsFound` resulted in an error, and the button `bc` that was clicked to call the method.

###### updateSpreadsheet

This `updateUser` method uses `ManualLog` instance `lod` and a GoogleAPI `link` to update the draft `draft` stats of a team `team` and the draft's subs `subs`, within the spreadsheet tab `tab` with values `spreadsheet`, using a map `data`, given the total games played `gamesPlayed`, and games won `gameWins`, retrieving a 0 (if there were no errors) or 1 (if there were), an index `offset` to offset the `playerTypes`/`errorsFound` arrays, and the button `bc` that was clicked to call the method.

###### matchReport

The `matchReport` method reports the draft `draft`, given its button click `bc`.

----

#### Draft

###### Draft

The `Draft` method constructs a draft template, given the player who initialized it `initialPlayer`, the abbreviation of the section which the draft is occurring in `abbreivation`, the number draft `draft` it is for the section, and the user inputted slash command `sc`.

###### toggleDraft

The `toggleDraft` method activates/deactivates the draft.

###### inProgress

The `inProgress` method checks whether the draft has started yet, i.e. if the draft is activated or not.

###### getNumDraft

The `getNumDraft` method retrieves the number draft this draft is.

###### getProcess

The `getProcess` method retrieves the formal execution process of this draft.

###### getPlayers

The `getPlayers` method retrieves the core players of the draft.

###### getSubs

The `getSubs` method retrieves the subs of the draft.

###### getDraftRole

The `getDraftRole` method retrieves the role ping of the draft's section.

###### getDraftChannel

The `getDraftChannel` method retrieves the draft chat channel that the draft is linked with.

###### updateReport

The `updateReport` method edits/sends a draft queue summary of a started draft, given a button clicked `bc`.

###### newPing

The `newPing` method formats a new ping for gathering players.

###### attemptDraft

The `attemptDraft` method tries to start a draft after a "Join Draft" button `bc` was clicked.

###### draftContains

The `draftContains` method checks whether a player `player` is within a draft's list of players `lst` or not (it returns the index of their spot in the draft queue or -1 if it cannot be found), using the original button clicked `bc`.

###### convertToSub

The `convertToSub` method tries to find out if the player `player` who requested a sub is part of the draft or not, retrieving them if they were, using the original button clicked `bc`.

###### requestSub

The `requestSub` method tries to have a player request for a sub after a "Request Sub" button `bc` was clicked.

###### addSub

The `addSub` method tries to add a sub to the draft after a "Join As Sub" button `bc` was clicked.

###### removePlayer

The `removePlayer` method removes a core player from the draft queue after a "Leave" button was clicked.

###### hasEnded

The `hasEnded` method checks if the draft has finished or not after a "End Draft" button was clicked, with respect to the initial draft request.

###### runCmd

The `runCmd` method runs the `lp/io startdraft` commands, given its slash command `sc`.

###### Buttons (static class)
1. `joinDraft` - method that retrieves the "Join Draft" button.
2. `leave` - method that retrieves the "Leave" button.
3. `link` - method that retrieves the "Draft Channel" button.
4. `requestSub` - method that retrieves the "Request Sub" button.
5. `joinAsSub` - method that retrieves the "Join As Sub" button.
6. `end` - method that retrieves the "End Draft" button.

----

#### DraftPlayer

###### DraftPlayer

The `DraftPlayer` method, the class's constructor, builds the attributes of the draft player, given the player's Discord ID `playerID`.

###### getID

The `getID` method retrieves the Discord ID of the player.

###### isActive

The `isActive` method checks whether a player is currently active within the draft.

###### setInactive

The `setInactive` method sets a player's status within the draft to inactive.

###### getWins

The `getWins` method retrieves the player's match wins during the draft.

###### incrementWins

The `incrementWins` method gives a match win to the player.

###### getLosses

The `getLosses` method retrieves the player's match losses during the draft.

###### incrementLosses

The `incrementLosses` method gives a match loss to the player.

###### getPings

The `getPings` method retrieves the amount of times the player has pinged during the draft.

###### incrementPings

The `incrementPings` method increases the player's amount of pings by one.

----

#### ManualLog

###### ManualLog

The `ManualLog` method, the class's constructor, builds the assigned cycle log attributes given its prefix `abbreviation`.

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

The `sendReport` method sends a summary of the match report, given the players `players`, whether they were new or existing players `playerTypes`, which players `errorsFound` resulted in an error, and the user's inputted slash command `sc`.

###### updateUser

This `updateUser` method uses the GoogleAPI `link` to update the stats of a user `user`, within the spreadsheet tab `tab` with values `spreadsheet`, using a map `data`, and given the original user input command `cmd`, total games played `gamesPlayed`, and games won `gameWins`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### addUser

This `addUser` method uses the GoogleAPI `link` to add the stats of a user `user`, within the spreadsheet tab `tab` with values `spreadsheet`, and given the original user input command `cmd`, total games played `gamesPlayed`, and games won `gameWins`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### runCmd

The `runCmd` method runs the `lp/io cycle` and `lp/io sub` commands, given its slash command `sc`.

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

The `runCmd` method runs the `mit genmaps` command, given its slash command `sc`.

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

The `sendReport` method sends a revert summary of the previous report, given the last command input `lastInput`, the amount of user args `userArgs` within the input, and which players `errorsFound` resulted in an error, and the user inputted slash command `sc`.

###### undoUser

This `undoUser` method uses the GoogleAPI `link` to revert the stats of a user `user` to its previous state, within the spreadsheet tab `tab` with values `spreadsheet`, using a map `data`, and given the original user input `args`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### runCmd

The `runCmd` method runs the `lp/io undo` commands, given its slash command `sc`.

----




## Persistence

The project saves and loads data from four Google Sheets spreadsheets, two each for the `Log` and `Graduate` classes.

These spreadsheets are connected and interacted with using the Google Sheets API, linked through the Gradle components of this project. Feature summary updates are also sent, through the channel the user originally typed commands in, by the bot using the Discord JDA API, also linked through Gradle.

Additionally, the `lp/io undo` commands allows a user to revert a cycle command, by saving and loading the previous cycle command, saved in text files.

----




## Licensing and Rights

For more info on licensing and copyright, see the information listed under licensing file `LICENSE`.

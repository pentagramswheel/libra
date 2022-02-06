# Libra Design Documentation
**Contributors:** Wil Aquino, Turtle#1504

**Date:** February 17, 2021

**Last Updated:** February 5, 2022

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
    - [Components](#components)
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
    - [DraftProcess](#draftprocess)
    - [Log](#log)
    - [MapGenerator](#mapgenerator)
    - [Undo](#undo)
* [Persistence](#persistence)
* [Licensing and Rights](#licensing-and-rights)


## Introduction
[ TBD ]


## How to Install
After pulling this project, you can import it using the <medium><a href='https://www.jetbrains.com/help/idea/gradle.html'>JetBrains' official IntelliJ Gradle documentation</a></medium>. All dependencies will be imported upon starting up the project with Gradle (often included with IntelliJ). If another IDE or medium is being used, Gradle must be installed to import the project dependencies.

To fully configure the bot, the credentials listed in `ConfigExample.java` must be filled out in its entirety. Afterwards, the file must be renamed to `Config.java` for integration.

The main module to run is `Main.java` but to start the bot from the console, run the following:
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
| lp/io startdraft | Starts an automatic MIT draft. |
| lp/io forcesub | Forces a player within a MIT draft to become a sub. |
| lp/io forceend | Forces a MIT draft to end. |
| lp/io cycle | Manually updates players' MIT draft stats through an affiliated spreadsheet. | 1. `games played` - the amount of games played in a set.<br />2. `score` - the amount of winning games of the set.<br />3. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br /> |
| lp/io sub | Manually updates subs' MIT draft stats through an affiliated spreadsheet. | 1. `games played` - the amount of games played in a set.<br />2. `score` - the amount of winning games of the set.<br />3. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br /> |
| lp/io undo | Reverts the previous MIT draft command, *once and only once*. |
| lp/io add | Gives players a MIT draft area role. | 1. `users` - Discord users in the form of Discord pings. |
| lp/io grad | Graduates players from a MIT draft area, logging their status on an affiliated spreadsheet and replacing their current draft role with its graduate role. | 1. `users` - Discord users in the form of Discord pings. |

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
1. `int MAX_LP_DRAFTS` - the maximum number of LaunchPoint drafts.
2. `int MAX_IO_DRAFTS` - the maximum number of Ink Odyssey drafts.
3. `TreeMap<Integer, Draft> lpDrafts` - a map of numbers/buttons to LaunchPoint drafts.
4. `TreeMap<Integer, Draft> ioDrafts` - a map of numbers/buttons to Ink Odyssey drafts.
5. `ArrayHeapMinPQ<Integer> lpQueue` - a queue of numbered LaunchPoint drafts.
6. `ArrayHeapMinPQ<Integer> lpQueue` - a queue of numbered Ink Odyssey drafts.

----

### Tools

#### ArrayHeapMinPQ

A class which builds a minimum heap priority queue (This class is taken from another project and is therefore not detailed here. See the class itself for more details).

----

#### ButtonBuilder

A class which builds a button quickly.

###### Instance Variables
1. `Button button` - the button that was built.

----

#### Command

An interface outlining the format of the bot's command implementations.

----

#### Components

A class for storing components used throughout the bot.

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

A class which gives roles to users in MIT, processing the command `lp/io add`.

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
4. `int setWins` - the user's amount of won sets in a cycle.
5. `int setLosses` - the user's amount of lost sets in a cycle.
6. `int gamesWon` - the user's amount of won games in a cycle.
7. `int gamesLost` - the user's amount of lost games in a cycle.

----

#### Section

A class for parenting MIT section-specific commands.

###### Instance Variables
1. `String name` - the name of this section.
2. `String prefix` - the prefix of this section. 
3. `String role` - the role of this section.
4. `String emote` - the emote of this section.
5. `Color color` - the color of this section.
6. `String gradSheetID` - the graduates spreadsheet ID for this section.
7. `String cyclesSheetID` - the graduates spreadsheet ID for this section.

----

### Drafts (Engine)

#### AutoLog

A class which automatically updates the draft stats of a user.

#### Draft

A class which forms and starts drafts.

###### Instance Variables
1. `boolean initialized` - a flag for checking if a draft has been initialized.
2. `long startTime` - the starting time of the draft's initialization.
3. `int numDraft` - the formal number of the draft, with respect to the draft maps in `Events`.
4. `DraftProcess draftProcess` - the formal process for the draft's execution.
5. `List<DraftPlayer> players` - the original core players of the draft.
6. `List<DraftPlayer> subs` - the subs of the draft, if any.
7. `int captain1` - the index of team one's captain.
8. `int captain2` - the index of team two's captain.
9. `int subsNeededTeam1` - the amount of subs needed for the first team of the draft.
10. `int subsNeededTeam2` - the amount of subs needed for the second team of the draft.
11. `Role draftRole` - the section of MIT which this draft is occurring in.
12. `TextChannel draftChat` - the draft chat which this draft is linked to.
13. `String messageID` - the Discord message ID of the draft request.
----

#### DraftPlayer

A class which represents a player within a draft.

###### Instance Variables
1. `boolean active` - a flag for checking whether a player is active in the draft or not.
2. `int matchWins` - the player's match wins during the draft.
3. `int matchLosses` - the player's match losses during the draft.
4. `int pings` - the player's amount of pings during the draft.

----

#### DraftProcess

A class which manages and processes drafts.

###### Instance Variables
1. `boolean started` - a flag for checking if the draft has started.
2. `Draft draft` - The draft which is to be processed.
3. `DraftPlayer captain1` - The first captain of the draft.
4. `DraftPlayer captain2` - The second captain of the draft.
5. `List<DraftPlayer> regularPlayers` - The non-captain players of the draft.
6. `List<DraftPlayer> team1` - The first team of the draft.
7. `List<DraftPlayer> team2` - The second team of the draft.
8. `int MAX_SCORE` - The maximum score for a draft.
9. `int scoreTeam1` - The `team1`'s scoreboard of the draft.
10. `int scoreTeam2` - The `team2`'s scoreboard of the draft.
11. `List<String> endButtonClicked` - The players who have clicked the "End Draft" button consecutively.
12. `int NUM_PLAYERS_TO_END_DRAFT` - The number of players required to formally end the draft.

----

#### ManualLog

A class which manually updates the draft stats of a user by processing the `lp/io cycle` and `lp/io sub` commands.

----

#### MapGenerator

A class which generates map lists.

----

#### Undo

A class which reverts draft commands, processing the command `lp/io undo`.

----


## Persistence

The project saves and loads data from four Google Sheets spreadsheets, two each for the `Log` and `Graduate` classes.

These spreadsheets are connected and interacted with using the Google Sheets API, linked through the Gradle components of this project. Feature summary updates are also sent, through the channel the user originally typed commands in, by the bot using the Discord JDA API, also linked through Gradle.

Additionally, the `lp/io undo` commands allows a user to revert a cycle command, by saving and loading the previous cycle command, saved in text files.

----




## Licensing and Rights

For more info on licensing and copyright, see the information listed under licensing file `LICENSE`.

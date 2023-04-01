# Libra Design Documentation

![Libra](img/mit_libra.png)
**Author(s):** <medium><a href='https://github.com/pentagramswheel'>Wil Aquino</a></medium>

**Honorable Mention(s):** <medium><a href='https://github.com/Turtle1504cb'>Turtle</a></medium>, <medium><a href='https://github.com/kjhf'>Slate</a></medium>

**Creation Date:** February 17, 2021

**Last Updated:** April 1, 2023

**Command and Usage Documentation:** <medium><a href='https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing'>How To Use Libra - MullowayIT's Bot Documentation</a></medium>

**Table of Contents:**
- [Introduction](#introduction)
- [How to Install](#how-to-install)
- [Classes and Data Structures](#classes-and-data-structures)
  + [Main](#main)
  + [Config](#config)
  + [Events](#events)
  * [Tools](#tools)
    + [ArrayHeapMinPQ](#arrayheapminpq)
    + [Components](#components)
    + [DiscordWatch](#discordwatch)
    + [FileHandler](#filehandler)
    + [GoogleSheetsAPI](#googlesheetsapi)
    + [Builders (Tools)](#builders-tools)
      - [ButtonBuilder](#buttonbuilder)
      - [SelectionMenuBuilder](#selectionmenubuilder)
  * [Engine](#engine)
    + [Add](#add)
    + [Award](#award)
    + [DraftCup](#draftcup)
    + [Graduate](#graduate)
    + [Section](#section)
  * [Cycles (Engine)](#cycles-engine)
    + [AutoLog](#autolog)
    + [ManualLog](#manuallog)
    + [PlayerStats](#playerstats)
    + [PointsCalculator](#pointscalculator)
    + [Undo](#undo)
  * [Games (Engine)](#games-engine)
    + [Game](#game)
    + [GameProperties](#gameproperties)
    + [GameType](#gametype)
    + [MapGenerator](#mapgenerator)
    + [Player](#player)
    + [PlayerTests](#playertests)
    + [Process](#process)
    + [Team](#team)
    + [Drafts (Games)](#drafts-games)
      - [DraftGame](#draftgame)
      - [DraftPlayer](#draftplayer)
      - [DraftProcess](#draftprocess)
      - [DraftTeam](#draftteam)
    + [Minigames (Games)](#minigames-games)
      - [DraftGame](#minigame)
      - [DraftProcess](#miniprocess)
  * [Profiles (Engine)](#profiles-engine)
    + [PlayerInfo](#playerinfo)
    + [Profile](#profile)
    + [ProfileTests](#profiletests)
    + [PronounsBuilder](#pronounsbuilder)
  * [Templates (Engine)](#templates-engine)
    + [Command](#command)
    + [GameReqs](#gamereqs)
    + [ProcessReqs](#processreqs)
- [Persistence](#persistence)
- [Licensing and Rights](#licensing-and-rights)




## Introduction
Libra is a multi-functional Discord bot designed to handle many tasks, with respect to the <medium><a href='https://twitter.com/MullowayIT'>Mulloway Institute of Turfing (MIT)</a></medium>, a draft server for Nintendo's competitive shooter IP, Splatoon. Her main goal is to keep track of a player database, implement convenience features for running tournaments, and most importantly, host several automated draft systems from beginning (queuing players) to end (reporting scores).




## How to Install
After pulling this project, you can import it using the <medium><a href='https://www.jetbrains.com/help/idea/gradle.html'>JetBrains' official IntelliJ Gradle documentation</a></medium>. All dependencies will be imported upon starting up the project with Gradle.

If another IDE or medium is being used, Gradle must be installed to import the project dependencies. If you prefer Maven, the Gradle dependencies listed in <medium><a href='https://github.com/pentagramswheel/libra/blob/main/build.gradle'>build.gradle</a></medium> can be translated.

To fully configure the bot, the credentials listed in the <medium><a href='https://github.com/pentagramswheel/libra/blob/main/src/main/java/bot/ConfigExample.java'>ConfigExample.java</a></medium> class must be filled out in its entirety. Afterwards, the file must be renamed to `Config.java` for integration.

The main module to run is <medium><a href='https://github.com/pentagramswheel/libra/blob/main/src/main/java/bot/Main.java'>Main.java</a></medium> but to start the bot from the console, run the following:
```
javac *
java src/main/java/bot/Main.java
```

----




## Classes and Data Structures
#### Main

The entry point of the bot. It constructs the bot and prepares it for processing commands.

##### Instance Variables
1. `String NAME` - the name of the bot.
2. `Color mitColor` - the color of MIT.
3. `Color freshwatershoalsColor` - the color of Freshwater Shoals.
4. `Color launchpointColor` - the color of LaunchPoint.
5. `Color inkodysseyColor` - the color of Ink Odyssey.
6. `Color inkodysseygraduateColor` - the color of Ink Odyssey graduates.

----

#### Config

A class consisting of credential-specific information, pertaining to Discord and the bot's persistence. Due to the credentials of this class, the secrets have been redacted. See `ConfigExample.java` for a template.

----

#### Events

The class which parses through user-inputted commands, as referenced in `Usage`.

##### Instance Variables
1. `Random RANDOM_GENERATOR` - a random number generator for the bot. 
2. `int MAX_FS_DRAFTS` - the maximum number of Freshwater Shoals drafts.
3. `int MAX_LP_DRAFTS` - the maximum number of LaunchPoint drafts.
4. `int MAX_IO_DRAFTS` - the maximum number of Ink Odyssey drafts.
5. `TreeMap<Integer, Draft> fsDrafts` - a map of numbers/buttons to Freshwater Shoals drafts.
6. `TreeMap<Integer, Draft> lpDrafts` - a map of numbers/buttons to LaunchPoint drafts.
7. `TreeMap<Integer, Draft> ioDrafts` - a map of numbers/buttons to Ink Odyssey drafts.
8. `ArrayHeapMinPQ<Integer> fsQueue` - a queue of numbered Freshwater Shoals drafts.
9. `ArrayHeapMinPQ<Integer> lpQueue` - a queue of numbered LaunchPoint drafts.
10. `ArrayHeapMinPQ<Integer> lpQueue` - a queue of numbered Ink Odyssey drafts.

----

### Tools

#### ArrayHeapMinPQ

A class which builds a minimum heap priority queue (This class is taken from another project and is therefore not detailed here. See the class itself for more details).

----

#### Components

A class for storing components used throughout the bot.

----

#### DiscordWatch

A class used as a timer and clock for Discord.

##### Instance Variables
1. `long timerStart` - the starting time of the first timer of the watch.
2. `long timerDuration` - how long the first timer of the watch should last.
3. `long timerStart2` - the starting time of the second timer of the watch.
4. `long timerDuration2` - how long the second timer of the watch should last.

----

#### FileHandler

A class which handles files (currently only for saving text).

###### Instance Variables
1. `File file` - an object representation for a text file.

----

#### GoogleSheetsAPI

A class which navigates a Google Sheet (spreadsheet).

##### Instance Variables
1. `Sheets sheetsService` - an object representation for the Google Sheets SDK.
2. `String spreadsheetID` - the credential ID of the spreadsheet.

----

#### Builders (Tools)

##### ButtonBuilder

A class which builds a button quickly.

###### Instance Variables
1. `Button button` - the button that was built.

----

##### SelectionMenuBuilder

A class which builds a selection menu quickly.

###### Instance Variables
1. `SelectionMenu menu` - the menu that was built.

----

### Engine

#### Add

A class which enters players into MIT, processing the command `lp/io add`.

----

#### Award

A class which awards players roles within MIT, processing the command `lp/io award`.

----

#### DraftCup

A class which manages Draft Cup (tournament) work, processing the command `dc lookup`.

----

#### Graduate

A class which graduates a user in a section within MIT, processing the command `lp/io grad`, granting the associated roles.

##### Instance Variables
1. `String TAB` - the tab to reference within the profiles spreadsheet.

----

#### Section

A class for parenting MIT section-specific commands.

##### Instance Variables
1. `String name` - the name of this section.
2. `String prefix` - the prefix of this section. 
3. `String role` - the role of this section.
4. `String emote` - the emote of this section.
5. `Color color` - the color of this section.
6. `String gradSheetID` - the graduates spreadsheet ID for this section.
7. `String cyclesSheetID` - the Cycles spreadsheet ID for this section.
8. `String calculationsSheetID` - the calculations spreadsheet ID for this section.
9. `String CYCLES_TAB` - the tab to reference within the Cycles spreadsheet.
10. `String CYCLES_START_COLUMN` - the Cycles spreadsheet column that starts the needed information.
11. `String CYCLES_END_COLUMN` - the Cycles spreadsheet column that ends the needed information.

----

### Cycles (Engine)

#### AutoLog

A class which automatically updates the draft stats of a user.

----

#### ManualLog

A class which manually updates the draft stats of a user by processing the `lp/io log` and `lp/io sub` commands.

----

#### PlayerStats

A class for storing information about a player within MIT.

##### Instance Variables
1. `int numRow` - the numbered row of the player's data within their associated draft spreadsheet.
2. `String name` - the player's name.
3. `String nickname` - the player's nickname on the server.
4. `int setWins` - the player's amount of set wins.
5. `int setLosses` - the player's amount of set losses.
6. `int gamesWon` - the player's amount of won matches.
7. `int gamesLost` - the player's amount lost matches.

----

#### PointsCalculator

A class which calculates MIT leaderboard points for cycle changes.

##### Instance Variables
1. `int MINIMUM_SETS` - the number of played sets needed to accrue points.
2. `int MAX_CATGEORY_POINTS` - the maximum amount of points per scoring category.
3. `int NUM_TOTAL_SCORES` - the total scoring categories to calculate.
4. `char SCORE_COLUMNS_START` - the first column where points are inputted.

----

#### Undo

A class which reverts draft commands, processing the command `lp/io undo`.

----

### Games (Engine)

#### Game

A class which forms and starts drafts/games.

##### Instance Variables

(Generalized process S and player P)

1. `boolean initialized` - a flag for checking if a draft/game has been initialized.
2. `GameProperties properties` - the properties of the draft/game.
3. `DiscordWatch watch` - a watch to use throughout the draft/game.
4. `int numDraft` - the formal number of the draft/game, with respect to the draft maps in `Events`.
5. `TreeMap<String, P> players` - the players of the draft/game.
6. `HashSet<String> playerHistory` - a history of players which have entered the draft/game queue at any point.
7. `int numInactive` - the number of inactive players within the draft/game.
8. `TextChannel draftChat` - the draft chat which this draft/game is linked to.
9. `String messageID` - the Discord message ID of the draft/game request.
10. `S process` - the formal process for the draft/game's execution.

----

#### GameProperties

A class which represents the properties of a draft/game.

##### Instance Variables
1. `GameType gameType` - the type of the draft/game.
2. `String name` - the formal name of the draft/game.
3. `int playersPerTeam` - the number of players needed on each team.
4. `int minimumPlayersToStart` - the minimum number of players to start the draft/game.
5. `int maximumPlayersToStart` - the maximum number of players to start the draft/game.
6. `int numMatches` - the number of total matches to play in the draft/game.
7. `int rotation` - the number of matches to play before rotating teams, if any.
8. `int winningScore` - the amount of points needed to win the draft/game.
9. `int mapGens` -  the number of map generations made for the draft/game.

----

#### GameType

A class which represents the type of a draft/game.

##### Enumerations
1. `DRAFT` - a type representations for Drafts (scored).
2. `RANKED` - a type representation for Ranked games (unscored).
3. `TURF_WAR` - a type representation for Turf War games (unscored).
4. `HIDE_AND_SEEK` - a type representation for Hide and Seek games (unscored).
5. `JUGGERNAUT` - a type representation for Juggernaut games (unscored).
6. `SPAWN_RUSH` - a type representation for Spawn Rush games (unscored).

----

#### MapGenerator

##### Instance Variables
1. `GameReqs foundDraft` - a draft/game associated with the map generator.
2. `int MIN_MAPS` - the minimum number of maps to generate in a single maplist.
3. `int MAX_MAPS` - the maximum number of maps to generate in a single maplist.
4. `int MAX_DRAFT_MAPLISTS` - the maximum number of map generations for a draft/game.

----

#### Player

A class which represents a player within a draft/game.

##### Instance Variables
1. `String name` -  the name of the player.
2. `boolean active` - a flag for checking whether the player is active in the draft/game or not.
3. `boolean teamStatus` - a flag for checking the player's team status.
5. `boolean subStatus` - a flag for checking the player's sub status.
6. `int subs` - the number of times the player subbed out.

----

#### PlayerTests

A class for testing teams and players.

----

#### Process

A class which formally manages and processes drafts/games.

##### Instance Variables

(Generalized draft/game G and team T)

1. `boolean started` - a flag for checking if the draft/game has started.
2. `G game` - the draft/game to be processed.
3. `T team1, team2, team3` - the teams of the draft/game.
4. `int turn` - the current turn of the draft/game (if used).
5. `HashSet<String> endButtonClicked` - the players who have clicked the `End Draft` button consecutively.
6. `String messageID` - the Discord message ID of the draft/game request.

----

#### Team

A class which represents a team within a draft/game.

##### Instance Variables

(Generalized player P)

1. `int maxPlayers` - the maximum number of players on a team for a draft/game.
2. `TreeMap<String, P>` - the players on a team for a draft/game.
3. `int playersNeeded` - the amount of active players a team needs, at any given time, for a draft/game.
4. `int minimumScore` - a team's minimum amount of points to gain, if any.
3. `int maximumScore` - a team's maximum amount of points to gain, if any.
4. `int score` - the team's total score, if any.

----

#### Drafts (Games)

##### DraftGame

A class which forms and starts a draft, processing the command `lp/io startdraft` and handling other commands such as `lp/io forcesub`, etc.

----

##### DraftPlayer

A class which represents a player within a draft.

###### Instance Variables
1.`boolean captainStatus1, captainStatus2` - flags for checking captaincy with respect to the two teams of the draft.
2. `int minimumPoints` - the minimum amount of points a player can have.
3. `int maximumPoints` - the maximum amount of points a player can have.
4. `int matchWins` - the player's match wins during the draft.
5. `int matchLosses` - the player's match losses during the draft.

----

##### DraftProcess

A class which formally manages and processes drafts.

----

##### DraftTeam

A class which represents a team within a draft.

###### Instance Variables
1. `DraftTeam opponents` - the opponents of the team.

----

#### Minigames (Games)

##### MiniGame

A class which forms and starts a minigame, processing the command `fs startdraft` and handling other commands such as `fs forcesub`, etc.

###### Instance Variables
1.`int cappedSize` - the maximum number of players allowed in the minigame (it becomes capped if the game starts early).

----

##### MiniProcess

A class which formally manages and processes minigames.

---- 

### Profiles (Engine)

#### PlayerInfo

A class for storing information about a player within MIT.

##### Instance Variables
1. `int numRow` - the numbered row of the player's data within their associated draft spreadsheet.
2. `String tag` - the player's Discord tag.
3. `String nickname` - the player's nickname on the server.
4. `String friendcode` - the player's Nintendo Switch friend code.
5. `String pronouns` - the player's preferred pronouns.
6. `String playstyle` - the player's preferred playstyle.
7. `String weapons` - the player's preferred main weapons.
8. `String rank` - the player's average rank in-game.
9. `String team` - the player's competitive team, if any.

----

#### Profile

A class which manages the profile database of MIT.

##### Instance Variables
1. `String spreadsheetID` - the profiles spreadsheet ID.
2. `String START_COLUMN` - the starting information column of the profiles spreadsheet.
3. `String END_COLUMN` - the ending information column of the profiles spreadsheet.
4. `String TAB` - the tab to reference within the profiles spreadsheet.
5. `Pattern FC_PATTERN` - a pattern for friend codes to strictly follow.
6. `Pattern PRONOUNS_PATTERN` - a pattern for pronouns to strictly follow.
7. `Pattern WEAPONS_PATTERN` - a pattern for weapons to strictly follow.

----

#### ProfileTests

A class for testing various aspects of a profile.

----

#### PronounsBuilder

A class for constructing gramattical pronouns.

##### Instance Variables
1. `Pattern HE_REGEX` - a pattern for matching pronouns with the phrase "he" in it.
2. `Pattern SHE_REGEX` - a pattern for matching pronouns with the phrase "she" in it.
3. `Pattern THEY_REGEX` - a pattern for matching pronouns with the phrase "they" in it.
4. `Pattern IT_REGEX` - a pattern for matching pronouns with the phrase "it" in it.
5. `Pattern ALL_REGEX` - a pattern for matching pronouns with the phrase "all" or "any" in it.
6. `Pattern ASK_REGEX` - a pattern for matching pronouns with the phrase "ask" (for pronouns) in it.
7. `List<String> subjects` - subjects for a person.
8. `List<String> objects` - objects for a person.
9. `List<String> possessivePronouns` - possessive pronouns for a person.
10. `List<String> possessiveAdjectives` - possessive adjectives for a person.

----

### Templates (Engine)

#### Command

An interface outlining the format of the bot's command implementations.

----

#### GameReqs

A template for classes which forms and starts draft/games.

----

#### ProcessReqs

A template for classes which formally manages and processes draft/games.

----




## Persistence

The project saves and loads data from Google Sheets spreadsheets, for the <medium><a href='https://github.com/pentagramswheel/libra/blob/main/src/main/java/bot/Engine/Cycles/ManualLog.java'>ManualLog</a></medium>, <medium><a href='https://github.com/pentagramswheel/libra/blob/main/src/main/java/bot/Engine/Graduate.java'>Graduate</a></medium>, <medium><a href='https://github.com/pentagramswheel/libra/blob/main/src/main/java/bot/Engine/Cycles/PointsCalculator.java'>PointsCalculator</a></medium>, and <medium><a href='https://github.com/pentagramswheel/libra/blob/main/src/main/java/bot/Engine/Profiles/Profile.java'>Profile</a></medium> classes.

These spreadsheets are connected and interacted with using the Google Sheets API, linked through the Gradle components of this project. Feature summary updates are also sent, through the channel the user originally typed commands in, by the bot using the Discord JDA API, also linked through Gradle.

The `lp/io undo` commands allows a user to revert a cycle command, by saving and loading the previous cycle command, saved in text files.

The `lp/io cyclescalc` commands allow a user to perform a Cycle Top 10 calculation, while updating the section leaderboards.

The `mit profile` commands allow a user to create and modify their official MIT profile.

The <medium><a href='https://github.com/pentagramswheel/libra/blob/main/badwords.txt'>badwords.txt</a></medium> text file, partially created by <medium><a href='https://github.com/nantonakos/badwords'>nantonakos</a></medium>, is referenced when finding profanity within phrases.

----




## Licensing and Rights

For more info on licensing and copyright, see the information listed under licensing file <medium><a href='https://github.com/pentagramswheel/libra/blob/main/LICENSE'>LICENSE</a></medium>.

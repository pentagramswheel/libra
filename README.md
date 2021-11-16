# LaunchPoint Simp Design Documentation
**Contributors:** Wil Aquino

**Date:** February 17, 2021

**Last Updated:** August 10, 2021

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
    - [GoogleAPI](#googleapi)
    - [Time](#time)
  + [Engine](#engine)
    - [Add](#add)
    - [Graduate](#graduate)
    - [PlayerStats](#playerstats)
  + [Drafts (Engine)](#drafts-engine)
    - [Log](#log)
    - [Undo](#undo)
* [Algorithms](#algorithms)
  - [Events](#events-1)
  - [Commands](#commands-1)
  + [Tools](#tools-1)
    - [Command](#command-1)
    - [GoogleAPI](#googleapi-1)
    - [Time](#time-1)
  + [Engine](#engine-1)
    - [Add](#add-1)
    - [Graduate](#graduate-1)
    - [PlayerStats](#playerstats-1)
  + [Drafts (Engine)](#drafts-engine-1)
    - [Log](#log-1)
    - [Undo](#undo-1)
* [Persistence](#persistence)
* [Licensing and Rights](#licensing-and-rights)




## Introduction
LaunchPoint Simp is an official staff convenience bot (written in Java) for the Splatoon Discord server: LaunchPoint. As its official simp, not only will it simp for everyone, but it will also help everyone to as much of its capacity.




## How to Install
After pulling this project, you can import it using the <medium><a href='https://www.jetbrains.com/help/idea/gradle.html'>JetBrains' official IntelliJ Gradle documentation</a></medium>. All dependencies will be imported upon starting up the project with Gradle (often included with IntelliJ). If another IDE is being used, Gradle must be installed to import the project dependencies.

To start the bot from the console, run the following:
```
java src/main/java/bot/Main.java
```




## Command Usage
| Command | Usage | Parameters |
| :-------: | ------- | ------- |
| lphelp | Outputs a list of the bot's commands. |
| lphelp? | Output troubleshooting information for the bot's commands. |
| lpstatus | Checks to see if the bot is online. |
| lpcycle | Updates players' LaunchPoint Cycles stats through an affiliated spreadsheet. | 1. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br />2. `games played` - the amount of games played in a set.<br />3. `score` - the amount of winning games of the set.
| lpsub | Updates subs' LaunchPoint Cycles stats through an affiliated spreadsheet. | 1. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.<br />2. `games played` - the amount of games played in a set.<br />3. `score` - the amount of winning games of the set. |
| lpundo | Reverts the previous draft command, *once and only once*. |
| lpadd | Gives roles to players in LaunchPoint. | 1. `users` - Discord users in the form of Discord pings. |
| lpgrad | Graduates players from LaunchPoint, logging their status on an affiliated spreadsheet and replacing their "LaunchPoint" role with the "LaunchPoint Graduate" role on the Discord server. | 1. `users` - Discord users in the form of Discord pings. |
| lpexit | Remotely shuts down the bot, by terminating its program. |

----




## Classes and Data Structures
#### Main

The entry point of the bot's implementation. It constructs the bot and prepares it for processing commands.

----

#### Discord

A class consisting of credential-specific information, pertaining to Discord and the bot's persistence. Due to the credentials of this class, the secrets have been redacted. See DiscordExample.java for a template.

----

#### Events

The class which parses through user-inputted commands, as referenced in `Usage`.

###### Instance Variables
1. `JDABuilder BOT` - an object representation of the bot.
2. `Guild SERVER` - an object representation of the Discord server.
3. `MessageChannel ORIGIN` - the original channel the user-inputted command was sent in.

----

#### Commands

The class which formally runs the commands.

----

### Tools

#### Command

An interface outlining the format of the bot's command implementations.

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

A class which gives roles to users in LaunchPoint, processing the command `lpadd`.

###### Instance Variables
1. `Role lpRole` - an object representation of the 'LaunchPoint' role.
2. `Role coachRole` - an object representation of the 'Coaches' role.

----

#### Graduate

A class which graduates a user from LaunchPoint, processing the command `lpgrad`, granting the associated roles.

###### Instance Variables
1. `Role lpRole` - an object representation of the 'LaunchPoint' role.
2. `Role gradRole` - an object representation of the 'LaunchPoint Graduate' role.

----

#### PlayerStats

A class for storing information about a Discord user.

###### Instance Variables
1. `String positionLP` - the row position of the user's data within the LaunchPoint spreadsheet.
2. `String name` - the user's Discord tag.
3. `String nickname` - the user's nickname on the server.
4. `int setWins` - the user's amount of won sets.
5. `int setLosses` - the user's amount of lost sets.
6. `int gamesWon` - the user's amount of won games.
7. `int gamesLost` - the user's amount of lost games.

----

### Drafts (Engine)

#### Log

A class which updates the LaunchPoint Cycles stats of a user, processing the command `lpcycle`.

----

#### Undo

A class which reverts LaunchPoint Cycle commands, processing the command `lpundo`.

----




## Algorithms
#### Events

###### printArgsError

The `printArgsError` method prints an error message when a command has the incorrect arguments.

###### argsValid

The `argsValid` method checks whether a command has the incorrect amount of arguments.

###### cycleFormatInvalid

The `cycleFormatInavlid` method checks whether the `lpcycle` or `lpsub` command has the correct amount of total parameters `totalArgs` and correct amount of total users `userArgs`.

###### gamesPlayedInvalid

The `gamesPlayersInvalid` method checks whether the parameter `games played` was not switched with the `score` parameter, of the `lpcycle` or `lpsub` commands, , specified as the first item of `args`.

###### cycleArgsValid

The `cycleArgsValid` method checks whether the `lpcycle` or `lpsub` command, specified as the first item of `args`, was called with the correct format, given `users`.

###### printHelpString

The `printHelpString` method prints the script for the `lphelp` command.

###### printTroubleshootString

The `printTroubleshootString` method prints the script for the `lphelp?` command.

###### onMessageReceived

The `onMessageReceieved` method parses through user input `e`, checking if a command was used, and executing based on the command, if any.

----

#### Commands

###### saveContents

The `saveContents` method saves the user input `args` to the `load.txt` file.

###### runCyclesCmd

The `runCyclesCmd` method formally runs the `lpcycle` or `lpsub` command, loading users `players` and user input `args`.

###### runUndoCmd

The `runUndoCmd` method formally runs the `lpundo` command, saving the user input to a file.

###### runAddCmd

The `runAddCmd` method formally runs the `lpadd` or `lpcoach` command, loading users `players` and user input `args`.

###### runGradCmd

The `runGradCmd` method formally runs the `lpgrad` command, loading users `players`.

###### runExitCmd

The `runExitCmd` method shuts down the bot.

----

### Tools

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

The `readSection` method reads a section `tab` of the spreadsheet `spreadsheet` and organizes it into a map, in the form of a red-black tree, indexing by Discord user ID.

###### appendRow

The `appendRow` method appends a row `row` to at the end of the spreadsheet `spreadsheet` of its section `tab`.

###### updateRow

The `updateRow` method updates a section `tab` of the spreadsheet `spreadsheet` to be the given row `row`.

----

#### Time

###### currentTime

The `currentTime` method retrieves the current time of the running machine.

----

### Engine

#### Command

###### runCmd

The `runCmd` method runs a command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.

###### getRole (DEFAULT)

The `retrieveRole` method retrieves the role `role` from the server.

###### addRole (DEFAULT)

The `addRole` method adds a role `role` to a user `user`.

###### removeRole (DEFAULT)

The `removeRole` method removes a role `role` to a user `user`.

###### sendToDiscord (DEFAULT)

The `sendToDiscord` method sends a message `msg` to the origin channel.

###### wait (DEFAULT)

The `wait` method pauses the program for `ms` milliseconds.

###### log (DEFAULT)

The `log` method logs a processed command message `msg` onto the console.

----

#### Add

###### enter

The `enter` method adds the "LaunchPoint" role to a user `user` and retrieves a welcome message.

###### coach

The `coach` method adds the "Coaches" role to a user `user`, and retrieves a welcome message.

###### runCmd

The `runCmd` method runs the `lpadd` or `lpcoach` command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.

----

#### Graduate

###### graduate

The `graduate` method adds a user `user` to a spreadsheet list of LaunchPoint graduates and gives them the "LaunchPoint Graduate" role.

###### runCmd

The `runCmd` method runs the `lpgrad` command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.

----

#### PlayerStats

###### PlayerStats

The `PlayerStats` method, the class's constructor, initializes the class's instance variables. An error may be caught here if a formatting problem is found within the spreadsheet.

###### getPositionLP

The `getPositionLP` method retrieves the row number the player is located at within the LaunchPoint spreadsheet.

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

### Drafts (Engine)

#### Log

###### checkForSub

The `checkForSub` method checks if the included players, given in `args` were subs.

###### getGamesPlayed

The `getGamesPlayed` method retrieves the amount of games played, given `args`.

###### getGamesWon

The `getGamesWon` method retrieves the amount of games won, given `args`.

###### cycleSetWon

The `cycleSetWon` method checks if a cycle set was won, given the amount of won games `won` and the total amount of games played `played`.

###### sum

The `sum` method returns the sum of all values within an array `arr`, by using recursion on each index `i`.

###### sendReport

The `sendReport` method sends a summary of all errors during the match report, given the wins/losses `wins`/`losses`, players `players`, whether they were new or existing players `playerTypes`, and which players `errorsFound` resulted in an error.

###### updateUser (overloaded)

This `updateUser` method uses the GoogleAPI `link` to update the stats of a user `user`, detecting if they were a sub or not via the flag `notSub`, within the spreadsheet tab `tab` with values `spreadsheet`, using a map `data`, and given the original user input `args`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### updateUser

This `updateUser` method calls its overloaded self with a parameter detecting if the user was a sub or not.

###### addUser (overloaded)

This `addUser` method uses the GoogleAPI `link` to add the stats of a user `user`, detecting if they were a sub or not via the flag `notSub`, within the spreadsheet tab `tab` with values `spreadsheet`, and given the original user input `args`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### addUser

This `addUser` method calls its overloaded self with a parameter detecting if the user was a sub or not.

###### runCmd

The `runCmd` method runs the `lpcycle` or `lpsub` command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.

----

#### Undo

##### retrieveLastMessage

The `retrieveLastMessage` method retrieves the previous cycle logging command.

###### sendReport

The `sendReport` method sends a revert summary of the previous report, given the last command input `lastInput`, the amount of user args `userArgs` within the input, and which players `errorsFound` resulted in an error.

###### undoUser (overloaded)

This `undoUser` method uses the GoogleAPI `link` to revert the stats of a user `user` to its previous state, detecting if they were a sub or not via the flag `notSub`, within the spreadsheet tab `tab` with values `spreadsheet`, using a map `data`, and given the original user input `args`, retrieving a 0 (if there were no errors) or 1 (if there were).

###### undoUser

This `undoUser` method calls its overloaded self with a parameter detecting if the user was a sub or not.

###### runCmd

The `runCmd` method runs the `lpundo` command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.

----




## Persistence

The project saves and loads data from two Google Sheets spreadsheets, one each for the `Log` and `Graduate` classes.

These spreadsheets are connected and interacted with using the Google Sheets API, linked through the Gradle components of this project. Feature summary updates are also sent, through the channel the user originally typed commands in, by the bot using the Discord JDA API, also linked through Gradle.

Additionally, the `lpundo` command allows a user to revert a cycle command, by saving and loading the previous cycle command, logged into a text file `load.txt`.

----




## Licensing and Rights

For more info on licensing and copyright, see the information listed under licensing file `LICENSE`.

# LaunchPoint Simp Design Document
**Contributors:** Wil Aquino

**Date:** February 17, 2021

## Introduction
LaunchPoint Simp is an official staff convenience bot for the Splatoon Discord server: LaunchPoint. As its official simp, not only will it simp for everyone, but it will also help everyone to as much of its capacity.

##Usage##
**lphelp**

Outputs a list of the bot's commands.

----

**lpcycle**

Updates a player's LaunchPoint Cycles stats through an affiliated spreadsheet.

**Parameters**
1. `user` - a Discord user in the form of a Discord ping; one or four users can be given.
2. `games played` - the amount of games played in a set.
3. `score` - the amount of winning games of the set.

----

**lpgrad**

Graduates a player from LaunchPoint, logging their status on an affiliated spreadsheet and giving them the "LaunchPoint Graduate" role on the Discord server.

**Parameters**
1. `user` - a Discord user in the form of a Discord ping.



## Classes and Data Structures
**Main**

The entry point of the bot's implementation. It constructs the bot and prepares it for processing commands.


----

**Discord**

A class consisting of credential-specific information, pertaining to Discord and the bot's persistence.


----

**Events**

The class which parses through user-inputted commands, as referenced in `Usage`.

**Instance Variables**
1. `JDABuilder BOT` - an object representation of the bot.
2. `Guild SERVER` - an object representation of the Discord server.
3. `MessageChannel ORIGIN` - the original channel the user-inputted command was sent in.


----

**GoogleAPI (Engine)**

A class which navigates a Google Sheet (spreadsheet).

**Instance Variables**
1. `Sheets sheetsService` - an object representation for the Google Sheets SDK.
2. `String APPLICATION_NAME` - the name of the application.
3. `String spreadsheetID` - the credential ID of the spreadsheet.


----

**Command (Engine)**

An interface outlining the format of the bot's command implementations.


----

**PlayerStats (Engine)**

A class for storing information about a Discord user.

**Instance Variables**
1. `String position` - the row position of the user's data.
2. `List<Object> stats` - the user's actual stats.


----

**CyclesLog (Engine)**

A class which updates the LaunchPoint Cycles stats of a user, processing the command `lpcycle`.


----

**Graduate (Engine)**


A class which graduates a user from LaunchPoint, processing the command `lpgrad`.



## Algorithms
**Events**

**checkArgs**

The `checkArgs` method ensures a command, specified as the first item of `args`, consists of `n`-1 parameters.

**cycleArgsValid**

The `cycleArgsValid` method ensures the `lpcycle` command, specified as the first item of `args`, consists of the correct amount of parameters and users in `users`.

**getHelpString**

The `getHelpString` method retrieves the script for the `lphelp` command.

**runCyclesCmd**

The `runCyclesCmd` method formally runs the `lpcycle` command, loading users `players` and user input `args`.

**runGradCmd**

The `runGradCmd` method formally runs the `lpgrad` command, loading a user `player`.

**onMessageReceived**

The `onMessageReceieved` method parses through user-input `e`, checking if a command was used, and executing based on the command, if any.


----

**GoogleAPI (Engine)**

**GoogleAPI**

The `GoogleAPI` method, the class's constructor, initializes the class's instance variables.

**authorize**

The `authorize` method produces an OAuth exchange with Google for proper user access.

**getSheetsService**

The `getSheetsService` method processes an OAuth exchange and establishes a link with the spreadsheet, via the Google Sheets SDK.

**getSheet**

The `getSheet` method retrieves the object representation of the spreadsheet.

**getSpreadsheetID**

The `getSpreadSheetID` method retrieves the spreadsheet's affiliated ID.

**readSection**

The `readSection` method reads a section `section` of values `vals` from the spreadsheet and organizes it into a map, in the form of a red-black tree.

**appendRow**

The `appendRow` method appends a row `row` to at the end of the values `vals` of a section `section` of the spreadsheet.

**updateRow**

The `updateRow` method updates a section `section` of the spreadsheet's values `vals` to be the given row `row`.


----

**Command (Engine)**

**runCmd**

The `runCmd` method runs a command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.

**sendToDiscord (DEFAULT)**

The `sendToDiscord` method sends a message `msg` to the origin channel.


----

**PlayerStats (Engine)**

**PlayerStats**

The `PlayerStats` method, the class's constructor, initializes the class's instance variables.

**getPosition**

The `getPosition` method retrieves the row number the player is located at within the spreadsheet.

**getStats**

The `getStats` method retrieves the user's actual stats.


----

**CyclesLog (Engine)**

**cycleSetWon**

The `cycleSetWon` method checks if a cycle set was won, given the amount of won games `won` and the total amount of games played `played`.

**updateUser**

The `updateUser` method uses the GoogleAPI `link` to update the stats of a user `user`, at location `range` within the spreadsheet values `tableVals`, using a map `table`, given the original user input `args`.

**addUser**

The `updateUser` method uses the GoogleAPI `link` to add the stats of a user `user`, at location `range` within the spreadsheet values `tableVals`, given the original user input `args`.

**runCmd**

The `runCmd` method runs a command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.


----

**Graduate (Engine)**

**addRole**

The `addRole` method adds the "LaunchPoint Graduate" role to a user `user` within the Discord server.

**graduateUser**

The `graduateUser` method adds a user `user` to a spreadsheet list of LaunchPoint graduates.

**runCmd**

The `runCmd` method runs a command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.



## Persistence

The project saves and loads data from two Google Sheets spreadsheets, one each for the `CyclesLog` and `Graduate` classes.

These spreadsheets are connected and interacted with using the Google Sheets API, linked through the Gradle components of this project. Feature summary updates are also sent, through the origin channel the user originally typed commands in, by the bot using the Discord JDA API, also linked through Gradle.
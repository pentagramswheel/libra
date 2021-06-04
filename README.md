# LaunchPoint Simp Design Documentation
**Contributors:** Wil Aquino

**Date:** February 17, 2021

## Introduction
LaunchPoint Simp is an official staff convenience bot for the Splatoon Discord server: LaunchPoint. As its official simp, not only will it simp for everyone, but it will also help everyone to as much of its capacity.



## Command Usage
**lphelp**

Outputs a list of the bot's commands.

----

**lpstats**

Checks to see if the bot is online.

----

**lpcycle**

Updates players' LaunchPoint Cycles stats through an affiliated spreadsheet.

**Parameters**
1. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.
2. `games played` - the amount of games played in a set.
3. `score` - the amount of winning games of the set.

----

**lpsub**

Updates subs' LaunchPoint Cycles stats through an affiliated spreadsheet.

**Parameters**
1. `users` - a list of Discord users in the form of Discord pings; up to four users can be given.
2. `games played` - the amount of games played in a set.
3. `score` - the amount of winning games of the set.

----

**lpundo**

Reverts the previous draft command, once and only once.

----

**lpadd**

Gives roles to players in LaunchPoint.

**Parameters**
1. `users` - Discord users in the form of Discord pings.

----

**lpgrad**

Graduates players from LaunchPoint, logging their status on an affiliated spreadsheet and replacing their "LaunchPoint" role with the "LaunchPoint Graduate" role on the Discord server.

**Parameters**
1. `users` - Discord users in the form of Discord pings.



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
2. `String name` - the user's Discord tag.
3. `String nickname` - the user's nickname on the server.
4. `int setWins` - the user's amount of won sets.
5. `int setLosses` - the user's amount of lost sets.
6. `int gamesWon` - the user's amount of won games.
7. `int gamesLost` - the user's amount of lost games.


----

**CyclesLog (Engine)**

A class which updates the LaunchPoint Cycles stats of a user, processing the command `lpcycle`.


----

**Add (Engine)**

A class which gives roles to users in LaunchPoint, processing the command `lpadd`.

----

**Graduate (Engine)**


A class which graduates a user from LaunchPoint, processing the command `lpgrad`, granting the associated roles.



## Algorithms
**Events**

**printArgsError**

The `printArgsError` method prints an error message when a command has the incorrect arguments.

**argsValid**

The `argsValid` method checks whether a command has the incorrect amount of arguments.

**cycleFormatInvalid**

The `cycleFormatInavlid` method checks whether the `lpcycle` or `lpsub` command has the correct amount of total parameters `totalArgs` and correct amount of total users `userArgs`.

**gamesPlayedInvalid**

The `gamesPlayersInvalid` method checks whether the parameter `games played` was not switched with the `score` parameter, of the `lpcycle` or `lpsub` commands, , specified as the first item of `args`.

**cycleArgsValid**

The `cycleArgsValid` method checks whether the `lpcycle` or `lpsub` command, specified as the first item of `args`, was called with the correct format, given `users`.

**argAmtValid**

The `argAmtValid` method checks whether ping commands have the correct amount of parameters, given the original `args` and its `users`.

**printHelpString**

The `printHelpString` method prints the script for the `lphelp` command.

**saveContents**

The `saveContents` method saves the user input `args` to the `load.txt` file.

**runCyclesCmd**

The `runCyclesCmd` method formally runs the `lpcycle` or `lpsub` command, loading users `players` and user input `args`.

**runUndoCmd**

The `runUndoCmd` method formally runs the `lpundo` command, saving the user input to a file.

**runAddCmd**

The `runAddCmd` method formally runs the `lpadd` or `lpcoach` command, loading users `players` and user input `args`.

**runGradCmd**

The `runGradCmd` method formally runs the `lpgrad` command, loading users `players`.

**onMessageReceived**

The `onMessageReceieved` method parses through user input `e`, checking if a command was used, and executing based on the command, if any.


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

The `readSection` method reads a section `section` of values `vals` from the spreadsheet and organizes it into a map, in the form of a red-black tree, indexing by Discord user ID.

**appendRow**

The `appendRow` method appends a row `row` to at the end of the values `vals` of a section `section` of the spreadsheet.

**updateRow**

The `updateRow` method updates a section `section` of the spreadsheet's values `vals` to be the given row `row`.


----

**Command (Engine)**

**runCmd**

The `runCmd` method runs a command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.

**getRole (DEFAULT)**

The `retrieveRole` method retrieves the role `role` from the server.

**addRole (DEFAULT)**

The `addRole` method adds a role `role` to a user `user`.

**removeRole (DEFAULT)**

The `removeRole` method removes a role `role` to a user `user`.

**sendToDiscord (DEFAULT)**

The `sendToDiscord` method sends a message `msg` to the origin channel.


----

**PlayerStats (Engine)**

**PlayerStats**

The `PlayerStats` method, the class's constructor, initializes the class's instance variables.

**getPosition**

The `getPosition` method retrieves the row number the player is located at within the spreadsheet.

**getName**

The `getName` method retrieves the user's Discord tag.

**getNickname**

The `getNickname` method retrieves the user's Discord nickname on the server.

**getSetWins**

The `getSetWins` method retrieves the user's amount of won sets.

**getSetLosses**

The `getSetLosses` method retrieves the user's amount of lost sets.

**getGamesWon**

The `getGamesWon` method retrieves the user's amount of won games.

**getGamesLost**

The `getGamesLost` method retrieves the user's amount of lost games.


----

**CyclesLog (Engine)**

**checkForSub**

The `checkForSub` method checks if the included players, given in `args` were subs.

**getGamesPlayed**

The `getGamesPlayed` method retrieves the amount of games played, given `args`.

**getGamesWon**

The `getGamesWon` method retrieves the amount of games won, given `args`.

**cycleSetWon**

The `cycleSetWon` method checks if a cycle set was won, given the amount of won games `won` and the total amount of games played `played`.

**updateUser (overloaded)**

This `updateUser` method uses the GoogleAPI `link` to update the stats of a user `user`, detecting if they were a sub or not via a flag `notSub`, at location `range` within the spreadsheet values `tableVals`, using a map `table`, given the original user input `args`.

**updateUser**

This `updateUser` method calls its overloaded self with a parameter detecting if the user was a sub or not.

**addUser (overloaded)**

This `addUser` method uses the GoogleAPI `link` to add the stats of a user `user`, detecting if they were a sub or not via a flag `notSub`, at location `range` within the spreadsheet values `tableVals`, given the original user input `args`.

**addUser**

This `addUser` method calls its overloaded self with a parameter detecting if the user was a sub or not.

**runCmd**

The `runCmd` method runs the `lpcycle` or `lpsub` command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.


----

**Add (Engine)**

**enterUser**

The `enterUser` method adds the "LaunchPoint" role to a user `user`, and retrieves a welcome message.

**coachUsers**

The `coachUsers` method adds the "Coaches" role to a user `user`, and retrieves a welcome message.

**runCmd**

The `runCmd` method runs the `lpadd` or `lpcoach` command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.


----

**Graduate (Engine)**

**graduateUser**

The `graduateUser` method adds a user `user` to a spreadsheet list of LaunchPoint graduates and gives them the "LaunchPoint Graduate" role.

**runCmd**

The `runCmd` method runs the `lpgrad` command and outputs the result in a channel `outChannel`, the origin channel otherwise, given a list of users `users` and the original user input `args`.



## Persistence

The project saves and loads data from two Google Sheets spreadsheets, one each for the `CyclesLog` and `Graduate` classes.

These spreadsheets are connected and interacted with using the Google Sheets API, linked through the Gradle components of this project. Feature summary updates are also sent, through the channel the user originally typed commands in, by the bot using the Discord JDA API, also linked through Gradle.

Additionally, the `lpundo` command allows a user to revert a cycle command, by saving and loading the previous cycle command, logged into a text file `load.txt`.
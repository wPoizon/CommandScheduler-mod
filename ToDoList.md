 
 ## VERSION 1.0:

 ### HELP PAGE 1:
 ✅ `/commandscheduler` - should simply send a help menu of other commands

 ✅ `/commandscheduler help [page]`

 ✅ `/commandscheduler forcereload` - reloads the config files
 
 ### HELP PAGE 2:

 ✅ `/commandscheduler new interval <id> <unit> <interval> <command>`

 ✅ `/commandscheduler new atboot <id> <command>`

 ✅ `/commandscheduler new clockbased <id> <command>`
 
 ### HELP PAGE 3:

 ✅ `/commandscheduler list active` - should list all active scheduled commands

 ✅ `/commandscheduler list inactive` - should list all inactive scheduled commands

 ✅ `/commandscheduler list interval [page]`

 ✅ `/commandscheduler list clockbased [page]`
 
 ✅ `/commandscheduler list atboot [page]`

 ✅ `/commandscheduler details <id>` - gives all information about this scheduled command

 ### HELP PAGE 4:

 ✅ `/commandscheduler activate <id>`

 ✅ `/commandscheduler deactivate <id>`

 ✅ `/commandscheduler rename <id> <new id>`

 ✅ `/commandscheduler description <id> <description>` - adds a description

 ✅ `/commandscheduler addtime <id> <time>` - adds a time to when the scheduled command should run

 ✅ `/commandscheduler removetime <id> <time>` - removes a time for when the scheduled command shouldn't run

 ✅ `/commandscheduler remove <id>`

-----
 
 ### FUNCTIONALITY AND OTHER NOTES:

 I have tested everything and confirmed functionality for 1.20.2 for both singleplayer and for servers! The only quirk for singleplayer being that it seems that the atboot commands only runs the first time on that runtime. If you leave, wait and then rejoin, it does NOT send a new atboot command. If you however restart your game, it does successfully send a new atboot command.

 I also tested singleplayer functionality for 1.21.4 and everything did seem to work. It is likely it will be fully compatible for 1.21.5 however I must first test multiplayer functionality before being sure.
 
 I tested singleplayer functionality for 1.19.2 and it did boot, schedulers successfully ran and all logic seemed to work, although it seems that printouts don't really work. When for example running "/commandscheduler help" absolutely nothing shows. This is likely related to how 1.19 handles ServerCommandSource.sendFeedback().

 This likely means that I'll only initially release 1.0 for Minecraft versions between 1.20 and 1.21.5. I might at a later date make a new repository to adapt 1.0 for older versions, unless I start working on 1.1 of the mod first. We'll see.

----

 ## FOR UPCOMING VERSIONs:

 ❌ `/commandscheduler new singlefire <id> <date> <time> <command>` - A new type of scheduler! Fire the command once on a specific time stamp, and then delete itself. Requires new config file.
 
 ❌ `/commandscheduler list singlefire` - These should have pages! 

 ❌ `/commandscheduler addcommand <id> <command>` - The ability to have multiple commands run after each other on the same scheduler!
 
 ❌ `/commandscheduler removecommand <index>` - Removes a command from a scheduler
    
  ^ Implementing these also means we have to remove the `<command>` section entirely from the command to create any new scheduler. 

  This also makes it so the constructors need to be reworked, as they need a `<command>`...

  The removecommand could also give a confirmation like "Are you sure you want to remove this command: `[show command]`"

 💡 `/commandscheduler details <id>` - Already exists, but modify to nicely list all commands, with an associated number to simplify removing them

 💡 Add option to prevent OP's from being able modify or add new schedulers. Basically make it server-console or file editing only to add or modify. OPs would still however be able to view the current scheduler setups.
# FindPlayer
 A Minecraft Bukkit/Spigot plugin that can locate a player's location. This plugin utilizes a MYSQL database to store
 player's locations after they've left a Minecraft server. You can set database credentials in the config.yml file.
 
 ## Commands
 Currently there are 2 commands:
 <br>**/findp \[player\]** - This will return a player's coordinates, this works without a database for players who are
 currently in-game. With a database it can also return an offline player's coordinates.
 <br>**/findplist** - Returns a list of all players who have joined the game since the plugin was added to the server. A database is required for this command.
 
 ## Permissions
 - FindPlayer.findp
 - FindPlayer.findplist
 
 If you have any ideas for additional commands or you've found a bug feel free to send me an email at ellienntatar@outlook.com
 
 Thank you!
 
 Ellienn

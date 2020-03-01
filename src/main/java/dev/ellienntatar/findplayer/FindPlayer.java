package dev.ellienntatar.findplayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;



public class FindPlayer extends JavaPlugin implements Listener {
	private Connection connection;
	private String host, database, username, password;
	private int port;
	FileConfiguration config;
	Statement statement;
	Logger logger;
	
	@Override
    public void onEnable() { 
		logger = getLogger();
		logger.info("onEnable has been invoked!");
		saveDefaultConfig();
		config = getConfig();
		this.getCommand("findp").setExecutor(this);
		getServer().getPluginManager().registerEvents(this, this);
		
		host = config.getString("host");
	    port = 3306;
	    database = config.getString("database");
	    username = config.getString("username");
	    password = config.getString("password");   
	    try {    
	        openConnection();
	        statement = connection.createStatement();
	        statement.execute("CREATE TABLE IF NOT EXISTS playerLocations ("
	        		+ "playerName VARCHAR(255) PRIMARY KEY,"
	        		+ "playerLocation VARCHAR(255),"
	        		+ "playerWorld VARCHAR(255)"
	        		+ ");");
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	@Override
	public void onDisable() {
		logger.info("onDisable has been invoked!");
		try {
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (cmd.getName().equalsIgnoreCase("findp")) {
            	if(args.length == 1) {
            		Player p = Bukkit.getPlayer(args[0]);
            		//if the player searched for is in game
	    			if(p != null) {
	    				Location l = p.getLocation(); 
	    				String world = switchWorld(p.getWorld().getName());
	    				sender.sendMessage(ChatColor.AQUA + "Player "+ ChatColor.BLUE + "\""+  p.getName()  + "\""+ ChatColor.AQUA + " coordinates are: "+ ChatColor.BLUE + l.getBlockX() + ", "+ l.getBlockY() + ", " + l.getBlockZ()  + " \n(in: "+world+ ChatColor.BLUE + ")");
	    				return true;
	    			//player searched for is not in game, must search the database
	    			} else {
	    				if(statement != null) {
	    					try {
		    					ResultSet set = statement.executeQuery("SELECT playerLocation, playerWorld FROM playerLocations WHERE playerName = '"+args[0]+"'");
		    					boolean b = set.next();
		    					if(b) {
		    						sender.sendMessage(ChatColor.AQUA +"Player " + ChatColor.BLUE + "\""+  args[0]  + "\"" + ChatColor.AQUA + " last coordinates are: "+ ChatColor.BLUE + set.getString("playerLocation")  + " \n(in: " +set.getString("playerWorld")+ ChatColor.BLUE + ")");
		    						return true;
		    					}else {
		    						sender.sendMessage( ChatColor.RED + "Can't find a player by the name of "+ ChatColor.GOLD + "\"" + args[0] + "\"" + ChatColor.RED + " in the database, did you make a typo?");
		    					}
	    					} catch (SQLException e) {
	    						e.printStackTrace();
	    					}
	    				} else {
	    					sender.sendMessage(ChatColor.RED + "Error! No connection to database made, did you input the right values in the config file?");
	    				}
	    			}
            	} else {
            		sender.sendMessage(ChatColor.RED + "Error! No player name entered, did you forget to type something?");
            	}
            
            } else if(cmd.getName().equalsIgnoreCase("findplist")) {
            	if(statement != null) {
            		try {
            			ResultSet set = statement.executeQuery("SELECT playerName FROM playerLocations ORDER BY playerName");
            			String res = ChatColor.UNDERLINE + "List of all players who have joined the server before:\n" + ChatColor.RESET + " \n";
            			int i = 0;
            			while(set.next()) {
            				res += ChatColor.LIGHT_PURPLE + "" + ++i + ". " + ChatColor.GOLD + set.getString("playerName") + "\n";
            			}
            			sender.sendMessage(res);
            		} catch (SQLException e) {
            			e.printStackTrace();
            		}
            	} else {
					sender.sendMessage(ChatColor.RED + "Error! No connection to database made, did you input the right values in the config file?");
            	}
            }
    	return true; 
    }
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
	  if(statement != null) {
		  Player player = event.getPlayer();
		  Location loc = player.getLocation();
		  String world = switchWorld(player.getWorld().getName());
		  try {
			  String s = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
			  //update insert statement
			  statement.execute(
					  "INSERT INTO playerLocations (playerName, playerLocation, playerWorld)"
					  + "VALUES ('"+player.getName()+"', '"+s+"', '"+ world +"')"
					  + "ON DUPLICATE KEY UPDATE playerLocation = '"+s+"', playerWorld = '" + world + "'"
					  );
			  logger.info("Player \""+player.getName()+"\" location inserted or updated.");
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
	  } else {
		  logger.info("Connection to database not made! Cannot store player's last location.");
	  }
	  
	}
	
	public String switchWorld(String world) {
		String s;
		switch(world) {
	  		case "world":
	  			s = ChatColor.GREEN + "Overworld";
	  			break;
	  		case "world_nether":
	  			s = ChatColor.RED + "Nether";
	  			break;
	  		case "world_the_end":
	  			s = ChatColor.YELLOW + "End";
	  			break;
	  		default:
	  			s = "ERROR";
		}
		return s;
	}
	
	public void openConnection() throws SQLException, ClassNotFoundException {
	    if (connection != null && !connection.isClosed()) {
	        return;
	    }
	 
	    synchronized (this) {
	        if (connection != null && !connection.isClosed()) {
	            return;
	        }
	        Class.forName("com.mysql.jdbc.Driver");
	        connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
	    }
	}
}

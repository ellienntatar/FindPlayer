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
	        		+ "playerLocation VARCHAR(255)"
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
	    				sender.sendMessage(ChatColor.AQUA + "Player "+ ChatColor.BLUE + "\""+  p.getName()  + "\""+ ChatColor.AQUA + " coordinates are: "+ ChatColor.BLUE + l.getBlockX() + ", "+ l.getBlockY() + ", " + l.getBlockZ());
	    				return true;
	    			//player searched for is not in game, must search the database
	    			} else {
	    				if(statement != null) {
	    					try {
		    					ResultSet set = statement.executeQuery("SELECT playerLocation FROM playerLocations WHERE playerName = '"+args[0]+"'");
		    					boolean b = set.next();
		    					if(b) {
		    						sender.sendMessage(ChatColor.AQUA +"Player " + ChatColor.BLUE + "\""+  args[0]  + "\"" + ChatColor.AQUA + " last coordinates are: "+ ChatColor.BLUE + set.getString("playerLocation"));
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
            
            }
    	return true; 
    }
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
	  Player player = event.getPlayer();
	  Location loc = player.getLocation();
	  if(statement != null) {
		  try {
			  String s = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
			  //update insert statement
			  statement.execute(
					  "INSERT INTO playerLocations (playerName, playerLocation)"
					  + "VALUES ('"+player.getName()+"', '"+s+"')"
					  + "ON DUPLICATE KEY UPDATE playerLocation = '"+s+"'"
					  );
			  logger.info("Player \""+player.getName()+"\" location inserted or updated.");
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
	  } else {
		  logger.info("Connection to database not made! Cannot store player's last location.");
	  }
	  
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

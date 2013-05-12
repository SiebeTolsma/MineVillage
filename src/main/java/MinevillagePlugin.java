package net.bot2k3.siebe.Minevillage;

import java.lang.reflect.*;
import java.util.*;

import net.bot2k3.siebe.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.*;
import org.bukkit.scheduler.*;

/**
 * Provides the main plugin interface.
 */
public class MinevillagePlugin extends JavaPlugin
{
    private MinecraftServer server;

    /**
     * Occurs when the plugin is being enabled.
     */
    public void onEnable()
    {       
        this.server = new MinecraftServer(this.getLogger());
    }

    /**
     * Occurs when the plugin is being disabled.
     */
    public void onDisable()
    {
    }
    
    /**
     * Occurs when a command has been sent.
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        
        // get the player from the server.
        String playerName = sender.getName(); 
        Player player = this.getServer().getPlayerExact(playerName);
        
        if (player != null)
        {
            String name = command.getName();
            
            if (name.equals("village")) 
            {
                if (!this.server.isAvailable())
                {
                    // there is no server available.
                    player.sendMessage("Sorry, this command is not available at the moment.");
                    return true;
                }
        
                this.handleVillageCommand(player);
                return true;
            }
        }

        return false;
    }
    
    private void handleVillageCommand(Player player)
    {
        Location loc = player.getLocation();
        
        World world = loc.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL)
        {
            // um, there no villages in your weird world.
            player.sendMessage(ChatColor.RED + "Please use the /village command in the Overworld.");
            return;
        }
        
        // get the village at the given location.
        Object village = this.server.getVillage(
            (int)loc.getX(),
            (int)loc.getY(),
            (int)loc.getZ());
        
        if (village != null)
        {
            // get the village information.
            MinecraftVillage info = this.server.getVillageDetails(village, player.getName());
            if (info != null)
            {
                String message = 
                    ChatColor.YELLOW + "Found village! Here is the information:\n"  +
                    ChatColor.GREEN + "    Location (Center): " + ChatColor.WHITE + "X=" + info.center.getX() + ", Y=" + info.center.getY() + ", Z=" + info.center.getZ() + "\n" +
                    ChatColor.GREEN + "    Size (Radius): " + ChatColor.WHITE + info.size + "\n" +
                    ChatColor.GREEN + "    Population: " + ChatColor.WHITE + info.population + "\n" +
                    ChatColor.GREEN + "    Doors:\n";
                
                for (int i = 0; i < info.doors.length; i++)
                {
                    message += ChatColor.GREEN + "        " + i + ": " + ChatColor.WHITE;
                    message += "X=" + info.doors[i].getX() + ", ";
                    message += "Y=" + info.doors[i].getY() + ", ";
                    message += "Z=" + info.doors[i].getZ() + "\n";
                }
                
                message += ChatColor.GREEN + "    Standing/Popularity: " + ChatColor.WHITE + info.standing;
                
                // we're done building it, send it!
                player.sendMessage(message);
                    
                return;
            }
        }
        
        // nope, no village here!
        player.sendMessage(ChatColor.AQUA + "You don't appear to be in or near a known village.");
    }
}


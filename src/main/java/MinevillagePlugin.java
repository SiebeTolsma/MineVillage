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
                if (this.checkServerAvailable(player))
                {
                    this.handleVillageCommand(player);
                }
                
                return true;
            }
            else if (name.equals("villagedoors"))
            {
                if (this.checkServerAvailable(player))
                {
                    // there is no server available.
                    this.handleVillageDoorsCommand(player);
                }
                
                return true;
            }
        }

        return false;
    }
    
    private boolean checkServerAvailable(Player player)
    {
        if (!this.server.isAvailable())
        {
            player.sendMessage("Sorry, this command is not available at the moment.");
            return false;
        }
        
        return true;
    }
    
    private boolean checkWorld(Player player, Location loc, String cmd)
    {
        World world = loc.getWorld();
        
        if (world.getEnvironment() != World.Environment.NORMAL)
        {
            // um, there no villages in your weird world.
            player.sendMessage(ChatColor.RED + "Please use the " + ChatColor.DARK_RED + "/" + cmd + ChatColor.RED + " command in the Overworld.");
            return false;
        }
        
        return true;
    }
    
    private void handleVillageCommand(Player player)
    {
        Location loc = player.getLocation();
        
        if (this.checkWorld(player, loc, "village"))
        {
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
                        ChatColor.GREEN + "  Location (Center): " + ChatColor.GRAY + "X=" + ChatColor.WHITE + info.center.getX() + ChatColor.GRAY + ", Y=" + ChatColor.WHITE + info.center.getY() + ChatColor.GRAY + ", Z=" + ChatColor.WHITE + info.center.getZ() + "\n" +
                        ChatColor.GREEN + "  Size (Radius): " + ChatColor.WHITE + info.size + "\n" +
                        ChatColor.GREEN + "  Population: " + ChatColor.WHITE + info.population + "\n" +
                        ChatColor.GREEN + "  Doors: " + ChatColor.WHITE + info.doors.length + ChatColor.YELLOW + " (Tip: " + ChatColor.AQUA + "/villagedoors" + ChatColor.YELLOW + ")\n" +
                        ChatColor.GREEN + "  Standing/Popularity: " + ChatColor.WHITE + info.standing;
                    
                    // we're done building it, send it!
                    player.sendMessage(message);
                        
                    return;
                }
            }
            
            // nope, no village here!
            player.sendMessage(ChatColor.AQUA + "You don't appear to be in or near a known village.");
        }
    }
    
    
    private void handleVillageDoorsCommand(Player player)
    {
        Location loc = player.getLocation();
        
        if (this.checkWorld(player, loc, "villagedoors"))
        {
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
                    String message = ChatColor.YELLOW + "Found village! Here are the doors:\n";
                       
                    for (int i = 0; i < info.doors.length; i++)
                    {
                        message += ChatColor.GREEN + "  " + i + ": ";
                        message += ChatColor.GRAY + "X=" + ChatColor.WHITE + info.doors[i].getX();
                        message += ChatColor.GRAY + ", Y=" + ChatColor.WHITE + info.doors[i].getY();
                        message += ChatColor.GRAY + ", Z=" + ChatColor.WHITE + info.doors[i].getZ() + "\n";
                    }
                    
                    player.sendMessage(message);
                        
                    return;
                }
            }
            
            // nope, no village here!
            player.sendMessage(ChatColor.AQUA + "You don't appear to be in or near a known village.");
        }
    }
}


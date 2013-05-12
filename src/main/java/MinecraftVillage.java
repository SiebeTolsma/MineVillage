package net.bot2k3.siebe.Minevillage;

import org.bukkit.util.*;

public class MinecraftVillage
{
    /**
     * The center of the village.
     */
    public Vector center;
    
    /**
     * The size.
     */
    public int size;
    
    /**
     * The population.
     */
    public int population;
    
    /**
     * The players popularity/standing.
     */
    public int standing;
    
    /**
     * The doors.
     */
    public Vector[] doors;
    
    /**
     * Initializes a new instance of the MinecraftVillage class.
     */
    public MinecraftVillage(Vector center, int size, int population, Vector[] doors, int standing)
    {
        this.center = center;
        this.size = size;
        this.population = population;
        this.doors = doors;
        this.standing = standing;
    }
}

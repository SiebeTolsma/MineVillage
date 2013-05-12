package net.bot2k3.siebe.Minevillage;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import org.bukkit.util.*;

public class MinecraftServer
{
    private Logger logger;
    private Object server;
    private String version;
    
    /**
     * Initializes a new instance of the MinecraftServer class.
     */
    public MinecraftServer(Logger logger)
    {
        this.logger = logger;
        this.version = "";
        this.initialize();
    }
    
    /**
     * Checks whether the Minecraft server can be inspected.
     */
    public boolean isAvailable()
    {
        return this.server != null;
    }
    
    /**
     * Gets the overworld.
     */
    public Object getOverworld()
    {
        if (this.isAvailable())
        {
            return this.invokeMethod(
                this.server.getClass(),
                "getWorldServer",
                this.server,
                new Class[] { int.class },
                new Object[] { 0 });
        }
        
        return null;
    }
    
    /**
     * Gets the village at the given location.
     */
    public Object getVillage(int x, int y, int z)
    {
        Object world = this.getOverworld();
        if (world != null)
        {
            // just look for all villages, get the closest one (yes, we're cheating a bit here, but whatever.)
            Object villages = this.getFieldValue(
                world.getClass(), 
                "villages",
                world);
                
            if (villages != null)
            {
                // locate the nearest village to our coordinates.
                return this.invokeMethod(
                    villages.getClass(),
                    "getClosestVillage",
                    villages,
                    new Class[] { int.class, int.class, int.class, int.class },
                    new Object[] { x, y, z, 32 });        
            }
            else
            {
                // no such collection?
                this.logger.severe("FAILED TO GET VILLAGE COLLECTION.");
            }
        }
        else
        {
            // whoops, failed to get the overworld.
            this.logger.severe("FAILED TO GET OVERWORLD.");
        }
        
        return null;
    }
    
    /**
     * Gets the village details.
     */
    public MinecraftVillage getVillageDetails(Object village, String playerName)
    {
        Class<?> villageClass = village.getClass();
        
        Object objCenter = this.invokeMethod(
            villageClass,
            "getCenter",
            village);
        
        Object objSize = this.invokeMethod(
            villageClass,
            "getSize",
            village);
            
        Object objPopulation = this.invokeMethod(
            villageClass,
            "getPopulationCount",
            village);
            
        Object objStanding = this.invokeMethod(
            villageClass,
            "a",
            village,
            new Class[] { String.class },
            new Object[] { playerName });
        
        return new MinecraftVillage(
            objCenter == null ? null : this.getVector(objCenter), 
            this.toInt(objSize),
            this.toInt(objPopulation), 
            this.getVillageDoors(village),
            this.toInt(objStanding));
    }
    
    /**
     * Gets the doors in a village.
     */
    public org.bukkit.util.Vector[] getVillageDoors(Object village)
    {
        Object objDoors = this.invokeMethod(
            village.getClass(),
            "getDoors",
            village);
            
        if (objDoors != null)
        {
            // for each door, get the information.
            // (we know this is a List, so we can safely cast and walk over it.)
            List listDoors = (List)objDoors;
            
            // set up the output list.
            org.bukkit.util.Vector[] doors = new org.bukkit.util.Vector[listDoors.size()];
            
            for (int i = 0; i < doors.length; i++)
            {
                // store the door.
                Object door = listDoors.get(i);
                Class<?> doorClass = door.getClass();
                
                int x = this.toInt(this.getFieldValue(doorClass, "locX", door));
                int y = this.toInt(this.getFieldValue(doorClass, "locY", door));
                int z = this.toInt(this.getFieldValue(doorClass, "locZ", door));
                
                doors[i] = new org.bukkit.util.Vector(x, y, z);
            }
            
            return doors;
        }
        
        this.logger.severe("FAILED TO GET DOORS.");
        
        return new org.bukkit.util.Vector[0];
    }
    
    /**
     * Gets a vector from a Coordinates object.
     */
    public org.bukkit.util.Vector getVector(Object coords)
    {
        Class<?> coordClass = coords.getClass(); 
        
        Object objX = this.getFieldValue(coordClass, "x", coords);
        Object objY = this.getFieldValue(coordClass, "y", coords);
        Object objZ = this.getFieldValue(coordClass, "z", coords);
        
        int x = this.toInt(objX);
        int y = this.toInt(objY);
        int z = this.toInt(objZ);
        
        return new org.bukkit.util.Vector(x, y, z);
    }
    
    private void initialize()
    {
        Class<?> nms  = this.getClass("MinecraftServer");
        if (nms != null)
        {
            this.server = this.invokeMethod(nms, "getServer");
        }
    }
    
    private Integer toInt(Object o)
    {
        return o == null ? -1 : (Integer)o;
    }
    
    private Class<?> getClass(String name)
    {
        // attempt to locate it using our current version.
        Class<?> c = this.getVersionedClass(name, this.version);
        if (c != null)
        {
            return c;
        }
        
        for (int major = 0; major < 10; major++)
        {
            for (int minor = 0; minor < 10; minor++)
            {
                for (int rev = 0; rev < 10; rev++)
                {
                    String version = "v" + major + "_" + minor + "_R" + rev;
                    
                    // attempt to get the class for this version.
                    c = this.getVersionedClass(name, version);
                    if (c != null)
                    {
                        // found it, consider this version "good".
                        this.version = version;
                        return c;
                    }
                }
            }
        }
        
        // whoops, failed to find that class :-(
        this.logger.severe("FAILED TO FIND CLASS: " + name);
        
        return null;
    }
    
    private Class<?> getVersionedClass(String name, String version)
    {
        try
        {
            if (version.isEmpty())
            {
                return Class.forName("net.minecraft.server." + name);
            }
            else
            {
                return Class.forName("net.minecraft.server." + version + "." + name);
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    private Method getMethod(Class<?> cls, String name, Class<?>[] args)
    {
        try
        {
            Method method = cls.getMethod(name, args);
            method.setAccessible(true);
            return method;
        }
        catch (Exception e)
        {
            this.logger.severe("EXCEPTION in getMethod(" + name + "): " + e.toString());
            return null;
        }
    }
    
    private Field getField(Class<?> cls, String name)
    {
        try
        {
            Field field = cls.getField(name);
            field.setAccessible(true);
            return field;
        }
        catch (Exception e)
        {
            this.logger.severe("EXCEPTION in getField(" + name + "): " + e.toString());
            return null;
        }
    }
    
    private Object invokeMethod(Class<?> cls, String name)
    {
        return this.invokeMethod(cls, name, null, null, null);
    }
    
    private Object invokeMethod(Class<?> cls, String name, Class<?>[] args, Object[] argv)
    {
        return this.invokeMethod(cls, name, null, args, argv);
    }
    
    private Object invokeMethod(Class<?> cls, String name, Object inst)
    {
        return this.invokeMethod(cls, name, inst, null, null);
    }
    
    private Object invokeMethod(Class<?> cls, String name, Object inst, Class<?>[] args, Object[] argv)
    {
        Method method = this.getMethod(cls, name, args);
        if (method != null)
        {
            try
            {
                // found the method, invoke it with the given arguments.
                return method.invoke(inst, argv);
            }
            catch (Exception e)
            {
                // aw, we failed to invoke the method :-(
                this.logger.severe("EXCEPTION in invokeMethod(" + name + "): " + e.toString());
            }
        }
        
        return null;
    }
    
    private Object getFieldValue(Class<?> cls, String name)
    {
        return this.getFieldValue(cls, name, null);
    }
    
    private Object getFieldValue(Class<?> cls, String name, Object inst)
    {
        Field field = this.getField(cls, name);
        if (field != null)
        {
            try
            {
                return field.get(inst);
            }
            catch (Exception e)
            {
                // failed to get the value from the field :-(
                this.logger.severe("EXCEPTION in getFieldValue(" + name + "): " + e.toString());
            }
        }
        
        return null;
    }
}

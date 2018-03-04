/// GameEntities
///
/// Purpose: Store a "template" version of each IWObject used in the game
///
/// Entities created here will have their parameters read from .dat files.
///
/// When new entities need to be created, instead of reading those files again,
///   a clone of the corresponding entity in this class is created.
/// 
/// This class must be initialized as an object within Game, and must be initialized
///   after the Game has read settings.ini
///
/// IMPORTANT NOTE: When adding a new entity to the game, the entity's filename (not
///   including extension) MUST be added on a new line in data\entities.dat

import java.io.*;
import java.util.ArrayList;

class GameEntities {

   Game main;
	public static ArrayList<String> mobNames = new ArrayList<String>();
	public static ArrayList<IWBattler> mobs = new ArrayList<IWBattler>();
	public static ArrayList<String> projectileNames = new ArrayList<String>();
	public static ArrayList<IWProjectile> projectiles = new ArrayList<IWProjectile>();
	public static ArrayList<String> wallNames = new ArrayList<String>();
	public static ArrayList<IWWall> walls = new ArrayList<IWWall>();
	public static ArrayList<String> itemNames = new ArrayList<String>();
	public static ArrayList<IWPickup> items = new ArrayList<IWPickup>();
	
	public GameEntities(Game p) {
      this.main = p;
		BufferedReader file; String names = "";
      
      File folder; String name;
      
      /// Read all .dat in data/entities
      folder = new File("data/entities/");
      for (File f : folder.listFiles()) {
         if (f.getName().endsWith(".dat")) {
            name = f.getName().substring(0,f.getName().indexOf(".dat"));
            if (name.startsWith("MOB_") || name.startsWith("BOSS_") || name.equals("PLAYER")) {
               mobNames.add(name);
               mobs.add(new IWBattler(name,0.0,0.0,this.main,null));
            } else if (name.startsWith("PROJECTILE_")) {
               projectileNames.add(name);
               projectiles.add(new IWProjectile(name,0.0,0.0,this.main,null));
            }
         }
      }
      
      /// Read all .dat except TEMPLATE.dat in data/pickups
      folder = new File("data/pickups/");
      for (File f : folder.listFiles()) {
         if (f.getName().endsWith(".dat")) {
            name = f.getName().substring(0,f.getName().indexOf(".dat"));
            if (!name.equals("TEMPLATE")) {
               itemNames.add(name);
               items.add(new IWPickup(name,0.0,0.0,this.main));
            } 
         }
      }
      
      /// Read all .dat except TEMPLATE.dat in data/doodads
      folder = new File("data/doodads/");
      for (File f : folder.listFiles()) {
         if (f.getName().endsWith(".dat")) {
            name = f.getName().substring(0,f.getName().indexOf(".dat"));
            if (!name.equals("TEMPLATE")) {
               wallNames.add(name);
               walls.add(new IWWall(name,0.0,0.0,this.main));
            } 
         }
      }
	}
	
	/// Finds the index of an entity with a specific ID in a given ArrayList
	public static int findIndexInNames(String s, ArrayList<String> e) {
		for (int i = 0; i < e.size(); i++)
			if (e.get(i).equals(s)) return i;
		return -1;
	}
	
	/// Takes an entity ID and returns its index in the appropriate
	/// ArrayList, or returns an invalid index if the ID is empty
	public int getIDIndex(String s, ArrayList<String> e) {
		if (s.equals("0")) return -1;
		if (s.equals("")) return -1;
		return findIndexInNames(s,e);
	}
	
	/// Get a reference to the "template" versions of each entity
	/// using their index in the appropriate ArrayList
	
		public IWBattler getIWBattler(int i) {
			if (i < 0 || i >= mobs.size()) return null;
			return mobs.get(i);
		}
		public IWProjectile getIWProjectile(int i) {
			if (i < 0 || i >= projectiles.size()) return null;
			return projectiles.get(i);
		}
		public IWWall getIWWall(int i) {
			if (i < 0 || i >= walls.size()) return null;
			return walls.get(i);
		}
		public IWPickup getIWPickup(int i) {
			if (i < 0 || i >= items.size()) return null;
			return items.get(i);
		}
	
	/// Get a reference to the "template" versions of an entity using its ID
	
		public IWBattler getIWBattlerByID(String s) {
			int i = getIDIndex(s,mobNames);
			return getIWBattler(i);
		}
		public IWProjectile getIWProjectileByID(String s) {
			int i = getIDIndex(s,projectileNames);
			return getIWProjectile(i);
		}
		public IWWall getIWWallByID(String s) {
			int i = getIDIndex(s,wallNames);
			return getIWWall(i);
		}
		public IWPickup getIWPickupByID(String s) {
			int i = getIDIndex(s,itemNames);
			return getIWPickup(i);
		}
	
	/// Get a clone of an entity that has the same characteristics
	/// as the "template" version but has its own positioning/gameplay
	/// values - position, AI, HP, velocity, etc.
	/// Uses the entity's index in the appropriate ArrayList
	
		public IWBattler getIWBattlerClone(int i) {
			if (i < 0 || i >= mobs.size()) return null;
			return new IWBattler(mobs.get(i));
		}
		public IWProjectile getIWProjectileClone(int i) {
			if (i < 0 || i >= projectiles.size()) return null;
			return new IWProjectile(projectiles.get(i));
		}
		public IWWall getIWWallClone(int i) {
			if (i < 0 || i >= walls.size()) return null;
			return new IWWall(walls.get(i));
		}
		public IWPickup getIWPickupClone(int i) {
			if (i < 0 || i >= items.size()) return null;
			return new IWPickup(items.get(i));
		}
	
	/// Get a new copy of an entity using its ID
	/// Uses the getIWObjectClone(int) methods
	
		public IWBattler getIWBattlerCloneByID(String s) {
			int i = getIDIndex(s,mobNames);
			return getIWBattlerClone(i);
		}
		public IWProjectile getIWProjectileCloneByID(String s) {
			int i = getIDIndex(s,projectileNames);
			return getIWProjectileClone(i);
		}
		public IWWall getIWWallCloneByID(String s) {
			int i = getIDIndex(s,wallNames);
			return getIWWallClone(i);
		}
		public IWPickup getIWPickupCloneByID(String s) {
			int i = getIDIndex(s,itemNames);
			return getIWPickupClone(i);
		}
	
}
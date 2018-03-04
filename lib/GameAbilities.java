/// GameAbilities
///
/// Purpose: Store a "template" version of each Ability used in the game
///
/// Abilities created here will have their parameters read from .dat files.
///
/// When new abilities need to be created, instead of reading those files again,
///   a clone of the corresponding ability in this class is created.
/// 
/// This class must be initialized as an object within Game, and must be initialized
///   after the Game has read settings.ini
///
/// IMPORTANT NOTE: When adding a new ability to the game, the ability's filename (not
///   including extension) MUST be added on a new line in data\abilities.dat

import java.io.*;
import java.util.ArrayList;

class GameAbilities {

   Game main;
   
   public static ArrayList<Ability> abilities = new ArrayList<Ability>();
	
	public GameAbilities(Game p) {
      this.main = p;
      
      File folder; String name;
      
      folder = new File("data/abilities/");
      for (File f : folder.listFiles()) {
         if (f.getName().endsWith(".dat")) {
            name = f.getName().substring(0,f.getName().indexOf(".dat"));
            if (!name.equals("TEMPLATE")) {
               abilities.add(new Ability(name, this.main));
            }
         }
      }
	}
	
	/// Finds the index of the ability in the list that has a matching ID
	public static int findIndexInNames(String s) {
		for (int i = 0; i < abilities.size(); i++)
			if (abilities.get(i).id.equals(s)) return i;
		return -1;
	}
	
	/// Creates a clone of the ability from its index in the class's list
	/// Also immediately sets the new clone's owner
	public static Ability getClone(int i, IWObject owner) {
		return abilities.get(i).getClone(owner);
	}
	
	/// Creates a clone of the ability from its file ID
	/// Also immediately sets the new clone's owner
	public static Ability getCloneByID(String s, IWObject owner) {
		if (s.equals("0") || s.equals("")) return null;
		int i = findIndexInNames(s);
		if (i == -1) return null;
		return getClone(i,owner);
	}
	
}
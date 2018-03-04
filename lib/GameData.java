
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

class GameData {
   
   static ArrayList<Level> levels = new ArrayList<Level>();
   
   public GameData() {
   
      File folder;
      
      // Read all .level files in data/levels/
      folder = new File("data/levels/");
      for (File f : folder.listFiles()) {
         if (f.toString().endsWith(".level")) {
            levels.add(new Level(f));
         }
      }
   }
   
   // Levels never change and only one instance is needed, so a clone is not returned
   public static Level getLevel(int num) {
      for (int i = 0; i < levels.size(); i++) {
         if (levels.get(i).number == num) {
            return levels.get(i);
         }
      }
      return null;
   }
}
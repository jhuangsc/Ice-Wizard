/// GameImages
///
/// Purpose: Store all of the images used by the game
///
/// When entities, abilities, effects, etc. are created, they must have an image
///   associated with them. This is set by their .dat files and must be a reference
///   to an image stored in this class. Each object will have a reference to the
///   corresponding image, which will be used in drawing.
/// 
/// This class is initialized as a static field in Game.

import java.awt.*;
import java.awt.geom.*; 
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.io.BufferedReader;
import javax.imageio.*;

class GameImages {

	/// Stores images as well as their filenames in parallel arrays
   static ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
   static ArrayList<String> imageNames = new ArrayList<String>();
	
	public GameImages() {
   
      File folder; String name;
      
      /// Read files and their names sans extension
      folder = new File("art/");
      for (File f : folder.listFiles()) {
         if (f.getName().endsWith(".png") || f.getName().endsWith(".PNG")) {
            try {
               name = f.getName().toUpperCase();
               images.add(ImageIO.read(f));
               imageNames.add(name.substring(0,name.indexOf(".PNG")));
            } catch (IOException e) { }
         }
      }
	}
	
	/// Finds the index of the image in the list that has a matching file ID
	public static int findIndexInNames(String s) {
		for (int i = 0; i < imageNames.size(); i++)
			if (imageNames.get(i).equals(s)) return i;
		return -1;
	}
	
	/// Gets a reference to an image by its index in the list
	public static BufferedImage get(int i) {
		return images.get(i);
	}
	
	/// Gets a reference to an image by its file name (sans extension)
	public static BufferedImage get(String s) {
		int i = findIndexInNames(s);
		if (i == -1) return null;
		return images.get(i);
	}
	
}
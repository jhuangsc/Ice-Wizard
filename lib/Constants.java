/// Constants
/// by Jonathan Huang
/// First created April 25, 2013
/// 
/// This class exists solely to store global constants for easy access.

import java.awt.*;

class Constants {

   public static final int MAP_TILE_SIZE = 20;
   public static final long COMMAND_BUFFER_TIME = 1000;
   public static final int OVERLAP_LIMIT = 5;

   /// Game states
   public static final int
      GAMESTATE_NONE = 0,
      GAMESTATE_LOADING1 = 1,
      GAMESTATE_LOADING2 = 2,
      GAMESTATE_LOADING3 = 3,
      GAMESTATE_MAINMENU = 10,
      GAMESTATE_OPTIONS = 11,
      GAMESTATE_LOADGAME = 12,
      GAMESTATE_INGAME = 20,
      GAMESTATE_PAUSED = 21,
      GAMESTATE_DIED = 22,
      GAMESTATE_GAMEOVER = 23,
      GAMESTATE_CLEARED = 24,
      GAMESTATE_WON = 25,
      GAMESTATE_LEVEL = 26,
      GAMESTATE_HELP = 27,
      GAMESTATE_DEBUG = 28,
      GAMESTATE_CREDITS = 99;

   /// IWObject types
   public static final int
      TYPE_IWOBJECT = 1,
      TYPE_IWMOVER = 10,
      TYPE_BATTLER = 11,
      TYPE_PROJECTILE = 12,
      TYPE_EMITTER = 13,
      TYPE_DOODAD = 20,
      TYPE_ITEM = 21,
      TYPE_DUMMY = 99;
      
   /// Debug Spawner modes
   public static final int
      DEBUG_SPAWN_MOBS = 0,
      DEBUG_SPAWN_BOSSES = 1,
      DEBUG_SPAWN_WALLS = 2,
      DEBUG_SPAWN_ITEMS = 3;
   
   /// Factions
   public static final int
      FACTION_NEUTRAL = 0,
      FACTION_PLAYER = 1,
      FACTION_NPC = 2,
      FACTION_ENEMY = 3,
      FACTION_GAIA = 4;
      
   /// Vector types
   public static final String
      VECTOR_CONTINUOUS_FORCE = "ContinuousForce",
      VECTOR_VELOCITY = "Velocity",
      VECTOR_MOBMOVEMENT = "IWBattlerBaseSpeed",
      VECTOR_OBJMOVEMENT = "ObjectBaseSpeed",
      VECTOR_XBASESPEED = "XBaseSpeed",
      VECTOR_YBASESPEED = "YBaseSpeed";
   
   /// Colors
   public static final Color
      COLOR_BLACK = new Color(0,0,0),
      COLOR_BLUE = new Color(0,127,0),
      COLOR_CYAN = new Color(127,127,255),
      COLOR_GREEN = new Color(0,180,0),
      COLOR_GREY = new Color(127,127,127),
      COLOR_MAGENTA = new Color(127,0,127),
      COLOR_PURPLE = new Color(210,50,255),
      COLOR_RED = new Color(127,0,0),
      COLOR_WHITE = new Color(255,255,255),
      COLOR_YELLOW = new Color(255,200,0),
      COLOR_ALPHA_BLACK = new Color(0,0,0,180),
      COLOR_ALPHA_BLUE = new Color(0,0,127,180),
      COLOR_ALPHA_GREEN = new Color(0,127,0,180),
      COLOR_ALPHA_GREY = new Color(127,127,127,180),
      COLOR_ALPHA_PURPLE = new Color(210,50,255,180),
      COLOR_ALPHA_RED = new Color(127,0,0,180),
      COLOR_ALPHA_WHITE = new Color(255,255,255,180);
   
   public Constants() { }
}
/**
	ICE WIZARD game
		by Jonathan Huang - Apr 2012
*/
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

public class Game extends JFrame implements MouseListener, MouseMotionListener, KeyListener, ActionListener {

	/// Meta
	static int fps = 50, timerdelay = 10, sx = 800, sy = 600;
	static Font font = new Font("Cambria", Font.PLAIN, 12);
	static GameImages images = new GameImages();
   static GameData data = new GameData();
	GameAbilities abilities;
	GameEntities entities;
	GameWindow canvas;
   Camera camera;
	Container cnt;
	Timer updater = new Timer(5,this), theartbeat = new Timer(1000,this);
	Timer tfps, tdelay;
	boolean paused = false, gameOver = false, won = false;
	boolean cheatsEnabled = true;
	
	int debugSpawnType = 0;
	
	Tooltip[] dialogs;
	int dialogState = -1;
	
	/// Key tracking
	static final int KEY_ESC = 0, KEY_W = 1, KEY_A = 2, KEY_S = 3, KEY_D = 4, KEY_SPACE = 5;
	ArrayList<Integer> keys = new ArrayList();
	
	/// Gameplay objects
	ArrayList<IWBattler> mobs;
	ArrayList<IWProjectile> projectiles;
	ArrayList<IWWall> walls;
	ArrayList<IWPickup> items;
	ArrayList<VisualEffect> vfx;
	IWBattler player;
	
	/// Meta gameplay
   Level currentLevel;
	int level = 0;
	int lives = 3;
   double collisionTolerance = 4;
	double mx = 0.0, my = 0.0; // Position of the mouse on the window
   double tx = 0.0, ty = 0.0; // Position of the mouse on the map
	double knockbackMult = 1.0, speedMult = 1.0;
	final double speedMin = 0.1, speedCap = 2.0;
	
	public Game(String title) {
		super(title);
		
		dialogs = new Tooltip[100];
		dialogs[Constants.GAMESTATE_MAINMENU] = new Tooltip(Dialogs.STORY_INTRODUCTION, this);
		dialogs[Constants.GAMESTATE_PAUSED] = new Tooltip(Dialogs.GAME_PAUSED, this);
		dialogs[Constants.GAMESTATE_DIED] = new Tooltip(Dialogs.PLAYER_DIED, this);
		dialogs[Constants.GAMESTATE_GAMEOVER] = new Tooltip(Dialogs.GAME_OVER, this);
		dialogs[Constants.GAMESTATE_WON] = new Tooltip(Dialogs.STORY_EPILOGUE, this);
		dialogs[Constants.GAMESTATE_CLEARED] = new Tooltip(Dialogs.LEVEL_CLEARED, this);
		dialogs[Constants.GAMESTATE_LEVEL] = new Tooltip(Dialogs.LEVEL_0, this);
		dialogs[Constants.GAMESTATE_HELP] = new Tooltip(Dialogs.HELP_DIALOG, this);
		dialogs[Constants.GAMESTATE_DEBUG] = new Tooltip(Dialogs.DEBUG_DIALOG, this);
		dialogs[Constants.GAMESTATE_DEBUG].adjustWidth(50);
		dialogs[Constants.GAMESTATE_DEBUG].adjustHeight(-70);
		
		readSettings();
		abilities = new GameAbilities(this);
		entities = new GameEntities(this);
		
		setSize(sx+4+4, sy+4+30);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		cnt = this.getContentPane();
		cnt.setBackground(Constants.COLOR_BLACK);
		cnt.setForeground(Constants.COLOR_WHITE);
		
		canvas = new GameWindow(this);
		canvas.repaint();
		cnt.add(canvas);
		
		tfps = new Timer(1000/fps, this);
		tfps.start();
		
		setFocusable(true);
		setVisible(true);
		
		setDialog(Constants.GAMESTATE_MAINMENU);
		canvas.repaint();
		printDifficulty();
		if (cheatsEnabled) System.out.println("Cheats are enabled.");
	}
	
	public void startGame() {
		mobs = new ArrayList<IWBattler>();
		projectiles = new ArrayList<IWProjectile>();
		walls = new ArrayList<IWWall>();
		items = new ArrayList<IWPickup>();
		vfx = new ArrayList<VisualEffect>();
		
		player = entities.getIWBattlerCloneByID("PLAYER");
		player.initIWBattler(null, 0.5*sx, 0.5*sy, Math.PI/2.0);
      camera = new Camera(null,sx,sy);
		
		startNextLevel();
		
		updater.start();
		tdelay.start();
		theartbeat.start();
	}
	
	/// Sets which dialog is currently visible
	/// Use -1 to make them all invisible
	public void setDialog(int n) {
		dialogState = n;
		try {
			for (int i = 0; i < dialogs.length; i++) {
            if (dialogs[i] != null) {
               if (n == Constants.GAMESTATE_NONE) 
                  dialogs[i].setVisible(false);
               else if (i == n)
                  dialogs[i].setVisible(true);
               else
                  dialogs[i].setVisible(false);
            }
			}
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Error: Dialog index out of bounds!");
		}
	}
	
	public void pause() {
		pause(true);
	}
	
	public void pause(boolean displayGenericMessage) {
		theartbeat.stop();
		tdelay.stop();
		paused = true;
		if (displayGenericMessage)
			setDialog(Constants.GAMESTATE_PAUSED);
	}
	
	public void resume() {
		theartbeat.start();
		tdelay.start();
		paused = false;
		setDialog(Constants.GAMESTATE_NONE);
	}
	
	public void openHelpDialog() {
		pause(false);
		setDialog(Constants.GAMESTATE_HELP);
	}
	
	public void openDebugDialog() {
		pause(false);
		setDialog(Constants.GAMESTATE_DEBUG);
	}
	
	public void resetPlayer() {
      player.setPosition(currentLevel.startx, currentLevel.starty);
      camera.setFocus(player.x, player.y);
		player.revive();
      player.clearAllEffects();
		player.recoverHP(player.hpmax);
		player.recoverMP(player.mpmax);
		player.rechargeShield(player.shieldmax);
		player.ai.resetCooldowns();
		player.ai.clearVFX();
      projectiles = new ArrayList<IWProjectile>();
	}
	
	public void restartLevel() {
		pause(false);
      
		if (lives == 0) {
			setDialog(Constants.GAMESTATE_GAMEOVER);
		} else {
			setDialog(Constants.GAMESTATE_DIED);
         projectiles = new ArrayList<IWProjectile>();
			lives--;
		}
	}
	
	public void victory() {
		/// Pick up all remaining items upon level clear
		for (int i = 0; i < items.size(); i++)
			items.get(i).pickedUpBy(player);
      player.clearEffects();
		pause(false);
		if (level == 15) winGame();
		else setDialog(Constants.GAMESTATE_CLEARED);
	}
	
	public void winGame() {
		pause(false);
		updateSpeedMult(0.05);
		printDifficulty();
		setDialog(Constants.GAMESTATE_WON);
	}
	
	public void startNextLevel() {
		pause(false);
		level++;
      currentLevel = GameData.getLevel(level);
      camera.setMap(currentLevel);
		
		mobs = new ArrayList<IWBattler>();
		projectiles = new ArrayList<IWProjectile>();
		walls = new ArrayList<IWWall>();
		items = new ArrayList<IWPickup>();
		vfx = new ArrayList<VisualEffect>();
      currentLevel.start(this);
      camera.setFocus(player.x, player.y);
      //canvas.updateRenderRegion((currentLevel.w - sx > 0? currentLevel.w - sx : 0), (currentLevel.h - sy > 0? currentLevel.h - sy : 0));
		
		switch(level) {
			case 1: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_1); break;
			case 2: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_2); break;
			case 3: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_3); break;
			case 4: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_4); break;
			case 5: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_5); break;
			case 6: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_6); break;
			case 7: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_7); break;
			case 8: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_8); break;
			case 9: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_9); break;
			case 10: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_10); break;
			case 11: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_11); break;
			case 12: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_12); break;
			case 13: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_13); break;
			case 14: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_14); break;
			case 15: dialogs[Constants.GAMESTATE_LEVEL].setText(Dialogs.LEVEL_15); break;
			default: break;
		}
		setDialog(Constants.GAMESTATE_LEVEL);
	}
		
	public void readSettings() {
		BufferedReader file;
		try {
			file = new BufferedReader(new FileReader("settings.ini"));
			while (file.ready()) {
				String ln = file.readLine();
				if (ln.indexOf("=") < 0) continue;
				String tag = ln.substring(0,ln.indexOf("=")).toLowerCase();
				String val = ln.substring(ln.indexOf("=")+1);
				
				if (tag.equals("width")) {
					try {
						int s = Integer.parseInt(val);
						if (s < 800) sx = 800;
						else sx = s;
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field WIDTH");
					}
				} else if (tag.equals("height")) {
					try {
						int s = Integer.parseInt(val);
						if (s < 640) sy = 640;
						else sy = s;
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field HEIGHT");
					}
				} else if (tag.equals("fps")) {
					try {
						int f = Integer.parseInt(val);
						if (f < 30) fps = 30;
						else if (f > 60) fps = 60;
						else fps = f;
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field FPS");
						tdelay = new Timer(10,this);
					}
				} else if (tag.equals("timerdelay")) {
					try {
						int t = Integer.parseInt(val);
						if (t < 0) t = 10;
						if (t > 50) t = 50;
						timerdelay = t;
						tdelay = new Timer(t,this);
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field FPS");
					}
				} else if (tag.equals("startinglives")) {
					try {
						int l = Integer.parseInt(val);
						if (l < 1) l = 1;
						else lives = l;
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field STARTINGLIVES");
					}
				} else if (tag.equals("knockbackmult")) {
					try {
						knockbackMult = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field KNOCKBACKMULT");
					}
				} else if (tag.equals("speedmult")) {
					try {
						speedMult = Double.parseDouble(val);
						if (speedMult > speedCap) speedMult = speedCap;
						if (speedMult < speedMin) speedMult = speedMin;
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field SPEEDMULT");
					}
				} else if (tag.equals("startlevelat")) {
					try {
						level = Integer.parseInt(val) - 1;
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field STARTLEVELAT");
					}
				} else if (tag.equals("cheatsenabled")) {
					cheatsEnabled = (val.equals("1")? true : false);
				} else if (tag.equals("collisiontolerance")) {
               try {
						collisionTolerance = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error in settings.ini: Invalid input for field COLLISIONTOLERANCE");
					}
            }
			} file.close();
		} catch (IOException e) {
			System.out.println("Error: settings.ini not found or syntax error found in settings.ini");
		}
	}
		
	//###################################################
	//## KEYBOARD INPUT HANDLING ########################
	//###################################################
		
		public void addKeyPressed(int key) {
			if (!getIsIn(keys,key)) keys.add(key);
		}
		public void removeKeyPressed(int key) {
			if (getIsIn(keys,key)) removeFrom(keys,key);
		}
		public boolean getIsIn(ArrayList<Integer> ar, int key) {
			for (int i = 0; i < ar.size(); i++) 
				if (ar.get(i) == key) return true;
			return false;
		}
		public void removeFrom(ArrayList<Integer> ar, int key) {
			for (int i = 0; i < ar.size(); i++) {
				if (ar.get(i) == key) {
					ar.remove(i);
					return;
				}
			}
		}
		
	//###################################################
	//## GARBAGE COLLECTION #############################
	//###################################################
		
		/// Clears expired vectors from any ArrayList<Vector>
		public void clearVelocities(ArrayList<Vector> ar) {
			for (int i = 0; i < ar.size(); i++) {
				if (ar.get(i).isGarbage()) {
					ar.remove(i);
					clearVelocities(ar);
					break;
				}
			}
		}
		/// Clears expired vectors from any ArrayList<IWProjectile>
		public void clearGarbageP(ArrayList<IWProjectile> ar) {
			for (int i = 0; i < ar.size(); i++) {
				if (ar.get(i).isGarbage()) {
					ar.remove(i);
					clearGarbageP(ar);
					break;
				}
			}
		}
		/// Removes destroyed walls
		public void clearIWWalls() {
			for (int i = 0; i < walls.size(); i++) {
				if (walls.get(i).isGarbage()) {
					walls.remove(i);
					clearIWWalls();
					break;
				}
			}
		}
		/// Removes dead entities
		public void clearIWBattlers() {
			for (int i = 0; i < mobs.size(); i++) {
				if (mobs.get(i).isDead()) {
					mobs.remove(i);
					clearIWBattlers();
					break;
				}
			}
		}
		/// Removes used items
		public void clearIWPickups() {
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).garbage) {
					items.remove(i);
					clearIWPickups();
					break;
				}
			}
		}
		/// Removes expired VFX
		public void clearVFX() {
			for (int i = 0; i < vfx.size(); i++) {
				if (vfx.get(i).isExpired()) {
					vfx.remove(i);
					clearVFX();
					break;
				}
			}
		}
	
	//###################################################
	//## TIMER EVENTS ###################################
	//###################################################
	
		/// Updates player's movement vectors based on keys pressed
		public void updateInput() {
      
         /// Update position of the cursor relative to the map
         double[] cb = camera.getCameraBounds();
         tx = mx + cb[0];
         ty = my + cb[2];
         player.ai.target(tx,ty);
		
			/// Update player movement in Y axis 
			if (getIsIn(keys,KEY_W) && getIsIn(keys,KEY_S)) {
				player.stopY();
			} else {
				if (getIsIn(keys,KEY_W))
					player.moveUp();
				else if (getIsIn(keys,KEY_S))
					player.moveDown();
				else
					player.stopY();
			}
			/// Update player movement in X axis
			if (getIsIn(keys,KEY_A) && getIsIn(keys,KEY_D)) {
				player.stopX();
			} else {
				if (getIsIn(keys,KEY_A))
					player.moveLeft();
				else if (getIsIn(keys,KEY_D))
					player.moveRight();
				else 
					player.stopX();
			}
		}
		
		public void actionPerformed(ActionEvent evt) {
			/// Called every 5 ms (update only user input during game only)
			if (evt.getSource() == updater)
				if (dialogState != Constants.GAMESTATE_MAINMENU)
					updateInput();
			
			/// Called every frame
			if (evt.getSource() == tfps) {
				
				/// Redraw everything
				canvas.repaint();
				
			/// Called every 0.01 s (or n ms defined by timerdelay in settings.ini)
			} else if (evt.getSource() == tdelay && !paused) {
         
				/// Don't do any garbage collection in the main menu
				if (dialogState != Constants.GAMESTATE_MAINMENU) {
			
					/// Garbage collection: remove expired objects from game
					clearGarbageP(projectiles);
					clearVelocities(player.velocities);
					for (int i = 0; i < mobs.size(); i++)
						clearVelocities(mobs.get(i).velocities);
					for (int i = 0; i < projectiles.size(); i++)
						clearVelocities(projectiles.get(i).velocities);
					clearIWWalls();
					clearIWBattlers();
					clearIWPickups();
               clearVFX();
               
               /// Update velocities in player object
               for (int i = 0; i < player.velocities.size(); i++) 
                  player.velocities.get(i).update();
               
               /// Update velocities (durations) in all other objects
               for (int i = 0; i < mobs.size(); i++)
                  for (int n = 0; n < mobs.get(i).velocities.size(); n++)
                     mobs.get(i).velocities.get(n).update();
               for (int i = 0; i < projectiles.size(); i++)
                  for (int n = 0; n < projectiles.get(i).velocities.size(); n++)
                     projectiles.get(i).velocities.get(n).update();
            
               /// Update movement
               player.move();
               for (int i = 0; i < mobs.size(); i++) 
                  mobs.get(i).move();
               for (int p = 0; p < projectiles.size(); p++) 
                  projectiles.get(p).move();
               
               /// Update AI, durations, cooldowns, regeneration, etc for entities
               for (int i = 0; i < mobs.size(); i++) 
                  mobs.get(i).update();
               for (int w = 0; w < walls.size(); w++)
                  walls.get(w).update();
               for (int p = 0; p < projectiles.size(); p++)
                  projectiles.get(p).update();
               player.update();
               
               /// Update camera position
               camera.setFocus(player.x, player.y);
            
            }
				
			} else if (evt.getSource() == theartbeat) {
            
				if (player.isDead())
					restartLevel();
				else if (mobs.size() == 0 && dialogState != Constants.GAMESTATE_WON)
					victory();
			}
		}
	
	//###################################################
	//## KEY INPUT METHODS ##############################
	//###################################################
		
		public void keyPressed(KeyEvent evt) {
			int key = evt.getKeyCode();
			
			if (key == KeyEvent.VK_ESCAPE) {
			
				System.exit(0);
				
			} else if (key == KeyEvent.VK_H) {
			
				if (dialogState == Constants.GAMESTATE_HELP) resume();
				else if (dialogState != Constants.GAMESTATE_MAINMENU) openHelpDialog();
				
			} else if (key == KeyEvent.VK_G) {
			
				if (!cheatsEnabled) return;
				
				if (dialogState == Constants.GAMESTATE_DEBUG) resume();
				else if (dialogState != Constants.GAMESTATE_MAINMENU) openDebugDialog();
				
			} else if (key == KeyEvent.VK_SPACE) {
			
				if (dialogState == Constants.GAMESTATE_MAINMENU) {
					try {
                  double dshield = player.shieldmax - 100;
                  double dmp = player.mpmax - 100;
                  startGame();
                  player.modMaxShield(dshield);
                  player.modMaxMP(dmp);
               } catch (NullPointerException e) {
                  startGame();
               }
				} else if (dialogState == Constants.GAMESTATE_LEVEL) {
					setDialog(Constants.GAMESTATE_NONE);
					resume();
				} else if (dialogState == Constants.GAMESTATE_CLEARED) {
					startNextLevel();
				} else if (dialogState == Constants.GAMESTATE_WON) {
					level = 0;
					setDialog(Constants.GAMESTATE_MAINMENU);
				} else if (dialogState == Constants.GAMESTATE_DIED) {
					resetPlayer();
					resume();
				} else if (dialogState == Constants.GAMESTATE_GAMEOVER) {
					level = 0;
					lives = 3;
					startNextLevel();
				} else {
					if (paused) resume();
					else pause();
				}
				
			} else if (key == KeyEvent.VK_U) {
				debugSpawnType = Constants.DEBUG_SPAWN_MOBS;
			} else if (key == KeyEvent.VK_I) {
				debugSpawnType = Constants.DEBUG_SPAWN_BOSSES;
			} else if (key == KeyEvent.VK_O) {
				debugSpawnType = Constants.DEBUG_SPAWN_WALLS;
			} else if (key == KeyEvent.VK_P) {
				debugSpawnType = Constants.DEBUG_SPAWN_ITEMS;
			}
			
			if (dialogState != Constants.GAMESTATE_MAINMENU) {
				updateInput();
				
				if (key == KeyEvent.VK_W) {
					addKeyPressed(KEY_W);
				} else if (key == KeyEvent.VK_S) {
					addKeyPressed(KEY_S);
				} else if (key == KeyEvent.VK_A) {
					addKeyPressed(KEY_A);
				} else if (key == KeyEvent.VK_D) {
					addKeyPressed(KEY_D);
				} else if (key == KeyEvent.VK_Q) {
					player.useAbility("POTION_HEAL");
				} else if (key == KeyEvent.VK_E) {
					player.useAbility("POTION_MANA");
				} else if (key == KeyEvent.VK_1) {
					player.useAbility("HASTE");
				} else if (key == KeyEvent.VK_2) {
					player.useAbility("FURY");
				} else if (key == KeyEvent.VK_3) {
					player.useAbility("SHIELD");
				} else if (key == KeyEvent.VK_4) {
					player.useAbility("REVERSAL");
				} else if (key == KeyEvent.VK_5) {
					player.useAbility("FROZENORB");
				}
				
				if (cheatsEnabled) {
				
					if (key == KeyEvent.VK_6) {
						player.useAbility("CHEAT_FROZENORB");
					} else if (key == KeyEvent.VK_7) {
						player.useAbility("CHEAT_INFINITY");
					} else if (key == KeyEvent.VK_8) {
						player.useAbility("CHEAT_IMMORTALITY");
					} else if (key == KeyEvent.VK_9) {
						player.useAbility("CHEAT_BULLETSTORM");
					} else if (key == KeyEvent.VK_0) {
						player.useAbility("CHEAT_DIVINESHIELD");
					} else if (key == KeyEvent.VK_R) {
						player.useAbility("CHEAT_KILLEVERYTHING");
					} else if (key == KeyEvent.VK_T) {
						player.charge = 100.0;
					} else if (key == KeyEvent.VK_F) {
						spawnIWPickupAt("ITEM_1UP",tx,ty); 
					
					} else if (key == KeyEvent.VK_Z) {
						switch(debugSpawnType) {
							case Constants.DEBUG_SPAWN_MOBS: spawnIWBattlerAt("MOB_FLAMESPRITE",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_BOSSES: spawnIWBattlerAt("BOSS_DIRTYOLDMAN",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_WALLS: spawnIWWallAt("WALL_ARCANEBARRIERH",tx,ty); break;
							case Constants.DEBUG_SPAWN_ITEMS: spawnIWPickupAt("ITEM_AMMO",tx,ty); break;
						} 
					} else if (key == KeyEvent.VK_X) {
						switch(debugSpawnType) {
							case Constants.DEBUG_SPAWN_MOBS: spawnIWBattlerAt("MOB_BLAZINGIMP",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_BOSSES: spawnIWBattlerAt("BOSS_FROSTFIREGOLEM",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_WALLS: spawnIWWallAt("WALL_ARCANEBARRIERV",tx,ty); break;
							case Constants.DEBUG_SPAWN_ITEMS: spawnIWPickupAt("ITEM_POTION_HEALTH",tx,ty); break;
						}
					} else if (key == KeyEvent.VK_C) {
						switch(debugSpawnType) {
							case Constants.DEBUG_SPAWN_MOBS: spawnIWBattlerAt("MOB_FLAMINGJUSTICAR",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_BOSSES: spawnIWBattlerAt("BOSS_MADMAGE",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_WALLS: spawnIWWallAt("WALL_STONEWALLH",tx,ty); break;
							case Constants.DEBUG_SPAWN_ITEMS: spawnIWPickupAt("ITEM_POTION_MANA",tx,ty); break;
						}
					} else if (key == KeyEvent.VK_V) {
						switch(debugSpawnType) {
							case Constants.DEBUG_SPAWN_MOBS: spawnIWBattlerAt("MOB_FIREGOLEM",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_BOSSES: spawnIWBattlerAt("BOSS_ASPECTOFCHAOS",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_WALLS: spawnIWWallAt("WALL_STONEWALLV",tx,ty); break;
							case Constants.DEBUG_SPAWN_ITEMS: spawnIWPickupAt("ITEM_BOOST_MANA",tx,ty); break;
						}
					} else if (key == KeyEvent.VK_B) {
						switch(debugSpawnType) {
							case Constants.DEBUG_SPAWN_MOBS: spawnIWBattlerAt("MOB_FLAMETURRET",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_BOSSES: spawnIWBattlerAt("BOSS_THEFIRELORD",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_WALLS: spawnIWWallAt("WALL_BARRICADE",tx,ty); break;
							case Constants.DEBUG_SPAWN_ITEMS: spawnIWPickupAt("ITEM_BOOST_SHIELD",tx,ty); break;
						}
					} else if (key == KeyEvent.VK_N) {
						switch(debugSpawnType) {
							case Constants.DEBUG_SPAWN_MOBS: spawnIWBattlerAt("MOB_INFERNOTURRET",tx,ty,Math.toRadians(90)); break;
							case Constants.DEBUG_SPAWN_BOSSES: break;
							case Constants.DEBUG_SPAWN_WALLS: break;
							case Constants.DEBUG_SPAWN_ITEMS: spawnIWPickupAt("ITEM_RECHARGE",tx,ty); break;
						}
					}
				}
			}
		}
		
		public void keyReleased(KeyEvent evt) {
			if (dialogState == Constants.GAMESTATE_MAINMENU) return;
			int key = evt.getKeyCode();
			updateInput();
			
			/// Remove the released key from the keysPressed ArrayList
			/// Also start moving in the opposite direction if the corresponding key is pressed
			/// Note that WASD and corresponding arrow keys have equivalent values
			if (key == KeyEvent.VK_W) {
				removeKeyPressed(KEY_W);
			} else if (key == KeyEvent.VK_S) {
				removeKeyPressed(KEY_S);
			} else if (key == KeyEvent.VK_A) {
				removeKeyPressed(KEY_A);
			} else if (key == KeyEvent.VK_D) {
				removeKeyPressed(KEY_D);
			} 
		}
		
		public void keyTyped(KeyEvent evt) {
			
		}
	
	//###################################################
	//## MOUSE INPUT METHODS ############################
	//###################################################
		
		public void updateMX(MouseEvent evt) {
         mx = evt.getX() - 4.0;
		}
		
		public void updateMY(MouseEvent evt) {
         my = evt.getY() - 30.0;
		}
		
		public void mousePressed(MouseEvent evt) {
			updateMX(evt);
			updateMY(evt);
			int button = evt.getButton();
			if (button == MouseEvent.BUTTON1) {
				/// if left mouse button, fire snowflakes
				if (dialogState != Constants.GAMESTATE_MAINMENU) {
					player.ai.firing[0] = true;
					player.useAbility("SNOWFLAKE");
				}
				
			} else if (button == MouseEvent.BUTTON2) {
				/// if middle mouse button, unleash frost nova
				if (dialogState != Constants.GAMESTATE_MAINMENU) 
					player.useAbility("FROSTNOVA");
				
			} else if (button == MouseEvent.BUTTON3) {
				/// if right mouse button, fire ice shard
				if (dialogState != Constants.GAMESTATE_MAINMENU) {
					player.ai.firing[1] = true;
					player.useAbility("ICESHARD");
				}
			}
		}
		
		public void mouseReleased(MouseEvent evt) {
			updateMX(evt);
			updateMY(evt);
			int button = evt.getButton();
			if (button == MouseEvent.BUTTON1) {
				if (dialogState != Constants.GAMESTATE_MAINMENU) player.ai.firing[0] = false;
			} else if (button == MouseEvent.BUTTON2) {
			
			} else if (button == MouseEvent.BUTTON3) {
				if (dialogState != Constants.GAMESTATE_MAINMENU) player.ai.firing[1] = false;
			}
		}
		
		public void mouseClicked(MouseEvent evt) {
			updateMX(evt);
			updateMY(evt);
		}
		
		public void mouseEntered(MouseEvent evt) { }
		
		public void mouseExited(MouseEvent evt) { }
		
		public void mouseDragged(MouseEvent evt) {
			updateMX(evt);
			updateMY(evt);
		}
		
		public void mouseMoved(MouseEvent evt) {
			updateMX(evt);
			updateMY(evt);
		}
		
		public double getAngleToMouse() {
			return Math.atan2( (ty - player.y), (tx - player.x) );
		}

	//###################################################
	//## GAME STUFF #####################################
	//###################################################
	
		public void spawnIWBattlerAt(String mobID, double x, double y, double angleFacing) {
			int i = mobs.size();
			mobs.add(entities.getIWBattlerCloneByID(mobID));
			mobs.get(i).initIWBattler(null,x,y,angleFacing);
		}
		
		public void spawnIWBattlerAt(String mobID, double x, double y) {
			spawnIWBattlerAt(mobID,x,y,Math.toRadians(90));
		}
		
		public void spawnIWProjectileAt(String projID, IWBattler owner, double x, double y, double angleFacing) {
			int i = projectiles.size();
			projectiles.add(entities.getIWProjectileCloneByID(projID));
			projectiles.get(i).initIWProjectile(owner,x,y,angleFacing);
		}
		
		public void spawnIWWallAt(String wallID, double x, double y) {
			int i = walls.size();
			walls.add(entities.getIWWallCloneByID(wallID));
			walls.get(i).initIWWall(x,y);
		}
		
		public void spawnIWPickupAt(String itemID, double x, double y) {
			int i = items.size();
			items.add(entities.getIWPickupCloneByID(itemID));
			items.get(i).initIWPickup(x,y);
		}
		
		/// Update speed multiplier in all projectiles
		/// Main method of implementing difficulty
		public void updateSpeedMult(double ds) {
			double old = speedMult;
			speedMult += ds;
			if (speedMult > speedCap) speedMult = speedCap;
			if (speedMult < speedMin) speedMult = speedMin;
			
			for (int i = 0; i < entities.projectiles.size(); i++) {
				entities.projectiles.get(i).baseSpeed /= old;
				entities.projectiles.get(i).baseSpeed *= speedMult;
			}
		}
		
		public void printDifficulty() {
			double s = speedMult;
			System.out.println("Difficulty: "+s+" ("+(
            s>2.0? "I'm not paying for your funeral." :
            s>1.9? "You need to drop this right now." :
            s>1.8? "This is all your fault." :
            s>1.7? "Really, you're making yourself suffer." :
            s>1.6? "Please quit. I only have your sanity in mind." : 
            s>1.5? "Just give up. Before it's too late." : 
            s>1.4? "Are you serious?" :
            s>1.3? "Absurd" :
            s>1.2? "Ridiculous" : 
            s>1.1? "Insane" : 
            s>1.0? "Good Luck" :
            s>0.9? "Seriously Hectic" :
            s>0.8? "Hectic" :
            s>0.7? "Very Hard" : 
            s>0.6? "Hard" : 
            s>0.5? "Challenging" :
            s>0.4? "Normal" : 
            s>0.3? "Fairly Easy" : 
            s>0.2? "Quite Easy" : 
         "Pathetic") + ")");
		}
	
	//###################################################
	//## MAIN ###########################################
	//###################################################
	
	public static void main(String args[]) {
		new Game("Ice Wizard");
	}
}
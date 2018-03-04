/// Random
/// 
/// Purpose: Provide easy access to a basic RNG that is easier
///   to use than figuring out what numbers to use with Math.random()
///   every time a random number is needed.

public class Random {
	
	/// Returns int between 0 and max, inclusive
	public static int randInt(int max) {
		return (int)(Math.random()*(max+1));
	}
	
	/// Returns int between min and max, inclusive
	/// If min > max, they switch positions
	public static int randInt(int min, int max) {
		if (min > max) {
			int temp = min;
			min = max;
			max = temp;
		}
		return (int)(Math.random()*(max-min+1)) + min;
	}
	
	/// Returns either true or false
	public static boolean randBool() {
		if (randInt(0,1) == 1) return true;
		return false;
	}
	
	/// Returns random angle in degrees
	public static float randAngle() {
		return (float)randInt(0,360);
	}
	
	/// Returns random angle in radians
	public static float randAngleRad() {
		return (float)Math.toRadians(randAngle());
	}
}
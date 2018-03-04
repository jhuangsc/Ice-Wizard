/// Vector
///
/// Purpose: Representing velocities for Entities in the game
///
/// Vectors are generally used for movement (base speed vectors), but are also
///   used for Knockback. Movement vectors are maintained or replaced so long as
///   their owner is set to move independently, while Knockback vectors always have
///   a set duration.
///
/// Vectors have an angle and velocity, but are otherwise non-functional. Determining
///   movement based on velocity vectors is handled by Entities, not by the 

class Vector {

	Game main;
   double x, y;      // x and y components of this vector
	double magnitude;	// length of resultant vector
	double direction;	// angle in radians
	int duration;		// time this vector has existed, in milliseconds
	int durationMax;	// time this vector takes to expire, in milliseconds
	String id;			// name of vector
	boolean garbage = false;

	public Vector(String name, double force, double theta, int dur, Game main) {
		this.id = name;
		this.magnitude = force;
		this.direction = theta;
      this.x = this.magnitude*Math.cos(this.direction);
      this.y = this.magnitude*Math.sin(this.direction);
		this.duration = 0;
		this.durationMax = dur;
	}
	
	public void update() {
		if (durationMax <= 0 || garbage) return;
		
		this.duration += main.timerdelay;
		if (duration >= durationMax) garbage = true;
	}
	
	/// Returns whether this vector has expired
	public boolean isGarbage() {
		return this.garbage;
	}
	
	/// Returns x component of vector
	public double getX() {
		return this.x;
	}
	
	/// Returns y component of vector
	public double getY() {
		return this.y;
	}
	
	public boolean matchID(String name) {
		if (this.id.equals(name)) return true;
		return false;
	}
}
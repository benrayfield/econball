/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.econball;
import humanaicore.common.CoreUtil;
import java.awt.Color;

public class Ball{
	
	public double timeLastStartedPopping = CoreUtil.time()-10;
	
	public double radius;
	
	/** If this is positive, radius is pushed to become bigger *
	public float forceOnRadius;
	*/
	public double radiusSpeed;
	
	public final double minRadius;
	
	/** Used to choose radiusSpeed. radius cant be directly changed, only by changing radiusSpeed.
	This is to allow bounce calculations to include the difference in radius speeds and position speeds.
	*/
	public double targetRadius;
	
	public final double position[], speed[];
	
	public boolean isTouchingAnotherBall;
	
	public Color mainColor = Color.white;
	
	public Color textColor = Color.orange;
	
	public Ball(int dims, float minRadius){
		position = new double[dims];
		speed = new double[dims];
		this.minRadius = this.radius = this.targetRadius = minRadius;
	}
	
	/** area (by targetRadius) if 2d */
	public double mass(){
		return targetRadius*targetRadius*Math.PI;
	}
	
	/** sets area (by targetRadius) */
	public void setMass(double newMass){
		targetRadius = Math.sqrt(newMass/Math.PI);
	}
	
	/** same as setMass(mass()*mult) but faster and with less roundoff */
	public void multMass(double mult){
		targetRadius *= Math.sqrt(mult);
	}

}
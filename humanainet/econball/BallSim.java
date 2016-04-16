/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.econball;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import humanaicore.common.CoreUtil;

public class BallSim{
	
	public final Map<String,Ball> balls = new HashMap();
	
	public Rectangle walls;
	
	public double gravity = 10;
	//public double gravity = .5; //TODO
	//public double gravity = 0; //TODO
	
	public double friction = 2;
	
	public double changeRadiusSpeed = 10;
	public double radiusFriction = 130;
	
	public final int dims;
	
	public BallSim(int dims, Rectangle walls){
		this.dims = dims;
		this.walls = walls;
	}
	
	/** Use timeNow instead of CoreUtil.time() because simulated time may slightly differ
	to simulate many cycles at once, like between painting the screen.
	*/
	public void nextState(double timeNow, double secondsSinceLastCall){
		Ball ballsArray[];
		synchronized(balls){
			ballsArray = balls.values().toArray(new Ball[0]);
		}
		final Rectangle walls = this.walls;
		final double gravity = this.gravity;
		final double friction = this.friction;
		final double changeRadiusSpeed = this.changeRadiusSpeed;
		final double radiusFriction = this.radiusFriction;
		
		double addToYSpeedForGravity = gravity*secondsSinceLastCall;
		
		for(Ball ball : ballsArray){
			ball.speed[1] += addToYSpeedForGravity;
			
			double minY = walls.y+ball.radius;
			if(ball.position[1] < minY){ //bounce on ceiling
				ball.speed[1] = Math.abs(ball.speed[1]);
			}
						
			double maxY = walls.y+walls.height-ball.radius;
			if(maxY < ball.position[1]){ //bounce on floor
				ball.speed[1] = -Math.abs(ball.speed[1]);
			}
			
			double minX = walls.x+ball.radius;
			if(ball.position[0] < minX){ //bounce on left wall
				ball.speed[0] = Math.abs(ball.speed[0]);
			}
			
			double maxX = walls.x+walls.width-ball.radius;
			if(maxX < ball.position[0]){ //bounce on right wall
				ball.speed[0] = -Math.abs(ball.speed[0]);
			}
		}
		
		/*for(Popbol ball : ballsArray){
			"TODO bounce on inside of circle game area"
		}*/
		
		if(dims != 2) throw new RuntimeException("Dims must be 2 but is "+dims);
		
		for(int i=0; i<ballsArray.length; i++){
			ballsArray[i].isTouchingAnotherBall = false;
		}
		
		/*
		//change each radius (TODO radiusSpeed) depending on how long ago last popped
		for(int i=0; i<ballsArray.length; i++){
			Popbol ball = ballsArray[i];
			double secondsSincePop = timeNow - ball.timeLastStartedPopping;
			//secondsSincePop = Math.max(0, Math.min(secondsSincePop, 60));
			//If secondsSincePop is small, mult should be large and gradually shrink to 1.
			double mult = 1 + 3/(1+secondsSincePop);
			//double mult = 1 + secondsSincePop;
			//if(balls.get("x") == ball){
			//	System.out.println("ball x's secondsSincePop = "+secondsSincePop);
			//}
			ball.radius = ball.minRadius*mult;
			//ball.radius = ball.minRadius*2;
			//ball.radius = ball.minRadius;
		}*/
		for(int i=0; i<ballsArray.length; i++){
			Ball ball = ballsArray[i];
			double secondsSincePop = timeNow - ball.timeLastStartedPopping;
			//If secondsSincePop is small, mult should be large and gradually shrink to 1.
			double mult = 1 + 3/(1+secondsSincePop);
			ball.targetRadius = ball.minRadius*mult;
			double wantChangeRadius = ball.targetRadius-ball.radius;
			ball.radiusSpeed += wantChangeRadius*secondsSinceLastCall*changeRadiusSpeed;
			
			//friction on radiusSpeed
			double subtractFromRadiusSpeed = radiusFriction*secondsSinceLastCall;
			if(ball.radiusSpeed < 0){
				ball.radiusSpeed = Math.min(ball.radiusSpeed+subtractFromRadiusSpeed, 0);
			}else{
				ball.radiusSpeed = Math.max(0, ball.radiusSpeed-subtractFromRadiusSpeed);
			}
		}
		
		//bounce on eachother
		for(int i=1; i<ballsArray.length; i++){
			for(int j=0; j<i; j++){
				Ball a = ballsArray[i];
				Ball b = ballsArray[j];
				double dx = b.position[0]-a.position[0];
				double dy = b.position[1]-a.position[1];
				double distSq = dx*dx+dy*dy;
				if(distSq == 0) throw new RuntimeException(
					"Balls cant have exact same center: "+ballsArray[i]+" and "+ballsArray[j]);
				double minDist = a.radius+b.radius;
				if(distSq < minDist*minDist){ //bounce on eachother
					a.isTouchingAnotherBall = b.isTouchingAnotherBall = true;
					double speedDx = b.speed[0]-a.speed[0];
					double speedDy = b.speed[1]-a.speed[1];
					double radiusSpeedAtEachother = a.radiusSpeed+b.radiusSpeed;
					double dist = Math.sqrt(distSq);
					double normDx = dx/dist, normDy = dy/dist;
					double speedAtEachother = -(speedDx*normDx + speedDy*normDy);
					speedAtEachother += radiusSpeedAtEachother;
					if(0 < speedAtEachother){
						//double addToSpeed = 2*speedAtEachother;
						//double addToEachSpeed = addToSpeed/2;
						//double addToEachSpeedX = normDx*addToEachSpeed;
						//double addToEachSpeedY = normDy*addToEachSpeed;
						double addToEachSpeedX = normDx*speedAtEachother;
						double addToEachSpeedY = normDy*speedAtEachother;
						b.speed[0] += addToEachSpeedX;
						a.speed[0] -= addToEachSpeedX;
						b.speed[1] += addToEachSpeedY;
						a.speed[1] -= addToEachSpeedY;
						//"ERROR, instead just set their speed using absval"
					}
				}
			}
		}
		
		//friction
		double subtractFromEachSpeed = friction*secondsSinceLastCall;
		for(int i=0; i<ballsArray.length; i++){
			Ball ball = ballsArray[i];
			double speedX = ball.speed[0], speedY = ball.speed[1];
			double speed = Math.sqrt(speedX*speedX + speedY*speedY);
			if(0 < speed){
				double newSpeed = speed-subtractFromEachSpeed;
				if(newSpeed <= 0){
					ball.speed[0] = 0;
					ball.speed[1] = 0;
				}else{
					double mult = newSpeed/speed;
					ball.speed[0] *= mult;
					ball.speed[1] *= mult;
				}
			}
		}
		
		//move
		for(Ball ball : ballsArray){
			for(int d=0; d<dims; d++){
				ball.position[d] += ball.speed[d]*secondsSinceLastCall;
				ball.radius += ball.radiusSpeed*secondsSinceLastCall;
			}
		}
	}
	
	public void startPopping(String ballName){
		Ball b = balls.get(ballName);
		if(b != null){
			b.timeLastStartedPopping = CoreUtil.time();
		}
	}
	
	/** Moves the ball randomly until its farther away from its closest ball than newMinDist. */
	public void moveBallToRandomMoreEmptyPlace(Ball ball, double newMinDist, Random rand){
		final Rectangle walls = this.walls;
		double minDist = distanceToClosestOtherBall(ball);
		while(minDist < newMinDist){
			double minX = walls.x+ball.radius;
			double maxX = walls.x+walls.width-ball.radius;
			double minY = walls.y+ball.radius;
			double maxY = walls.y+walls.height-ball.radius;
			ball.position[0] = minX+rand.nextDouble()*(maxX-minX);
			ball.position[1] = minY+rand.nextDouble()*(maxY-minY);
			minDist = distanceToClosestOtherBall(ball);
		}
		System.out.println("Done moving ball="+ball+" minDist="+minDist);
	}
	
	/** Distance is negative if balls overlap */
	public double distanceToClosestOtherBall(Ball ball){
		Ball ballsArray[];
		synchronized(balls){
			ballsArray = balls.values().toArray(new Ball[0]);
		}
		if(ballsArray.length == 1) return Double.MAX_VALUE;
		double minDist = Double.MAX_VALUE;
		for(int i=0; i<ballsArray.length; i++){
			if(ballsArray[i] != ball){
				Ball otherBall = ballsArray[i];
				double dx = ball.position[0]-otherBall.position[0];
				double dy = ball.position[1]-otherBall.position[1];
				double dist = Math.sqrt(dx*dx + dy*dy);
				dist -= ball.radius+otherBall.radius;
				//distance is negative if balls overlap
				minDist = Math.min(minDist, dist);
			}
		}
		//System.out.println("minDist="+minDist+" ball="+ball);
		return minDist;
	}

}
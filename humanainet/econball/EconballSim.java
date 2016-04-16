/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.econball;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import humanaicore.common.CoreUtil;

public class EconballSim extends BallSim{
	
	public final Ball circleGameArea;
	
	public final Ball centerOfGravity;
	
	protected double constTotalMass = 30000;
	protected boolean normToConstantMass = true;
	
	protected double minMass = 1000, maxMass = constTotalMass/2;
	
	public EconballSim(int dims, Rectangle walls){
		super(dims,walls);
		gravity = 0;
		circleGameArea = new Ball(dims, .5f*Math.min(walls.width, walls.height));
		circleGameArea.position[0] = walls.x+.5f*walls.width;
		circleGameArea.position[1] = walls.y+.5f*walls.height;
		centerOfGravity = new Ball(dims, 0);
		radiusFriction = 30;
		//changeRadiusSpeed = 40;
		changeRadiusSpeed = 5;
	}
	
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
		
		if(dims != 2) throw new RuntimeException("Dims must be 2 but is "+dims);
		
		for(int i=0; i<ballsArray.length; i++){
			ballsArray[i].isTouchingAnotherBall = false;
		}
		
		for(int i=0; i<ballsArray.length; i++){
			Ball ball = ballsArray[i];
			
			//this pop logic is replaced by continuousCenteredEconbit
			//double secondsSincePop = timeNow - ball.timeLastStartedPopping;
			//If secondsSincePop is small, mult should be large and gradually shrink to 1.
			//double mult = 1 + 3/(1+secondsSincePop);
			//ball.targetRadius = ball.minRadius*mult;
			
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
		
		//continuousCenteredEconbit, the main strategy of the game:
		//adjust target radius speeds (do continuousCenteredEconbit using vector from circleGameArea to centerOfGravity)
		double sumX = 0, sumY = 0, sumMass = 0;
		for(int i=0; i<ballsArray.length; i++){
			Ball ball = ballsArray[i];
			double m = ball.mass();
			sumX += ball.position[0]*m;
			sumY += ball.position[1]*m;
			sumMass += m;
		}
		centerOfGravity.position[0] = sumX/sumMass;
		centerOfGravity.position[1] = sumY/sumMass;
		centerOfGravity.setMass(sumMass);
		System.out.println("total mass "+sumMass);
		double econbitX = (centerOfGravity.position[0]-circleGameArea.position[0])/circleGameArea.radius;
		double econbitY = (centerOfGravity.position[1]-circleGameArea.position[1])/circleGameArea.radius;
		double econbitDist = Math.sqrt(econbitX*econbitX + econbitY*econbitY);
		if(econbitDist != 0){ //continuousCenteredEconbit has 0 or slightly negative effect at center
			double econbitUnitDirectionX = econbitX/econbitDist; //range -1 to 1
			double econbitUnitDirectionY = econbitY/econbitDist; //range -1 to 1
			//econbitBifraction is positive because range -1 to 1 is aligned in direction of centerOfGravity
			double econbitBifraction = econbitDist/circleGameArea.radius;
			double econbitFraction = (econbitBifraction+1)/2;
			
			//Doing this to all balls along the same line conserves mass (except roundoff)
			//double econbitA = econbitFraction;
			//double econbitB = 1-econbitFraction;
			//sum of ballA, and sum of ballB, both become .5 as summed from all the balls which each are
			//part a and part b depending on position, along each line through center.
			//double multA = .5/econbitA;
			//double multB = .5/econbitB;
			//No the multA and multB must be from sum along that line. But doesnt centerOfGravity do that?
			
			//Instead of the commented loop below this loop,
			//rewrite it from perspective of the line intersecting ball and circleGameArea
			double sumA = 0;
			double sumB = 0;
			for(int i=0; i<ballsArray.length; i++){
				Ball ball = ballsArray[i];
				double ballX = (ball.position[0]-circleGameArea.position[0])/circleGameArea.radius; //range -1 to 1
				double ballY = (ball.position[1]-circleGameArea.position[1])/circleGameArea.radius; //range -1 to 1
				double ballBifraction = ballX*econbitUnitDirectionX + ballY*econbitUnitDirectionY; //range -1 to 1
				//ballBifraction *= .1; //FIXME remove this line
				double ballFraction = (ballBifraction+1)/2;
				double ballA = 1-ballFraction;
				double ballB = ballFraction;
				double m = ball.mass();
				sumA += ballA*m;
				sumB += ballB*m;
			}
			double sumAB = sumA+sumB; //sumA+sumB should equal sumMass
			double sumAFraction = sumA/sumAB;
			double sumBFraction = sumB/sumAB;
			double multA = .5/sumAFraction;
			double multB = .5/sumBFraction;
			//double multB = .5/econbitB;
			for(int i=0; i<ballsArray.length; i++){
				Ball ball = ballsArray[i];
				double ballX = (ball.position[0]-circleGameArea.position[0])/circleGameArea.radius; //range -1 to 1
				double ballY = (ball.position[1]-circleGameArea.position[1])/circleGameArea.radius; //range -1 to 1
				double ballBifraction = ballX*econbitUnitDirectionX + ballY*econbitUnitDirectionY; //range -1 to 1
				//ballBifraction *= .1; //FIXME remove this line
				double ballFraction = (ballBifraction+1)/2;
				double ballA = 1-ballFraction;
				double ballB = ballFraction;
				double ballMassMult = ballA*multA + ballB*multB; //ball winning when its enlarging as positive
				ball.multMass(ballMassMult);
			}
			/*for(int i=0; i<ballsArray.length; i++){
				Popbol ball = ballsArray[i];
				double ballX = (ball.position[0]-circleGameArea.position[0])/circleGameArea.radius; //range -1 to 1
				double ballY = (ball.position[1]-circleGameArea.position[1])/circleGameArea.radius; //range -1 to 1
				double ballBifraction = ballX*econbitUnitDirectionX + ballY*econbitUnitDirectionY; //range -1 to 1
				double ballFraction = (ballBifraction+1)/2;
				double ballA = ballFraction;
				double ballB = 1-ballFraction;
				double ballMassMult = ballA*multA + ballB*multB; //ball winning when its enlarging as positive
				ball.multMass(ballMassMult);
			}*/
		}
		
		for(int i=0; i<ballsArray.length; i++){
			Ball ball = ballsArray[i];
			double m = ball.mass();
			ball.setMass(Math.max(minMass, Math.min(m, maxMass)));
		}
		
		if(normToConstantMass){
			//FIXME make it work accurate except roudoff without this, then put this code back in.
			//I dont want gameplay to depend on this:
			double allMassMult = constTotalMass/centerOfGravity.mass();
			for(int i=0; i<ballsArray.length; i++){
				Ball ball = ballsArray[i];
				ball.multMass(allMassMult);
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
		
		//bounce inward from perimeter of circleGameArea
		if(circleGameArea.speed[0] != 0 || circleGameArea.speed[1] != 0) throw new RuntimeException(
			"circleGameArea cant move");
		for(Ball ball : ballsArray){
			double dx = ball.position[0]-circleGameArea.position[0];
			double dy = ball.position[1]-circleGameArea.position[1];
			double centerToCenter = Math.sqrt(dx*dx + dy*dy);
			if(circleGameArea.radius <= centerToCenter+ball.radius){ //collision
				double normDx = dx/centerToCenter;
				double normDy = dy/centerToCenter;
				double speedAtEachother = -(ball.speed[0]*normDx + ball.speed[1]*normDy);
				speedAtEachother -= ball.radiusSpeed;
				ball.speed[0] += 2*normDx*speedAtEachother;
				ball.speed[1] += 2*normDy*speedAtEachother;
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
				//ball.radius += ball.radiusSpeed*secondsSinceLastCall;
				ball.radius = ball.targetRadius;
			}
		}
	}
	
	public double distanceToClosestOtherBall(Ball ball){
		double sup = super.distanceToClosestOtherBall(ball);
		double dx = ball.position[0]-circleGameArea.position[0];
		double dy = ball.position[1]-circleGameArea.position[1];
		double centerToCenter = Math.sqrt(dx*dx + dy*dy);
		double distanceToCircleGameAreaPerimeter = circleGameArea.radius-(centerToCenter+ball.radius);
		return Math.min(sup, distanceToCircleGameAreaPerimeter);
	}

}
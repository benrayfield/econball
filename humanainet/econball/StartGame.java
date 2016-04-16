/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.econball;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.Random;
import humanaicore.realtimeschedulerTodoThreadpool.RealtimeScheduler;
import humanaicore.common.Rand;
import humanaicore.common.ScreenUtil;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class StartGame{
	
	public static void main(String args[]){
		JFrame window = new JFrame("Econball - opensource GNU GPL 2+ - unzip this jar file to get source code");
		//w.setLocation(300,200);
		//int w = 900, h = 900;
		int w = 700, h = 700;
		window.setSize(w,h);
		Rectangle rect = new Rectangle(w/10, h/10, w*4/5,h*4/5);
		int dims = 2;
		BallSim sim = new EconballSim(dims, rect);
		//float minRadius = 30;
		//float minRadius = 15;
		float minRadius = 10;
		//float minRadius = 35;
		//float minRadius = 95;
		float maxEstimatedRadius = minRadius*2;
		float minCenterX = rect.x+maxEstimatedRadius;
		float maxCenterX = rect.x+rect.width-maxEstimatedRadius;
		float minCenterY = rect.y+maxEstimatedRadius;
		float maxCenterY = rect.y+rect.height-maxEstimatedRadius;
		Random rand = Rand.strongRand;
		//for(char c='a'; c<='z'; c++){
		for(char c='a'; c<='g'; c++){
			String ballName = ""+c;
			Ball b = new Ball(2,minRadius);
			b.position[0] = minCenterX+rand.nextFloat()*(maxCenterX-minCenterX);
			b.position[1] = minCenterY+rand.nextFloat()*(maxCenterY-minCenterY);
			float red = rand.nextFloat();
			float green = rand.nextFloat();
			float blue = rand.nextFloat();
			b.mainColor = new Color(red,green,blue);
			b.textColor = new Color(1-red,1-green,1-blue);
			synchronized(sim.balls){
				sim.balls.put(ballName, b);
				if(1 < sim.balls.size()){
					sim.moveBallToRandomMoreEmptyPlace(b, 0, Rand.strongRand);
				}
			}
		}
		GamePanel panel = new GamePanel(sim,"a");
		window.add(panel);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setVisible(true);
		//ScreenUtil.moveToScreenCenter(window);
		ScreenUtil.moveToScreenCenterHorizontally(window, 0);
		RealtimeScheduler.start(panel);
	}

}

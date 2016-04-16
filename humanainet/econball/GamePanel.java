/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.econball;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Map;
import humanaicore.common.CoreUtil;
import humanaicore.realtimeschedulerTodoThreadpool.Task;
import humanaicore.realtimeschedulerTodoThreadpool.TimedEvent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class GamePanel extends JTextArea implements Task, KeyListener, MouseMotionListener, MouseListener{
	
	public final BallSim sim;
	
	//public int prevMouseX, prevMouseY;
	
	public double mouseAcceleratesBallMult = .7;
	
	int constMouseX = 50, constMouseY = 50;
	
	public static Robot robot;
	
	public final String playerBallName;
	
	public GamePanel(BallSim sim, String playerBallName){
		this.sim = sim;
		this.playerBallName = playerBallName;
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
		try{
			robot = new Robot();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	protected Font font;
	
	public void paint(Graphics g){
		int w = getWidth(), h = getHeight();
		sim.walls = new Rectangle(0,0,w,h);
		g.setColor(Color.black);
		g.fillRect(0, 0, w, h);
		if(sim instanceof EconballSim){
			g.setColor(Color.white);
			Ball c = ((EconballSim)sim).circleGameArea;
			c.position[0] = .5f*w;
			c.position[1] = .5f*h;
			c.radius = .5f*Math.min(w, h);
			g.drawOval((int)(c.position[0]-c.radius),  (int)(c.position[1]-c.radius), (int)(c.radius*2), (int)(c.radius*2));
		}
		int fontSize = 50;
		if(font == null){
			//font = g.getFont().deriveFont(140);
			font = new Font("SansSerif", Font.PLAIN, fontSize);
		}
		g.setFont(font);
		synchronized(sim.balls){
			for(Map.Entry<String,Ball> entry : sim.balls.entrySet()){
				String ballName = entry.getKey();
				Ball ball = entry.getValue();
				//g.setColor(ball.isTouchingAnotherBall ? Color.white : ball.mainColor);
				g.setColor(ball.mainColor);
				int xStart = (int)(ball.position[0]-ball.radius);
				int yStart = (int)(ball.position[1]-ball.radius);
				int twiceRadius = (int)(ball.radius*2);
				g.fillOval(xStart, yStart, twiceRadius, twiceRadius);
				g.setColor(ball.textColor);
				g.drawString(ballName,
					(int)(ball.position[0]-15),
					(int)(ball.position[1]+15));
			}
		}
		if(sim instanceof EconballSim){
			g.setColor(Color.white);
			Ball centerOfGravity = ((EconballSim)sim).centerOfGravity;
			g.fillRect((int)centerOfGravity.position[0]-1, (int)centerOfGravity.position[1]-1, 3, 3);
		}
	}
	
	protected double lastTime = CoreUtil.time();

	public void event(Object context){
		//May slightly differ from actual time to run many cycles in a loop, so use its time.
		if(context instanceof TimedEvent){
			TimedEvent t = (TimedEvent) context;
			double now = t.time;
			double secondsSinceLastCall = now-lastTime;
			secondsSinceLastCall = Math.max(secondsSinceLastCall, .1);
			lastTime = now;
			//TODO? int cycles = 100;
			int cycles = 1;
			double timePerCycle = secondsSinceLastCall/cycles;
			for(int i=0; i<cycles; i++){
				double simulatedTime = now+timePerCycle*i;
				sim.nextState(simulatedTime, timePerCycle);
			}
			repaint();
		}
	}

	public double preferredInterval(){
		return .01;
	}

	public void keyTyped(KeyEvent e){}

	public void keyPressed(KeyEvent e){
		String ballName = ""+e.getKeyChar();
		//System.out.println("ballName "+ballName);
		synchronized(sim.balls){
			Ball ball = sim.balls.get(ballName);
			if(ball != null){
				ball.timeLastStartedPopping = CoreUtil.time();
				System.out.println("Popping "+ball);
			}
		}
	}

	public void keyReleased(KeyEvent e){}
	
	public Ball getPlayerBall(){
		return sim.balls.get(playerBallName);
	}

	public void mouseMoved(MouseEvent e){
		System.out.println("width="+getWidth());
		//constMouseX = getWidth()/2, constMouseY = getHeight()/2;
		int x = e.getX(), y = e.getY();
		int dx = x-constMouseX;
		int dy = y-constMouseY;
		synchronized(sim.balls){
			Ball b = getPlayerBall();
			b.speed[0] += dx*mouseAcceleratesBallMult;
			b.speed[1] += dy*mouseAcceleratesBallMult;
		}
		//prevMouseX = x;
		//prevMouseY = y;
		Point p = getLocationOnScreen();
		robot.mouseMove(p.x+constMouseX, p.y+constMouseY);
	}
	
	public void mouseDragged(MouseEvent e){ mouseMoved(e); }

	public void mouseClicked(MouseEvent e){
		if(e.getButton() == MouseEvent.BUTTON1){
			synchronized(sim.balls){
				for(Ball b : sim.balls.values()){
					b.speed[0] = 0;
					b.speed[1] = 0;
				}
			}
		}else if(e.getButton() == MouseEvent.BUTTON3){
			synchronized(sim.balls){
				double m = 0;
				for(Ball b : sim.balls.values()){
					m += b.mass();
				}
				double aveMass = m/sim.balls.size();
				for(Ball b : sim.balls.values()){
					b.setMass(aveMass);
				}
			}
		}
	}

	public void mousePressed(MouseEvent e){}

	public void mouseReleased(MouseEvent e){}

	public void mouseEntered(MouseEvent e){}

	public void mouseExited(MouseEvent e){}
	
}
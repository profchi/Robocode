package Profchi;



import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.*;

public class Myrobo4corners extends AdvancedRobot {
	
	static double [] states = new double [41472];
	double d, br,dx,dy, energy, ver, hor,firee;
	double enemy;
	double previous = 0, present;
	double alpha = 0.1;
	double gamma = 0.9;
	double reward = 0;
	static double maxQ;
	static boolean initialised = false; 
	
	
	
	public void run() {
		if(!initialised) {
			
			//for(int i = 0; i < 116640; ++i) {
				//states[i] = Math.random() - 0.5;
				//out.println(states[i]);
			//}
			initialised = true;
		}
		
		while(true) {
			
			turnLeft(180);
			//takeAction();
			
		}
	}
	public void onScannedRobot(ScannedRobotEvent e) {
		//stop();
			//ahead(100);
			//fire(0);
		out.println("scanned");
			stop();
			getParameters(e);
			takeAction();
			scan();
		}

	public void getParameters(ScannedRobotEvent e) {
		//double angle = Math.toRadians((getHeading() + e.getBearing()) % 360);
		//dx = (int)Math.abs(Math.sin(angle) * e.getDistance())/100;
		//dy = (int)Math.abs(getY() + Math.cos(angle) * e.getDistance())/100;
		d = (e.getDistance()+ 200)/401;
		d = (int) d;
		br =  ((e.getBearing() + 180) % 360) / 90;
		br = (int) br;
		enemy = e.getEnergy(); 
		
	}
	
	public void takeAction() {
		dx = (int) getX()/ 100;
		if(dx == 8)
			dx = dx - 1;
		dy = (int) getY() / 100;
		if(dy == 6)
			dy = dy - 1;
		energy = getEnergy() - enemy;
		if(energy > 20)
			energy = 3;
		else if (energy > 0)
			energy = 2;
		else if(energy > -20)
			energy = 1;
		else
			energy = 0;
		int maxposition = 0;
		
		present = (d*13824) + (br*3456) + (dx*432) + (dy*72) + (energy*18);
		//out.println(d + "  " + br);
		//out.println(present);
		
		maxQ = states[(int)present];
		
		for (int i = 0 ; i < 18; ++i) {
			if (states[(int)present + i] > maxQ) {
				maxposition = i;
				maxQ = states[(int)present + i];
			}
		}
		firee = maxposition%2;
		//gun = (maxposition % 9)/ 3;
		hor = (maxposition % 6)/ 2;
		ver = maxposition/6;
		present += maxposition;
		//out.println(maxposition);
		updateStates();
		previous = present;
		
		
		
		setTurnRight((hor - 1)* 90);
		setAhead((ver - 1) * 100);
		if (firee == 1)
			setFire(2);
		execute();
	}
		public void onDeath(DeathEvent e) {
		reward = -3;
		updateStates();
		out.println("deadedadead");
	}
	
	
	public void onHitRobot(HitRobotEvent e) { out.println("hitrobot"); 
		double temp = getEnergy();
		if (temp < energy)
			reward = -0.2;
		else
			reward = 0.1;
		updateStates();
	}
	public void onHitWall (HitWallEvent e) {out.println("hitwall"); reward = -0.2;
	updateStates();
	}
	public void onRobotDeath (RobotDeathEvent e) {
		reward = 3;
		updateStates();
		out.println("gotcha");
		}
	@Override
	public void onBulletHit(BulletHitEvent e) {out.println("hit"); reward = 0.5;
	updateStates();
	}
	public void onBulletMissed(BulletMissedEvent e) { out.println("hitmis"); reward = -0.2;
	updateStates();
	}
	public void onHitByBullet(HitByBulletEvent e ) {
		out.println("hitme");
		reward = -0.5;
		updateStates();
		}
	public void updateStates() {
		states[(int)previous] += alpha*(reward + (gamma*maxQ) - states[(int)previous]);
		out.println(previous + " " + present);
		out.println(states[(int)previous] + " " + states[(int) present]);
		reward = 0;
		
	}
	
}

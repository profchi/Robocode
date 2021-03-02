package Profchi;


import robocode.DeathEvent;
import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.*;

public class First extends Robot {
	
	static double [] states = new double [46080];
	double dx,dy,cdx,cdy, energy, action;
	double previous = 0, present;
	double alpha = 0.1;
	double gamma = 0.9;
	double reward = 0;
	double maxQ;
	static boolean initialised = false; 

	
	
	public void updateStates() {
		states[(int)previous] += alpha*(reward + (gamma*maxQ) - states[(int)previous]);
		out.println(previous + "  " + present);
		out.println(states[(int) previous] + "   " + maxQ);
		reward = 0;
	}
	
	public void run() {
		if(!initialised) {
			
			for(int i = 0; i < 46080; ++i) {
				states[i] = 0.5;
			}
			initialised = true;
			out.println("initialised");
		}
		
		
		while(true) {
			
			turnLeft(90);
			takeAction();
			
		}
	}
	
	public void getParameters(ScannedRobotEvent e) {
		double angle = Math.toRadians((getHeading() + e.getBearing()) % 360);
		dx = (int)Math.abs(Math.sin(angle) * e.getDistance())/100;
		dy = (int)Math.abs(getY() + Math.cos(angle) * e.getDistance())/100;
		
		
	}
	
	public void takeAction() {
		cdx = (int) Math.abs(getX() - 399.5)/50;
		if(cdx == 8)
			cdx = cdx - 1;
		cdy = (int) Math.abs(getY() - 299.5)/50;
		if(cdy == 6)
			cdy = cdy - 1;
		energy = getEnergy();
		if(energy > 65)
			energy = 2;
		else if (energy > 15)
			energy = 1;
		else
			energy = 0;
		int maxposition = 0;
		
		present = (dx*5760) + (dy*720) + (cdx*90) + (cdy*15) + (energy*5);
		
		
		maxQ = states[(int)present];
		
		for (int i = 0 ; i <=4; ++i) {
			if (states[(int)present + i] > maxQ) {
				maxposition = i;
				maxQ = states[(int)present + i];
			}
		}
		present += maxposition;
		out.println(maxposition);
		updateStates();
		previous = present;
		switch(maxposition){
			case 0:
				ahead(105);
				break;
			case 1:
				ahead(-105);
				break;
			case 2:
				turnRight(90);
				ahead(-100);
				break;
			case 3:
				turnRight(-90);
				ahead(-100);
				break;
			case 4:
				fire(2);
				break;
			default:
				break;
			}
			
		
	}
	public void onScannedRobot(ScannedRobotEvent e) {
	//stop();
		//ahead(100);
		//fire(0);
		stop();
		getParameters(e);
		takeAction();
		scan();
	}
	public void onDeath(DeathEvent e) {
		reward = -1;
		updateStates();
		out.println("deadedadead");
	}
	
	
	public void onHitRobot(HitRobotEvent e) { out.println("hitrobot"); reward = -0.1;}
	public void onHitWall (HitWallEvent e) {out.println("hitwall"); reward = -0.1;}
	public void onRobotDeath (RobotDeathEvent e) {
		reward = 1;
		updateStates();
		out.println("gotcha");
		}
	@Override
	public void onBulletHit(BulletHitEvent e) {out.println("hit"); reward = 0.3;}
	public void onBulletMissed(BulletMissedEvent e) { out.println("hitmis"); reward = -0.1;}
	public void onHitByBullet(HitByBulletEvent e ) {
		out.println("hitme");
		reward = -0.3;
		}
	
}

package Profchi;




import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.RobotDeathEvent;

import robocode.ScannedRobotEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class MyRobo2nd extends AdvancedRobot {
	
	static double [] states = new double [58320];
	static ArrayList<Double> outputs = new ArrayList<Double>();
	static int games = 0; 
	static double gamesInSection = 0, wins = 0;
	double d, br,dx,dy, energy, ver, hor, gun, firee;
	double previous = 0, present;
	double alpha = 0.1;
	double gamma = 0.9;
	double reward = 0;
	static double exploration = 1;
	static double maxQ;
	static boolean initialised = false; 
	
	
	
	public void run() {
		if(games % 200 == 0 & games != 0 ) {
			outputs.add(wins/gamesInSection);
			wins = 0;
			gamesInSection = 0;
			
		}
		out.println(exploration);
		if(games == 100000 )
			exploration = 0;
		++ games;
		++gamesInSection;
		
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
		d = e.getDistance()/200.01;
		d = (int) d;
		br =  ((e.getBearing() + 180) % 360) / 90;
		br = (int) br;
		
	}
	
	public void takeAction() {
		dx = (int) getX()/ 200;
		if(dx == 4)
			dx = dx - 1;
		dy = (int) getY() / 200;
		if(dy == 3)
			dy = dy - 1;
		energy = getEnergy();
		if(energy > 65)
			energy = 2;
		else if (energy > 15)
			energy = 1;
		else
			energy = 0;
		int maxposition = 0;
		
		present = (d*11664) + (br*2916) + (dx*729) + (dy*243) + (energy*81);
		
		maxQ = states[(int)present];
		
		for (int i = 0 ; i < 81; ++i) {
			if (states[(int)present + i] > maxQ) {
				maxposition = i;
				maxQ = states[(int)present + i];
			}
		}
		if (Math.random() < exploration) {
			double random;
			do {
				random = Math.random() * 81;
				random = (int)random;
			}
			while (random == maxposition);
			maxposition = (int)random;
			//maxQ = states[(int)present + maxposition];
		}
		
		firee = maxposition%3;
		gun = (maxposition % 9)/ 3;
		hor = (maxposition % 27)/ 9;
		ver = maxposition/27;
		present += maxposition;
		//out.println(maxposition);
		updateStates();
		previous = present;
		
		
		
		setTurnRight((hor - 1)* 90);
		setAhead((ver - 1) * 100);
		setTurnGunRight((gun -1) * 90);
		if (firee ==2)
			setFire(firee);
		execute();
	}
		public void onDeath(DeathEvent e) {
		reward = -3;
		updateStates();
		out.println("deadedadead");
	}
	
	
	public void onHitRobot(HitRobotEvent e) {
		out.println("hitrobot"); 
		double temp = getEnergy();
		if (temp < energy)
			reward = -0.2;
		else
			reward = 0.1;
		updateStates();
	}
	public void onHitWall (HitWallEvent e) {
		out.println("hitwall"); 
		reward = -0.2;
		updateStates();
	}
	public void onRobotDeath (RobotDeathEvent e) {
		++ wins;
		reward = 3;
		updateStates();
		out.println("gotcha");
		}
	@Override
	public void onBulletHit(BulletHitEvent e) {
		out.println("hit"); 
		reward = 0.5;
		updateStates();
	}
	public void onBulletMissed(BulletMissedEvent e) { 
		out.println("hitmis"); 
		reward = -0.2;
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
	public void onBattleEnded(BattleEndedEvent evnt)
	{
		String location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Tracker\\..txt";
		try {
	        BufferedWriter writer = new BufferedWriter(new FileWriter(location));
	            for (double x: outputs) {
	                writer.write(x + "");
	                writer.newLine();
	            }
	            writer.close();
	        } catch (IOException e) {}
		
		location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Tracker\\Qvalues.txt";
		try {
	        BufferedWriter writer = new BufferedWriter(new FileWriter(location));
	            for (double x: states) {
	                writer.write(x + "");
	                writer.newLine();
	            }
	            writer.close();
	        } catch (IOException e) {}
	}
}

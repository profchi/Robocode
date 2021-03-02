package Profchi; 

import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.RobotDeathEvent;

import robocode.ScannedRobotEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;

import neuralnetwork.*;

public class MyRoboNeural extends AdvancedRobot {
	
	static double [] states = new double [93];
	static ArrayList<Double> outputs = new ArrayList<Double>();
	static ArrayList<Double> errors = new ArrayList<Double>();
	
	static double games = 0; 
	static double gamesInSection = 0, wins = 0;
	double distanceToRobot, bearingToRobot,xCordinate,yCordinate, energy; //states
	double motion, turning, moveGun, fire;								//actions
	static double previous = 0, present;
	double alpha = 0.1;
	double gamma = 0.9;
	double reward = 0;
	static boolean doOnce = true;
	static double exploration = 0;
	static double maxQ;
	static boolean initialised = false; 
	int [] myArray = {9, 6, 4, 1};
	static double [] [] trainingInputs = new double [1][];
	static double [] [] prevInputs;
	double [] outNeurons;
	
	static NeuralNetwork profchi = new NeuralNetwork(); 
    
	
	
	public void run() {
		// calculate and store win rate after 200 games
		if(doOnce) {
			profchi.createNeuralNetwork(myArray);
		    profchi.initialiseNetwork();
		   loadWeights();
		    //out.println("loaded");
		    doOnce = false;
		    trainingInputs[0] = new double [9];
		}
		//out.println("loaded");
		if(games % 200 == 0 & games != 0 ) {
			outputs.add(wins/gamesInSection);
			wins = 0;
			gamesInSection = 0;
			
		}
		//set exploration to zero after training
		if(games == 60000 )
			exploration = 0;
		++ games;
		++gamesInSection;
		
		while(true) {
			// Keep turning till robot is scanned
			turnLeft(180);
			
			
		}
	}
	
	public void loadWeights() {
		int index = 0;
		int arraysize = 0;
		String location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Weights\\Spinbot.txt";
		states = readFile(location);
		//out.println(states[92]);
		for (int i = 10; i < profchi.neurons.length ; ++i ) {
			arraysize = profchi.neurons[i].getWeights().length;
			double [] weights = new double[arraysize];
			for(int j = 0; j < arraysize; ++j ) {
				//weights [j] = states[index];
				weights [j] = Math.random()*2 - 1;
				++index;
			}
			profchi.neurons[i].setWeights(weights);
   	 	}
		
	}
	public void onScannedRobot(ScannedRobotEvent e) {
		
			stop();
			prevInputs = trainingInputs;
			getParameters(e);	//Get parameters of robot scanned
			takeAction();
			scan();
		}
	// get robot parameters and convert to position on array
	public void getParameters(ScannedRobotEvent e) {
		distanceToRobot = e.getDistance()/1000;
		trainingInputs[0][0] = distanceToRobot;
		bearingToRobot =  ((e.getBearing() + 180) % 360) / 360;
		trainingInputs[0][1] = bearingToRobot;
	}
		

	public double [] readFile(String fileToRead) {
		int count = 0;
		double val;
		double [] states = new double [93];
		File file = new File(fileToRead);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
		
	        String st;
	        while((st=br.readLine()) != null){
	        	val = Double.parseDouble(st);
	        	states[count] = val;
	            ++count;
	        }
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error Reading");
		}
		return states;
	}
	
	public void takeAction() {
		// Get myrobot parameters and convert to dimension positions
		xCordinate = getX()/ 800;
		trainingInputs[0][2] = xCordinate;
		
		yCordinate = getY() / 600;
		trainingInputs[0][3] = yCordinate;
		
		energy = getEnergy()/ 100;
		trainingInputs[0][4] = energy;
		
		int maxposition = 0;
		maxQ = -1;
		// Equivalent index on Q state space
		//present = (distanceToRobot*11664) + (bearingToRobot*2916) + (xCordinate*729) + (yCordinate*243) + (energy*81);
		
		//maxQ = states[(int)present];
		
		// Get action with maximum Q value in a state
		for (int i = 0 ; i < 54; ++i) {
			motion = i/18;
			trainingInputs[0][5] = motion - 1;
			
			turning = (i % 18)/ 6;
			trainingInputs[0][6] = turning - 1;
			
			moveGun = (i % 6)/ 2;
			trainingInputs[0][7] = moveGun - 1;
			
			fire = i%2;
			if (fire == 0)
				trainingInputs[0][8] = -1;
			else
				trainingInputs[0][8] = 1;
			
			profchi.feedInputs(trainingInputs,0);
			profchi.forwardPropagation();
			outNeurons = profchi.extractOutput();
			if (outNeurons[0] > maxQ) {
				maxposition = i;
				maxQ = outNeurons[0];
				
			}
			
			
		}
		
		motion = maxposition/18;
		trainingInputs[0][5] = motion - 1;
		
		turning = (maxposition % 18)/ 6;
		trainingInputs[0][6] = turning - 1;
		
		moveGun = (maxposition % 6)/ 2;
		trainingInputs[0][7] = moveGun - 1;
		
		fire = maxposition % 2;
		if (fire == 0)
			trainingInputs[0][8] = -1;
		else
			trainingInputs[0][8] = 1;
		
		//profchi.feedInputs(trainingInputs,0);
		profchi.feedInputs(prevInputs,0);
		profchi.forwardPropagation();
		outNeurons = profchi.extractOutput();
		previous = outNeurons[0];
		//Select a random action in the case of exploration
		if (Math.random() < exploration) {
			double random;
			do {
				random = Math.random() * 54;
				random = (int)random;
			}
			while (random == maxposition);
			maxposition = (int)random;
			
		}
		
		
		//Convert to the equivalent action dimension
		motion = maxposition/18;
		
		turning = (maxposition % 18)/ 6;
		
		moveGun = (maxposition % 6)/ 2;
		
		fire = maxposition%2;
		
		
		double t = alpha*(reward/2 + (gamma*maxQ)-previous);
		errors.add(t);
		updateStates();
		//previous = present;
		
		
		// Action for robot to perfrom
		setTurnRight((turning - 1)* 90);
		setAhead((motion - 1) * 100);
		setTurnGunRight((moveGun -1) * 90);
		if (fire > 0)
			//setFire(3);
			setFire(2);
		execute();
	}
	
	//Give rewards for specific events
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
	
	//Apply Q learning 
	public void updateStates() {
		double error;
		previous += alpha*(reward/2 + (gamma*maxQ)-previous);
		//error = present - previous;
		double [] outputToArray = {previous};
        profchi.backwardPropagation(outputToArray);
		reward = 0;
		
	}
	
	// Save survival rate and Q states at end of Battlel
	public void onBattleEnded(BattleEndedEvent evnt)
	{
		
		String location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Results\\R\\Chess.txt";
		try {
	        BufferedWriter writer = new BufferedWriter(new FileWriter(location));
	            for (double x: outputs) {
	                writer.write(x + "");
	                writer.newLine();
	            }
	            writer.close();
	        } catch (IOException e) {}
		
		location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Garbage\\Chess10000.txt";
		try {
	        BufferedWriter writer = new BufferedWriter(new FileWriter(location));
	            for (double x: errors) {
	                writer.write(x + "");
	                writer.newLine();
	            }
	            writer.close();
	        } catch (IOException e) {}
	}
}

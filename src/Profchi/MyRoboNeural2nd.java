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

public class MyRoboNeural2nd extends AdvancedRobot {
	
	static double [] weights = new double [93];
	static double [] states = new double [58320];
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
	static double exploration = 0, stateIndex=0;
	static double maxQ;
	static boolean initialised = false; 
	int [] myArray = {9, 6, 4, 1};
	static double [] [] trainingInputs = new double [1][];
	double [] outNeurons;
	
	double dist, bear;
	
	static NeuralNetwork profchi = new NeuralNetwork(); 
    
	
	
	public void run() {
		// calculate and store win rate after 200 games
		if(doOnce) {
			String location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Tracker\\OffPolicy_E=1_Qvalues.txt";
	    	//String location =  "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Corners\\Greedy,E=0.0_Qvalues.txt";
			states = readFile(location, 58320);
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
		String location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Weights\\Tracker.txt";
		weights = readFile(location, 93);
		//out.println(weights[92]);
		for (int i = 10; i < profchi.neurons.length ; ++i ) {
			arraysize = profchi.neurons[i].getWeights().length;
			double [] weight = new double[arraysize];
			out.println(arraysize);

			for(int j = 0; j < arraysize; ++j ) {
				weight [j] = weights[index];
				out.println(index);
				++index;
			}
			profchi.neurons[i].setWeights(weight);
   	 	}
		
	}
	public void onScannedRobot(ScannedRobotEvent e) {
		
			stop();
			getParameters(e);	//Get parameters of robot scanned
			takeAction();
			scan();
		}
	// get robot parameters and convert to position on array
	public void getParameters(ScannedRobotEvent e) {
		distanceToRobot = e.getDistance()/1000;
		trainingInputs[0][0] = distanceToRobot;
		dist = distanceToRobot/ 0.2001;
		dist = (int) dist;
		bearingToRobot =  ((e.getBearing() + 180) % 360) / 360;
		trainingInputs[0][1] = bearingToRobot;
		bear = bearingToRobot / 0.25;
		bear = (int) bear;
	}
		

	public double [] readFile(String fileToRead, int index) {
		int count = 0;
		double val;
		double [] weights = new double [index];
		File file = new File(fileToRead);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
		
	        String st;
	        while((st=br.readLine()) != null){
	        	val = Double.parseDouble(st);
	        	weights[count] = val;
	            ++count;
	        }
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error Reading");
		}
		return weights;
	}
	
	public void takeAction() {
		// Get myrobot parameters and convert to dimension positions
		xCordinate = getX()/ 800;
		trainingInputs[0][2] = xCordinate;
		
		double xcor = xCordinate / 0.2501;
		xcor = (int) xcor;
		
		yCordinate = getY() / 600;
		trainingInputs[0][3] = yCordinate;
		
		double ycor = yCordinate/0.3334;
		ycor = (int) ycor;
		
		energy = getEnergy()/ 100;
		trainingInputs[0][4] = energy;
		
		double ener = 0;
		if(energy > 0.65)
			ener = 2;
		else if (energy > 0.15)
			energy = 1;
		else
			energy = 0;
		
		int maxposition = 0;
		maxQ = -1;
		// Equivalent index on Q state space
		stateIndex = (dist*11664) + (bear*2916) + (xcor*729) + (ycor*243) + (ener*81);
		
		//maxQ = weights[(int)present];
		
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
		
		stateIndex += (motion*27) + (turning*9) + (moveGun*3);
		
		
		if (fire == 0) 
			trainingInputs[0][8] = -1;
		else {
			stateIndex += 2;
			trainingInputs[0][8] = 1;
		}
		
		profchi.feedInputs(trainingInputs,0);
		profchi.forwardPropagation();
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
		
		//present = maxQ;
		errors.add(states[(int)stateIndex] - maxQ);
		double [] outputToArray = {states[(int)stateIndex]};
	    profchi.backwardPropagation(outputToArray);
	     states[(int)stateIndex] = maxQ;
		//updateStates();
		//previous = present;
		
		
		// Action for robot to perfrom
		setTurnRight((turning - 1)* 90);
		setAhead((motion - 1) * 100);
		setTurnGunRight((moveGun -1) * 90);
		if (fire > 0)
			setFire(2);
		execute();
	}
	
	//Give rewards for specific events
		public void onDeath(DeathEvent e) {
		reward = -3;
		//updateStates();
		out.println("deadedadead");
	}
	
	
	public void onHitRobot(HitRobotEvent e) {
		out.println("hitrobot"); 
		double temp = getEnergy();
		if (temp < energy)
			reward = -0.2;
		else
			reward = 0.1;
		//updateStates();
	}
	public void onHitWall (HitWallEvent e) {
		out.println("hitwall"); 
		reward = -0.2;
		//updateStates();
	}
	public void onRobotDeath (RobotDeathEvent e) {
		++ wins;
		reward = 3;
		//updateStates();
		out.println("gotcha");
		}
	@Override
	public void onBulletHit(BulletHitEvent e) {
		out.println("hit"); 
		reward = 0.5;
		//updateStates();
	}
	public void onBulletMissed(BulletMissedEvent e) { 
		out.println("hitmis"); 
		reward = -0.2;
		//updateStates();
	}
	public void onHitByBullet(HitByBulletEvent e ) {
		out.println("hitme");
		reward = -0.5;
		//updateStates();
		}
	
	//Apply Q learning 
	public void updateStates() {
		double error;
		//error = alpha*(reward + (gamma*maxQ)-previous);
		//error = present - previous;
		//double [] outputToArray = {present + error};
       // profchi.backwardPropagation(outputToArray);
		//weights[(int)previous] += alpha*(reward + (gamma*maxQ) - weights[(int)previous]);
		//out.println(previous + " " + present);
		//out.println(weights[(int)previous] + " " + weights[(int) present]);
		reward = 0;
		
	}
	
	// Save survival rate and Q weights at end of Battlel
	public void onBattleEnded(BattleEndedEvent evnt)
	{
		
		String location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Results\\S\\Tracker.txt";
		try {
	        BufferedWriter writer = new BufferedWriter(new FileWriter(location));
	            for (double x: outputs) {
	                writer.write(x + "");
	                writer.newLine();
	            }
	            writer.close();
	        } catch (IOException e) {}
		
		location = "C:\\Users\\CHINEDU PC\\eclipse-workspace\\Robocodetest\\src\\outputs\\Garbage\\TrackerErrors10000.txt";
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

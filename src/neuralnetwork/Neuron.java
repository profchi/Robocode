/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neuralnetwork;
import java.util.ArrayList;
import java.util.*;
/**
 *
 * @author CHINEDU PC
 */
public class Neuron {
    private double [] inputs;
    private double output;
    private double [] weights;
    private double [] previousWeights;
    public List<Neuron> inputNeuron;
    public List<Neuron> outputNeuron;
    private String type;
    private String activationFunction;
    private double errorSignal;
    private static double learningRate = 0.1;
    private static boolean bipolar = true;
    private static double momentum = 0.2;

    
    public Neuron(String s){
        Initialise(s);
        this.activationFunction = "S";
        
    }
    public Neuron(String s, String activationFunction){
        Initialise(s);
        this.activationFunction = activationFunction;
    }
     //Initialises neuron with it's the layer it falss under
    public void Initialise(String s){
        inputNeuron = new ArrayList<Neuron>();
        outputNeuron = new ArrayList<Neuron>();
        type = s;
        output = 0;
        errorSignal = 0;
    }   
    // returns the input signals to a neuron
    public double [] getInputs(){
        return this.inputs;  
    }
    // returns the weights of a neuron
    public double [] getWeights(){
        return this.weights;  
    }
    //returns the output of a neuron
    public double  getOutput(){
        return this.output;  
    }
    // returns the error signal of a neuron
    public double  getErrorSignal(){
        return this.errorSignal;  
    }
    public void setInputs(double [] value){
        this.inputs = value;  
    }
    // sets the weights to a neuron to particular values
    public void setWeights(double [] value){
        this.weights = value;  
    }
    
    public void setPreviousWeights(double [] value){
        this.previousWeights = value;  
    }
    // returns if a neuron is in input, output or hidden layer
    public String  getType(){
        return this.type;  
    }
    // returns the activation fuction of a euron
    public String getActivationFunction(){
        return this.activationFunction;  
    }
    // Sets the activation functtion of a neuron
    public void setActivationFunction(String value){
        this.activationFunction = value;  
    }
    // Links a neuron to a neuron which it's outputsignal goes to
    public void addOutputNeuron(Neuron neuron){
        outputNeuron.add(neuron);
    } 
    //Links a neuron to an input neuron to that particular neuron
    public void addInputNeuron(Neuron neuron){
        inputNeuron.add(neuron);
        neuron.addOutputNeuron(this);
    }
    //returns the number of neurons the neurons output is connected to
    public int numberOfOutputNeurons(){
        return outputNeuron.size();
    }
    
    //Updates the output of a neuron based on the input signal and weights
    public void updateOutput(double value){
        if( type == "input")
            output = value;
        else{
            output = 0;
            for(int i = 0; i < inputNeuron.size(); ++i){
                inputs[i] = inputNeuron.get(i).getOutput();
                output += weights[i]*inputs[i];
            }
            output = activationFunction(this, output);
         }
    } 
    //Updates the error signal of a neuron
    public void updateErrorSignal(int neuronIndexOnLayer, double [] output){
        if(this.inputNeuron == null)
            return;
        double sumOfWeightedOutputNeuron = 0, result;
        if (getType() == "hidden"){
            for (int i = 0; i < this.outputNeuron.size(); ++i){
                sumOfWeightedOutputNeuron += outputNeuron.get(i).getErrorSignal()*outputNeuron.get(i).getWeights()[neuronIndexOnLayer];
            }
        }
        else if(getType() == "output")
            sumOfWeightedOutputNeuron = output[neuronIndexOnLayer] - getOutput();
        errorSignal = sumOfWeightedOutputNeuron * Derivative(this,getOutput());
        
    }
    // Updates the input weights of a neuron
    public void updateWeights(){
        double [] weightDifference = new double [weights.length];
        for (int i = 0; i < weights.length; ++i){
            weightDifference[i] = weights[i] - previousWeights[i];
            previousWeights[i] = weights[i];                
            weights[i] = weights[i] + (momentum * weightDifference[i]) + (learningRate*errorSignal*inputNeuron.get(i).getOutput());
            
        }
    }
    
     public static double activationFunction(Neuron neuron, double output){
        double result = output;
        //Sigmoid
        if (neuron.getActivationFunction() == "S"){
            if (bipolar)
                result = (1 - Math.exp(-1*output))/(1 + Math.exp(-1*output));
            else
                result = 1/(1 + Math.exp(-1*output));
        }
        //ReLU
        else if(neuron.getActivationFunction() == "R"){
            result = Math.exp(output);
            result = Math.log(1 + result);
        }
        //TanH
        else if(neuron.getActivationFunction() == "T"){
            result = (Math.exp(output) - Math.exp(-1*output))/(Math.exp(output) + Math.exp(-1*output));
        }
        return result;
    }
    public static double Derivative(Neuron neuron, double output){
        double result = output;
        //Sigmoid
        if (neuron.getActivationFunction() == "S"){
            if (bipolar)
                result =(1-(output*output))/2;
            else
                result = output*(1 - output);
        }
        //ReLU
        else if(neuron.getActivationFunction() == "R"){
            result = Math.log(Math.exp(output) - 1);
            result = 1/(1 + Math.exp(-1*output)); 
        }
        //TanH
        else if(neuron.getActivationFunction() == "T"){
            result = 1 - (output*output);
        }    
        return result;
    }
}

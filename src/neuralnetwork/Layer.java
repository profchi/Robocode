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
public class Layer {
    
    private List<Neuron> layer;
    private String type;       
    //defines if it's an input output or hidden layer
    
    public Layer (String s) {
        layer = new ArrayList<Neuron>();
        type = s;
    }

    // Adds a neuron to a layer
    public void addNeuron(Neuron neuron){
        layer.add(neuron);
    }
    
    // Get's the number of neurons in a layer
    public int layerSize(){
        return layer.size();
    }
    
    //Returns a neuron at a particular index in the layer
    public Neuron getNeuron(int i){
        return layer.get(i);
    }
}

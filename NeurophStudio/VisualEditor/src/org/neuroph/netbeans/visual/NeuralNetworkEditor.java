package org.neuroph.netbeans.visual;

import org.netbeans.api.visual.widget.Widget;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.input.InputFunction;
import org.neuroph.core.input.WeightedSum;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.netbeans.visual.widgets.NeuronConnectionWidget;
import org.neuroph.netbeans.visual.widgets.NeuronWidget;
import org.neuroph.nnet.comp.layer.CompetitiveLayer;
import org.neuroph.nnet.comp.layer.InputLayer;
import org.neuroph.nnet.comp.neuron.CompetitiveNeuron;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.NeuronFactory;
import org.neuroph.util.NeuronProperties;

/**
 *
 * @author Ana
 */
public class NeuralNetworkEditor {

    NeuralNetwork neuralNet;

    public NeuralNetworkEditor(NeuralNetwork neuralNet) {
        this.neuralNet = neuralNet;
    }

    public void addCompetitiveLayer(int numOfNeurons, NeuronProperties neuronProperties, int neuralNetWidgetChildrenSize) {
        CompetitiveLayer newCompetitiveLayer = new CompetitiveLayer(numOfNeurons, neuronProperties);
        neuralNet.addLayer(neuralNetWidgetChildrenSize, newCompetitiveLayer);

    }

    public void addCustomLayer(Class<? extends Neuron> someNeuron, Class<? extends TransferFunction> someTF, Class<? extends InputFunction> someIF, int numberOfNeurons, int layerIdx) {
        Layer newLayer = new Layer();
        for (int j = 0; j < numberOfNeurons; j++) {
            Neuron newNeuron = NeuronFactory.createNeuron(new NeuronProperties(someNeuron, someIF, someTF));
            newLayer.addNeuron(newNeuron);
        }
        neuralNet.addLayer(layerIdx, newLayer);
    }

    public void addInputLayer(int numberOfNeurons) {
        InputLayer layer = new InputLayer(numberOfNeurons);
        neuralNet.addLayer(0, layer);
        neuralNet.setInputNeurons(layer.getNeurons());
    }

    public void addEmptyLayer(int index, Layer layer) {
        neuralNet.addLayer(index, layer);
    }

    public void addCompetitiveNeuron(TransferFunction transferFunction, Layer layer) {
        CompetitiveNeuron competitiveNeuron = new CompetitiveNeuron(new WeightedSum(), transferFunction);
        layer.addNeuron(competitiveNeuron);
    }

    public void addNeuronAt(Layer layer, Neuron neuron, int index) {
        layer.addNeuron(index, neuron);
    }

    public void addNeuron(Layer layer, Neuron neuron) {
        layer.addNeuron(neuron);
    }

    public void setNeuronInputFunction(Neuron neuron, InputFunction inputFunction) {
        neuron.setInputFunction(inputFunction);
    }

    public void setLayerInputFunction(Layer layer, Class<? extends InputFunction> inputFunction) throws Exception {
        for (int i = 0; i < layer.getNeuronsCount(); i++) {
            layer.getNeuronAt(i).setInputFunction(inputFunction.newInstance());
        }
    }

    public void setNeuronTransferFunction(Neuron neuron, TransferFunction transferFunction) {
        neuron.setTransferFunction(transferFunction);
    }

    public void setLayerTransferFunction(Layer layer, Class<? extends TransferFunction> transferFunction) throws Exception {
        for (int i = 0; i < layer.getNeuronsCount(); i++) {
            layer.getNeuronAt(i).setTransferFunction(transferFunction.newInstance());
        }
    }

    public void setLayerLabel(Layer layer, String label) {
        layer.setLabel(label);

    }

    public void setNeuronLabel(Neuron neuron, String label) {
        neuron.setLabel(label);
    }

    public void setLearningRule(LearningRule learningRule) {
        neuralNet.setLearningRule(learningRule);
    }

//    public void setShowConnections(NeuralNetworkScene scene) {
//        scene.setShowConnections(!scene.isShowConnections());
//    }
    public void removeNeuron(Neuron neuron) {
        Layer layer = neuron.getParentLayer();
        layer.removeNeuron(neuron);
    }

    public void removeLayer(Layer layer) {
        layer.getParentNetwork().removeLayer(layer);
    }

    public void removeAllInputConnections(Layer layer) {
        for (Neuron n : layer.getNeurons()) {
            n.removeAllInputConnections();
        }
    }

    public void removeAllOutputConnections(Layer layer) {
        for (Neuron n : layer.getNeurons()) {
            n.removeAllOutputConnections();
        }
    }

    public void removeAllInputConnections(Neuron neuron) {
        neuron.removeAllInputConnections();
    }

    public void removeAllOutputConnections(Neuron neuron) {
        neuron.removeAllOutputConnections();
    }

    public void removeConnection(Widget srcWidget, Widget trgWidget) {
        if (trgWidget == null) {
            ((NeuronConnectionWidget) srcWidget).removeConnection();
            srcWidget.removeFromParent();
        } else {
            ((NeuronWidget) trgWidget).getNeuron().removeInputConnectionFrom(((NeuronWidget) srcWidget).getNeuron());
        }
    }

    public void createFullConnection(int index) {
        Layer fromLayer = neuralNet.getLayerAt(index - 1);
        Layer toLayer = neuralNet.getLayerAt(index);
        ConnectionFactory.fullConnect(fromLayer, toLayer);
    }

    public void createDirectConnection(int index) {
        Layer fromLayer = neuralNet.getLayerAt(index - 1);
        Layer toLayer = neuralNet.getLayerAt(index);
        int number = 0;
        if (fromLayer.getNeuronsCount() > toLayer.getNeuronsCount()) {
            number = toLayer.getNeuronsCount();
        } else {
            number = fromLayer.getNeuronsCount();
        }

        for (int i = 0; i < number; i++) {
            Neuron fromNeuron = fromLayer.getNeurons()[i];
            Neuron toNeuron = toLayer.getNeurons()[i];
            ConnectionFactory.createConnection(fromNeuron, toNeuron);
        }
    }

    public void setAsInputNeuron(NeuronWidget neuronWidget) {
        boolean inputNeuronAdded = false;
        Neuron neuron = neuronWidget.getNeuron();
        Layer layer = neuron.getParentLayer();
        Layer firstLayer = neuralNet.getLayerAt(0);
        Neuron[] inputNeurons = neuralNet.getInputNeurons();
        Neuron[] newInputNeurons = new Neuron[inputNeurons.length + 1];
        if (layer != firstLayer) {
            layer.removeNeuron(neuron);
            if (isOutputNeuron(neuron)) {
                removeFromOutputNeurons(neuron);
            }
            firstLayer.addNeuron(0, neuron);
        }
        for (int i = 0, j = 0; i < newInputNeurons.length; i++) {
            if (neuron == firstLayer.getNeuronAt(i) && !inputNeuronAdded) {
                newInputNeurons[i] = neuron;
                inputNeuronAdded = true;
            } else {
                newInputNeurons[i] = inputNeurons[j];
                j++;
            }
        }
        neuralNet.setInputNeurons(newInputNeurons);
    }

    public void setAsOutputNeuron(NeuronWidget neuronWidget) {
        boolean outputNeuronAdded = false;
        Neuron neuron = neuronWidget.getNeuron();
        Layer layer = neuron.getParentLayer();
        Layer lastLayer = neuralNet.getLayerAt(neuralNet.getLayersCount() - 1);
        Neuron[] outputNeurons = neuralNet.getOutputNeurons();
        Neuron[] newOutputNeurons = new Neuron[outputNeurons.length + 1];
        if (layer != lastLayer) {
            layer.removeNeuron(neuron);
            if (isInputNeuron(neuron)) {
                removeFromInputNeurons(neuron);
            }
            lastLayer.addNeuron(neuron);
        }
        for (int i = 0, j = 0; i < newOutputNeurons.length; i++) {
            if (neuron == neuron.getParentLayer().getNeuronAt(i) && !outputNeuronAdded) {
                newOutputNeurons[i] = neuron;
                outputNeuronAdded = true;
            } else {
                newOutputNeurons[i] = outputNeurons[j];
                j++;
            }
        }
        neuralNet.setOutputNeurons(newOutputNeurons);

    }

    public boolean isInputNeuron(Neuron neuron) {
        for (Neuron inputNeuron : neuralNet.getInputNeurons()) {
            if (inputNeuron == neuron) {
                return true;
            }
        }
        return false;
    }

    public boolean isOutputNeuron(Neuron neuron) {
        for (Neuron outputNeuron : neuralNet.getOutputNeurons()) {
            if (outputNeuron == neuron) {
                return true;
            }
        }
        return false;
    }

    private void removeFromOutputNeurons(Neuron neuron) {
        Neuron[] outputNeurons = neuralNet.getOutputNeurons();
        Neuron[] newOutputNeurons = new Neuron[neuralNet.getOutputNeurons().length - 1];
        int j = 0;
        for (int i = 0; i < outputNeurons.length; i++) {
            if (outputNeurons[i] != neuron) {
                newOutputNeurons[j] = outputNeurons[i];
                j++;
            }
        }
        neuralNet.setOutputNeurons(newOutputNeurons);
    }

    private void removeFromInputNeurons(Neuron neuron) {
        Neuron[] inputNeurons = neuralNet.getInputNeurons();
        Neuron[] newInputNeurons = new Neuron[neuralNet.getInputNeurons().length - 1];
        int j = 0;
        for (int i = 0; i < inputNeurons.length; i++) {
            if (inputNeurons[i] != neuron) {
                newInputNeurons[j] = inputNeurons[i];
                j++;
            }
        }
        neuralNet.setInputNeurons(newInputNeurons);
    }
}
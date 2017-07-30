package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.FeatureNode;
import ir.ac.iust.dml.kg.raw.distantsupervison.Configuration;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.DbHandler;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

/**
 * Created by hemmatan on 7/8/2017.
 */
public class DeepClassifier extends Classifier {

    private MultiLayerNetwork multiLayerNetwork;
    private DataNormalization normalizer = new NormalizerStandardize();

    public DeepClassifier() {
        super();
    }

    public MultiLayerNetwork getMultiLayerNetwork() {
        return multiLayerNetwork;
    }

    public DataNormalization getNormalizer() {
        return normalizer;
    }

    public void train(int maximumNumberOfTrainingExamples) {

        CorpusDbHandler trainDbHandler = new CorpusDbHandler(DbHandler.trainTableName);

        if (Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.LOAD_DATA_FROM_DB)) {
            trainDbHandler.load(trainData);
        } else {
            createTrainData(maximumNumberOfTrainingExamples, trainDbHandler);
        }
        trainDbHandler.close();

        if (!Configuration.trainingSetMode.equalsIgnoreCase(Constants.trainingSetModes.LOAD_DATA_FROM_CSV)) {
            initializeModels(true);
            extractProblemParamsFromDBData();//problem.x , problem.y , problem.n , problem.l
            FeatureNode[][] featureNodes = new FeatureNode[problem.l][];
            FeatureExtractor.convertFeatureNodesToCSVandSave(featureNodes, problem.y, SharedResources.trainCSV);
            this.numberOfClasses = trainData.getNumberOfClasses().intValue();
        }


        //DataSet DataSetBatch = loadTrainDataFromCSV();


        final int numInputs = this.numberOfFeatures;
        int outputNum = this.numberOfClasses;
        int iterations = 1000;
        long seed =500;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                //.activation(Activation.SIGMOID)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.ADADELTA)
                .learningRate(0.005)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder().activation(Activation.SIGMOID).nIn(numInputs).nOut(2000).build())
                .layer(1, new OutputLayer.Builder().activation(Activation.SOFTMAX).nIn(2000).nOut(outputNum).build())
                /*.layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(numInputs).nOut(1000).build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX).nIn(1000).nOut(outputNum).build())*/
                .backprop(true).pretrain(false)
                .build();

        //run the model
        multiLayerNetwork = new MultiLayerNetwork(conf);
        multiLayerNetwork.init();
        multiLayerNetwork.setListeners(new ScoreIterationListener(500));

        int cnt = 0;
        while(iteratorTrain.hasNext()){
            System.out.println(++cnt);
            DataSetBatch = iteratorTrain.next();
            if (cnt==1) normalizer.fit(DataSetBatch);//Collect the statistics (mean/stdev) from the training data. This does not modify the input data
            normalizer.transform(DataSetBatch);     //Apply normalization to the training data
            multiLayerNetwork.fit(DataSetBatch);
        }

        //Save the model
        File locationToSave = new File("MyMultiLayerNetwork.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally
        boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
        try {
            ModelSerializer.writeModel(multiLayerNetwork, locationToSave, saveUpdater);
            ModelSerializer.addNormalizerToModel(locationToSave, normalizer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadNetworkAndNormalizer() {
        File locationToSave = new File("MyMultiLayerNetwork.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally
        boolean saveUpdater = true;
        try {
            MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
            this.normalizer = ModelSerializer.restoreNormalizerFromFile(locationToSave);
            this.multiLayerNetwork = restored;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testOnGoldCSV() {
        int batchSizeTest = 5560;
        DataSetIterator iteratorTest = null;
        try {
            iteratorTest = readCSVDataset(SharedResources.testCSV,
                   batchSizeTest , this.getNumberOfFeatures(), this.getNumberOfClasses());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DataSet testDataSetBatch = null;
        testDataSetBatch = iteratorTest.next();
        normalizer.transform(testDataSetBatch);     //Apply normalization to the training data
        INDArray output = multiLayerNetwork.output(testDataSetBatch.getFeatureMatrix());
        Evaluation eval = new Evaluation(this.getNumberOfClasses());
        eval.eval(testDataSetBatch.getLabels(), output);
        System.out.println(eval.stats());
    }
}

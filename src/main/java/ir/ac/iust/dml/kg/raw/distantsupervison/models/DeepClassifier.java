package ir.ac.iust.dml.kg.raw.distantsupervison.models;

import de.bwaldvogel.liblinear.FeatureNode;
import ir.ac.iust.dml.kg.raw.distantsupervison.Configuration;
import ir.ac.iust.dml.kg.raw.distantsupervison.Constants;
import ir.ac.iust.dml.kg.raw.distantsupervison.SharedResources;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.CorpusDbHandler;
import ir.ac.iust.dml.kg.raw.distantsupervison.database.DbHandler;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.lossfunctions.LossFunctions;

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
        int iterations = 200;
        long seed =100;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .activation(Activation.SIGMOID)
                .weightInit(WeightInit.XAVIER)
                .learningRate(0.001)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(numInputs).nOut(1000).build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX).nIn(1000).nOut(outputNum).build())
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
            normalizer.fit(DataSetBatch);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
            normalizer.transform(DataSetBatch);     //Apply normalization to the training data
            multiLayerNetwork.fit(DataSetBatch);
        }

    }
}
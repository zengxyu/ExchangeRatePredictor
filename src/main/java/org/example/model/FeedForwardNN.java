package org.example.model;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.example.util.Constant;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class FeedForwardNN {
    //Random number generator seed, for reproducability 随机数生成器种子
    public static final int seed = 12345;
    //Network learning rate 学习率
    public static final double learningRate = 0.01;

    public static final int numInputs = Constant.N;
    private static final int numOutputs = Constant.M;

    /** Returns the network configuration, 2 hidden DenseLayers of size 64.
     * 配置网络，
     * 以下配置是一个极为简单的神经网络模型，一个输入层，两个隐藏层，一个输出层
     * 输入层有numInputs = N = 5（与开题报告中的N一致）个输入
     * 隐藏层有numHiddenNodes = 64 个神经元
     * 输出层有numOutputs = M （与开题报告中的N一致）个输出
     */
    public static MultiLayerConfiguration getDeepDenseLayerNetworkConfiguration1() {
        final int numHiddenNodes = 64;
        return new NeuralNetConfiguration.Builder()
            .seed(seed)
            .weightInit(WeightInit.XAVIER)
            .updater(new Nesterovs(learningRate, 0.9))
            .list()
            .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                .activation(Activation.TANH).build())
            .layer(new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                .activation(Activation.TANH).build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .activation(Activation.IDENTITY)
                .nIn(numHiddenNodes).nOut(numOutputs).build())
            .build();
    }

    /**
     * Returns the network configuration, 2 hidden DenseLayers of size 64.
     * 配置网络，这是另外一个网络，一个输入层，三个隐藏层，一个输出层
     * 输入层有numInputs = N = 5（与开题报告中的N一致）个输入
     * 隐藏层1有numHiddenNodes = 128 个神经元
     * 隐藏层2有numHiddenNodes = 64 个神经元
     * 隐藏层3有numHiddenNodes = 32 个神经元
     * 输出层有numOutputs = M （与开题报告中的N一致）个输出
     */
    public static MultiLayerConfiguration getDeepDenseLayerNetworkConfiguration2() {
        final int numHiddenNodes1 = 128;
        final int numHiddenNodes2 = 64;
        final int numHiddenNodes3 = 32;
        return new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, 0.9))
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes1)
                        .activation(Activation.TANH).build())
                .layer(new DenseLayer.Builder().nIn(numHiddenNodes1).nOut(numHiddenNodes2)
                        .activation(Activation.TANH).build()).layer(new DenseLayer.Builder().nIn(numHiddenNodes2).nOut(numHiddenNodes3)
                        .activation(Activation.TANH).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(numHiddenNodes3).nOut(numOutputs).build())
                .build();
    }
}

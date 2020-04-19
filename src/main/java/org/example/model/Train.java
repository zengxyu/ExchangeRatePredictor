package org.example.model;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.example.util.Constant;
import org.example.util.PlotLine;
import org.example.util.PlotScatter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Train {
    private static final Logger log = LoggerFactory.getLogger(Train.class);

    /**
     * 首先，先制作训练集测试集
     */
    public static void preProcessData() {
        PreprocessData.getInstance().preProcessData(Constant.SAMPLE_FILE_NAME);
    }

    /**
     * 训练模型，并保存
     */
    public static MultiLayerNetwork trainModel(String trainFilePath) throws IOException, InterruptedException {


        //初始化用户界面后端
        UIServer uiServer = UIServer.getInstance();

        //设置网络信息（随时间变化的梯度、分值等）的存储位置。这里将其存储于内存。
        StatsStorage statsStorage = new InMemoryStatsStorage();         //或者： new FileStatsStorage(File)，用于后续的保存和载入

        //将StatsStorage实例连接至用户界面，让StatsStorage的内容能够被可视化
        uiServer.attach(statsStorage);


        //迭代次数
        int numEpoch = 40;
        //获取训练数据和测试数据
        MultiDataSetIterator trainIterator = RateDataReader.readDataset(trainFilePath, Constant.TRAIN_BATCH_SIZE);

        //开始训练
        log.info("Initialize nn model...");
        MultiLayerNetwork model = new MultiLayerNetwork(FeedForwardNN.getDeepDenseLayerNetworkConfiguration1());
        model.init();

        log.info("Start training...");
        model.setListeners(new ScoreIterationListener(1),new StatsListener(statsStorage));
        //然后添加StatsListener来在网络定型时收集这些信息
        model.fit(trainIterator, numEpoch);
        log.info("Finish training...");

        return model;
    }

    /**
     * 评估模型
     */
    public static Map<String, Object> testModel(MultiLayerNetwork model, String testFilePath, double mean) throws IOException, InterruptedException, ParseException {
        Map<String, Object> hashMap = new HashMap<>();
        MultiDataSetIterator testIterator = RateDataReader.readDataset(testFilePath, Constant.TEST_BATCH_SIZE);
        MultiDataSetIterator dateIterator = RateDataReader.readDateDataset(testFilePath,Constant.TEST_BATCH_SIZE);

        List<Long> dateList = new ArrayList<>();
        List<Double> realLabels = new ArrayList<>();
        List<Double> predictedLabels = new ArrayList<>();
        log.info("Evaluating...");
        while (testIterator.hasNext()) {
            MultiDataSet dataSet = testIterator.next();
            MultiDataSet dSet= dateIterator.next();
            INDArray feature = dataSet.getFeatures()[0];
            long date = dSet.getLabels()[0].getLong(0);
            double realLabel = dataSet.getLabels()[0].getDouble(0) + mean;
            double predictedLabel = model.output(feature, false).getDouble(0) + mean;

            dateList.add(date);
            realLabels.add(realLabel);
            predictedLabels.add(predictedLabel);

            log.info("Features:" + feature.toString());
            log.info("Real Result:" + String.valueOf(realLabel));
            log.info("Prediction Result:" + String.valueOf(predictedLabel));
            log.info("================================");
        }
        hashMap.put(Constant.PLOT_DATE_LIST,dateList);
        hashMap.put(Constant.PLOT_REAL_LABEL_LIST, realLabels);
        hashMap.put(Constant.PLOT_PREDICTED_LABEL_LIST, predictedLabels);
        return hashMap;
    }

    /**
     * 保存模型
     */
    public static void saveModel(MultiLayerNetwork model, String savePath) throws IOException {
        log.info("Save model...");
        File modelFile = new File(Constant.MODEL_BASE_DIR + "/" + savePath + ".zip");
        if (modelFile.exists()){
            modelFile.delete();
        }
        ModelSerializer.writeModel(model, modelFile, true);//保存训练好的网络
        log.info("Finish saving model");
    }

    /**
     * 可视化数据（比较模型预测值与真实值）
     * 线
     * @param  map
     */
    public static void plotLine(Map<String,Object> map,boolean predict) {
        SwingUtilities.invokeLater(() -> {
            PlotLine example = new PlotLine(map,predict);
            example.setSize(800, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {

        preProcessData();
        String[] FNames = Constant.FNames;
        String[] currency = Constant.CURRENCY;
        List<Double> meanList = PreprocessData.getInstance().getMeanList();
        for (int i = 0; i < currency.length; i++) {
            //该货币的平均汇率
            double mean = meanList.get(i);
            // 模型的保存位置
            String savePath = Constant.MODEL_SAVE_PATH_Prefix + currency[i];
            //训练模型
            MultiLayerNetwork model = trainModel(FNames[i * 3]);
            //保存模型
            saveModel(model, savePath);
            //评估模型，返回真实标签和预测标签
            Map<String, Object> hashMap = testModel(model, FNames[i * 3 + 1], mean);
            //画图可视化真实标签和预测标签
            String title = Constant.CURRENCY[i] + "Comparison chart : Real Labels VS Predicted Labels";
            hashMap.put(Constant.PLOT_TITLE,title);
            plotLine(hashMap,true);
        }

    }

}

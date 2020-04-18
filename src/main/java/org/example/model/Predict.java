package org.example.model;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.example.util.Constant;
import org.example.util.PlotLine;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.shape.Concat;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

public class Predict {
    private static final Logger log = LoggerFactory.getLogger(Predict.class);

    /**
     * 加载已经训练好的模型
     */
    public static MultiLayerNetwork loadModel(String filePath) throws IOException {
        MultiLayerNetwork restored_model = ModelSerializer.restoreMultiLayerNetwork(filePath);
        return restored_model;
    }

    /**
     * 可视化数据（比较模型预测值与真实值）
     * 线
     *
     * @param title
     * @param realLabels
     * @param predictedLabels
     */
    public static void plotLine(String title, List<Long> dateList, List<Double> realLabels, List<Double> predictedLabels) {
        SwingUtilities.invokeLater(() -> {
            PlotLine example = new PlotLine(title, dateList, realLabels, predictedLabels);
            example.setSize(800, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        for (int i = 0; i < Constant.CURRENCY.length; i++) {
            String modelPath = Constant.MODEL_BASE_DIR + "/" + Constant.MODEL_SAVE_PATH_Prefix + Constant.CURRENCY[i] + ".zip";

            log.info("...开始加载模型" + modelPath + "...");
            MultiLayerNetwork model = loadModel(modelPath);
            MultiDataSetIterator predictIterator = RateDataReader.readDataset(Constant.FNames[3 * i + 2], Constant.TEST_BATCH_SIZE);;
            MultiDataSetIterator dateIterator = RateDataReader.readDateDataset(Constant.FNames[3 * i + 2],Constant.TEST_BATCH_SIZE);
            log.info("...完成加载模型...");
            //输入数据
            log.info("...输入数据...");
            List<Double> realLabels = new ArrayList<>();
            List<Double> predictedLabels = new ArrayList<>();
            //预测一条数据
            if (predictIterator.hasNext()) {
                MultiDataSet dataSet = predictIterator.next();
                MultiDataSet dSet = dateIterator.next();
                INDArray feature = dataSet.getFeatures()[0];
                INDArray dateArray = dSet.getLabels()[0];
                INDArray realLabel = dataSet.getLabels()[0];
                INDArray predictedLabel = model.output(feature, false);

                List<Long> dateList = INDLongArray2List(dateArray);
                List<Double> realLabelList = INDDoubleArray2List(realLabel);
                List<Double> predictedLabelList = INDDoubleArray2List(predictedLabel);
                Double mean = PreprocessData.getInstance().getMeanList().get(i);
                unNormaliseList(realLabelList,mean);
                unNormaliseList(predictedLabelList,mean);
                log.info("Features:" + feature.toString());
                log.info("Real Result:" + String.valueOf(realLabelList));
                log.info("Prediction Result:" + String.valueOf(predictedLabelList));
                plotLine(Constant.CURRENCY[i], dateList, realLabelList, predictedLabelList);
                log.info("================================");
            }
            log.info("...开始预测...");
        }
    }

    public static void unNormaliseList(List<Double> predictedLabelList, Double mean) {
        for (int i = 0; i < predictedLabelList.size(); i++) {
            predictedLabelList.set(i,predictedLabelList.get(i)+mean);
        }
    }

    public static List<Long> INDLongArray2List(INDArray array) {
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < array.rows(); i++) {
            for (int j = 0; j < array.columns(); j++) {
                list.add(array.getLong(i, j));
            }
        }
        return list;
    }

    public static List<Double> INDDoubleArray2List(INDArray array) {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < array.rows(); i++) {
            for (int j = 0; j < array.columns(); j++) {
                list.add(array.getDouble(i, j));
            }
        }
        return list;
    }

}

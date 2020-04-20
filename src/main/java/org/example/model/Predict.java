package org.example.model;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.example.util.Constant;
import org.example.util.PlotLine;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.shape.Concat;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

import static org.example.util.Constant.CURRENCY;

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
     * @param map
     */
    public static void plotLine(Map<String, Object> map, boolean predict) {
        SwingUtilities.invokeLater(() -> {
            PlotLine example = new PlotLine(map, predict);
            example.setSize(800, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }

    /**
     * 根据输入数据获取规范化的特征
     * @param inputRates
     * @param mean
     * @return
     */

    public static INDArray getFeatures(double[][] inputRates, double mean) {
        if (inputRates[0].length != Constant.N) {
            log.error("您输入的数据个数与模型输入单元数量N不一致，请检查是否N=" + Constant.N + "个");
            System.exit(0);
        }
        //normalize
        for (int i = 0; i < inputRates[0].length; i++) {
            inputRates[0][i] -= mean;
        }
        INDArray feature = Nd4j.create(inputRates);
        return feature;
    }

    public static void main(String[] args) throws IOException {
        double[][] inputRates = new double[][]{{}};
        //输入货币类型： 美元-0, 欧元-1, 英镑-2
        Integer currencyType = 0;

        //输入数据
        String fileName = "predict_data.csv";
        File file = new File(new File(Constant.BASE_DIR), fileName);
        if (file.isFile() && file.exists())
        { // 判断文件是否存在
            InputStreamReader in = new InputStreamReader(new FileInputStream(file),"UTF-8");// 考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(in);
            currencyType = Integer.valueOf(bufferedReader.readLine());
            String[] data = bufferedReader.readLine().split(",");
            inputRates[0] = Stream.of(data).mapToDouble(Double::parseDouble).toArray();
            bufferedReader.close();
            in.close();
        }
        else
        {
            log.info("找不到预测指定的文件:"+file.getPath());
            System.exit(0);
        }
        //============================================================================
        String modelPath = Constant.MODEL_BASE_DIR + "/" + Constant.MODEL_SAVE_PATH_Prefix + Constant.CURRENCY[currencyType] + ".zip";
        log.info("================预测程序================");
        log.info("...您的输入为:" + Arrays.toString(inputRates[0]) + ";汇率类型为:" + CURRENCY[currencyType]);
        log.info("...开始加载模型" + modelPath + "...");
        MultiLayerNetwork model = loadModel(modelPath);
        log.info("...完成加载模型...");

        log.info("...开始预测...");
        double mean = PreprocessData.getInstance().getMeanList().get(currencyType);
        INDArray feature = getFeatures(inputRates, mean);
        INDArray predictedLabelArray = model.output(feature, false);
        List<Double> predictedLabelList = unNormaliseList(predictedLabelArray, mean);
        log.info("...Features:" + feature.toString());
        log.info("...Prediction Result:" + predictedLabelList);
        //表格的title
        String title = "Predict the exchange rate of " + Constant.CURRENCY[currencyType] + " in " + Constant.M + " days";
        Map<String, Object> map = new HashMap<>();
        map.put(Constant.PLOT_TITLE, title);
        map.put(Constant.PLOT_PREDICTED_LABEL_LIST, predictedLabelList);
        plotLine(map, false);
        log.info("...结束预测...");

    }

    /**
     * 去规范化
     *
     * @param predictedLabelArray
     * @param mean
     * @return
     */
    public static List<Double> unNormaliseList(INDArray predictedLabelArray, Double mean) {
        List<Double> unNormalisedList = new ArrayList<>();
        for (int i = 0; i < predictedLabelArray.rows(); i++) {
            for (int j = 0; j < predictedLabelArray.columns(); j++) {
                unNormalisedList.add(predictedLabelArray.getDouble(0, j) + mean);
            }
        }
        return unNormalisedList;
    }


}

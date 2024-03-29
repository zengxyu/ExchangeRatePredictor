package org.example.util;

import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * 超参
 */
public class Constant {
    //用前5天的数据预测后10天的
    public static final int N = 5;
    public static final int M = 5;
    //训练集与测试集比例为7:2:1
    public static final double RATIO_TRAIN = 0.8;
    public static final double RATIO_TEST = 0.2;

    //Base directory
    public static final String BASE_DIR = "data";
    // Mean csv
    public static final String MEAN_FILE_NAME = "mean.csv";
    //样本文件名
    public static final String SAMPLE_FILE_NAME = "samples.xlsx";

    //所有训练集测试集文件的名字
    public static String[] FNames = new String[]{
            "usd_train_samples.csv", "usd_test_samples.csv", "usd_predict_samples.csv",
            "eur_train_samples.csv", "eur_test_samples.csv", "eur_predict_samples.csv",
            "gbp_train_samples.csv", "gbp_test_samples.csv", "gbp_predict_samples.csv"};

    public static String[] SPLIT_KEY = new String[]{
            "TrainSet", "TestSet", "PredictSet"
    };
    //
    public static final String DATE = "date";
    public static String[] CURRENCY = new String[]{"USD_CNY", "EUR_CNY", "GBP_CNY"};
    public static final String MODEL_SAVE_PATH_Prefix = "model_";
    // Train Batch Size
    public static final int TRAIN_BATCH_SIZE = 32;
    // Test Batch Size
    public static final int TEST_BATCH_SIZE = 1;

    public static final String REAL_LABELS = "realLabels";
    public static final String PREDICTED_LABELS = "predictedLabels";
    public static final String MIN = "min";
    public static final String MAX = "max";

    //保存模型与加载模型
    //Model Base directory 保存模型的文件夹
    public static final String MODEL_BASE_DIR = "model_save";
    public static final String MODEL_NAME = "/exchange_rate_model.zip";

    //PlotLine的键值
    public static final String PLOT_TITLE="";
    public static final String PLOT_DATE_LIST="dateList";
    public static final String PLOT_PREDICTED_LABEL_LIST="predictedLabelList";
    public static final String PLOT_REAL_LABEL_LIST="realLabelList";

    // 三种货币的代号
    public static final int USD_CNY = 0;
    public static final int EUR_CNY = 1;
    public static final int GBP_CNY = 2;

}

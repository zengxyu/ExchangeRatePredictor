package org.example.model;

import org.apache.poi.xssf.usermodel.*;
import org.example.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threadly.util.StringUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 这个类实现的功能有
 * 1. 功能1：将数据划分为6=3*2个文件，美元，欧元，英镑三种货币各有一个训练集一个测试集
 */

public class PreprocessData {
    private static final Logger log = LoggerFactory.getLogger(PreprocessData.class);
    /**
     * 单例模式
     */
    private static PreprocessData preprocessData;

    private PreprocessData() {
    }

    public static PreprocessData getInstance() {
        if (preprocessData == null) {
            preprocessData = new PreprocessData();
        }
        return preprocessData;
    }

    /**
     * 从文件中读入数据到 Map<String, ArrayList> 的数据结构
     *
     * @param path
     * @return Map<String, ArrayList>
     */
    private Map<String, ArrayList> Xlsx2Array(String path) throws IOException, ParseException {
        //获取文件
        File baseDir = new File(Constant.BASE_DIR);
        FileInputStream in = new FileInputStream(new File(baseDir, path));
        //用XSSFWorkbook打开文件
        XSSFWorkbook book = new XSSFWorkbook(in);
        //获取第一张表格
        XSSFSheet sheet = book.getSheetAt(0);
        //三个list分别保存美元，欧元，英镑

        //要返回的Map
        Map<String, ArrayList> hashMap = new HashMap<>();
        String[] keys = new String[]{Constant.DATE, Constant.CURRENCY[0], Constant.CURRENCY[1], Constant.CURRENCY[2]};
        for (int i = 0; i < keys.length; i++) {
            hashMap.put(keys[i], new ArrayList<>());
        }

        //遍历表格sheet中的每一行，前三行除外
        for (int k = 3; k < sheet.getPhysicalNumberOfRows(); k++) {
            //获取第k行
            XSSFRow row = sheet.getRow(k);
            //检查第k行是否为null
            if (row == null || (row.getCell(0) == null ||
                    StringUtils.isNullOrEmpty(row.getCell(0).getRawValue()))) {
                continue;
            }
            //把第k行第1,2,3,4列分别保存在 "date" "USD_CNY", "EUR_CNY", "GBP_CNY",并保存在hash map中
            hashMap.get(keys[0]).add(getDateFromCell(row.getCell(0)).getTime());
            for (int i = 1; i < keys.length; i++) {
                hashMap.get(keys[i]).add(Double.valueOf(row.getCell(i).toString()));
            }
        }
        in.close();
        return hashMap;

    }

    /**
     * 从表中格子cell中获取日期，因为该列日期格式不同，有的是字符串有的是日期，所以这个方法将其统一为日期格式。
     *
     * @param xssfCell
     * @return
     */
    public Date getDateFromCell(XSSFCell xssfCell) throws ParseException {
        Date date = null;
        switch (xssfCell.getCellType()) {
            case XSSFCell.CELL_TYPE_STRING:
                date = string2Date(xssfCell.getStringCellValue());
                break;
            case XSSFCell.CELL_TYPE_NUMERIC:
                date = xssfCell.getDateCellValue();
                break;
            default:
                date = new Date();
                break;
        }
        return date;
    }

    /**
     * 将字符串转为日期
     *
     * @param d
     * @return
     */
    public Date string2Date(String d) throws ParseException {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        date = fmt.parse(d);
        return date;
    }


    /**
     * 用于规范数据，所有数据均减去其平均值
     *
     * @param doubleList
     * @return
     */
    private Double normalize(List<Double> doubleList) {
        Double average = doubleList.stream().mapToDouble(val -> val).average().orElse(0.0);
        for (int i = 0; i < doubleList.size(); i++) {
            doubleList.set(i, Double.valueOf(doubleList.get(i)) - average);
        }
        return average;
    }
    /**
     * 将其划分为 usd训练集 usd测试集,usd预测, eur训练集,eur测试集,eur预测,gbp 训练集, gbp测试集,gdp预测
     *
     * @param filePath
     */
    private void makeDataSet(String filePath) throws IOException, ParseException {
        //经过处理后的hashMap的键值, 将其划分为 usd训练集 usd测试集,usd预测, eur训练集,eur测试集,eur预测,gbp 训练集, gbp测试集,gdp预测
//        String[] processedKeys = Constant.FNames;
        // Train,Test,Predict 这三个键值
        //从表格中读取未经处理的数据
        Map<String, ArrayList> unprocessedMap = Xlsx2Array(filePath);
        //第一项处理：先翻转。因为表格时间顺序是倒置的，所以需要翻转
        Map<String, ArrayList> reversedMap = reverseAllList(unprocessedMap);
        //第二项处理: 分成训练集，测试集和预测集
        Map<String, Map<String, List>> splitMap = splitData(reversedMap, Constant.RATIO_TRAIN, Constant.RATIO_TEST);
        // 第2.1 处理训练集
        processTrainTestDataSet(splitMap.get(Constant.SPLIT_KEY[0]), true);
        // 第2.2 处理测试集
        processTrainTestDataSet(splitMap.get(Constant.SPLIT_KEY[1]), false);
        // 第2.3 处理预测集
//        processPredictDataSet(splitMap.get(Constant.SPLIT_KEY[2]));
    }

    /**
     * 将数据结构为List<List<Object>> recordList的数据写入6个文件中
     */
    private void write2CSV(List<List<Object>> recordList, String fileName) throws IOException {
        File baseDir = new File(Constant.BASE_DIR);
        File file = new File(baseDir, fileName);
        if (file.exists()) {//如果文件存在，先删除
            file.delete();
        }
        if (!file.exists()) {//然后当文件不存在，创建文件
            file.createNewFile();
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        for (int i = 0; i < recordList.size(); i++) {
            List<Object> oneRow = recordList.get(i);
            for (int j = 0; j < oneRow.size(); j++) {
                out.write(String.valueOf(oneRow.get(j)));
                out.write(",");
            }
            out.newLine();
        }
        out.flush();
        out.close();
        log.info("文件:" + file.getName() + "数据构建完毕!");
    }

    /**
     * 处理用于预测的数据
     */
    private void processPredictDataSet(Map<String, List> predictMap) throws IOException {
        String[] keys = new String[]{Constant.DATE, Constant.CURRENCY[0], Constant.CURRENCY[1], Constant.CURRENCY[2]};
        List<Date> dateList = predictMap.get(keys[0]);
        //遍历每种货币
        for (int i = 1; i < keys.length; i++) {
            List<Double> currencyRateList = predictMap.get(keys[i]);
            normalize(currencyRateList);
            int len = currencyRateList.size();
            // 某种货币的预测数据的List
            List<List<Object>> recordList = new ArrayList<>();
            //按照开题报告中的映射N->M关系，生成每一行训练数据
            int j = 0;
            while (j < len - Constant.N - Constant.M) {
                //获取第j行到第j+N行的数据作为一个record的输入，第j+N+1到第j+N+M行的数据作为一个Record的输出,
                //下一条数据与该数据不重复，所以j跳到j += (Constant.N + Constant.M);
                List<Object> record = new ArrayList<>();
                record.addAll(dateList.subList(j, j + Constant.N + Constant.M));
                record.addAll(currencyRateList.subList(j, j + Constant.N + Constant.M));
                recordList.add(record);
                j += (Constant.N + Constant.M);
            }
            write2CSV(recordList, Constant.FNames[3 * i - 1]);
        }
    }

    /**
     * 处理训练集数据
     * 写入三种货币训练集的平均值，写入文件
     * 生成一条条Record, 写入文件
     *
     * @param trainMap
     */
    private void processTrainTestDataSet(Map<String, List> trainMap, boolean train) throws IOException {
        String[] keys = new String[]{Constant.DATE, Constant.CURRENCY[0], Constant.CURRENCY[1], Constant.CURRENCY[2]};
        //三种货币的平均值记录在此处
        List<Double> meanList = new ArrayList<>();
        List<Date> dateList = trainMap.get(keys[0]);
        //遍历每种货币
        for (int i = 1; i < keys.length; i++) {
            List<Double> currencyRateList = trainMap.get(keys[i]);
            int len = currencyRateList.size();
            //三种货币分别获得平均值，并规范化
            meanList.add(normalize(currencyRateList));
            // 某种货币的训练数据的List
            List<List<Object>> recordList = new ArrayList<>();
            //按照开题报告中的映射N->M关系，生成每一行训练数据
            for (int j = 0; j < len - Constant.N - Constant.M; j++) {
                //获取第j行到第j+N行的数据作为一个record的输入，第j+N+1到第j+N+M行的数据作为一个Record的输出
                List<Object> record = new ArrayList<>();
                record.addAll(dateList.subList(j, j + Constant.N + Constant.M));
                record.addAll(currencyRateList.subList(j, j + Constant.N + Constant.M));
                recordList.add(record);
            }
            if (train) {
                write2CSV(recordList, Constant.FNames[3 * i - 3]);
            } else {
                write2CSV(recordList, Constant.FNames[3 * i - 2]);
            }
        }
        //训练集才写入平均值到文件
        if (train) {
            writeMean2CSV(meanList);
        }
    }

    /**
     * 记录平均值到文件
     *
     * @param meanList
     */
    private void writeMean2CSV(List<Double> meanList) throws IOException {
        File baseDir = new File(Constant.BASE_DIR);
        File file = new File(baseDir, Constant.MEAN_FILE_NAME);
        if (file.exists()) {//如果文件存在，先删除
            file.delete();
        }
        if (!file.exists()) {//然后当文件不存在，创建文件
            file.createNewFile();
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        for (int i = 0; i < meanList.size(); i++) {
            out.write(String.valueOf(meanList.get(i)));
            out.write(",");
        }
        out.flush();
        out.close();
    }

    /**
     * 将三种货币划分为训练集，测试集，和用来预测的集合，比例默认为7:2:1
     *
     * @param map
     * @param ratio_train
     * @param ratio_test
     * @return Map<String, Map < String, List>> 第一层Map下是 train,test,predict， 第二层Map下是Date, USD, EUR, GBP, 所以共12个List
     */
    private Map<String, Map<String, List>> splitData(Map<String, ArrayList> map, double ratio_train, double ratio_test) {
        String[] TrainTestPredictKeys = Constant.SPLIT_KEY;
        String[] unprocessedKeys = new String[]{Constant.DATE, Constant.CURRENCY[0], Constant.CURRENCY[1], Constant.CURRENCY[2]};

        // 划分好的数据
        Map<String, Map<String, List>> splitMap = new HashMap<>();
        // 初始化
        for (int i = 0; i < TrainTestPredictKeys.length; i++) {
            splitMap.put(TrainTestPredictKeys[i], new HashMap<>());
        }
        //遍历 Date, USD, EUR, GBP
        for (int j = 0; j < map.keySet().size(); j++) {
            List arrayList = map.get(unprocessedKeys[j]);
            // 货币list的长度
            int len = arrayList.size();
            // 分割训练集与测试集的位置
            int trainCutIndex = (int) (ratio_train * len);
            // 分割测试集与预测集的位置
            int testCutIndex = (int) ((ratio_train + ratio_test) * len);
            List trainList = arrayList.subList(0, trainCutIndex);
            List testList = arrayList.subList(trainCutIndex, testCutIndex);
            List predictList = arrayList.subList(testCutIndex, len);
            splitMap.get(TrainTestPredictKeys[0]).put(unprocessedKeys[j], trainList);
            splitMap.get(TrainTestPredictKeys[1]).put(unprocessedKeys[j], testList);
            splitMap.get(TrainTestPredictKeys[2]).put(unprocessedKeys[j], predictList);
        }
        return splitMap;
    }

    /**
     * 翻转Map中所有数据
     *
     * @param unprocessedMap
     * @return
     */
    private Map<String, ArrayList> reverseAllList(Map<String, ArrayList> unprocessedMap) {
        for (String key : unprocessedMap.keySet()) {
            Collections.reverse(unprocessedMap.get(key));
        }
        return unprocessedMap;
    }

    public List<Double> getMeanList() throws IOException {
        File base_dir = new File(Constant.BASE_DIR);
        File meanFile = new File(base_dir, Constant.MEAN_FILE_NAME);
        //第二步：从字符输入流读取文本，缓冲各个字符，从而实现字符、数组和行（文本的行数通过回车符来进行判定）的高效读取。
        BufferedReader textFile = new BufferedReader(new FileReader(meanFile));
        String lineDta = "";
        List<Double> list = new ArrayList<>();

        //第三步：将文档的下一行数据赋值给lineData，并判断是否为空，若不为空则输出
        while ((lineDta = textFile.readLine()) != null) {
            String[] meanArray = lineDta.split(",");
            for (int i = 0; i < meanArray.length; i++) {
                list.add(Double.valueOf(meanArray[i]));
            }
        }
        textFile.close();
        return list;
    }

    public void preProcessData(String filePath) {
        log.info("开始处理数据...");
        try {
            makeDataSet(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        log.info("结束处理数据...");
    }

}

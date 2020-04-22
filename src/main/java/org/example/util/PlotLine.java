package org.example.util;

import java.awt.Color;
import java.io.File;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * 画折线图
 */
public class PlotLine extends JFrame {

    private static final long serialVersionUID = 6294689542092367723L;

    /**
     * 获取两个List的最大值最小值
     *
     * @param realLabels
     * @param predictedLabels
     * @return
     */
    private Map<String, Double> getRange(List<Double> realLabels, List<Double> predictedLabels) {
        Map<String, Double> map = new HashMap<>();
        //找到这两个集合中的最大值和最小值
        List<Double> allLabels = new ArrayList<>();
        allLabels.addAll(realLabels);
        allLabels.addAll(predictedLabels);
        Double min = allLabels.stream().mapToDouble(val -> val).min().orElse(0.0);
        Double max = allLabels.stream().mapToDouble(val -> val).max().orElse(0.0);
        map.put(Constant.MIN, min - 0.1);
        map.put(Constant.MAX, max + 0.1);
        return map;
    }

    /**
     * 重载上面方法
     *
     * @param predictedLabels
     * @return
     */

    private Map<String, Double> getRange(List<Double> predictedLabels) {
        Map<String, Double> map = new HashMap<>();
        //找到这两个集合中的最大值和最小值
        Double min = predictedLabels.stream().mapToDouble(val -> val).min().orElse(0.0);
        Double max = predictedLabels.stream().mapToDouble(val -> val).max().orElse(0.0);
        map.put(Constant.MIN, min - 0.1);
        map.put(Constant.MAX, max + 0.1);
        return map;
    }

    public PlotLine(Map<String, Object> map, boolean train) {
        //需要显示的数据
        String title = "";
        List<Long> dateList = null;
        List<Double> realLabelList = null;
        List<Double> predictedLabelList = null;

        // Create dataset 画图参数
        XYDataset dataset = null;
        Map<String, Double> rangeMap = null;
        String xAxisTitle = "";


        if (train) {
            //在训练或测试时，需要显示真实标签和预测标签
            title = (String) map.get(Constant.PLOT_TITLE);
            //获取日期list
            dateList = (List<Long>) map.get(Constant.PLOT_DATE_LIST);
            //获取预测标签list
            predictedLabelList = (List<Double>) map.get(Constant.PLOT_PREDICTED_LABEL_LIST);
            //获取真实标签list
            realLabelList = (List<Double>) map.get(Constant.PLOT_REAL_LABEL_LIST);
            //获取y轴range的范围
            rangeMap = getRange(realLabelList, predictedLabelList);
            //获取x轴title
            xAxisTitle = "Days from ";
            xAxisTitle += new Date(dateList.get(0));
            //创建表
            dataset = createDataset(realLabelList, predictedLabelList);
        } else {
            //在预测时，只显示预测标签
            title = (String) map.get(Constant.PLOT_TITLE);
            //获取预测标签list
            predictedLabelList = (List<Double>) map.get(Constant.PLOT_PREDICTED_LABEL_LIST);
            //获取y轴range的范围
            rangeMap = getRange(predictedLabelList);
            //创建表
            dataset = createDataset(predictedLabelList);
            xAxisTitle = "Days";
        }

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xAxisTitle, "Exchange rate", dataset, PlotOrientation.VERTICAL, true, true, true);
        //Changes background color
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 255, 255));
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoTickUnitSelection(train);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(rangeMap.get(Constant.MIN), rangeMap.get(Constant.MAX));

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private XYDataset createDataset(List<Double> predictedLabels) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series2 = new XYSeries("Predicted Labels");
        //RealLabels
        for (int i = 1; i <= predictedLabels.size(); i++) {
            series2.add(i, predictedLabels.get(i-1));
        }
        dataset.addSeries(series2);
        return dataset;
    }

    private XYDataset createDataset(List<Double> predictedLabels, List<Double> realLabels) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Real labels");
        //RealLabels
        for (int i = 0; i < realLabels.size(); i++) {
            series1.add(i, realLabels.get(i));
        }
        dataset.addSeries(series1);
        XYSeries series2 = new XYSeries("Predicted Labels");
        //RealLabels
        for (int i = 0; i < predictedLabels.size(); i++) {
            series2.add(i, predictedLabels.get(i));
        }

        dataset.addSeries(series2);
        return dataset;
    }

}

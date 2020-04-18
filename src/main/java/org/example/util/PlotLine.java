package org.example.util;

import java.awt.Color;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
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
        map.put(Constant.MIN, min - 0.05);
        map.put(Constant.MAX, max + 0.05);
        return map;
    }

    public PlotLine(String title, List<Long> dateList, List<Double> realLabels, List<Double> predictedLabels) {

        // Create dataset
        XYDataset dataset = createDataset(dateList, realLabels, predictedLabels);
        String t = title + " Comparison chart : Real Labels VS Predicted Labels";
        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                t, "Date from ( " + new Date(dateList.get(0)) + " )", "Exchange rate", dataset, PlotOrientation.VERTICAL, true, true, true);
        //Changes background color
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 255, 255));

        Map<String, Double> rangeMap = getRange(realLabels, predictedLabels);

        NumberAxis domain = (NumberAxis) plot.getRangeAxis();
        domain.setRange(rangeMap.get(Constant.MIN), rangeMap.get(Constant.MAX));
        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }


    private XYDataset createDataset(List<Long> dateList, List<Double> realLabels, List<Double> predictedLabels) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Real labels");
        //RealLabels
        for (int i = 0; i < realLabels.size(); i++) {
            series1.add(i, realLabels.get(i));
        }
        XYSeries series2 = new XYSeries("Predicted Labels");
        //RealLabels
        for (int i = 0; i < predictedLabels.size(); i++) {
            series2.add(i, predictedLabels.get(i));
        }
        dataset.addSeries(series1);
        dataset.addSeries(series2);

        return dataset;
    }

    public static void main(String[] args) {

    }
}

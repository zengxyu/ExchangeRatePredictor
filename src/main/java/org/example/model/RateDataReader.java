package org.example.model;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.datavec.RecordReaderMultiDataSetIterator;
import org.example.util.Constant;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class RateDataReader {
    private static File baseFile = new File(Constant.BASE_DIR);

    public static MultiDataSetIterator readDataset(
            String fileName, int batchSize)
            throws IOException, InterruptedException {
        File file = new File(baseFile, fileName);
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(file));
        MultiDataSetIterator iterator = new RecordReaderMultiDataSetIterator.Builder(batchSize)
                .addReader("myReader", rr)
                .addInput("myReader", Constant.N + Constant.M, 2 * Constant.N + Constant.M - 1)  //Input: columns 0 to 2 inclusive
                .addOutput("myReader", 2 * Constant.N + Constant.M, 2 * Constant.N + 2 * Constant.M - 1) //Output: columns 3 to 4 inclusive
                .build();
        return iterator;//
    }

    public static MultiDataSetIterator readDateDataset(
            String fileName, int batchSize)
            throws IOException, InterruptedException {
        File file = new File(baseFile, fileName);
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(file));
        MultiDataSetIterator iterator = new RecordReaderMultiDataSetIterator.Builder(batchSize)
                .addReader("myReader", rr)
                .addInput("myReader", 0, Constant.N - 1)  //Input: columns 0 to 2 inclusive
                .addOutput("myReader", Constant.N, Constant.N + Constant.M - 1) //Output: columns 3 to 4 inclusive
                .build();
        return iterator;//
    }
}

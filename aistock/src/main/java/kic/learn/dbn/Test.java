package kic.learn.dbn;

import kic.learn.YahooFetcher;
import yahoofinance.histquotes.Interval;
import yusugomori.DBN.DBN;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 * Created by kic on 30.03.15.
 */
public class Test {
    public static void main(String[] args) {
        String modelName = args.length>0 ? args[0] : "/tmp/DBN-candle.3.obj";
        String symbol = "GOOG";
        int days = 50;
        DBN dbn;

        try {
            FileInputStream fin = new FileInputStream(modelName);
            ObjectInputStream oos = new ObjectInputStream(fin);
            dbn = (DBN) oos.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Test
        YahooFetcher yf = new YahooFetcher(days, dbn.window, dbn.predict, dbn.quantiles, dbn.weekly ? Interval.WEEKLY : Interval.DAILY, symbol);
        int[] right = new int[yf.classificationSize()];

        while (yf.hasNext(YahooFetcher.PREDICT)) {
            yf.next();

            int[] target = yf.classification();
            double[] predicted = new double[target.length];

            dbn.predict(yf.toFeatures(YahooFetcher.PREDICT), predicted);
            for (int i=0; i<predicted.length; i++){
                System.out.println(target[i] + " " + predicted[i] + "\t" + (predicted[i] - (double) target[i]));
                if (target[i] == 1 && predicted[i] > 0.5) right[i]++;
                if (target[i] == 0 && predicted[i] < 0.5) right[i]++;
            }

            System.out.println();
        }

        System.out.println("size: " + yf.size(YahooFetcher.PREDICT));
        System.out.println("correct: " + Arrays.toString(right));
    }
}

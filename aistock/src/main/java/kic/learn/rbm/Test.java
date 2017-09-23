package kic.learn.rbm;


import kic.learn.Renderer;
import kic.learn.YahooFetcher;
import yahoofinance.histquotes.Interval;
import yusugomori.RBM.RBM;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * Created by kic on 29.03.15.
 */
public class Test {
    public static void main(String[] args) {
        System.out.println("args: modelfile symbol nrofdays");
        String modelName = args.length>0 ? args[0] : "/tmp/RBM-candle.2.obj";
        String symbol = args.length>1 ? args[1] : "GOOG";
        int nrOfDays = args.length>2 ? Integer.parseInt(args[2]) : 50;

        RBM rbm;

        try {
            FileInputStream fin = new FileInputStream(modelName);
            ObjectInputStream oos = new ObjectInputStream(fin);
            rbm = (RBM) oos.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        YahooFetcher yf = new YahooFetcher(nrOfDays, rbm.window, rbm.predict, rbm.quantiles, rbm.weekly ? Interval.WEEKLY : Interval.DAILY, symbol);
        Renderer render = new Renderer(rbm.window, rbm.quantiles);
        render.show();

        while (yf.hasNext(YahooFetcher.TRAIN)) {
            int[][][] matrix = yf.next();

            int[] display = yf.toFeatures(YahooFetcher.TRAIN, matrix);
            int[] reconstruct = yf.toFeatures(YahooFetcher.PREDICT, matrix);
            double[] reconsructed = new double[reconstruct.length];

            rbm.reconstruct(reconstruct, reconsructed);
            render.draw(matrix, yf.reverse(reconsructed));

            // check
            render.pause();
        }
    }
}

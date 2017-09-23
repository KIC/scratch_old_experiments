package kic.learn.rbm;

import kic.learn.Renderer;
import kic.learn.SYMBOLS;
import kic.learn.YahooFetcher;
import yahoofinance.histquotes.Interval;
import yusugomori.RBM.RBM;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

/**
 * Created by kic on 29.03.15.
 */
public class Train {
    public static void main(String[] args) {
        System.out.println("args: modelfile days window predict quantiles hiddenNurons epochs debugEpochs weekly");

        String saveModelAs = args.length>0 ? args[0] : "/tmp/RBM-candle.3.obj";
        int nrOfDays = args.length>1 ? Integer.parseInt(args[1]) : 365 * 2;
        int window = args.length>2 ? Integer.parseInt(args[2]) : 3;
        int predict = args.length>3 ? Integer.parseInt(args[3]) : 1;
        int quantiles = args.length>4 ? Integer.parseInt(args[4]) : 120;
        int n_hidden = args.length>5 ? Integer.parseInt(args[5]) : 90;
        int training_epochs = args.length>6 ? Integer.parseInt(args[6]) : 1000;
        int debugEpochs = args.length>7 ? Integer.parseInt(args[7]) : 10;
        boolean weekly = args.length>8 ? Boolean.parseBoolean(args[8]) : false;

        String[] symols = new String[]{"AAPL", "MSFT"}; //, "BBRY"};
        //String[] symols = SYMBOLS.nasdaq100;

        YahooFetcher yf = new YahooFetcher(nrOfDays, window, predict, quantiles, weekly ? Interval.WEEKLY : Interval.DAILY, symols);

        Random rng = new Random(123);

        double learning_rate = 0.1;
        int k = 1;

        int train_N = yf.size(YahooFetcher.TRAIN); // leave last traning data for debugging
        int n_visible = yf.featuresSize();


        // start learning
        RBM rbm = new RBM(window, predict, quantiles, train_N, n_visible, n_hidden, null, null, null, rng, weekly);
        Renderer render = new Renderer(window, quantiles);
        if (debugEpochs>0) render.show();

        System.out.println("training set size: " + yf.size(YahooFetcher.TRAIN_1));

        // train
        for(int epoch=0; epoch<training_epochs; epoch++) {
            yf.reset();

            while(yf.hasNext(YahooFetcher.TRAIN_1)) {
                int[][][] matrix = yf.next();
                int[] input = yf.toFeatures(YahooFetcher.TRAIN, matrix);

                rbm.contrastive_divergence(input, learning_rate, k);
            }

            // debug
            if (epoch % debugEpochs == 0 || epoch >= training_epochs-1) {
                if (!yf.hasNext(YahooFetcher.TRAIN)) throw new RuntimeException("No data left to debug");

                // fetch last training data as debugging frame
                int[][][] matrix = yf.next();
                int[] input = yf.toFeatures(YahooFetcher.TRAIN, matrix);
                double[] reconstructed = new double[input.length];

                rbm.reconstruct(input, reconstructed);
                render.draw(matrix, yf.reverse(reconstructed));
            }

            System.out.println("finished epoch: " + epoch);
        }

        // save model
        try {
            FileOutputStream fout = new FileOutputStream(saveModelAs);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(rbm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // wait for user
        if (debugEpochs>0) render.pause();

        // and predict the unknown
        if (!yf.hasNext(YahooFetcher.PREDICT)) throw new RuntimeException("No Data left to predict");

        int[][][] matrix = yf.next();
        int[] input = yf.toFeatures(YahooFetcher.PREDICT, matrix);
        double[] reconstructed = new double[input.length];

        rbm.reconstruct(input, reconstructed);
        render.draw(matrix, yf.reverse(reconstructed));
    }
}

package kic.learn.dbn;

import kic.learn.SYMBOLS;
import kic.learn.YahooFetcher;
import yahoofinance.histquotes.Interval;
import yusugomori.DBN.DBN;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by kic on 29.03.15.
 */
public class Train {
    public static void main(String[] args) {
        System.out.println("args: filename, nrOfDays, window, predict, quantiles, preepochs, fineepochs, hidden_1,..,hidden_N, symbol1,..,N");

        String saveModelAs = args.length>0 ? args[0] : "/tmp/DBN-candle.3.obj";
        int nrOfDays = args.length>1 ? Integer.parseInt(args[1]) : 200 * 1;
        int window = args.length>2 ? Integer.parseInt(args[2]) : 3;
        int predict = args.length>3 ? Integer.parseInt(args[3]) : 1;
        int quantiles = args.length>4 ? Integer.parseInt(args[4]) : 120;
        int pretraining_epochs = args.length>5 ? Integer.parseInt(args[5]) : 800;
        int finetraining_epochs = args.length>6 ? Integer.parseInt(args[6]) : 400;
        int[] hidden = {70, 70};
        if (args.length>7) {
            String[] layers = args[7].split(("\\s*,\\s*"));
            hidden = new int[layers.length];
            for (int i=0; i<layers.length; i++) hidden[i] = Integer.parseInt(layers[i]);
        }

        String[] symols = args.length>8 ? args[8].split("\\s*,\\s*") : SYMBOLS.nasdaq100; //new String[]{"AAPL", "MSFT"}; //, "BBRY"};


        YahooFetcher yf = new YahooFetcher(nrOfDays, window, predict, quantiles, Interval.WEEKLY, symols);

        Random rng = new Random(123);

        double learning_rate = 0.1;
        int k = 1;

        int train_N = yf.size(YahooFetcher.TRAIN_1); // leave last traning data for debugging
        int n_visible = yf.featuresSize();
        int n_out = yf.classificationSize();

        // start learning
        DBN dbn = new DBN(window, predict, quantiles, train_N, n_visible, hidden, n_out, rng, false);

        //Renderer render = new Renderer(window, quantiles);
        //render.show();

        System.out.println("training set size: " + yf.size(YahooFetcher.TRAIN_1));

        int x=0;
        int[][] pretrains = new int[yf.size(YahooFetcher.TRAIN_1)][yf.featuresSize()];
        int[][] classes = new int[yf.size(YahooFetcher.TRAIN_1)][yf.classificationSize()];
        while(yf.hasNext(YahooFetcher.TRAIN_1)) {
            yf.next();
            pretrains[x] = yf.toFeatures(YahooFetcher.TRAIN);
            classes[x] = yf.classification();

            x++;
        }

        // pretrain
        System.out.println("Start pretrain");
        dbn.pretrain(pretrains, learning_rate, k, pretraining_epochs);

        System.out.println("Start finetune");
        dbn.finetune(pretrains, classes, learning_rate, finetraining_epochs);


        // save model
        try {
            FileOutputStream fout = new FileOutputStream(saveModelAs);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(dbn);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // wait for user
        //render.pause();

        // test network
        System.out.println("Start test");
        YahooFetcher yf2 = new YahooFetcher(nrOfDays, window, predict, quantiles, dbn.weekly ? Interval.WEEKLY : Interval.DAILY, "GOOG");
        int[] right = new int[yf2.classificationSize()];

        while (yf2.hasNext(YahooFetcher.PREDICT)) {
            yf2.next();

            int[] target = yf2.classification();
            double[] predicted = new double[target.length];

            dbn.predict(yf2.toFeatures(YahooFetcher.PREDICT), predicted);
            for (int i=0; i<predicted.length; i++){
                System.out.println(target[i] + " " + predicted[i] + "\t" + (predicted[i] - (double) target[i]));
                if (target[i] == 1 && predicted[i] > 0.5) right[i]++;
                if (target[i] == 0 && predicted[i] < 0.5) right[i]++;
            }

            System.out.println();
        }

        System.out.println("size: " + yf2.size(YahooFetcher.PREDICT));
        System.out.println("correct: " + Arrays.toString(right));
    }
}

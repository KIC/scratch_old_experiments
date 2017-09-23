package kic.learn;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kic on 27.03.15.
 */
public class YahooFetcher {
    public static final int TRAIN = 1;
    public static final int TRAIN_1 = 2;
    public static final int PREDICT = 3;
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<HistoricalQuote> history = new ArrayList<>();
    private int[][][] matrix;
    private int[] classification;
    private final int windowSize, predict, nrOfQuantiles;
    private int cursor = -1;
    private int size = -1;


    private IResacale minmaxmargin = (min, max) -> {
        double margin = (max - (max + min) / 2) * 1; //0.75;
        return  new double[] {min -margin, max + margin};
    };

    private IResacale minmax10Percent = (min, max) -> {
        double avg = (max + min) / 2;
        return  new double[] {avg * 0.94, max * 1.06};
    };

    public YahooFetcher(int days, int windowSize, int predict, int nrOfQuantiles, String... symbols) {
        this(days, windowSize, predict, nrOfQuantiles, Interval.DAILY, symbols);
    }

    public YahooFetcher(int days, int windowSize, int predict, int nrOfQuantiles, Interval interval, String... symbols) {
        this.windowSize = windowSize;
        this.nrOfQuantiles = nrOfQuantiles;
        this.predict = predict;

        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, -days);

        Calendar to = Calendar.getInstance();

        for (String symbol : symbols) {
            //Stock stock = debug ? YahooFinance.get(symbol) : YahooFinance.get(symbol, from, to, Interval.DAILY);
            Stock stock = YahooFinance.get(symbol, from, to, interval);
            List<HistoricalQuote> quotes = stock.getHistory();
            for (HistoricalQuote hq : quotes) history.add(0, hq);
        }

        this.size = history.size() - windowSize + 1;
        reset();
    }


    public int[][][] next() {
        if (cursor>=history.size()) throw new IndexOutOfBoundsException("no data left");

        // get scale
        double v;
        double max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        for (int i=cursor-windowSize+1; i<=cursor; i++) {
            if ((v=history.get(i).getHigh().doubleValue())>max) max = v;
            if ((v=history.get(i).getLow().doubleValue())<min) min = v;
        }

        //double[] minmax = minmaxmargin.rescale(min, max);
        double[] minmax = minmax10Percent.rescale(min, max);
        min = minmax[0];
        max = minmax[1];

        // build features
        int cd = 0;
        double qtlSize = (max - min) / nrOfQuantiles;
        //int[][][] matrix = new int[windowSize + predict][3][nrOfQuantiles];
        matrix = new int[windowSize + predict][3][nrOfQuantiles];
        classification = new int[2*predict];
        int ci=0;

        for (int hd=cursor-windowSize+1; hd<=cursor+predict; hd++) {
            if (hd >= history.size()) continue;

            HistoricalQuote hq = history.get(hd);

            double o = hq.getOpen().doubleValue();
            double h = hq.getHigh().doubleValue();
            double l = hq.getLow().doubleValue();
            double c = hq.getClose().doubleValue();
            double c_1 = history.get(Math.max(0, hd-1)).getClose().doubleValue();

            for (int qunatile = 0; qunatile < nrOfQuantiles; qunatile++) {
                double q1 = min + qunatile * qtlSize;
                double q2 = min + (qunatile + 1) * qtlSize;

                if (c > o) {
                    // col 1
                    matrix[cd][0][qunatile] = q1 >= o && q2 <= c ? 1 : 0;

                    // col 2
                    matrix[cd][1][qunatile] = (q2 > c && q2 <= h) || (q1 < o && q1 >= l) ? 1 : 0;

                    // col 3
                    Arrays.fill(matrix[cd][2], 0);
                } else {
                    // col 1
                    Arrays.fill(matrix[cd][0], 0);

                    // col 2
                    matrix[cd][1][qunatile] = (q2 > o && q2 <= h) || (q1 < c && q1 >= l) ? 1 : 0;

                    // col 3
                    matrix[cd][2][qunatile] = q1 >= c && q2 <= o ? 1 : 0;
                }
            }

            //classify
            if (hd>cursor) {
                classification[ci++] = c > o ? 1 : 0;
                classification[ci++] = c > c_1 ? 1 : 0;
            }

            // Next day
            cd++;
        }

        cursor++;
        return matrix;
    }

    public int[] classification() {
        return classification;
    }

    public int[] toFeatures(int modus) {
        return toFeatures(modus, matrix);
    }

    public int[] toFeatures(int modus, int[][][] matrix) {
        // generate feature list
        int[] features = new int[(windowSize + predict) * 3 * nrOfQuantiles];
        int l=0;

        // if modus train
        if (modus == TRAIN || modus == TRAIN_1) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    for (int k = 0; k < matrix[i][j].length; k++) {
                        features[l++] = matrix[i][j][k];
                    }
                }
            }
        } else if (modus == PREDICT) {
            for (int i = 0; i < windowSize; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    for (int k = 0; k < matrix[i][j].length; k++) {
                        features[l++] = matrix[i][j][k];
                    }
                }
            }
        }

        return features;
    }

    public int[][][] reverse(int[] features) {
        int[][][] matrix = new int[windowSize + predict][3][nrOfQuantiles];

        int j=0, k=0, l=0;
        for (int i=0; i<features.length; i++) {
            if (k>=nrOfQuantiles) {
                k=0;
                j++;
                if (j>2) {
                    j=0;
                    l++;
                }
            }

            matrix[l][j][k++] = features[i];
        }

        return matrix;
    }

    public double[][][] reverse(double[] features) {
        double[][][] matrix = new double[windowSize + predict][3][nrOfQuantiles];

        int j=0, k=0, l=0;
        for (int i=0; i<features.length; i++) {
            if (k>=nrOfQuantiles) {
                k=0;
                j++;
                if (j>2) {
                    j=0;
                    l++;
                }
            }

            matrix[l][j][k++] = features[i];
        }

        return matrix;
    }

    public static void main(String... args) {
        YahooFetcher yf = new YahooFetcher(100, 2, 1, 90, "AAPL");
        int[][][] matrix = yf.next();


        Renderer r = new Renderer(yf.windowSize + yf.predict, yf.nrOfQuantiles);
        r.show();

        while (yf.hasNext(TRAIN)) {
            int[][][] next = yf.next();
            r.draw(next);

            int[] f = yf.toFeatures(TRAIN, next);
            int[][][] reverse = yf.reverse(f);
            System.out.println(Arrays.toString(f));
            System.out.println(Arrays.deepEquals(next, reverse));

            try {

                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int featuresSize() {
        return (windowSize + predict) * 3 * nrOfQuantiles;
    }


    public int classificationSize() {
        return predict * 2;
    }

    public boolean hasNext(int modus) {
        if (modus == TRAIN) {
            return cursor + predict < history.size();
        } else if (modus == TRAIN_1) {
            return cursor + predict < history.size() -1;
        } else if (modus == PREDICT) {
            return cursor < history.size();
        } else {
            return false;
        }
    }

    public int size(int modus) {
        if (modus == TRAIN) {
            return history.size() - windowSize - predict +1;
        } else if (modus == TRAIN_1) {
            return history.size() - windowSize - predict;
        } else if (modus == PREDICT) {
            return history.size() - windowSize +1;
        } else {
            return -1;
        }
    }

    public void reset() {
        cursor = windowSize-1;
    }

}

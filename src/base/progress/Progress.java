package base.progress;

import java.text.DecimalFormat;

/**
 * Created by alext on 6/19/14.
 * TODO document class
 * Shamelessly stolen from http://stackoverflow.com/posts/1001340/revisions
 */
public class Progress {
    private static final DecimalFormat DF = new DecimalFormat("#.00");

    public static void updateProgress(double progressPercentage) {
        final int width = 50; // progress bar width in chars

        System.out.print("\r[");
        int i = 0;
        for (; i <= (int) (progressPercentage * width); i++) {
            System.out.print(".");
        }
        for (; i < width; i++) {
            System.out.print(" ");
        }
        System.out.print("]");
    }

    public static void updateProgressCuncurrent(String marker, double progressPercentage) {
        synchronized (System.out.getClass()) {
            System.out.println("[ " + marker + " " + DF.format(progressPercentage) + " ]% accomplished.");
        }
    }

}

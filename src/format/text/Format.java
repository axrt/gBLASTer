package format.text;

import sequence.protein.ORF;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public interface Format {

    public String formatORF(ORF toFormat);
    public boolean checkFormatting(String toCheck);
    public String getAc(String record);
    public String getSequence(String record);

}

package format.text;

import alphabet.character.*;
import sequence.Sequence;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public interface Format {

    public boolean checkFormatting(String toCheck);
    public String getAc(String record);
    public String getSequence(String record);

}


package engine.analysis;

import java.util.Arrays;
import java.util.List;

/**
 * A naive analyzer implementation for debugging purposes,
 * it only does tokenization based on white spaces.
 */

public class NaiveAnalyzer implements Analyzer {

    @Override
    public List<String> analyze(String text) {
        return Arrays.asList(text.toLowerCase().split("\\s+"));
    }

}

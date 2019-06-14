
package engine.analysis;

import java.util.*;

public class PunctuationTokenizer implements Tokenizer {

    public static Set<String> punctuations = new HashSet<>();
    static {
        punctuations.addAll(Arrays.asList(",", ".", ";", "?", "!"));
    }

    public PunctuationTokenizer() {}

    public List<String> tokenize(String text) {
        List<String> result = new ArrayList<>();
        StringBuilder del = new StringBuilder(" \n\t\r");
        for (String temp : punctuations) {
            del.append(temp);
        }
        StringTokenizer st = new StringTokenizer(text, del.toString());
        while (st.hasMoreTokens()) {
            String s = st.nextToken().toLowerCase();
            if (!StopWords.stopWords.contains(s) && s.length() != 0) {
                result.add(s);
            }
        }
        return result;
    }

}

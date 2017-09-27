import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

public class MyAnalyzer extends StopwordAnalyzerBase {
 
   private Version matchVersion;
   public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
   public MyAnalyzer(Version matchVersion) {
     this.matchVersion = matchVersion;
   }

   public MyAnalyzer() {
	   System.out.println("My analyzer called");
	     this.matchVersion = matchVersion;
   }
   
   @Override
   protected TokenStreamComponents createComponents(String fieldName) {
    // final Tokenizer source= new WhitespaceTokenizer();
	 //  TokenStream stream = analyzer.tokenStream("field", new StringReader(text));
	  // TokenStream result = new LengthFilter(matchVersion, source, 3, Integer.MAX_VALUE);
	  // Tokenizer source1 = new LowerCaseTokenizer(matchVersion, result);
     final Tokenizer src = new StandardTokenizer();;
     TokenStream tok = new StandardFilter(src); 
     tok = new LowerCaseFilter(tok);
     tok = new StopFilter(tok,STOP_WORDS_SET);
     //tok = new StopFilter(tok,StopAnalyzer.ENGLISH_STOP_WORDS_SET);
     return new TokenStreamComponents(src, new PorterStemFilter(tok));
   }
   /*
   public static void main(String[] args) throws IOException {
     // text to tokenize
     final String text = "This is a demo of the TokenStreams REPLACEMENT API";
     
     Version matchVersion = Version.LUCENE_5_3_1; // Substitute desired Lucene version for XY
     MyAnalyzer analyzer = new MyAnalyzer(matchVersion);
     TokenStream stream = analyzer.tokenStream("field", new StringReader(text));
     
     // get the CharTermAttribute from the TokenStream
     CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
 
     try {
       stream.reset();
     
       // print all tokens until stream is exhausted
       while (stream.incrementToken()) {
         System.out.println(termAtt.toString());
       }
     
       stream.end();
     } finally {
       stream.close();
     }
   }
   */
 }

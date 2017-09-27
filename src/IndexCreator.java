import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.tidy.Tidy;

import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing.
 * Run it with no command-line arguments for usage information.
 */
public class IndexCreator{
  
  private IndexCreator() {}

  /** Index all text files under a directory. */
  public static void startIndexCreator(String[] indexArgs) {
	  System.out.println("index args");
    String indexPath = "index";
    String docsPath = null;
    boolean create = true;
    for(int i=0;i<indexArgs.length;i++) {
      if ("-index".equals(indexArgs[i])) {
        indexPath = indexArgs[i+1];
        i++;
      } else if ("-docs".equals(indexArgs[i])) {
        docsPath = indexArgs[i+1];
        i++;
      } else if ("-update".equals(indexArgs[i])) {
        create = false;
      }
    }

    if (docsPath == null) {
      System.err.println("Usage: " + "No docs path");
      System.exit(1);
    }

    final Path docDir = Paths.get(docsPath);
    if (!Files.isReadable(docDir)) {
     // System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
     // System.out.println("Indexing to directory '" + indexPath + "'...");

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      Analyzer analyzer = new MyAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

      if (create) {
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwc.setOpenMode(OpenMode.CREATE);
      } else {
        // Add new documents to an existing index:
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      }

      IndexWriter writer = new IndexWriter(dir, iwc);
      indexDocs(writer, docDir);

      writer.close();

      Date end = new Date();
     // System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      //System.out.println(" caught a " + e.getClass() +
       //"\n with message: " + e.getMessage());
    }
  }

  /**
   * Indexes the given file using the given writer, or if a directory is given,
   * recurses over files and directories found under the given directory.
   * 
   * NOTE: This method indexes one document per input file.  This is slow.  For good
   * throughput, put multiple documents into your input file(s).  An example of this is
   * in the benchmark module, which can create "line doc" files, one document per line,
   * using the
   * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
   * >WriteLineDocTask</a>.
   *  
   * @param writer Writer to the index where the given file/dir info will be stored
   * @param path The file to index, or the directory to recurse into to find files to index
   * @throws IOException If there is a low-level I/O error
   */
  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
        	  //System.out.println(attrs.lastModifiedTime());
        	  	
        	  indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
            
          } catch (IOException ignore) {
            // don't index files that can't be read.
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
    }
  }

  /** Indexes a single document */
  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
    try (InputStream stream = Files.newInputStream(file)) {
      // make a new, empty document
    	//Reader inpStreamReader = new InputStreamReader(stream);
    	//HTMLParser parser = new HTMLParser(new StringReader("Text"));
    //	DemoHTMLParser parser = new DemoHTMLParser();
    	//parser.parse(arg0, arg1, arg2, arg3, arg4)
    	//
      Tidy tidy = new Tidy();
      tidy.setQuiet(true);
      tidy.setShowWarnings(false);
      org.w3c.dom.Document root = tidy.parseDOM(stream, null);
              
      Element rawDoc = root.getDocumentElement();
      String body = getBody(rawDoc);
      String title = getTitle(rawDoc);
      System.out.println(title);
      Document doc = new Document();
      
      // Add the path of the file as a field named "path".  Use a
      // field that is indexed (i.e. searchable), but don't tokenize 
      // the field into separate words and don't index term frequency
      // or positional information:
      Field pathField = new StringField("path", file.toString(), Field.Store.YES);
      doc.add(pathField);
      
      // Add the last modified date of the file a field named "modified".
      // Use a LongField that is indexed (i.e. efficiently filterable with
      // NumericRangeFilter).  This indexes to milli-second resolution, which
      // is often too fine.  You could instead create a number based on
      // year/month/day/hour/minutes/seconds, down the resolution you require.
      // For example the long value 2011021714 would mean
      // February 17, 2011, 2-3 PM.
      doc.add(new LongField("modified", lastModified, Field.Store.YES));
      Date modifiedDate = new Date(lastModified);//lastModified.
      SimpleDateFormat smpDate = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss");
      //System.out.println(smpDate.format( modifiedDate));
      doc.add(new StringField("modTime",smpDate.format( modifiedDate), Field.Store.YES));
      
      // Add the contents of the file to a field named "contents".  Specify a Reader,
      // so that the text of the file is tokenized and indexed, but not stored.
      // Note that FileReader expects the file to be in UTF-8 encoding.
      // If that's not the case searching for special characters will fail.
     //doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
      
      
     // doc.add(new TextField("contents", lastModified + " " + title + " " +body ,Field.Store.YES));
      doc.add(new TextField("contents", body ,Field.Store.YES));
      doc.add(new TextField("title", title,Field.Store.YES));
      
      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
        // New index, so we just add the document (no old document can be there):
        System.out.println("adding " + file);
        writer.addDocument(doc);
      } else {
        // Existing index (an old copy of this document may have been indexed) so 
        // we use updateDocument instead to replace the old one matching the exact 
        // path, if present:
        //System.out.println("updating " + file);
        writer.updateDocument(new Term("path", file.toString()), doc);
      }
    }
  }
  
  protected static String getTitle(Element rawDoc) {
      if (rawDoc == null) {
          return null;
      }

      String title = "";

      NodeList children = rawDoc.getElementsByTagName("title");
      if (children.getLength() > 0) {
          Element titleElement = ((Element) children.item(0));
          Text text = (Text) titleElement.getFirstChild();
          if (text != null) {
              title = text.getData();
          }
      }
      return title;
  }

  protected static String getBody(Element rawDoc) {
      if (rawDoc == null) {
          return null;
      }

      String body = "";
      NodeList children = rawDoc.getElementsByTagName("body");
      if (children.getLength() > 0) {
          body = getText(children.item(0));
      }
      return body;
  }
  
  protected static String getText(Node node) {
      NodeList children = node.getChildNodes();
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < children.getLength(); i++) {
          Node child = children.item(i);
          switch (child.getNodeType()) {
              case Node.ELEMENT_NODE:
                  sb.append(getText(child));
                  sb.append(" ");
                  break;
              case Node.TEXT_NODE:
                  sb.append(((Text) child).getData());
                  break;
          }
      }
      return sb.toString();
  }

}
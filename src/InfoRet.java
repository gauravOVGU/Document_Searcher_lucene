import gui.ava.html.image.generator.HtmlImageGenerator;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.postingshighlight.*;
import org.apache.lucene.search.spans.SpanScorer;

import javax.swing.JPanel;
import javax.print.attribute.standard.PrinterLocation;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Paths;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JScrollPane;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class InfoRet {

	private JFrame frame;
	private JTextField textField;
	private JTextField pathVar;
	private JTextField queryStr;
	private JTable searchTable;
	private String textForSearch;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InfoRet window = new InfoRet();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public InfoRet() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1250, 650);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(33, 38, 1250, 650);
		frame.getContentPane().add(tabbedPane);
		
		JPanel IndexFiles = new JPanel();
		tabbedPane.addTab("Index Files", null, IndexFiles, null);
		IndexFiles.setLayout(null);
		
		JLabel Path = new JLabel("PATH:");
		Path.setBounds(12, 23, 56, 16);
		IndexFiles.add(Path);
		
		pathVar = new JTextField();
		/*athVar.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				if(!pathVar.getText().equals(null)){
					JOptionPane.showMessageDialog(null, "Inavlid File path!");
				}
				
			}
		});*/
		pathVar.setToolTipText("Enter the folder to be indexed.");
		pathVar.setBounds(60, 20, 344, 25);
		IndexFiles.add(pathVar);
		pathVar.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			String docsPath;

			public void actionPerformed(ActionEvent arg0) {
				try{
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if(!pathVar.equals(null)){
					File pwd = new File(pathVar.getText()); 
					fc.setCurrentDirectory(pwd);
				}
				int returnVal = fc.showOpenDialog(tabbedPane);
				File currDir = fc.getCurrentDirectory();
				System.out.println(arg0.toString());
				if(!fc.getSelectedFile().equals(null))
					docsPath =  fc.getSelectedFile().getPath();
				else 
					docsPath = currDir.getPath();
 				pathVar.setText(docsPath);
				}
				catch(Exception exp)
				{
					//Handle java null point exception
				}
			}
		});
		btnBrowse.setBounds(406, 19, 97, 26);
		IndexFiles.add(btnBrowse);
		
		JButton btnStartIndexing = new JButton("Start Indexing");
		btnStartIndexing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Lets Redirect");
				if(!pathVar.getText().equals(null)){
					System.out.println("Lets Redirect inside");
					final String [] docsPath = new String [] {"-docs" ,pathVar.getText()};
					(new Thread(){
						public void run(){
							IndexCreator.startIndexCreator(docsPath);
						}
					}).start();
				}
				else
					JOptionPane.showMessageDialog(null, "Please enter the path!");
			}
		});
		btnStartIndexing.setBounds(194, 56, 113, 25);
		IndexFiles.add(btnStartIndexing);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(27, 144, 476, -6);
		IndexFiles.add(separator);
		
		JScrollPane scrollPaneForTextArea = new JScrollPane();
		scrollPaneForTextArea.setBounds(12, 94, 964, 409);
		IndexFiles.add(scrollPaneForTextArea);
		
		
		//JScrollPane scrollPaneforTextArea = new JScrollPane();
		//IndexFiles.add(scrollPaneforTextArea);
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPaneForTextArea.setViewportView(textArea);
		/*Redirecting SOP outputs to JTextArea*/
		PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
		final PrintStream sysOut = System.out; //Backup of System.out
		final PrintStream sysErr = System.err; //Backup of System.err
		System.setOut(printStream);
		System.setErr(printStream);
		//scrollPaneforTextArea.add(textArea);
		
		JPanel SearchFiles = new JPanel();
		tabbedPane.addTab("Search Files", null, SearchFiles, null);
		SearchFiles.setLayout(null);
		
		JLabel lblQuery = new JLabel("Query :");
		lblQuery.setBounds(12, 13, 56, 16);
		SearchFiles.add(lblQuery);
		
		queryStr = new JTextField();
		queryStr.setBounds(65, 10, 438, 22);
		SearchFiles.add(queryStr);
		queryStr.setColumns(10);
		
		JLabel lblNumOfResults = new JLabel("Num Of Results :");
		lblNumOfResults.setBounds(12, 56, 102, 27);
		SearchFiles.add(lblNumOfResults);
		
		final JSpinner spinner = new JSpinner();
		spinner.setBounds(114, 58, 142, 22);
		spinner.setValue(50);
		SearchFiles.add(spinner);
		
		JButton btnStartSearch = new JButton("Start Search");
		
		btnStartSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				/*Remove the existing rows in the table*/
				DefaultTableModel tableModle = (DefaultTableModel) searchTable.getModel();
				if(tableModle.getRowCount() != 0){
						tableModle.setRowCount(0);
						//tableModle.fireTableDataChanged();
				}
				
				String [] searchArgs;
				System.setErr(sysErr);
				System.setOut(sysOut);
				//System.out.println("inside search index!!");
				//JOptionPane.showMessageDialog(null, "Please enter query!");
				
				if(queryStr.getText().trim().isEmpty()){
					JOptionPane.showMessageDialog(null, "Please enter query!");
				}
				if(!queryStr.getText().trim().isEmpty()){
					if(!spinner.getValue().equals(null)){
						//searchArgs = 
					}
					textForSearch=queryStr.getText();
					searchArgs = new String [] {"-index","C:\\Users\\Gaurav Sharma\\workspace\\InfoRetrieval\\index","-query",queryStr.getText(),"-paging",spinner.getValue().toString()};
					//searchArgs = new String [] {"-index","C:\\Users\\Prash\\workspace\\InfoRetrieval\\index","-query","index","-paging",spinner.getValue().toString()};
					//(new Thread(){
						//public void run(){
							
							try {
								ScoreDoc[] scoreDocs = SearchIndex.startSearch(searchArgs);
							    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("C:\\Users\\Gaurav Sharma\\workspace\\InfoRetrieval\\index")));
							    IndexSearcher searcher = new IndexSearcher(reader);
							    MyAnalyzer analyser = new MyAnalyzer();
							    Query query = MultiFieldQueryParser.parse( "index",   new String[] {"contents","title","modTime"},new BooleanClause.Occur[] {Occur.SHOULD,Occur.SHOULD,Occur.SHOULD},analyser);
							    
							    TopDocs hit = searcher.search(query, 10);
							    
							    SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
					            Highlighter highlighter = new Highlighter( htmlFormatter,new QueryScorer(query));
					           
							     for(int i = 0; i < (int)spinner.getValue() ; i++){
							    	 Document doc = searcher.doc(scoreDocs[i].doc);
							         String lastModifiedTime = doc.get("modTime");
							         String summary = doc.get("contents");
							         String relevance = String.valueOf(scoreDocs[i].score);
							         String path = doc.get("path");
							         String title = "File Image Icon:"+path;
							         
							         
							         //String 
							         //int id = hit.scoreDocs[i].doc;
						              //Document docs = searcher.doc(id);
						              //String text = doc.get("contents");
						             /* TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), i, "contents", analyser);
						              TextFragment[] frag = null;
									try {
										frag = highlighter.getBestTextFragments(tokenStream, summary, false, 1);
										//for (int j = 0; j < frag.length; j++) {
							              //  if ((frag[j] != null) && (frag[j].getScore() > 0)) {
							                  System.out.println(("Highlighter new:: " + frag[0].toString()));
							                //}
							              //}
									} catch (InvalidTokenOffsetsException exp) {
										// TODO Auto-generated catch block
										exp.printStackTrace();
									}//highlighter.getBestFragments(tokenStream, text, 3, "...")
									catch (Exception exp)
									{
										exp.printStackTrace();
									}
									*/
							         //System.out.println("Before adding");
									textField = new JTextField();
									for(int j =0;j<searchTable.getColumnCount();j++){
										searchTable.getColumnModel().getColumn(j).setCellRenderer(getRenderer());
							         }
										tableModle.addRow(new Object[] {String.valueOf(i+1),title,path,summary,lastModifiedTime,relevance});
										
							         //System.out.println("after table add");  
							         //searchOut.add(new SearchOutput(String.valueOf(1), "title", "path", "summary", "lastModifiedTime", "relevance"));
							       //  System.out.println(i);
							     }
							/*	Iterator<SearchOutput> iteratorSearch = searchOut.iterator();
								while(iteratorSearch.hasNext()){
									SearchOutput tempObj = iteratorSearch.next();
									tableModle.addRow(new Object[] {tempObj.rank,tempObj.title,tempObj.path,tempObj.summary,tempObj.lastModifiedTime,tempObj.relevance});
								}*/
								
							} catch (Exception exp) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
								//Handle the exception
							}
					//	}
				//	}).start();
					
					//tableModle.addRow(new Object[] {"Col1" + i,"col2"+ i,"col3"+ i,"col4"+ i,"col5"+ i,"col6"+ i,"col7"+ i});
					
				}
			}
		});
		btnStartSearch.setBounds(268, 57, 111, 25);
		SearchFiles.add(btnStartSearch);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(22, 96, 1200, 500);
		SearchFiles.add(scrollPane);
		
		
        String[] columnNames = {"Rank",
                "Image",
                "Path",
                "Summary",
                "Last Modified",
                "Relevance"};
        Object[][] data = {        };
		//searchTable = new JTable(data,columnNames);
		//searchTable.setModel(new DefaultTableModel());
       searchTable = new JTable(new DefaultTableModel(data,columnNames)){
    	   public boolean isCellEditable(int row, int column){ 
    		   return true;
    		}
       };
       searchTable.setCellSelectionEnabled(true);
      searchTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      // searchTable.isCellEditable(row, column)
      
		/*Resizing the columns*/
		searchTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		searchTable.getColumnModel().getColumn(1).setPreferredWidth(400);
		searchTable.getColumnModel().getColumn(2).setPreferredWidth(5);
		searchTable.getColumnModel().getColumn(3).setPreferredWidth(400);
		searchTable.getColumnModel().getColumn(4).setPreferredWidth(5);
		searchTable.getColumnModel().getColumn(5).setPreferredWidth(5);
		searchTable.setRowHeight(200);
		
		
		scrollPane.setViewportView(searchTable);
		
		JButton btnStop = new JButton("Stop!");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		btnStop.setBounds(391, 57, 97, 25);
		SearchFiles.add(btnStop);
		//searchTable.
	}
	public static BufferedImage getAllImages(File directory, boolean descendIntoSubDirectories) throws IOException {
        BufferedImage image = ImageIO.read(new File(System.getProperty("user.dir")+"\\Resources\\Icons\\html.png"));
        int imagedim = image.getWidth()* image.getHeight();
        File[] f = directory.listFiles();
        for (File file : f) {
            if (file != null && (file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".jpg")) && !file.getName().startsWith("tn_")) {
            	
            	try {
            		BufferedImage imageTmp = ImageIO.read(file);
            		if(imageTmp!=null)
            		{
            		int imagedimTmp = imageTmp.getWidth()* imageTmp.getHeight();
            	    if(imagedimTmp>imagedim)
            	    {
            	    	image=imageTmp;
            	    	imagedim=imagedimTmp;
            	    }
            	    }
            	} catch (IOException ex) {
            	    ex.printStackTrace();
            	}
            }
            if (descendIntoSubDirectories && file.isDirectory()) {
            	BufferedImage imageTmp = getAllImages(file, true);
            	if(imageTmp!=null)
        		{
        		int imagedimTmp = imageTmp.getWidth()* imageTmp.getHeight();
        	    if(imagedimTmp>imagedim)
        	    {
        	    	image=imageTmp;
        	    	imagedim=imagedimTmp;
        	    }
        	    }
            }
        }
        image = scaleImage(image, BufferedImage.TYPE_INT_ARGB, 300, 200);
            return image;
    }
	
	public String getSummary(String content, String Query)
	{
		StringBuffer summary = new StringBuffer();
		int counter = 0;
		String[] sentences = content.split(Pattern.quote("."));
		if(content.contains(Query))
		{
			for(int i=0;i<sentences.length;i++)
			{
				if(sentences[i].contains(Query))
				{
					summary.append(sentences[i]);
					summary.append("...");
					counter++;
					if(counter>=3)
						break;
				}
			}
		}
		else 
			return content;
		
		return summary.toString();
	}
	
	/* public String[] getFragmentsWithHighlightedTerms(Analyzer analyzer, Query query, 
             String fieldName, String fieldContents, int fragmentNumber, int fragmentSize) throws IOException {

     TokenStream stream = TokenSources.getTokenStream(fieldName, fieldContents, analyzer);
     SpanScorer scorer = new SpanScorer(query, fieldName,new CachingTokenFilter(stream));
     Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmentSize);
     
     Highlighter highlighter = new Highlighter(scorer);
     highlighter.setTextFragmenter(fragmenter);
     highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);
             
     String[] fragments = highlighter.getBestFragments(stream, fieldContents, fragmentNumber);
             
     return fragments;
}*/
	private TableCellRenderer getRenderer() {
        return new TableCellRenderer() {
        	

            @Override
            public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
            	JTextArea Summary=new JTextArea(getSummary(arg1.toString(),textForSearch));
            	if(arg1.toString().contains("File Image Icon:"))
            	{
            		
            		String fileExt=arg1.toString().substring(arg1.toString().lastIndexOf(".")+1);
            		JLabel lbl = new JLabel();
            		lbl.setText((String)arg1.toString().substring(arg1.toString().lastIndexOf("\\")+1));
            		System.out.println(System.getProperty("user.dir")+"\\Resources\\Icons\\"+fileExt+".png");
            		if(fileExt.equals("html"))
            		{
            			BufferedImage image = null;
						try {
							image = getAllImages(new File(arg1.toString().substring(arg1.toString().indexOf(":")+1,arg1.toString().indexOf(".html"))+"_files"),true);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
            			
            			lbl.setIcon((new ImageIcon(image)));
            		}
            		else
            		{
            			lbl.setIcon(new ImageIcon(System.getProperty("user.dir")+"\\Resources\\Icons\\"+fileExt+".png"));
            		}
            		 //JScrollPane pane = new JScrollPane(lbl);
            			return lbl;
            	}
            	if(arg1 != null){
            		
                	Summary.setWrapStyleWord(true);
                	Summary.setLineWrap(true);
                    textField.setText(getSummary(arg1.toString(),textForSearch));
                    String string = getSummary(arg1.toString(),textForSearch).toUpperCase();
                    if(string.contains(textForSearch.toUpperCase())){
                    	System.out.println("Search for="+textForSearch);
                    	System.out.println("cell value="+textField.getText());
                 
                        
                        for (int i = -1; (i = string.indexOf(textForSearch.toUpperCase(), i + 1)) != -1; ) {
                        	try {
                        		Summary.getHighlighter().addHighlight(i,i+textForSearch.length(),new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(Color.RED));
                                
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                            
                        }
            
                    }
                } else {
                    textField.setText("");
                    textField.getHighlighter().removeAllHighlights();
                }
            	//JScrollPane pane = new JScrollPane(Summary);
    			return Summary;
            }
        };
    }
	public static BufferedImage scaleImage(BufferedImage image, int imageType,
	        int newWidth, int newHeight) {
	        // Make sure the aspect ratio is maintained, so the image is not distorted
	        double thumbRatio = (double) newWidth / (double) newHeight;
	        int imageWidth = image.getWidth(null);
	        int imageHeight = image.getHeight(null);
	        double aspectRatio = (double) imageWidth / (double) imageHeight;

	        if (thumbRatio < aspectRatio) {
	            newHeight = (int) (newWidth / aspectRatio);
	        } else {
	            newWidth = (int) (newHeight * aspectRatio);
	        }

	        // Draw the scaled image
	        BufferedImage newImage = new BufferedImage(newWidth, newHeight,
	                imageType);
	        Graphics2D graphics2D = newImage.createGraphics();
	        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);

	        return newImage;
	    }

	
}





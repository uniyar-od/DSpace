/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.disseminate;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1AfmPfbFont;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.poi.hssf.record.DrawingSelectionRecord;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.*;
import org.dspace.content.Collection;
import org.dspace.content.crosswalk.StreamDisseminationCrosswalk;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.PluginManager;
import org.dspace.handle.HandleManager;

import java.awt.*;
import java.io.*;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Citation Document produces a dissemination package (DIP) that is different that the archival package (AIP).
 * In this case we append the descriptive metadata to the end (configurable) of the document. i.e. last page of PDF.
 * So instead of getting the original PDF, you get a cPDF (with citation information added).
 *
 * @author Peter Dietz (peter@longsight.com)
 */
public class CitationDocument {
    /**
     * Class Logger
     */
    private static Logger log = Logger.getLogger(CitationDocument.class);

    private float rowHeight = 55f;

    /**
     * Comma separated list of collections handles to enable citation for.
     * webui.citation.enabled_collections, default empty/none. ex: =1811/123, 1811/345
     */
    private static String citationEnabledCollections = null;

    /**
     * Comma separated list of community handles to enable citation for.
     * webui.citation.enabled_communties, default empty/none. ex: =1811/123, 1811/345
     */
    private static String citationEnabledCommunities = null;

    /**
     * List of all enabled collections, inherited/determined for those under communities.
     */
    private static ArrayList<String> citationEnabledCollectionsList;

    private static File tempDir;

    private String font;
    private String fontSize;
    private String header1;
    private String header2;
    private String[] fields;
    private String footer;
    
    
    private int xwidth = 550;
    private int ygap = 15;
    private String xinit;
    private String yinit;
    private float lineSpace;
    
    private final String IMG_SEPARATOR ="###";

    public CitationDocument() {}
    
    public CitationDocument(String configuration) {
        //Load enabled collections
        
        font = ConfigurationManager.getProperty(configuration,"font");
        fontSize = ConfigurationManager.getProperty(configuration,"fontSize");
        // Configurable text/fields, we'll set sane defaults
        header1 = ConfigurationManager.getProperty(configuration, "header1");
        
        header2 = ConfigurationManager.getProperty(configuration, "header2");
        
        xinit= ConfigurationManager.getProperty(configuration, "xstartposition");
        yinit= ConfigurationManager.getProperty(configuration, "ystartposition");


        String fieldsConfig = ConfigurationManager.getProperty(configuration, "fields");
        if(StringUtils.isNotBlank(fieldsConfig)) {
            fields = fieldsConfig.split(",");
        } else {
            fields = new String[]{"dc.date.issued", "dc.title", "dc.creator", "dc.contributor.author", "dc.publisher", "_line_", "dc.identifier.citation", "dc.identifier.uri"};
        }

        footer = ConfigurationManager.getProperty(configuration, "footer");
        
        
        //Ensure a temp directory is available
        String tempDirString = ConfigurationManager.getProperty("dspace.dir") + "/temp";
        tempDir = new File(tempDirString);
        if(!tempDir.exists()) {
            boolean success = tempDir.mkdir();
            if(success) {
                log.info("Created temp directory at: " + tempDirString);
            } else {
                log.info("Unable to create temp directory at: " + tempDirString);
            }
        }
    	
    }


    /**
     * Should the citation page be the first page of the document, or the last page?
     * default => true. true => first page, false => last page
     * citation_as_first_page=true
     */
    private Boolean citationAsFirstPage = null;

    private Boolean isCitationFirstPage(String configuration) {
        if(citationAsFirstPage == null) {
            citationAsFirstPage = ConfigurationManager.getBooleanProperty(configuration, "citation_as_first_page", true);
        }

        return citationAsFirstPage;
    }

    public File makeCitedDocument(Bitstream bitstream)
            throws IOException, SQLException, AuthorizeException, COSVisitorException {
    	return makeCitedDocument(null, bitstream,"disseminate-citation");
    }
    
    /**
     * Creates a
     * cited document from the given bitstream of the given item. This
     * requires that bitstream is contained in item.
     * <p>
     * The Process for adding a cover page is as follows:
     * <ol>
     *  <li> Load source file into PdfReader and create a
     *     Document to put our cover page into.</li>
     *  <li> Create cover page and add content to it.</li>
     *  <li> Concatenate the coverpage and the source
     *     document.</li>
     * </p>
     *
     * @param bitstream The source bitstream being cited. This must be a PDF.
     * @return The temporary File that is the finished, cited document.
     * @throws java.io.FileNotFoundException
     * @throws SQLException
     * @throws org.dspace.authorize.AuthorizeException
     */
    public File makeCitedDocument(Bitstream bitstream,String configuration)
            throws IOException, SQLException, AuthorizeException, COSVisitorException {
    	return makeCitedDocument(null, bitstream,configuration);
    }
    
    public File makeCitedDocument(Context context, Bitstream bitstream,String configuration)
            throws IOException, SQLException, AuthorizeException, COSVisitorException {
        PDDocument document = new PDDocument();
        PDDocument sourceDocument = new PDDocument();
        try {
            Item item = (Item) bitstream.getParentObject();
            sourceDocument = sourceDocument.load(bitstream.retrieve());
            PDPage coverPage = new PDPage(PDPage.PAGE_SIZE_LETTER);
            generateCoverPage(context,document, coverPage, item,configuration);
            addCoverPageToDocument(document, sourceDocument, coverPage,configuration);

            document.save(tempDir.getAbsolutePath() + "/bitstream.cover.pdf");
            return new File(tempDir.getAbsolutePath() + "/bitstream.cover.pdf");
        } finally {
            sourceDocument.close();
            document.close();
        }
    }

    private void generateCoverPage(Context context,PDDocument document, PDPage coverPage, Item item,String configuration) throws IOException, COSVisitorException {
        PDPageContentStream contentStream = new PDPageContentStream(document, coverPage);
        try {
            int ypos = StringUtils.isNotBlank(yinit)?Integer.parseInt(yinit):730;
            int xpos = StringUtils.isNotBlank(xinit)?Integer.parseInt(xinit):30;
            
            Locale currLocale = null;
            if(context!= null) {
            	currLocale = context.getCurrentLocale();
            }
            
            

            PDFont pdFont = PDType1Font.HELVETICA;
            if(StringUtils.isNotBlank(font)){
            	if(StringUtils.equalsIgnoreCase(font, "times")){
            		pdFont = PDType1Font.TIMES_ROMAN;
            	}else if(StringUtils.equalsIgnoreCase(font, "courier")){
            		pdFont = PDType1Font.COURIER;
            	}
            }
            
            contentStream.setNonStrokingColor(Color.BLACK);

            int size = StringUtils.isNotBlank(fontSize)? Integer.parseInt(fontSize):10;
            
            rowHeight = 1.2f*size*3;
            if(StringUtils.isNotBlank(header1)){
            	ypos -=(ygap);
            	String text = StringUtils.replace(header1, "[[date]]", DCDate.getCurrent().toString());
            	ypos= drawStringImageWordWrap(document, coverPage, contentStream, text, xpos, ypos, pdFont, size,item,configuration);
            }
            //drawTable(coverPage, contentStream, ypos, xpos, content, head1Font, 11, false);

            if(StringUtils.isNotBlank(header2)){
            	String text = StringUtils.replace(header2, "[[date]]", DCDate.getCurrent().toString());
            	ypos= drawStringImageWordWrap(document, coverPage, contentStream, text, xpos, ypos, pdFont, size,item,configuration);
            }

           
            String[][] labelValues = new String[fields.length][2];
            int x=0;
            for(String field : fields) {

            	String label="";
            	String value ="";

            	if(StringUtils.isNotBlank(field) && StringUtils.equals(field, "[[citation]]")){
					label = I18nUtil.getMessage("metadata.coverpage.citation",
							currLocale, false);
            		value=makeCitation(item);
            	}else{
            		label = I18nUtil.getMessage("metadata.coverpage." + field,
							currLocale, false);
            		Metadatum[] meta = item.getMetadataByMetadataString(field);
            		if(meta != null){
	            		for(int z=0;z<meta.length;z++){
	            			if(z>0) {
	            				value+="; ";
	            			}
	            			value += meta[z].value;
	            			
	            		}
            		}
				}

            	labelValues[x][0]=label;
            	labelValues[x][1]=value;
            	
            	x++;
            	
            }
            drawTable(coverPage, contentStream, ypos, xpos, labelValues, pdFont, size, true);
            ypos-=(rowHeight*x);
            
            if(StringUtils.isNotBlank(footer)){
            	ypos -=(ygap);
            	String text = StringUtils.replace(footer, "[[date]]", DCDate.getCurrent().toString());
            	ypos= drawStringImageWordWrap(document, coverPage, contentStream, text, xpos, ypos, pdFont, size,item,configuration);
            }

        } finally {
            contentStream.close();
        }
    }

    private void addCoverPageToDocument(PDDocument document, PDDocument sourceDocument, PDPage coverPage, String configuration) {
        List<PDPage> sourcePageList = sourceDocument.getDocumentCatalog().getAllPages();

        if (isCitationFirstPage(configuration)) {
            //citation as cover page
            document.addPage(coverPage);
            for (PDPage sourcePage : sourcePageList) {
                document.addPage(sourcePage);
            }
        } else {
            //citation as tail page
            for (PDPage sourcePage : sourcePageList) {
                document.addPage(sourcePage);
            }
            document.addPage(coverPage);
        }
        sourcePageList.clear();
    }

    public int drawStringImageWordWrap(PDDocument document, PDPage page, PDPageContentStream contentStream, String text,
            int startX, int startY, PDFont pdfFont, float fontSize,Item item,String configuration) throws IOException {
    	String[] head = StringUtils.split(text,IMG_SEPARATOR);
    	for(String h: head){
    		String imgProp = ConfigurationManager.getProperty(configuration,"img."+h);
    		
    		if(StringUtils.isNotBlank(imgProp) ){
    			File file = new File(imgProp);
    			if(file!= null && file.exists()){

                	InputStream inImg = new FileInputStream(file);
                	
                	PDXObjectImage poi = new PDJpeg(document,inImg);
                	contentStream.drawImage(poi, startX, startY);
                	startY -=(ygap);
                	inImg.close();
    			}

    		}else{
    			startY = drawStringWordWrap(page,contentStream,h,startX,startY,pdfFont,fontSize);
    			startY -=(ygap);
    			
    		}
    	}
    	return startY;
    }
    
    
    public int drawStringWordWrap(PDPage page, PDPageContentStream contentStream, String text,
                                    int startX, int startY, PDFont pdfFont, float fontSize) throws IOException {
        float leading = 1.5f * fontSize;

        PDRectangle mediabox = page.findMediaBox();
        float margin = 72;
        float width = mediabox.getWidth() - 2*margin;

        List<String> lines = new ArrayList<>();
    	String[] pieces = StringUtils.split(text, " ");
    	String str ="";
    	for(int z=0;z<pieces.length;z++) {
    		if(z>0) {
    			str+=" ";
    		}
    		String piece = str+pieces[z];
    		float size = fontSize * pdfFont.getStringWidth(piece) / 1000;
    		if(size > width) {
    			lines.add(str);
    			str=pieces[z];
    		}else {
    			str=piece;
    		}
    	}
    	lines.add(str);

        contentStream.beginText();
        contentStream.setFont(pdfFont, fontSize);
        contentStream.moveTextPositionByAmount(startX, startY);
        int currentY = startY;
        for (String line: lines)
        {
            contentStream.drawString(line);
            currentY -= leading;
            contentStream.moveTextPositionByAmount(0, -leading);
        }
        contentStream.endText();
        return currentY;
    }

    public int drawStringCellWordWrap(PDPage page, PDPageContentStream contentStream, String text,
            int startX, int startY,float cellWidth, PDFont pdfFont, float fontSize) throws IOException {
    	float leading = 1.5f * fontSize;

    	/*PDRectangle mediabox = page.findMediaBox();
    	float margin = 72;
    	float cellwidth = mediabox.getWidth() - 2*margin;
    	 */
    	List<String> lines = new ArrayList<>();
    	
    	String[] pieces = StringUtils.split(text, " ");
    	String str ="";
    	for(int z=0;z<pieces.length;z++) {
    		if(z>0) {
    			str+=" ";
    		}
    		String piece = str+pieces[z];
    		float size = fontSize * pdfFont.getStringWidth(piece) / 1000;
    		if(size > cellWidth) {
    			lines.add(str);
    			str=pieces[z];
    		}else {
    			str=piece;
    		}
    	}
    	lines.add(str);
    	contentStream.beginText();
    	contentStream.setFont(pdfFont, fontSize);
    	contentStream.moveTextPositionByAmount(startX, startY);
    	int currentY = startY;
    	for (String line: lines)
    	{
    		contentStream.drawString(line);
    		currentY -= leading;
    		contentStream.moveTextPositionByAmount(0, -leading);
    	}
    	contentStream.endText();
    	return currentY;
	}

    
    
    public String getOwningCommunity(Item item) {
        try {
            Community[] comms = item.getCommunities();
            if(comms.length > 0) {
                return comms[0].getName();
            } else {
                return " ";
            }

        } catch (SQLException e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
    }

    public String getOwningCollection(Item item) {
        try {
            return item.getOwningCollection().getName();
        } catch (SQLException e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
    }

    public String getAllMetadataSeparated(Item item, String metadataKey) {
        Metadatum[] Metadatums = item.getMetadataByMetadataString(metadataKey);

        ArrayList<String> valueArray = new ArrayList<String>();

        for(Metadatum Metadatum : Metadatums) {
            if(StringUtils.isNotBlank(Metadatum.value)) {
                valueArray.add(Metadatum.value);
            }
        }

        return StringUtils.join(valueArray.toArray(), "; ");
    }

    /**
     * @param page
     * @param contentStream
     * @param y the y-coordinate of the first row
     * @param margin the padding on left and right of table
     * @param content a 2d array containing the table data
     * @throws IOException
     */
    public void drawTable(PDPage page, PDPageContentStream contentStream,
                                 float y, float margin,
                                 String[][] content, PDFont font, int fontSize, boolean cellBorders) throws IOException {
        final int rows = content.length;
        final int cols = content[0].length;
        final float tableWidth = page.findMediaBox().getWidth()-(2*margin);
        final float tableHeight = rowHeight * rows;
        final float colWidth = tableWidth/(float)cols;
        final float cellMargin=2f;

        float textx = margin+cellMargin;
        float texty = y-15;
        float celly= y-rowHeight;
        for(int i = 0; i < content.length; i++){
            for(int j = 0 ; j < content[i].length; j++){
            	contentStream.setFont(font, fontSize);
                String text = content[i][j];
            	if(j%2==0){
            		
            		contentStream.setNonStrokingColor(220, 220, 220); //gray background
            		contentStream.fillRect(margin,celly,colWidth-130 , rowHeight-1);
            		contentStream.setNonStrokingColor(0, 0, 0);
            		drawStringCellWordWrap(page, contentStream, text, (int)textx, (int)texty,colWidth-131, font, fontSize);
            		textx += colWidth-130;
            		
            	}else{
            		contentStream.setFont(PDType1Font.TIMES_BOLD, fontSize);
            		contentStream.setNonStrokingColor(0, 0, 0);
            		drawStringCellWordWrap(page, contentStream, text, (int)textx, (int)texty,colWidth+130, font, fontSize);
            		textx += colWidth+130;
            	}
                
            }
            celly-=rowHeight;
            texty-=rowHeight;
            textx = margin+cellMargin;
        }
        
        if(cellBorders) {
            //draw the rows
            float nexty = y ;
            for (int i = 0; i <= rows; i++) {
                contentStream.drawLine(margin,nexty,margin+tableWidth,nexty);
                nexty-= rowHeight;
            }

            //draw the columns
            float nextx = margin;
            for (int i = 0; i <= cols; i++) {
                contentStream.drawLine(nextx,y,nextx,y-tableHeight);
                if(i%2==0){
                	 nextx+= colWidth-130;	
                }else{
                	nextx += colWidth+130;
                }
                
            }
        }

        //now add the text

    }
    
    private String makeCitation(Item item){
    	String citation ="";
    	String type=item.getMetadata("dc.type");
        CoverpageCitationCrosswalk coverpageCrosswalk = null;

        if (type != null)
        {
            coverpageCrosswalk = (CoverpageCitationCrosswalk) PluginManager
                    .getNamedPlugin(CoverpageCitationCrosswalk.class,
                            type);
        }
        
        if(coverpageCrosswalk != null){
        	citation = coverpageCrosswalk.makeCitation(item);
        }
        
    	return citation;
    }
}

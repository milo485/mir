/*
 * Copyright (C) 2001, 2002 The Mir-coders group
 *
 * This file is part of Mir.
 *
 * Mir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Mir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, The Mir-coders gives permission to link
 * the code of this program with  any library licensed under the Apache Software License,
 * The Sun (tm) Java Advanced Imaging library (JAI), The Sun JIMI library
 * (or with modified versions of the above that use the same license as the above),
 * and distribute linked combinations including the two.  You must obey the
 * GNU General Public License in all respects for all of the code used other than
 * the above mentioned libraries.  If you modify this file, you may extend this
 * exception to your version of the file, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package mircoders.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;
import gnu.regexp.REMatchEnumeration;

import mir.config.MirPropertiesConfiguration;
import mir.entity.*;
import mir.log.LoggerWrapper;
import mir.misc.StringUtil;
import mir.util.DateTimeFunctions;
import mircoders.entity.EntityContent;
import mircoders.entity.EntityImages;
import mircoders.storage.*;

public class PDFGenerator{

  public Document document;
  public PdfWriter writer;
  public PdfContentByte cb;
  public String localImageDir;
  public float currentYPosition;
  public int currentPage;
  public float pageWidth;
  public float pageHeight;
  public float verticalMargin;
  public float horizontalMargin;
  public float topEdge;
  public float bottomEdge;
  public float rightEdge;
  public float leftEdge;

  public int    maxImageHeight;
  public int    maxImageWidth;
  protected LoggerWrapper logger;

  public int indexFontSize;
  public int indexLineHeight;
  public int indexFontFamily;

  public float footerHeight;
  public String footerText;
  public int footerFontSize;
  public int footerFontFamily;

  public int metaHeight;
  public int metaFontSize;
  public int metaFontFamily;

  public int descriptionLineHeight;
  public int descriptionFontSize;
  public int descriptionFontFamily;

  public int contentLineHeight;
  public int contentFontSize;
  public int contentFontFamily;

  public int sourceLineHeight;
  public int sourceFontSize;
  public int sourceFontFamily;

  public int bigImageCaptionFontSize;
  public int bigImageCaptionFontFamily;

  protected MirPropertiesConfiguration configuration;


  public PDFGenerator(ByteArrayOutputStream out){
    logger = new LoggerWrapper("PDFGenerator");
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (MirPropertiesConfiguration.PropertiesConfigExc e) {
      throw new RuntimeException("Can't get configuration: " + e.getMessage());
    }
    localImageDir=configuration.getString("Producer.Image.Path");

    try {
      indexFontSize   = Integer.parseInt(configuration.getString("PDF.Index.FontSize"));
      indexLineHeight = Integer.parseInt(configuration.getString("PDF.Index.LineHeight"));
      indexFontFamily = getFontByName(configuration.getString("PDF.Index.FontFamily"));

      footerText = configuration.getString("PDF.Footer.String");
      footerFontSize   = Integer.parseInt(configuration.getString("PDF.Footer.FontSize"));
      footerFontFamily = getFontByName(configuration.getString("PDF.Footer.FontFamily"));
      footerHeight = Integer.parseInt(configuration.getString("PDF.Footer.Height"));;

      metaFontSize   = Integer.parseInt(configuration.getString("PDF.Meta.FontSize"));
      metaFontFamily = getFontByName(configuration.getString("PDF.Meta.FontFamily"));
      metaHeight = Integer.parseInt(configuration.getString("PDF.Meta.Height"));;

      descriptionFontSize   = Integer.parseInt(configuration.getString("PDF.Description.FontSize"));
      descriptionLineHeight = Integer.parseInt(configuration.getString("PDF.Description.LineHeight"));
      descriptionFontFamily = getFontByName(configuration.getString("PDF.Description.FontFamily"));

      contentFontSize   = Integer.parseInt(configuration.getString("PDF.Content.FontSize"));
      contentLineHeight = Integer.parseInt(configuration.getString("PDF.Content.LineHeight"));
      contentFontFamily = getFontByName(configuration.getString("PDF.Content.FontFamily"));

      sourceFontSize   = Integer.parseInt(configuration.getString("PDF.Source.FontSize"));
      sourceLineHeight = Integer.parseInt(configuration.getString("PDF.Source.LineHeight"));
      sourceFontFamily = getFontByName(configuration.getString("PDF.Source.FontFamily"));

      bigImageCaptionFontSize   = Integer.parseInt(configuration.getString("PDF.BigImageCaption.FontSize"));
      bigImageCaptionFontFamily = getFontByName(configuration.getString("PDF.BigImageCaption.FontFamily"));

    }
    catch (NumberFormatException e){
      e.printStackTrace();
    }

    // step 1: make a document

    String pageSize = configuration.getString("PDF.Pagesize");

    if (pageSize.equals("LETTER")){
      document = new Document(PageSize.LETTER);
      pageWidth = 612;
      pageHeight = 792;
    }
    else {
       document = new Document(PageSize.A4);
       pageWidth=595;
       pageHeight=842;
    }

    maxImageHeight    = 250;
    maxImageWidth     = 250;


    verticalMargin = 20;
    horizontalMargin = 20;



    topEdge=pageHeight-verticalMargin;
    bottomEdge=verticalMargin;
    rightEdge=pageWidth-horizontalMargin;
    leftEdge=horizontalMargin;

    currentYPosition=topEdge;
    currentPage = 1;

    String headerText = configuration.getString("PDF.Title.String");

    try{
      writer = PdfWriter.getInstance(document, out);
      cb = writer.getDirectContent();

      document.open();
      addHeader(headerText);
    }
    catch(DocumentException de) {
      logger.error(de.getMessage());
    }
  }

  public void stop(){
    addFooter();
    document.close();
  }

  public void addHeader(String headerText){
    int titleFontSize=Integer.parseInt(configuration.getString("PDF.Title.FontSize"));
    int titleLineHeight=Integer.parseInt(configuration.getString("PDF.Title.LineHeight"));
    String titleFontFamily=configuration.getString("PDF.Title.FontFamily");
    String headerImage=configuration.getString("PDF.Header.Image");
    int headerImageHeight = Integer.parseInt(configuration.getString("PDF.Header.ImageHeight"));

    try {
      if ((! headerImage.equals("")) && headerImageHeight != 0){
  PdfTemplate template = cb.createTemplate(rightEdge-horizontalMargin,headerImageHeight);

  float toYPosition=currentYPosition - headerImageHeight;
  Image theImage = Image.getInstance(headerImage);
  theImage.setAbsolutePosition(0,0);
  //	theImage.scaleAbsolute(img_width,img_height);
  template.addImage(theImage);


  cb.addTemplate(template,leftEdge,toYPosition);
  currentYPosition = toYPosition;
      }
      if (! headerText.equals("")){
  ColumnText ct = new ColumnText(cb);
  //add a basic header
  ct.addText(new Phrase(headerText, new Font(getFontByName(titleFontFamily), titleFontSize,Font.BOLD)));
  float[] rightCol = {rightEdge,currentYPosition,rightEdge,currentYPosition-titleLineHeight};
  float[] leftCol = {leftEdge,currentYPosition,leftEdge,currentYPosition-titleLineHeight};
  ct.setColumns(leftCol,rightCol);
  ct.setYLine(currentYPosition);
  ct.setAlignment(Element.ALIGN_CENTER);
  ct.go();

  currentYPosition = currentYPosition - titleLineHeight;
      }
    }
    catch(DocumentException de) {
      logger.error(de.getMessage());
    }
    catch(MalformedURLException de) {
      logger.error(de.getMessage());
    }
    catch(IOException de) {
      logger.error(de.getMessage());
    }
  }
  public void addIndexItem(EntityContent entityContent){
    try {

      ColumnText ict = new ColumnText(cb);
      String theTitle = entityContent.getValue("title");
      String theCreator = entityContent.getValue("creator");
      Phrase titleP=new Phrase(" - " +  theTitle,new Font(indexFontFamily,indexFontSize,Font.BOLD));
      Phrase creatorP=new Phrase( " :: " + theCreator,new Font(indexFontFamily,indexFontSize));
      float toYPosition = currentYPosition - indexLineHeight;
      float[] leftIndexCols = {leftEdge,currentYPosition,leftEdge,toYPosition};
      float[] rightIndexCols = {rightEdge,currentYPosition,rightEdge,toYPosition};
      ict.addText(titleP);
      ict.addText(creatorP);
      ict.setColumns(leftIndexCols,rightIndexCols);
      ict.setYLine(currentYPosition);
      ict.setAlignment(Element.ALIGN_LEFT);
      int status=ict.go();
      currentYPosition = toYPosition;
    }
    catch(DocumentException de) {
      logger.error(de.getMessage());
    }



  }

  private int addTextLine(ColumnText ct,int lineHeight,int alignment,float left, float right){
    logger.debug("adding a line of text");
    if (! enoughY(lineHeight)){
      newPage();
    }
    float toYPosition = currentYPosition - lineHeight;
    float[] leftContentCols = {left,currentYPosition,left,toYPosition};
    float[] rightContentCols = {right,currentYPosition,right,toYPosition};
    ct.setColumns(leftContentCols,rightContentCols);
    ct.setYLine(currentYPosition);
    ct.setAlignment(alignment);
    try{
      int status=ct.go();
      currentYPosition = toYPosition;
      return status;
    }
    catch(DocumentException de) {
      logger.error(de.getMessage());
    }
    return 0;
  }

  public void addLine(){
    cb.setLineWidth(1f);
    cb.moveTo(rightEdge, currentYPosition-5);
    cb.lineTo(leftEdge, currentYPosition-5);
    cb.stroke();
    currentYPosition = currentYPosition - 10;

  }

  public void addFooter(){
    try{
    ColumnText fct = new ColumnText(cb);
    cb.setLineWidth(1f);
    cb.moveTo(rightEdge, bottomEdge+footerHeight-5);
    cb.lineTo(leftEdge, bottomEdge+footerHeight-5);
    cb.stroke();
    float[] leftFooterCols = {leftEdge,bottomEdge+footerHeight-1,leftEdge,bottomEdge};
    float[] rightFooterCols = {rightEdge,bottomEdge+footerHeight-1,rightEdge,bottomEdge};

    Paragraph footerP=new Paragraph(footerText,new Font(footerFontFamily,footerFontSize));
    fct.addText(footerP);

    fct.setColumns(leftFooterCols,rightFooterCols);
    fct.setYLine(bottomEdge+footerHeight-1);
    fct.setAlignment(Element.ALIGN_JUSTIFIED);
    int status=fct.go();

    Paragraph numberP=new Paragraph((new Integer(currentPage)).toString(),new Font(footerFontFamily,footerFontSize,Font.BOLD));
    fct.addText(numberP);
    fct.setAlignment(Element.ALIGN_RIGHT);
    status=fct.go();

    }
    catch (DocumentException de){
      logger.error(de.getMessage());
    }


  }

  public void newPage(){
    try{
      //add a footer
      addFooter();
      document.newPage();
      currentPage++;
      currentYPosition=topEdge;
    }
    catch(DocumentException de) {
      logger.error(de.getMessage());
    }
  }

  public void addArticleSeparator(){
    // make a line
    if (! enoughY(10)){
      newPage();
    }
    cb.setLineWidth(1f);
    cb.moveTo(rightEdge, currentYPosition-5);
    cb.lineTo(leftEdge, currentYPosition-5);
    cb.stroke();
    currentYPosition = currentYPosition - 10;
  }

  public void addArticleMetaInfo(ColumnText ct,String theTitle,String theCreator,String theDate){
      //see if we have room for the author and title

    if (! enoughY(metaHeight)){
  newPage();
      }

    Paragraph titleP=new Paragraph(theTitle+"\n",new Font(metaFontFamily,metaFontSize,Font.BOLD));
    Paragraph whowhenP=new Paragraph(theCreator + "  " + theDate ,new Font(metaFontFamily,metaFontSize));
    ct.addText(titleP);
    ct.addText(whowhenP);

    ct.setYLine(currentYPosition);
    ct.setAlignment(Element.ALIGN_LEFT);

    float toYPosition = currentYPosition - metaHeight;
    float[] leftHeadCols = {leftEdge,currentYPosition,leftEdge,toYPosition};
    float[] rightHeadCols = {rightEdge,currentYPosition,rightEdge,toYPosition};

    ct.setColumns(leftHeadCols,rightHeadCols);
    try {
      ct.go();
      currentYPosition = toYPosition;
    }
    catch(DocumentException de) {
      logger.error(de.getMessage());
    }

  }

  public void addArticleDescription(ColumnText ct,String theDescription){
    // Now we add the description, one line at a time, the ct should be empty at this point


    Paragraph descP=new Paragraph(theDescription,new Font(descriptionFontFamily,descriptionFontSize,Font.BOLD));
    ct.addText(descP);

    // every article has a description, so we can assume that:
    int status = ColumnText.NO_MORE_COLUMN;

    int brake=1000;
    while ((status & ColumnText.NO_MORE_TEXT) == 0 && brake >0){
      //there is still text left in the description.
      status = addTextLine(ct,descriptionLineHeight,Element.ALIGN_JUSTIFIED,leftEdge,rightEdge);
      brake--;
    }
    if (brake == 0)
      logger.error("runaway description...try increasing the line height or decreasing the font size");
  }

  public void addArticleContent(ColumnText ct,String theContent,Iterator images){
    //let's go ahead and add in all the body text
    Paragraph contentP=new Paragraph(theContent,new Font(contentFontFamily,contentFontSize));
    ct.addText(contentP);

    int contentLinesBeforeImages=3;
    //and assume we have at least one line of text in the content
    int status = ColumnText.NO_MORE_COLUMN;

    // let's add a little bit of text, like a couple of lines
    int x = 0;
    while (((status & ColumnText.NO_MORE_TEXT) == 0) && x<contentLinesBeforeImages){
      status=addTextLine(ct,contentLineHeight,Element.ALIGN_JUSTIFIED,leftEdge,rightEdge);
      x++;
    }

    boolean addImageOnLeft = true; //we will alternate within articles
    while (images.hasNext()){

      EntityImages currentImage=(EntityImages) images.next();
      float img_width=(new Float(currentImage.getValue("img_width"))).floatValue();
      float img_height=(new Float(currentImage.getValue("img_height"))).floatValue();
      if (img_height>maxImageHeight){
  img_width=(new Float((new Float(img_width*(maxImageHeight/img_height))).intValue())).floatValue();
  img_height=maxImageHeight;
      }
      if (img_width>maxImageWidth){
  img_height=(new Float((new Float(img_height*(maxImageWidth/img_width))).intValue())).floatValue();
  img_width=maxImageWidth;
      }

      String img_title=currentImage.getValue("title");
      String img_path=currentImage.getValue("publish_path");

      if ((status & ColumnText.NO_MORE_TEXT) == 0){
  // there is still text, so add an image which will have text wrapped around it, then add the text which
  // will be on the left or the right of the image

  float templateMinimumHeight = img_height+20;
  float templateWidth = img_width+10;
  float templateHeight = templateMinimumHeight+contentLineHeight-(templateMinimumHeight % contentLineHeight);

  PdfTemplate template = cb.createTemplate(templateWidth,templateHeight);


  //here we need a page check
  if (! enoughY((new Float(templateHeight)).intValue())){
    //ok, well just fill text to the bottom then
    float toYPosition = bottomEdge+footerHeight;
    float[] leftBottomCols = {leftEdge,currentYPosition,leftEdge,toYPosition};
    float[] rightBottomCols = {rightEdge,currentYPosition,rightEdge,toYPosition};
    ct.setColumns(leftBottomCols,rightBottomCols);
    ct.setYLine(currentYPosition);
    ct.setAlignment(Element.ALIGN_JUSTIFIED);
    try{
      status=ct.go();
    }
    catch(DocumentException de) {
      logger.error(de.getMessage());
    }
    newPage();
  }

  float toYPosition=currentYPosition - templateHeight;

  try {
    Image theImage = Image.getInstance(localImageDir+img_path);
    theImage.scaleAbsolute(img_width,img_height);
    theImage.setAbsolutePosition(5,13);

    template.addImage(theImage);
    template.beginText();
    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    template.setFontAndSize(bf, 8);
    template.setTextMatrix(5, 3);
    template.showText(img_title);
    template.endText();
  }
  catch(BadElementException de) {
    logger.error(de.getMessage());
  }
  catch(DocumentException de) {
    logger.error(de.getMessage());
  }
  catch (MalformedURLException de){
   logger.error(de.getMessage());
  }
  catch (IOException de){
    logger.error(de.getMessage());
  }


  float leftImageTextEdge=leftEdge;
  float rightImageTextEdge=rightEdge;


  if (addImageOnLeft){
    cb.addTemplate(template,leftEdge,toYPosition);
    leftImageTextEdge=leftEdge+templateWidth+5;
    addImageOnLeft = false;
  }
  else {
    cb.addTemplate(template,rightEdge-templateWidth,toYPosition);
    rightImageTextEdge = rightEdge-templateWidth-5;
    addImageOnLeft = true;
  }

  logger.debug("adding template at " + leftEdge + "," + toYPosition);

  //and fill some text while we are at it

  float[] leftBottomCols = {leftImageTextEdge,currentYPosition,leftImageTextEdge,toYPosition};
  float[] rightBottomCols = {rightImageTextEdge,currentYPosition,rightImageTextEdge,toYPosition};
  ct.setColumns(leftBottomCols,rightBottomCols);
  ct.setYLine(currentYPosition);
  ct.setAlignment(Element.ALIGN_JUSTIFIED);
  try{
  status=ct.go();
  currentYPosition=toYPosition;
  }
  catch(DocumentException de) {
    logger.error(de.getMessage());
  }


      }
      else {
  //add an image on the left with a big caption to the right
  currentYPosition = currentYPosition - 10;
  float templateMinimumHeight = img_height;
  float templateWidth = img_width;
  float templateHeight = templateMinimumHeight+contentLineHeight-(templateMinimumHeight % contentLineHeight);
  PdfTemplate template = cb.createTemplate(templateWidth,templateHeight);
  if (! enoughY((new Float(templateHeight)).intValue())){
    newPage();
  }
  float toYPosition=currentYPosition - templateHeight;
  try{
    Image theImage = Image.getInstance(localImageDir+img_path);
    theImage.setAbsolutePosition(0,13);
    theImage.scaleAbsolute(img_width,img_height);
    template.addImage(theImage);
  }
  catch(BadElementException de) {
    logger.error(de.getMessage());
  }
  catch(DocumentException de) {
    logger.error(de.getMessage());
  }
  catch(MalformedURLException de) {
    logger.error(de.getMessage());
  }
  catch(IOException de) {
    logger.error(de.getMessage());
  }

  cb.addTemplate(template,leftEdge,toYPosition);

  // add a big caption
  ColumnText cct = new ColumnText(cb);
  float[] leftCaptionCols = {leftEdge+templateWidth+5,currentYPosition-5,leftEdge+templateWidth+5,toYPosition};
  float[] rightCaptionCols = {rightEdge,currentYPosition-5,rightEdge,toYPosition};

  Paragraph captionP=new Paragraph(img_title,new Font(bigImageCaptionFontFamily,bigImageCaptionFontSize,Font.BOLD));
  cct.addText(captionP);
  cct.setColumns(leftCaptionCols,rightCaptionCols);
  cct.setYLine(currentYPosition-5);
  cct.setAlignment(Element.ALIGN_LEFT);
  try{
    cct.go();
    currentYPosition=toYPosition;
  }
  catch(DocumentException de) {
    logger.error(de.getMessage());
  }
      }
    }

    //add the rest of the text which might be left
    int brake = 10000;
    while ((status & ColumnText.NO_MORE_TEXT) == 0  && brake > 0){
      status=addTextLine(ct,contentLineHeight,Element.ALIGN_JUSTIFIED,leftEdge,rightEdge);
      brake --;
    }
    if (brake == 0)
      logger.error("runaway content....try decreasing font size or increasing line height");
  }

  private void addArticleSource(ColumnText ct,String theSource){
    Paragraph sourceP = new Paragraph(theSource,new Font(sourceFontFamily,sourceFontSize,Font.BOLD));
    ct.addText(sourceP);
    addTextLine(ct,sourceLineHeight,Element.ALIGN_RIGHT,leftEdge,rightEdge);
  }


  private boolean enoughY(int heightOfBlockToAdd){
    if ((currentYPosition - heightOfBlockToAdd - footerHeight) < bottomEdge )
      return false;
    else
      return true;
  }


  public void add(EntityContent entityContent){
    logger.error("adding a content Entity");

    /*
     * initialize
     * separate
     * meta info
     * description
     * content
     * --image with text
     * --normal text
     * --image without text
     * source
    */

   Iterator images = new EntityBrowser(
      DatabaseImages.getInstance(),
       "exists (select * from content_x_media where content_id=" + entityContent.getId() + " and media_id=id)",
       "id desc", 30, -1, 0);

    String isHTML  = entityContent.getValue("is_html");
    String theTitle = entityContent.getValue("title");
    String theCreator = entityContent.getValue("creator");
    String theDate = DateTimeFunctions.advancedDateFormat(
        configuration.getString("RDF.Meta.DateFormat"),
        StringUtil.convertMirInternalDateToDate(entityContent.getValue("webdb_create")),
        configuration.getString("Mir.DefaultTimezone"));


    String theDescriptionRaw = entityContent.getValue("description");
    String theContentRaw = entityContent.getValue("content_data");
    String theSource =  configuration.getString("Producer.ProductionHost") + "/" + configuration.getString("StandardLanguage") + entityContent.getValue("publish_path") + entityContent.getValue("id") + ".shtml";



    String theContent = "";
    String theDescription = "";

    if (isHTML.equals("1")){



      try {
  RE nobackslashr = new RE("\r");
  theContent= nobackslashr.substituteAll(theContentRaw,"");

  RE HxTag = new RE("</?h[1-6][^>]*>",RE.REG_ICASE);
  theContent = HxTag.substituteAll(theContent,"\n\n");

  RE ListItemTag = new RE("<li[^>]*>",RE.REG_ICASE);
  theContent = ListItemTag.substituteAll(theContent,"\n * ");

  RE ListTag = new RE("<(u|o)l[^>]*>",RE.REG_ICASE);
  theContent = ListTag.substituteAll(theContent,"\n");

  RE DivTag = new RE("</?div[^>]*>",RE.REG_ICASE);
  theContent= DivTag.substituteAll(theContent,"\n");

  RE PTag = new RE("<(p|P)([:space:]+[^>]*)?>");
  theContent= PTag.substituteAll(theContent,"\n    ");

  RE PTagClose = new RE("</(p|P)([:space:]+[^>]*)?>");
  theContent= PTagClose.substituteAll(theContent,"\n");

  RE BRTag = new RE("<(br|BR)([:space:]+[^>]*)?>");
  theContent= BRTag.substituteAll(theContent,"\n");

  RE ATagAll = new RE("<a[^>]*href=(?:\"|\')([^#\"\'][^\'\"]+)(?:\"|\')[^>]*>(.*?)</a>",RE.REG_ICASE);
  REMatchEnumeration atags= ATagAll.getMatchEnumeration(theContent);
  String theContentCopy=theContent;
  while (atags.hasMoreMatches()){
    REMatch atag = atags.nextMatch();
    String atagString=atag.toString();
    String atagStringHref=atag.toString(1);
    String atagStringText=atag.toString(2);
    int begin=theContentCopy.indexOf(atagString);
    theContentCopy=theContentCopy.substring(0,begin) + atagStringText + " ["+ atagStringHref + "] " + theContentCopy.substring(begin+atagString.length());
  }
  theContent=theContentCopy;

  RE noTags = new RE("<[^>]*>");
  theContent= noTags.substituteAll(theContent," ");

  theContent=mir.util.Translate.decode(theContent);

  RE re1 = new RE("\r?\n\r?\n");
  String theDescription1 = re1.substituteAll(theDescriptionRaw,"BREAKHERE");

  RE re2 = new RE("\r?\n");
  String theDescription2 = re2.substituteAll(theDescription1," ");

  RE re3 = new RE("BREAKHERE");
  theDescription = re3.substituteAll(theDescription2,"\n    ");


      }
      catch(REException ree){
  logger.error(ree.getMessage());
      }
    }
    else {
      try {
  RE re1 = new RE("\r?\n\r?\n");
  String theContent1 = re1.substituteAll(theContentRaw,"BREAKHERE");
  String theDescription1 = re1.substituteAll(theDescriptionRaw,"BREAKHERE");

  RE re2 = new RE("\r?\n");
  String theContent2 = re2.substituteAll(theContent1," ");
  String theDescription2 = re2.substituteAll(theDescription1," ");

  RE re3 = new RE("BREAKHERE");
  theContent = "    " + re3.substituteAll(theContent2,"\n    ");
  theDescription = re3.substituteAll(theDescription2,"\n    ");

      }
      catch(REException ree){
  logger.error(ree.getMessage());
      }
    }

    addArticleSeparator();

    ColumnText ct = new ColumnText(cb);

    addArticleMetaInfo(ct,theTitle,theCreator,theDate);
    addArticleDescription(ct,theDescription);
    addArticleContent(ct,theContent,images);
    addArticleSource(ct,theSource);

  }

  public int getFontByName(String fontName) {
    int theFont = 0;
    if (fontName.equalsIgnoreCase("helvetica")){
      theFont = Font.HELVETICA;
    }
    else {
      if (fontName.equalsIgnoreCase("courier")) {
  theFont = Font.COURIER;
      }
      else {
  if (fontName.equalsIgnoreCase("times")) {
    theFont = Font.TIMES_ROMAN;
  }
  else {
    logger.error("using helvetica because I can't get font for name: "+fontName);
    theFont = Font.HELVETICA;
  }
      }
    }

    return theFont;

  }
}



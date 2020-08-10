package com.adcb.ocr.pdf;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;


public class SaveImagesFromPdf extends PDFStreamEngine
{

//    public int imageNumber = 1;

    private String imageName;
    private String imagePath;
    private boolean imageSaved = false;

    /**
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        SaveImagesFromPdf printer = new SaveImagesFromPdf();
        String path = "/home/yamraaj/Downloads/";
        String fname = "Papapassport.pdf";
        printer.extractImage(path, fname);
    }

    public void extractImage(String pdfPath, String fileName) throws IOException{
        PDDocument document = null;
        String imageNameWithoutExt = fileName.split("\\.")[0];
        imageName = imageNameWithoutExt+".png";
        imagePath = pdfPath;
        try{
            document = PDDocument.load( new File(pdfPath+File.separator+fileName) );
            for( PDPage page : document.getPages() ) {
                processPage(page);
                break;
            }
        }
        finally {
            if( document != null ){
                document.close();
            }
        }
    }


    /**
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     *
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator( Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if( "Do".equals(operation) ){
            COSName objectName = (COSName) operands.get( 0 );
            PDXObject xobject = getResources().getXObject( objectName );
            if( xobject instanceof PDImageXObject){
                PDImageXObject image = (PDImageXObject)xobject;
//                int imageWidth = image.getWidth();
//                int imageHeight = image.getHeight();

                // same image to local
                BufferedImage bImage = image.getImage();
                ImageIO.write(bImage,"PNG",new File(imagePath+File.separator+imageName));
                imageSaved = true;
                System.out.println("Image saved.");
            } else if(xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject)xobject;
                showForm(form);
            }
        } else {
            super.processOperator( operator, operands);
        }
    }

}
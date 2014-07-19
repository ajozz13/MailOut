// $Id: FileUtility.java,v 1.14 2012-06-20 21:03:44 aochoa Exp $

package com.ibcinc.development.utilities.file;

import java.util.*;
import java.io.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Title:        Utility
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      International Bonded Couriers
 * @author Alberto J. Ochoa
 * @version 1.0
 */

public class FileUtility {

            private static File theFile;
            private Vector<Object> fileData;
            public static String NEWLINE;
            private ArrayList<String> fileContents;
		
            public FileUtility(File _theFile){
                    theFile = _theFile;
                    NEWLINE = System.getProperty("line.separator");
            }
		
            protected void getDataFromFile(){
                    StringTokenizer RI = new StringTokenizer(getContentsOfFile(),NEWLINE);
                    while(RI.hasMoreTokens()){
                            String temp = RI.nextToken();
                            fileData.addElement(temp.trim());
                    }
            }
            public ArrayList<String> getDataContentsFromFile(){
                String[] arr = getContentsOfFile().split(NEWLINE);
                fileContents = new ArrayList<String>(Arrays.asList( arr ));
                return fileContents;
            }
	
            public void setNewLineChar(String new_line_char){
                NEWLINE = new_line_char;
            }
            
            public String getContentsOfFile(){
                    String t = new String();
                    int fileLength=(int)getTheFile().length();
                    char buffer[] = new char[fileLength];
                    InputStreamReader input;
                    try{
                            int index = 0;
                            input = new FileReader(getTheFile());
                            while((index=input.read(buffer,0,buffer.length)) != -1){
                                    t=t+new String(buffer,0,index);
                            }
                    }catch(IOException e){
                            e.toString();
                    }
                    return t;
            }
		
            public Vector<Object> getFileData(){
                    if(fileData == null){
                            fileData = new Vector<Object>();
                            getDataFromFile();
                    }
                    return fileData;
            }

            public static File getTheFile(){
                    return theFile;
            }

            public static String getExtension(File f){
                    String ext = null;
                    String s = f.getName();
                    int i = s.lastIndexOf('.');
                if (i > 0 &&  i < s.length() - 1) {
                            ext = s.substring(i+1).toLowerCase();
                    }
                    return ext;
            }
		
            public static String verifyFileName(String fileName){
                    String windowsSep = "\\";
                    String uxSep = "/";
                    String name = fileName;
                    if(fileName.indexOf(windowsSep) != -1 || fileName.indexOf(uxSep) != -1){
                            if(fileName.indexOf(windowsSep) != -1){
                                    name = removeToLast(fileName,windowsSep);
                            }else{
                                    name = removeToLast(fileName,uxSep);	
                            }
                    }
                    return name;

            }

            private static String removeToLast(String wholeString, String sindex){
                    int index = wholeString.lastIndexOf(sindex); 
                    return wholeString.substring(index+1);	
            }
		
            //THIS method assumes you submit an XML formatted file.
            //IE is is not necessary to name a file .xml 		
            public static Element loadXMLFile(File myFile) throws Exception{
                    SAXBuilder builder = new SAXBuilder();
                    Document doc = builder.build(myFile);
                    return doc.getRootElement();
            }

            public static Element loadXMLFile() throws Exception{
                    return loadXMLFile(getTheFile());	
            }

            //Writing capabilities
            //Use this method to write partial data to a file
            //or dev.utilities.jdk.swing.FileSaver to save a whole file at once.
            public static void writeLineToFile(String data) throws Exception{
                FileWriter writer = new FileWriter(theFile, true);
                writer.write(data + NEWLINE);
                writer.close();
            }
            
            public static ArrayList<String> readFileFromSTDIN(ArrayList<String> contentHolder) throws Exception{
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String s;
                while((s = in.readLine()) != null && s.length() != 0){
                    contentHolder.add(s);
                }
                if (contentHolder.size() <= 0)
                    throw new Exception("STDIN File is Empty");
                return contentHolder;
            }


}
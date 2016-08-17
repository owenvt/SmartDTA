/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartdtajava;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Norm Marshall, Smart Mobility Inc., September 2015, updated for Little Rock region, April 2016
 */
public class Write_Load_to_TransCAD {
    
    //template constancts
    static int TEMPLATE_LINK_ID = 1;
    static int TEMPLATE_FROM_NODE = 2;
    static int TEMPLATE_TO_NODE = 3;
    static int TEMPLATE_LENGTH = 6;
    static int TEMPLATE_SPEED_LIMIT = 8;
    
    //outputLinkDTMOE constants
    static int OUTPUT_FROM = 0;
    static int OUTPUT_TO = 1;
    static int OUTPUT_TIMESTAMP = 4;
    static int OUTPUT_TRAVEL_TIME = 5;
    static int OUTPUT_VOLUME = 8;

    public static void main(String[] args) {

        //String[] periods = {"","AM", "MD", "PM", "NT"};
        //String basePath = "C:/CCMPODTA/DTA_CSVs/"; 
        String basePath = args[0];
        Double calc;
        String inFile, outFile;
        boolean eof;
        Integer i, n, p, linkDexID, linkDexFrom, linkDexTo, linkDexLength, linkDexSpeed;
        linkDexID = 0;
        linkDexFrom = 0;
        linkDexTo = 0;
        linkDexLength = 0;
        linkDexSpeed = 0;
        String line;
        Integer start_time = 60;
        Integer end_time = 120;
        Double mult = 1.0;
        //double[] phf = {0.0, 1.0, 6.0, 3.5, 12.0};
        String[] linkFields = new String[314];
        String[] inFields = new String[314];
        
        //read args
        String raw_time_periods = args[1];
        String raw_phfs = args[2];
        //get rid of single quotes and brackets, and parse into lists
        String[] periods = raw_time_periods.replace("'", "").replace("[", "").replace("]", "").replace(" ", "").split(",");
        String[] phf = raw_phfs.replace("'", "").replace("[", "").replace("]", "").replace(" ", "").split(",");
                
        //String inTCLinksFile = pathIn + "links.csv";

        //  time periods
        try {
            
            //read TClinks file and count lines
            String pathOut = basePath;
            String outLinksFile = pathOut + "load.csv";
            String pathIn = basePath + periods[0] + "/";
            String inTCLinksFile = pathIn + "links.csv";
            String inDTALinksFile = pathIn + "input_link.csv";
            int lineCount = 1;
            inFile = inDTALinksFile;
            FileReader inLinks = new FileReader(inFile);
            BufferedReader inBuff = new BufferedReader(inLinks);
            eof = false;
            while (!eof) {
                line = inBuff.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    lineCount++;
                }
            }
            inLinks.close();
            inBuff.close();
            
            //use the line count to create arrays
            Integer[] DTA_ID = new Integer[lineCount];
            Integer[] DTA_from = new Integer[lineCount];
            Integer[] DTA_to = new Integer[lineCount];
            Integer[] DTA_dir = new Integer[lineCount];
            Integer[] TC_from = new Integer[lineCount];
            Integer[] TC_to = new Integer[lineCount];
            
            Double[][] TC_length = new Double[lineCount][periods.length];
            Double[][] TC_speed = new Double[lineCount][periods.length];
            Double[][] AB_Vol = new Double[lineCount][periods.length];
            Double[][] BA_Vol = new Double[lineCount][periods.length];
            Double[][] Tot_Vol = new Double[lineCount][periods.length];
            Double[][] AB_Time = new Double[lineCount][periods.length];
            Double[][] BA_Time = new Double[lineCount][periods.length];
            Double[][] AB_VHT = new Double[lineCount][periods.length];
            Double[][] BA_VHT = new Double[lineCount][periods.length];
            Double[][] Tot_VHT = new Double[lineCount][periods.length];
            Double[][] AB_VMT = new Double[lineCount][periods.length];
            Double[][] BA_VMT = new Double[lineCount][periods.length];
            Double[][] Tot_VMT = new Double[lineCount][periods.length];
            
            Double[] DY_AB_Vol = new Double[lineCount];
            Double[] DY_BA_Vol = new Double[lineCount];
            Double[] DY_Tot_Vol = new Double[lineCount];
            Double[] DY_AB_VHT = new Double[lineCount];
            Double[] DY_BA_VHT = new Double[lineCount];
            Double[] DY_Tot_VHT = new Double[lineCount];
            Double[] DY_AB_VMT = new Double[lineCount];
            Double[] DY_BA_VMT = new Double[lineCount];
            Double[] DY_Tot_VMT = new Double[lineCount];
            
            //create the lineIndex
            inFile = inTCLinksFile;
            int[] lineIndex = new int[lineCount];
            inLinks = new FileReader(inFile);
            inBuff = new BufferedReader(inLinks);
            inBuff.readLine();   //remove header
            eof = false;
            i=1;
            while (!eof) {
                line = inBuff.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    inFields = line.split(",");
                    lineIndex[i] = Integer.parseInt(inFields[0]);
                    i++;
                }
            }
            inLinks.close();
            inBuff.close();

            for (p = 0; p < periods.length; p++) { // periods
                
                //System.out.println(periods[p]);
                //System.out.println(phf[p]);
                pathIn = basePath + periods[p] + "/";
                inTCLinksFile = pathIn + "links.csv";
                String inLinksFile = pathIn + "output_LinkTDMOE.csv";
                String templateLinksFile = pathIn + "CCMPO_link_template.csv";
                

                for (i = 1; i < lineCount; i++) {
                    AB_Vol[i][p] = 0.0;
                    BA_Vol[i][p] = 0.0;
                    AB_Time[i][p] = 0.0;
                    BA_Time[i][p] = 0.0;
                    AB_VHT[i][p] = 0.0;
                    BA_VHT[i][p] = 0.0;
                    AB_VMT[i][p] = 0.0;
                    BA_VMT[i][p] = 0.0;
                    TC_length[i][p] = 0.0;
                    TC_speed[i][p] = 0.0;
                    Tot_Vol[i][p] = 0.0;
                    Tot_VHT[i][p] = 0.0;
                    Tot_VMT[i][p] = 0.0;
                }

                System.out.println("DTA links structure");
                inFile = inDTALinksFile;
                FileReader inDTALinks = new FileReader(inFile);
                BufferedReader inBuffDTA = new BufferedReader(inDTALinks);
                // skip header link in input file
                line = inBuffDTA.readLine();
                // read data lines
                n = 1;
                eof = false;
                while (!eof) {
                    line = inBuffDTA.readLine();
                    if (line == null) {
                        eof = true;
                    } else {
                        inFields = line.split(",");
                        DTA_ID[n] = Integer.parseInt(inFields[1]);
                        DTA_from[n] = Integer.parseInt(inFields[2]);
                        DTA_to[n] = Integer.parseInt(inFields[3]);
                        DTA_dir[n] = Integer.parseInt(inFields[5]);
                        n += 1;
                    }
                }
                inDTALinks.close();
                inBuffDTA.close();

                System.out.println("TransCAD links structure");
                
                FileReader inLinkTemplate = new FileReader(templateLinksFile);
                BufferedReader inTemplate = new BufferedReader(inLinkTemplate);
                inTemplate.readLine();  //get rid of header
                line = inTemplate.readLine();
                inFields = line.split(",");
                String link_id_field_name = inFields[TEMPLATE_LINK_ID];
                String from_node_field_name = inFields[TEMPLATE_FROM_NODE];
                String to_node_field_name = inFields[TEMPLATE_TO_NODE];
                String length_field_name = inFields[TEMPLATE_LENGTH];
                String speed_field_name = inFields[TEMPLATE_SPEED_LIMIT];
            
                inFile = inTCLinksFile;
                FileReader inTCLinks = new FileReader(inFile);
                BufferedReader inBuffTC = new BufferedReader(inTCLinks);
                line = inBuffTC.readLine();
                line = line.replace("\"", "");
                inFields = line.split(",");
                for (i = 0; i < (inFields.length); i++) {
                    if (inFields[i].equals(from_node_field_name)) {
                        linkDexFrom = i;
                    }
                    if (inFields[i].equals(to_node_field_name)) {
                        linkDexTo = i;
                    }
                     if (inFields[i].equals(length_field_name)) {
                        linkDexLength = i;
                    }
                     if (inFields[i].equals(speed_field_name)) {
                        linkDexSpeed = i;
                    }                 }
                // read TransCAD links
                eof = false;
                int ln = 0;
                
                while (!eof) {
                    line = inBuffTC.readLine();
                    if (line == null) {
                        eof = true;
                    } else {
                        ln++;
                        inFields = line.split(",");
                        //n = Integer.parseInt(inFields[linkDexID]); // index by TransCAD link ID *MODIFIED*
                        //lineIndex[ln] = n;
                        TC_from[ln] = Integer.parseInt(inFields[linkDexFrom]);
                        TC_to[ln] = Integer.parseInt(inFields[linkDexTo]);
                        TC_length[ln][p] = Double.parseDouble(inFields[linkDexLength]);
                        if (inFields[linkDexSpeed].equals("")) {
                            TC_speed[ln][p] = 0.0;
                        } else {
                            TC_speed[ln][p] = Double.parseDouble(inFields[linkDexSpeed]);
                        }
                    }
                }
                inTCLinks.close();
                inBuffTC.close();

                // read DTA loaded links
                System.out.println("read DTA outputs");
                inFile = inLinksFile;
                inLinks = new FileReader(inFile);
                inBuff = new BufferedReader(inLinks);
                // skip header link in input file
                line = inBuff.readLine();
                // read data lines
                n = 1;
                eof = false;
                while (!eof) {
                    line = inBuff.readLine();
                    if (line == null) {
                        eof = true;
                    } else {
                        inFields = line.split(",");
                        // check beginning and end times
                        if (Integer.parseInt(inFields[OUTPUT_TIMESTAMP]) >= start_time) {
                            if (Integer.parseInt(inFields[OUTPUT_TIMESTAMP]) < end_time) {
                                if ((Integer.parseInt(inFields[OUTPUT_FROM]) != DTA_from[n]) || (Integer.parseInt(inFields[OUTPUT_TO])) != DTA_to[n]) {
                                    if ((Integer.parseInt(inFields[OUTPUT_FROM]) != DTA_to[n]) || (Integer.parseInt(inFields[OUTPUT_TO])) != DTA_from[n]) {
                                        // outString = String.valueOf(n);
                                        // System.out.println(outString);
                                        n += 1; // if this link isn't the right one, the next one should be
                                    }
                                }
                                if (Integer.parseInt(inFields[0]) == TC_from[DTA_ID[n]]) { // AB direction
                                    //outString = "AB " + String.valueOf(n) + "," + String.valueOf(DTA_ID[n]) + "," + inFields[0] + "," + inFields[1] + inFields[7] + "," + inFields[5];
                                    //System.out.println(outString);
                                    AB_Vol[DTA_ID[n]][p] += Double.valueOf(inFields[OUTPUT_VOLUME]); // volumes are exiting volumes which may be different from entering volumes
                                    AB_Time[DTA_ID[n]][p] += (Double.valueOf(inFields[OUTPUT_VOLUME]) * Double.valueOf(inFields[OUTPUT_TRAVEL_TIME]) / 60); // DTA_ID[n] is TransCAD ID
                                }
                                if (Integer.parseInt(inFields[0]) == TC_to[DTA_ID[n]]) { // BA direction
                                    //outString = "BA " + String.valueOf(n) + "," + String.valueOf(DTA_ID[n]) + "," + inFields[0] + "," + inFields[1] + inFields[7] + "," + inFields[5];
                                    //System.out.println(outString);
                                    BA_Vol[DTA_ID[n]][p] += Double.valueOf(inFields[OUTPUT_VOLUME]);
                                    BA_Time[DTA_ID[n]][p] += (Double.valueOf(inFields[OUTPUT_VOLUME]) * Double.valueOf(inFields[OUTPUT_TRAVEL_TIME]) / 60);
                                }
                            }
                        }
                    }
                }

                inLinks.close();
                inBuff.close();

            } // time periods
            
            //write output file
            System.out.println("write outputs");
            outFile = outLinksFile;
            FileWriter outLoad = new FileWriter(outFile);
            // write header
            String outString;
            outString = "Link_ID,";
            for(p=0; p<periods.length; p++) {
                outString += periods[p]+"_AB_Vol,"+periods[p]+"_BA_Vol,"+periods[p]+"_Tot_Vol,"+periods[p]+"_AB_VMT,"+periods[p]+"_BA_VMT,"+periods[p]+"_Tot_VMT,";
                outString += periods[p]+"_AB_VHT,"+periods[p]+"_BA_VHT,"+periods[p]+"_Tot_VHT,"+periods[p]+"_AB_DTA_Time,"+periods[p]+"_BA_DTA_Time,"+periods[p]+"_AB_Speed,"+periods[p]+"_BA_Speed,";
            }
            outString += "DY_AB_Vol,DY_BA_Vol,DY_Tot_Vol,DY_AB_VMT,DY_BA_VMT,DY_Tot_VMT,DY_AB_VHT,DY_BA_VHT,DY_Tot_VHT\r\n";
            outLoad.write(outString); //write data
            for (int ln = 1; ln < lineCount; ln++) {
                for (i = 1; i < lineCount; i++) {
                    DY_AB_Vol[i] = 0.0;
                    DY_BA_Vol[i] = 0.0;
                    DY_AB_VHT[i] = 0.0;
                    DY_BA_VHT[i] = 0.0;
                    DY_AB_VMT[i] = 0.0;
                    DY_BA_VMT[i] = 0.0;
                    DY_Tot_Vol[i] = 0.0;
                    DY_Tot_VHT[i] = 0.0;
                    DY_Tot_VMT[i] = 0.0;
                }
                outString = String.valueOf(lineIndex[ln]);  //tc index
                outString += ",";
                for(p=0; p<periods.length; p++) {                  
                    calc = mult * Double.valueOf(phf[p]) * AB_Vol[ln][p]; // adjusting from 60 minutes to entire time period
                    outString += String.valueOf(calc);
                    outString += ",";
                    calc = mult * Double.valueOf(phf[p]) * BA_Vol[ln][p]; // adjusting from 60 minutes to entire time period
                    outString += String.valueOf(calc);
                    outString += ",";
                    calc = mult * Double.valueOf(phf[p]) * (AB_Vol[ln][p] + BA_Vol[ln][p]); // adjusting from 60 minutes to entire time period
                    outString += String.valueOf(calc);
                    outString += ",";
                    calc = mult * Double.valueOf(phf[p]) * AB_Vol[ln][p] * TC_length[ln][p]; // VMT
                    outString += String.valueOf(calc);
                    outString += ",";
                    calc = mult * Double.valueOf(phf[p]) * BA_Vol[ln][p] * TC_length[ln][p];
                    outString += String.valueOf(calc);
                    outString += ",";
                    calc = mult * Double.valueOf(phf[p]) * (AB_Vol[ln][p] + BA_Vol[ln][p]) * TC_length[ln][p];
                    outString += String.valueOf(calc);
                    outString += ",";
                    calc = mult * Double.valueOf(phf[p]) * AB_Time[ln][p]; // VHT
                    outString += String.valueOf(calc);
                    outString += ",";
                    calc = mult * Double.valueOf(phf[p]) * BA_Time[ln][p];
                    outString += String.valueOf(calc);
                    outString += ",";
                    calc = mult * Double.valueOf(phf[p]) * (AB_Time[ln][p] + BA_Time[ln][p]);
                    outString += String.valueOf(calc);
                    outString += ",";
                    
                    if (AB_Vol[ln][p] > 0) {
                        calc = ((AB_Time[ln][p]/AB_Vol[ln][p])*60); // Time
                    } else {
                        calc = TC_length[ln][p] * 60 / TC_speed[ln][p] ;
                    }
                    outString += String.valueOf(calc);
                    outString += ",";
                    if (BA_Vol[ln][p] > 0) {
                        calc = ((BA_Time[ln][p]/BA_Vol[ln][p])*60);
                    } else {
                        calc = TC_length[ln][p] * 60 / TC_speed[ln][p];
                    }
                    outString += String.valueOf(calc);
                    outString += ",";
                    if (AB_Time[ln][p] > 0) {
                        calc = ((AB_Vol[ln][p]/AB_Time[ln][p])*TC_length[ln][p]); // Speed
                    } else {
                        calc = TC_speed[ln][p];
                    }
                    outString += String.valueOf(calc);
                    outString += ",";
                    if (BA_Time[ln][p] > 0) {
                        calc = ((BA_Vol[ln][p]/BA_Time[ln][p])*TC_length[ln][p]);
                    } else {
                        calc = TC_speed[ln][p];
                    }
                    outString += String.valueOf(calc);
                    outString += ",";
                    
                    DY_AB_Vol[ln] += mult * Double.valueOf(phf[p]) * AB_Vol[ln][p]; // adjusting from 60 minutes to entire time period
                    DY_BA_Vol[ln] += mult * Double.valueOf(phf[p]) * BA_Vol[ln][p];
                    DY_Tot_Vol[ln] += DY_AB_Vol[ln] + DY_BA_Vol[ln];
                    DY_AB_VMT[ln] += mult * Double.valueOf(phf[p]) * AB_Vol[ln][p] * TC_length[ln][p]; // VMT
                    DY_BA_VMT[ln] += mult * Double.valueOf(phf[p]) * BA_Vol[ln][p] * TC_length[ln][p];
                    DY_Tot_VMT[ln] += DY_AB_VMT[ln] + DY_BA_VMT[ln];
                    DY_AB_VHT[ln] += mult * Double.valueOf(phf[p]) * AB_Time[ln][p]; // VHT
                    DY_BA_VHT[ln] += mult * Double.valueOf(phf[p]) * BA_Time[ln][p];
                    DY_Tot_VHT[ln] += DY_AB_VHT[ln] + DY_Tot_VHT[ln];
                }
                //total volumes
                outString += String.valueOf(DY_AB_Vol[ln])+","+String.valueOf(DY_BA_Vol[ln])+","+String.valueOf(DY_Tot_Vol[ln])+",";
                outString += String.valueOf(DY_AB_VMT[ln])+","+String.valueOf(DY_BA_VMT[ln])+","+String.valueOf(DY_Tot_VMT[ln])+",";
                outString += String.valueOf(DY_AB_VHT[ln])+","+String.valueOf(DY_BA_VHT[ln])+","+String.valueOf(DY_Tot_VHT[ln])+"\r\n";
                
                //write line to file
                outLoad.write(outString);
            }
            outLoad.close();

        } catch (IOException e) {
            System.out.println("Error -- " + e.toString());
        }
    }
}

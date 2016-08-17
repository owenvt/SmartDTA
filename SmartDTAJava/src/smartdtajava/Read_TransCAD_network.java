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
import java.util.HashMap;

/**
 *
 * @author Norm Marshall, Smart Mobility Inc., September 2015, updated for Little Rock region, April 2016
 */
public class Read_TransCAD_network {
    
    //linkDex template constants for transparency
    static int LINK_ID = 1;
    static int FROM_NODE = 2;
    static int TO_NODE = 3;
    static int LINK_TYPE_NAME = 4;
    static int DIRECTION = 5;
    static int LENGTH = 6;
    static int NUMBER_OF_LANES = 7;
    static int SPEED_LIMIT = 8;
    static int LANE_CAPACITY = 10;
    static int LINK_TYPE = 11;
    
    static int NODE_ID = 1;
    static int X = 5;
    static int Y = 6;
    
    static int TC_line_num = 0;
    
    
    private static void readLinks(String[] inFields, int[] linkDex, int[] dCaps, int[] isCC, FileWriter outLink, boolean BA) {
        
        if (!inFields[linkDex[LINK_TYPE]].equals("")) {
            
            int iSpeed, iCap, num;
            double dSpeed, dCap;
            
            int iType = Integer.parseInt(inFields[linkDex[LINK_TYPE]]);
            int iLanes = Integer.parseInt(inFields[linkDex[NUMBER_OF_LANES]]);
            int dLanes = Integer.parseInt(inFields[linkDex[NUMBER_OF_LANES]]);
                            
            if (isCC[Integer.parseInt(inFields[linkDex[LINK_TYPE]])] == 1) {
                iLanes = 9; // centroid connectors
            }
            String outString = ",";
            //outString += inFields[linkDex[LINK_ID]]; // Link ID
            outString += String.valueOf(TC_line_num); // TransCAD line number
            outString += ",";
            if (BA == false) {          //AB
                outString += inFields[linkDex[FROM_NODE]]; // From ID
                outString += ",";
                outString += inFields[linkDex[TO_NODE]]; // To ID
            } else if (BA == true) {    //BA
                outString += inFields[linkDex[TO_NODE]]; // To ID (flipped)
                outString += ",";
                outString += inFields[linkDex[FROM_NODE]]; // From ID (flipped)
            }
            outString += ",,";
            outString += "1"; // Dir - all linkes converted to Dir=1
            outString += ",";
            outString += inFields[linkDex[LENGTH]]; // Length
            outString += ",";
            outString += String.valueOf(iLanes); // Lanes
            // AB Lanes
            outString += ",";
            if (inFields[linkDex[8]].equals("")) {
                dSpeed = 0.0;
            } else {
                dSpeed = Double.valueOf(inFields[linkDex[SPEED_LIMIT]]);
                iSpeed = (int) Math.round(dSpeed);
                outString += String.valueOf(iSpeed); // Speed
                outString += ",,"; // saturation flow rate

                try {
                    dCap = dCaps[iType];
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    dCap = 0;
                }

                iCap = (int) Math.round(dCap);
                outString += String.valueOf(iCap); // Speed
                outString += ",";
                outString += inFields[linkDex[LINK_TYPE]]; // Link type
                outString += ",,,,,,,,\r\n"; // end of line
                if (!inFields[linkDex[NUMBER_OF_LANES]].equals("")) {
                    num = Integer.parseInt(inFields[linkDex[NUMBER_OF_LANES]]);
                    if (num > 0) { // there are lanes
                        try {
                            outLink.write(outString);
                        }
                        catch (IOException e) {
                            System.out.println("Error writting to out_links file");
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Error: missing link_type in template");
        }
    }

    public static void main(String[] args) {
        
        int[] dCaps = new int[20];
        int[] isCC = new int[20];
        
        String basePath = args[0];
        String pathIn = basePath;
        
        String dCapClassFile = basePath + "input_link_type.csv";
        
        String inNodesFile = pathIn + "nodes.csv";
        String[] nodeFields = new String[8];
        int[] nodeDex = new int[8];
        
        String inLinksFile = pathIn + "links.csv";
        String[] linkFields = new String[20];
        int[] linkDex = new int[20];
        String[] linkBAFields = new String[20];
        int[] linkBADex = new int[20];

        String inFile, outFile;
        String outString, outString2;
        boolean eof;
        String[] inFields = new String[400];
        int p, i, j, iSpeed, iType, iCap, num, iLanes;
        double lon, lat, dSpeed, dTime, dCap, dLanes;
        double maxCap = 0.0; // can use to vary capacity assumptions for DTA
        String line, pathOut, templateNodesFile, outNodesFile, templateLinksFile, outLinksFile;

        Double VOT = 12.0 / 60.0; // $/hour / 60 minute/hour = $/minute

        //  read and write nodes
        pathOut = pathIn;
        templateNodesFile = pathOut + "CCMPO_node_template.csv";
        outNodesFile = pathOut + "input_node.csv";
        templateLinksFile = pathOut + "CCMPO_link_template.csv";  //TODO: generate this
        outLinksFile = pathOut + "input_link.csv";

        try {
            System.out.println("Nodes");
            FileReader inNodeTemplate = new FileReader(templateNodesFile);
            BufferedReader inTemplate = new BufferedReader(inNodeTemplate);
            FileReader inNode = new FileReader(inNodesFile);
            BufferedReader inBuff = new BufferedReader(inNode);
            FileWriter outNode = new FileWriter(outNodesFile);

            // read and write header
            line = inTemplate.readLine();
            outString = line + "\r\n";
            outNode.write(outString);

            // use template as index for TransCAD file
            line = inTemplate.readLine();
            inFields = line.split(",");     //inFields is used for a bunch of different things
            for (i = 0; i < inFields.length; i++) {
                nodeFields[i] = inFields[i];
            }
            line = inBuff.readLine();
            line = line.replace("\"", "");
            inFields = line.split(",");
            for (i = 0; i < (nodeFields.length - 1); i++) {
                if (!nodeFields[i].equals("")) {
                    for (j = 0; j < inFields.length; j++) {
                        if (nodeFields[i].equals(inFields[j])) {
                            nodeDex[i] = j;
                        }
                    }
                }
            }
            // read TransCAD nodes
            eof = false;
            while (!eof) {
                line = inBuff.readLine();   //nodes.csv
                if (line == null) {
                    eof = true;
                } else {
                    line = line.trim(); //to remove leading spaces
                    inFields = line.split(",");
                    // write node lines
                    outString = ","; // name
                    outString += inFields[nodeDex[NODE_ID]];
                    outString += ",,"; //delimiter, QEM, delimiter
                    outString += "1,unknown,"; //control_type, delimiter, control_type_name, delimiter        
                    lon = Double.parseDouble(inFields[nodeDex[X]]) / 1000000;
                    outString += Double.toString(lon); // lon
                    outString += ","; // delimiter
                    lat = Double.parseDouble(inFields[nodeDex[Y]]) / 1000000;
                    outString += Double.toString(lat); // lat
                    outString += ",\r\n"; // end of line
                    // skip unused centroids
                    i = Integer.parseInt(inFields[nodeDex[NODE_ID]]);
                    outNode.write(outString);
                }
            }
            inNodeTemplate.close();
            inTemplate.close();
            inNode.close();
            inBuff.close();
            outNode.close();
        } catch (IOException e) {
            System.out.println("Error -- " + e.toString());
        }

        try {
            System.out.println("Links");
            FileReader inLinkTemplate = new FileReader(templateLinksFile);
            BufferedReader inTemplate = new BufferedReader(inLinkTemplate);
            FileReader inLink = new FileReader(inLinksFile);
            BufferedReader inBuff = new BufferedReader(inLink);
            FileWriter outLink = new FileWriter(outLinksFile);
            
            FileReader capacityClassTemplate = new FileReader(dCapClassFile);
            BufferedReader capacityClass = new BufferedReader(capacityClassTemplate);
            //ditch header
            capacityClass.readLine();
            //fill dcap array
            eof = false;
            while(!eof) {
                line = capacityClass.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    inFields = line.split(",");
                    //dcaps[ID] = type
                    dCaps[Integer.parseInt(inFields[0])] = Integer.parseInt(inFields[3]);   //set each index to the correct capacity
                    if(inFields[2].equals("c")) {       //centroid connector
                        isCC[Integer.parseInt(inFields[0])] = 1;
                    } else {
                        isCC[Integer.parseInt(inFields[0])] = 0;
                    }
                }
            }
            capacityClassTemplate.close();
            capacityClass.close();

            // read and write headers
            line = inTemplate.readLine();
            outString = line + "\r\n";
            outLink.write(outString);
            outString = "Link,Scenario No, Start Day No,End Day No,Start Time in Min,End Time in Min,Charge for LOV ($),Charge for HOV ($),Charge for Truck ($),Charge for Intermodal $";
            outString += "\r\n";

            // use template as index for TransCAD file - reads AB template names
            line = inTemplate.readLine();
            inFields = line.split(",");
            for (i = 0; i < (inFields.length - 1); i++) {
                linkFields[i] = inFields[i];
            }
            line = inTemplate.readLine(); // reading 2nd line
            inFields = line.split(",");
            for (i = 0; i < (inFields.length - 1); i++) {
                linkBAFields[i] = inFields[i];
            }
            line = inBuff.readLine(); // reading header line of TransCAD links csv
            line = line.replace("\"", "");
            inFields = line.split(",");
            for (i = 0; i < (linkFields.length - 1); i++) { // looping through template fields
                if (!linkFields[i].equals("")) {
                    for (j = 0; j < (inFields.length); j++) { // looping through TransCAD fields
                        linkFields[i] = linkFields[i].replaceAll("^\"|\"$", ""); //gets rid of quotes
                        if (linkFields[i].equals(inFields[j])) {
                            linkDex[i] = j; // matched for AB
                            linkBADex[i] = j; // sets BA also unless overridden below
                        }
                        if (linkBAFields[i].equals(inFields[j])) {
                            linkBADex[i] = j;
                        }
                    }
                }
            }

            // read TransCAD links 
            eof = false;
            while (!eof) {
                line = inBuff.readLine(); // reading the rest of the TransCAD links csv
                if (line == null) {
                    eof = true;
                } else {
                    // one TransCAD line
                    TC_line_num++;
                    line = line.trim(); //to remove leading spaces
                    inFields = line.split(",");
                    // convert to 1-way links
                    // first block does dir = 1 and dir = 0, second block does dir = -1 and dir = 0
                    if (inFields[linkDex[DIRECTION]].equals("0") || inFields[linkDex[DIRECTION]].equals("1")) { // Dir
                        readLinks(inFields, linkDex, dCaps, isCC, outLink, false);  //AB links
                    }
                    if (inFields[linkDex[DIRECTION]].equals("0") || inFields[linkDex[DIRECTION]].equals("-1")) {
                        readLinks(inFields, linkDex, dCaps, isCC, outLink, true);  //BA links
                    }
                }
            }
            inTemplate.close();
            inLink.close();
            inBuff.close();
            outLink.close();
        } catch (IOException e) {
            System.out.println("Error -- " + e.toString());

            }
    }
}

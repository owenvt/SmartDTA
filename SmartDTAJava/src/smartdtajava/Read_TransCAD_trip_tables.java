/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartdtajava;

import java.lang.Math;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Norm Marshall, Smart Mobility Inc., September 2015, updated for Little Rock region, April 2016
 */
public class Read_TransCAD_trip_tables {

    public static void main(String[] args) {

        int lines_in_file;
        int max_lines_in_file = 3000;
        double periodFactor = 2.0; // converting to 120 minute assignment
        //String basePath = "C:/CCMPODTA/DTA_CSVs/"; 
        String basePath = args[0];
        String[] periods = {"", "AM", "MD", "PM", "NT"};
        String inFile, outFile, line, pathIn, pathOut, outString;
        boolean eof;
        String[] inTrips = new String[max_lines_in_file];
        String[] inTrucks = new String[max_lines_in_file];
        Integer p, i, j, iAutos, iTrucks;
        Integer[] iAutosRowSum = new Integer[max_lines_in_file];
        Integer[] iAutosColSum = new Integer[max_lines_in_file];       
        Double[] dAutosRowSum = new Double[max_lines_in_file];
        Double[] dAutosColSum = new Double[max_lines_in_file];
        Integer[] iTrucksRowSum = new Integer[max_lines_in_file];
        Integer[] iTrucksColSum = new Integer[max_lines_in_file];       
        Double[] dTrucksRowSum = new Double[max_lines_in_file];
        Double[] dTrucksColSum = new Double[max_lines_in_file];
        Integer[] TAZ = new Integer[1498];
        Double dTrips, dIntPart, dFracPart, rand;  
        
        //String inTableFile = "AM_total_autos.csv";
        String inTableFile = args[1];
        String inTrucksFile = args[2];
        String outTableFile = "input_demand.csv";
        String outReportFile = "trips_summary.csv";
        
        // read through once to get TAZ numbers
         try {
 
            pathIn = basePath + periods[0] + "/";  //TODO: folder structure
            pathOut = pathIn;
            inFile = pathOut + inTableFile;
            FileReader inTable = new FileReader(inFile);
            BufferedReader inBuff = new BufferedReader(inTable);
            i = 0;
            eof = false;
            while (!eof) {
                line = inBuff.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    i = i + 1;
                    inTrips = line.split(",");
                    //System.out.println(i);
                    //System.out.println(inTrips[0]);
                    TAZ[i] = Integer.valueOf(inTrips[0]);
                }
            }
            lines_in_file = i;
        }
        catch (IOException e) {
            System.out.println("Error -- " + e.toString());
            lines_in_file = 0;
        }       
        
        
        //  time periods
        for (p = 0; p <= 0; p++) {
            
            pathIn = basePath + periods[p] + "/";
            pathOut = pathIn;
            
            for (i = 1; i <= lines_in_file; i++) {
                iAutosRowSum[i] = 0;
                iAutosColSum[i] = 0;
                dAutosRowSum[i] = 0.0;
                dAutosColSum[i] = 0.0;
                iTrucksRowSum[i] = 0;
                iTrucksColSum[i] = 0;
                dTrucksRowSum[i] = 0.0;
                dTrucksColSum[i] = 0.0;            
            }
                  
            // tables
            try {
                System.out.println("Trips");
                inFile = pathOut + inTableFile;
                FileReader inTable = new FileReader(inFile);
                BufferedReader inBuff = new BufferedReader(inTable);
                inFile = pathOut + inTrucksFile;
                FileReader inTrucksTable = new FileReader(inFile);
                BufferedReader inTrucksBuff = new BufferedReader(inTrucksTable);                
                outFile = pathOut + outTableFile;
                FileWriter outTable = new FileWriter(outFile);
                outFile = pathOut + outReportFile;
                FileWriter outReport = new FileWriter(outFile);
                // write header
                outString = "from_zone_id,to_zone_id,number_of_trips_demand_type1,number_of_trips_demand_type2,number_of_trips_demand_type3\r\n";
                outTable.write(outString);
                // read data lines
                i = 0;
                eof = false;
                while (!eof) {
                    line = inBuff.readLine();
                    if (line == null) {
                        eof = true;
                    } else {
                        i = i + 1;
                        inTrips = line.split(",");
                        line = inTrucksBuff.readLine();
                        inTrucks = line.split(",");
                        for (j = 1; j <= lines_in_file; j++) {
                            outString = Integer.toString(TAZ[i]);
                            outString += ",";
                            outString += Integer.toString(TAZ[j]);
                            outString += ",";
                            // do trucks first
                            dTrips = Float.parseFloat(inTrucks[j]) * periodFactor; 
                            dTrucksRowSum[i] += dTrips;
                            dTrucksColSum[j] += dTrips;
                            dFracPart = dTrips % 1;
                            dIntPart = dTrips - dFracPart;
                            iTrucks = (int) Math.round(dIntPart);
                            rand = Math.random();
                            //System.out.println(rand);
                            if (dFracPart > rand) {
                                iTrucks += 1; // Monte Carlo for fractional part - particularly important for large number of cells < 0.5
                            } 
                            iTrucksRowSum[i] += iTrucks;
                            iTrucksColSum[j] += iTrucks;
                            dTrips = Float.parseFloat(inTrips[j]) * periodFactor; 
                            dAutosRowSum[i] += dTrips; // totals of real numbers for comparison at end
                            dAutosColSum[j] += dTrips;                                 
                            dFracPart = dTrips % 1;
                            dIntPart = dTrips - dFracPart;
                            iAutos = (int) Math.round(dIntPart);
                            rand = Math.random();
                            if (dFracPart > rand) {
                                iAutos += 1; // Monte Carlo for fractional part - particularly important for large number of cells < 0.5
                            }
                            iAutosRowSum[i] += iAutos;
                            iAutosColSum[j] += iAutos;
                            outString += String.valueOf(iAutos);
                            outString += ",0,"; // trucks are type 3 and file also needs type 2
                            outString += String.valueOf(iTrucks);
                            outString += "\r\n";
                            outTable.write(outString);
                        }
                    }
                }
                // write report
                outString = "TAZ,dRowAutos,dColAutos,iRowAutos,iColAutos,dRowTrucks,dColTrucks,iRowTrucks,iColTrucks\r\n";
                outReport.write(outString);
                for (i = 1; i <= lines_in_file; i++) {
                    outString = String.valueOf(TAZ[i]) + "," + String.valueOf(dAutosRowSum[i]) + "," + String.valueOf(dAutosColSum[i]) + ",";
                    outString += String.valueOf(iAutosRowSum[i]) + "," + String.valueOf(iAutosColSum[i]) + ",";
                    outString += String.valueOf(dTrucksRowSum[i]) + "," + String.valueOf(dTrucksColSum[i]) + ",";
                    outString += String.valueOf(iTrucksRowSum[i]) + "," + String.valueOf(iTrucksColSum[i]) + "\r\n";
                    outReport.write(outString);
                }

                inTable.close();
                inBuff.close();
                outTable.close();
                outReport.close();
            } catch (IOException e) {
                System.out.println("Error -- " + e.toString());
            }
        }
    }
}

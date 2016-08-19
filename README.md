# SmartDTA Release 8-16-2016
This software is released under GNU GPLv3.  It is intended to simplify the process of converting TransCAD static traffic models for use with NEXTA DTALite and dynamic traffic assignment, and return the outputs for viewing inside TrasCAD.

# Installation
Save all files to a folder on your computer.  In addition to SmartDTA, you will need:

TransCAD version 6

The following free tools:

Java https://java.com/en/download/

Visual Studio 2015  https://www.visualstudio.com/

NEXTA DTALite which is free and can be downloaded at https://github.com/xzhou99/dtalite_software_release

IronPython which is free and can be downloaded at http://ironpython.net/

# Usage
Unzip the folder in your desired directory.

Find the file "input_link_type.csv" in the "Shared" folder.  This file will be copied to each of your time period folders, so modify it to match the link types to the numbers specified in your model documentation.  This file may be modified in each of the time period folders as well, once they are created.

Open Command Prompt and navigate to SmartDTA folder.

Run SmartDTA.py with IronPython by entering "ipy SmartDTA.py".

Browse to select your project folder.  This is the base directory that contains your time period folders.

Add a time period and give it a name.

Find your network file that contains the links and nodes for the street network and export it.

Select the correct fields from the network.  If the fields are named something different for the BA direction, check the "more fields" box and select them.

Browse for the trips matrix and pick a core to match your time period.  Do the same with trucks (this may or may not be in the same matrix)

Select your peak hour factor.

Add another time period, or select all time perids you want to run, then click each of the buttons in the bottom right, "Run Java," "Run DTA," and "Write Load."  Give each time to complete.

There should now be a file called "load.csv" in your base project folder.  This may be opened in TransCAD and contains the model outputs.

# Notes

If there is a problem on any step, go back and make sure all prior boxes are selected before continuing.

If there is an issue with "smartdta.dbd" try recompiling it.  It is simply a compiled version of "export network.rsc" which is included.

SmartDTA.jar is the compiled java source code which is included.  It is used for converting csvs back and forth between TransCAD and DTALite formats.

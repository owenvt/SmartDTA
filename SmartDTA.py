#created for Smart Mobility
#by Owen Marshall, 2016

import sys, os
sys.path.append(r'C:\\Program Files (x86)\\IronPython 2.7\\Lib')

import clr
clr.AddReference("System.Drawing")
clr.AddReference("System.Windows.Forms")
clr.AddReference("System.Data.SqlServerCe")
clr.AddReference("System.Collections")
clr.AddReference("CaliperForm.dll")

from System.Data.SqlServerCe import *
from System.Drawing import Point, Size
from System.Windows.Forms import *
from System.IO import *
from System.Collections import *
#from System.Text import ConnectString

import CaliperForm
import subprocess
from subprocess import call
import shutil

class AddTimePeriod(Form):

    def __init__(self):
        self.Text = "Add Time Period"
        self.Size = Size(200, 120)

        self.nameBox = TextBox()
        self.nameBox.Text = "Name"
        self.nameBox.Location = Point(10, 10)
        self.nameBox.Size = Size(160, 25)

        self.okayButton = Button()
        self.okayButton.Text = "Okay"
        self.okayButton.Location = Point(10, 40)
        self.okayButton.Size = Size(70, 30)
        self.okayButton.Click += self.okayButtonPressed

        self.cancelButton = Button()
        self.cancelButton.Text = "Cancel"
       	self.cancelButton.Location = Point(100, 40)
        self.cancelButton.Size = Size(70, 30)
        self.cancelButton.Click += self.cancelButtonPressed

        self.Controls.Add(self.nameBox)
        self.Controls.Add(self.okayButton)
        self.Controls.Add(self.cancelButton)

    def okayButtonPressed(self, sender, args):
        cn = SqlCeConnection(form.connectionString)
        cn.Open()

        sql = (
            "insert into TimePeriods(T_Name, Network_File, Speed, Speed2, Lanes, Lanes2,"
            + "Functional_Class, Functional_Class2, Matrix_File, Matrix_Core, Direction, Trucks_File, Trucks_Core, PHF) values ('"
            + self.nameBox.Text + "','','','','','','','','','','','','','');"
            )
        cmd = SqlCeCommand(sql, cn)
        cmd.ExecuteNonQuery()
        #print "row inserted"

        form.updateList()
        Form.Close(self)

    def cancelButtonPressed(self, sender, args):
        Form.Close(self)


class SmartDTA(Form):

    def __init__(self):
        self.Text = 'SmartDTA'
        self.Size = Size(510, 620)

        self.label = Label()
        self.label.Text = "Select Time Period"
        self.label.Location = Point(10, 65)
        self.label.Height = 15
        self.label.Width = 200

        self.previousValue = "null"

        self.top_label = Label()
        self.top_label.Text = "Select Project Folder"
        self.top_label.Location = Point(10, 15)
        self.top_label.Height = 15
        self.top_label.Width = 200

        self.select_Pname = ComboBox()
        self.select_Pname.Location = Point(10, 30)
        self.select_Pname.Size = Size(300, 25)
        #self.select_Pname.SelectedIndexChanged += self.OnChanged

        self.select_Pdirectory = Button()
        self.select_Pdirectory.Text = "Browse"
        self.select_Pdirectory.Location = Point(310, 30)
        self.select_Pdirectory.Size = Size(70, 25)
        self.select_Pdirectory.Click += self.PbrowseButtonPressed

        self.listBox = ListBox()
        self.listBox.Location = Point(10, 80)
        self.listBox.Size = Size(300, 140)
        self.listBox.SelectedIndexChanged += self.OnChanged
        self.listBox.SelectionMode = SelectionMode.MultiExtended

        self.addButton = Button()
        self.addButton.Text = "Add"
        self.addButton.Location = Point(310, 80)
        self.addButton.Size = Size(70, 40)
        self.addButton.Click += self.addButtonPressed

        self.removeButton = Button()
        self.removeButton.Text = "Remove"
       	self.removeButton.Location = Point(310, 130)
        self.removeButton.Size = Size(70, 40)
        self.removeButton.Click += self.removeButtonPressed

        parameterLabelWidth = 90
        parameterLabelHeight = 15
        parameterBoxWidth = 180
        parameterBoxHeight = 25

        self.networkFileLabel = Label()
        self.networkFileLabel.Text = "Network File"
        self.networkFileLabel.Location = Point(10, 230)
        self.networkFileLabel.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.networkFileBox = TextBox()
        self.networkFileBox.Location = Point(100, 230)
        self.networkFileBox.Size = Size(parameterBoxWidth, parameterBoxHeight)

        self.select_Ndirectory = Button()
        self.select_Ndirectory.Text = "Browse"
        self.select_Ndirectory.Location = Point(280, 230)
        self.select_Ndirectory.Size = Size(70, 20)
        self.select_Ndirectory.Click += self.NbrowseButtonPressed

        self.speedLabel = Label()
        self.speedLabel.Text = "Speed Field"
        self.speedLabel.Location = Point(10, 300)
        self.speedLabel.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.speedBox = ComboBox()
        self.speedBox.Location = Point(100, 300)
        self.speedBox.Size = Size(parameterBoxWidth, parameterBoxHeight)
        self.speedBox.DropDown += self.get_field_speed

        self.speedLabel2 = Label()
        self.speedLabel2.Text = "Speed Field"
        self.speedLabel2.Location = Point(10, 320)
        self.speedLabel2.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.speedBox2 = ComboBox()
        self.speedBox2.Location = Point(100, 320)
        self.speedBox2.Size = Size(parameterBoxWidth, parameterBoxHeight)
        self.speedBox2.DropDown += self.get_field_speed2

        self.laneLabel = Label()
        self.laneLabel.Text = "Lanes Field"
        self.laneLabel.Location = Point(10, 340)
        self.laneLabel.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.laneBox = ComboBox()
        self.laneBox.Location = Point(100, 340)
        self.laneBox.Size = Size(parameterBoxWidth, parameterBoxHeight)
        self.laneBox.DropDown += self.get_field_lanes

        self.laneLabel2 = Label()
        self.laneLabel2.Text = "Lanes Field"
        self.laneLabel2.Location = Point(10, 360)
        self.laneLabel2.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.laneBox2 = ComboBox()
        self.laneBox2.Location = Point(100, 360)
        self.laneBox2.Size = Size(parameterBoxWidth, parameterBoxHeight)
        self.laneBox2.DropDown += self.get_field_lanes2

        self.directionCheckBox = CheckBox()
        self.directionCheckBox.Text = "More fields"
        self.directionCheckBox.Location = Point(290, 290)
        self.directionCheckBox.Size = Size(100, 40)
        self.directionCheckBox.Click += self.OnChanged

        self.functionalClassLabel = Label()
        self.functionalClassLabel.Text = "Functional Class"
        self.functionalClassLabel.Location = Point(10, 380)
        self.functionalClassLabel.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.functionalClassBox = ComboBox()
        self.functionalClassBox.Location = Point(100, 380)
        self.functionalClassBox.Size = Size(parameterBoxWidth, parameterBoxHeight)
        self.functionalClassBox.DropDown += self.get_field_functional_class

        self.functionalClassLabel2 = Label()
        self.functionalClassLabel2.Text = "Functional Class"
        self.functionalClassLabel2.Location = Point(10, 400)
        self.functionalClassLabel2.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.functionalClassBox2 = ComboBox()
        self.functionalClassBox2.Location = Point(100, 400)
        self.functionalClassBox2.Size = Size(parameterBoxWidth, parameterBoxHeight)
        self.functionalClassBox2.DropDown += self.get_field_functional_class2

        self.matrixFileLabel = Label()
        self.matrixFileLabel.Text = "Autos Table"
        self.matrixFileLabel.Location = Point(10, 480)
        self.matrixFileLabel.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.matrixFileBox = TextBox()
        self.matrixFileBox.Location = Point(100, 480)
        self.matrixFileBox.Size = Size(parameterBoxWidth, parameterBoxHeight)

        self.select_Mdirectory = Button()
        self.select_Mdirectory.Text = "Browse"
        self.select_Mdirectory.Location = Point(280, 480)
        self.select_Mdirectory.Size = Size(70, 20)
        self.select_Mdirectory.Click += self.MbrowseButtonPressed

        self.coreLabel = Label()
        self.coreLabel.Text = "Core Name"
        self.coreLabel.Location = Point(10, 500)
        self.coreLabel.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.coreBox = ComboBox()
        self.coreBox.Location = Point(100, 500)
        self.coreBox.Size = Size(parameterBoxWidth, parameterBoxHeight)
        self.coreBox.DropDown += self.get_core_names

        self.export_network_button = Button()
        self.export_network_button.Text = "Export"
        self.export_network_button.Location = Point(280, 250)
        self.export_network_button.Size = Size(70, 20)
        self.export_network_button.Click += self.OnChanged

        self.export_network_button.Click += self.Export_Network

        self.export_matrix_button = Button()
        self.export_matrix_button.Text = "Export"
        self.export_matrix_button.Location = Point(280, 500)
        self.export_matrix_button.Size = Size(70, 20)
        self.export_matrix_button.Click += self.OnChanged
        self.export_matrix_button.Click += self.Export_Matrix

        self.truckFileLabel = Label()
        self.truckFileLabel.Text = "Trucks Table"
        self.truckFileLabel.Location = Point(10, 530)
        self.truckFileLabel.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.truckFileBox = TextBox()
        self.truckFileBox.Location = Point(100, 530)
        self.truckFileBox.Size = Size(parameterBoxWidth, parameterBoxHeight)

        self.select_Tdirectory = Button()
        self.select_Tdirectory.Text = "Browse"
        self.select_Tdirectory.Location = Point(280, 530)
        self.select_Tdirectory.Size = Size(70, 20)
        self.select_Tdirectory.Click += self.TbrowseButtonPressed

        self.coreTLabel = Label()
        self.coreTLabel.Text = "Core Name"
        self.coreTLabel.Location = Point(10, 550)
        self.coreTLabel.Size = Size(parameterLabelWidth, parameterLabelHeight)

        self.coreTBox = ComboBox()
        self.coreTBox.Location = Point(100, 550)
        self.coreTBox.Size = Size(parameterBoxWidth, parameterBoxHeight)
        self.coreTBox.DropDown += self.get_Tcore_names

        self.export_truck_button = Button()
        self.export_truck_button.Text = "Export"
        self.export_truck_button.Location = Point(280, 550)
        self.export_truck_button.Size = Size(70, 20)
        self.export_truck_button.Click += self.OnChanged
        self.export_truck_button.Click += self.Export_Trucks

        self.run_java_button = Button()
        self.run_java_button.Text = "Run Java"
        self.run_java_button.Location = Point(380, 510)
        self.run_java_button.Size = Size(70, 20)
        self.run_java_button.Click += self.run_java
        self.run_java_button.Click += self.OnChanged

        self.run_dta_button = Button()
        self.run_dta_button.Text = "Run DTA"
        self.run_dta_button.Location = Point(380, 530)
        self.run_dta_button.Size = Size(70, 20)
        self.run_dta_button.Click += self.OnChanged
        self.run_dta_button.Click += self.run_dta_lite

        self.write_load_button = Button()
        self.write_load_button.Text = "Write Load"
        self.write_load_button.Location = Point(380, 550)
        self.write_load_button.Size = Size(70, 20)
        self.write_load_button.Click += self.OnChanged
        self.write_load_button.Click += self.write_load_to_transcad

        self.opperationLabel = Label()
        self.opperationLabel.Location = Point(380, 460)
        self.opperationLabel.Size = Size(70, 70)
        self.opperationLabel.Text = "Operation in progress... please wait"

        self.PHFLabel = Label()
        self.PHFLabel.Text = "Peak Hour Factor"
        self.PHFLabel.Location = Point(300, 360)
        self.PHFLabel.Size = Size(parameterLabelWidth*2, parameterLabelHeight)

        self.PHFBox = TextBox()
        self.PHFBox.Location = Point(300, 380)
        self.PHFBox.Size = Size(parameterBoxWidth/2, parameterBoxHeight)

        self.Controls.Add(self.top_label)
        self.Controls.Add(self.select_Pname)
        self.Controls.Add(self.select_Pdirectory)
        self.Controls.Add(self.label)
        self.Controls.Add(self.listBox)
        self.Controls.Add(self.addButton)
        self.Controls.Add(self.removeButton)
        self.Controls.Add(self.networkFileLabel)
        self.Controls.Add(self.networkFileBox)
        self.Controls.Add(self.select_Ndirectory)
        self.Controls.Add(self.speedLabel)
        self.Controls.Add(self.speedLabel2)
        self.Controls.Add(self.speedBox)
        self.Controls.Add(self.speedBox2)
        self.Controls.Add(self.laneLabel)
        self.Controls.Add(self.laneBox)
        self.Controls.Add(self.laneLabel2)
        self.Controls.Add(self.laneBox2)
        self.Controls.Add(self.directionCheckBox)
        self.Controls.Add(self.functionalClassLabel)
        self.Controls.Add(self.functionalClassBox)
        self.Controls.Add(self.functionalClassLabel2)
        self.Controls.Add(self.functionalClassBox2)
        self.Controls.Add(self.matrixFileLabel)
        self.Controls.Add(self.matrixFileBox)
        self.Controls.Add(self.select_Mdirectory)
        self.Controls.Add(self.coreLabel)
        self.Controls.Add(self.coreBox)
        self.Controls.Add(self.export_network_button)
        self.Controls.Add(self.export_matrix_button)
        self.Controls.Add(self.coreTLabel)
        self.Controls.Add(self.coreTBox)
        self.Controls.Add(self.export_truck_button)
        self.Controls.Add(self.truckFileLabel)
        self.Controls.Add(self.truckFileBox)
        self.Controls.Add(self.select_Tdirectory)
        self.Controls.Add(self.run_java_button)
        self.Controls.Add(self.run_dta_button)
        self.Controls.Add(self.write_load_button)
        self.Controls.Add(self.opperationLabel)
        self.Controls.Add(self.PHFLabel)
        self.Controls.Add(self.PHFBox)
        self.opperationLabel.Visible = False

        self.fileName = "TimePeriods.sdf"
        self.password = "timeperiods"

        self.connectionString = str.Format("DataSource=\"{0}\";Password='{1}'", self.fileName, self.password)

        if not(File.Exists(self.fileName)):
            en = SqlCeEngine(self.connectionString)
            en.CreateDatabase()
            #print "database created" 
            
        cn = SqlCeConnection(self.connectionString)

        cn.Open()

        #create table if it does not exist
        try:
            sql = (
                "create table TimePeriods("
                + "T_Name nvarchar(40) not null, "
                + "Network_File nvarchar(100), "
                + "Speed nvarchar(40), "
                + "Speed2 nvarchar(40), "
                + "Lanes nvarchar(40), "
                + "Lanes2 nvarchar(40), "
                + "Functional_Class nvarchar(40), "
                + "Functional_Class2 nvarchar(40), "
                + "Matrix_File nvarchar(100), "
                + "Matrix_Core nvarchar(40), "
                + "Direction nvarchar(40), "
                + "Trucks_File nvarchar(100), "
                + "Trucks_Core nvarchar(40), "
                + "PHF nvarchar(40));"
                )
            cmd = SqlCeCommand(sql, cn)
            cmd.ExecuteNonQuery()
            #print "table created" 
        except:
            pass #table already exists

        #create table if it does not exist
        try:
            sql = "create table Projects(P_Folder nvarchar(100) not null);"
            cmd = SqlCeCommand(sql, cn)
            cmd.ExecuteNonQuery()
            #print "table created" 
        except:
            pass  #table already exists

        self.fill_project_dropdown()
        self.updateList()

    def addButtonPressed(self, sender, args):
        addForm = AddTimePeriod()
        Form.ShowDialog(addForm)

    def removeButtonPressed(self, sender, args):
        cn = SqlCeConnection(self.connectionString)

        cn.Open()

        for item in self.listBox.SelectedItems:
	        sql = (
	            "delete from TimePeriods where T_Name='" + str(item) + "';"
	            )
	        cmd = SqlCeCommand(sql, cn)
	        result = cmd.ExecuteNonQuery()

        self.updateList()

    def PbrowseButtonPressed(self, sender, args):
     	fbd = FolderBrowserDialog()
     	result = fbd.ShowDialog()
     	if(not str.IsNullOrWhiteSpace(fbd.SelectedPath)):
     		self.select_Pname.Text = fbd.SelectedPath

    def NbrowseButtonPressed(self, sender, args):
     	fbd = OpenFileDialog()
     	result = fbd.ShowDialog()
     	if(not str.IsNullOrWhiteSpace(fbd.FileName)):
     		self.networkFileBox.Text = fbd.FileName

    def MbrowseButtonPressed(self, sender, args):
     	fbd = OpenFileDialog()
     	result = fbd.ShowDialog()
     	if(not str.IsNullOrWhiteSpace(fbd.FileName)):
     		self.matrixFileBox.Text = fbd.FileName

    def TbrowseButtonPressed(self, sender, args):
     	fbd = OpenFileDialog()
     	result = fbd.ShowDialog()
     	if(not str.IsNullOrWhiteSpace(fbd.FileName)):
     		self.truckFileBox.Text = fbd.FileName

    def updateList(self):  
        cn = SqlCeConnection(self.connectionString)

        cn.Open()

        sql = (
            "select T_Name from TimePeriods;"
            )
        cmd = SqlCeCommand(sql, cn)
        rdr = cmd.ExecuteReader()

        self.listBox.Items.Clear()
        while(rdr.Read()):
            self.listBox.Items.Add(rdr.GetString(0))

    def fill_project_dropdown(self):
    	cn = SqlCeConnection(self.connectionString)

    	cn.Open()

    	sql = (
            "select P_Folder from Projects;"
            )
    	cmd = SqlCeCommand(sql, cn)
    	rdr = cmd.ExecuteReader()

    	while(rdr.Read()):
            self.select_Pname.Items.Add(rdr.GetString(0))

    def update_database(self, cn, text, field):
        if self.previousValue == "null" and text != None and text != '':
            self.previousValue = self.listBox.SelectedItem  #if there was no previous item, it was the current one.
        sql = (
            "update TimePeriods set " + field + "='" + text
            + "' where T_Name='"+ self.previousValue + "';"
            )
        cmd = SqlCeCommand(sql, cn)
        rdr = cmd.ExecuteNonQuery()

    def query(self, cn, item, field):
    	sql = (
            "select " + field + " from TimePeriods where T_Name='" + item + "';"
            )
        cmd = SqlCeCommand(sql, cn)
        rdr = cmd.ExecuteReader()

        while(rdr.Read()):
            return_text = rdr.GetString(0)

        return return_text

    def update_box(self, cn, oldtext, field):
        return_text = self.query(cn, self.listBox.SelectedItem, field)
        #if no file specified, default to previous one
        if(return_text==''):
            return_text = oldtext
            sql = (
            "update TimePeriods set "+field+"='" + return_text
            + "' where T_Name='"+ str(self.listBox.SelectedItem) + "';"
            )
            cmd = SqlCeCommand(sql, cn)
            rdr = cmd.ExecuteNonQuery()

        return return_text

    def OnChanged(self, sender, args):
        self.check_time_period_folder(self.listBox.SelectedItem)
        cn = SqlCeConnection(self.connectionString)
        cn.Open()

        sql = "delete from Projects where P_Folder='" + str(self.select_Pname.Text) + "';"
        #print sql
        cmd = SqlCeCommand(sql, cn)
        rdr = cmd.ExecuteNonQuery()

        if self.select_Pname.Text != None:
        	sql = "insert into Projects values ('" + str(self.select_Pname.Text) + "');"
       		#print sql
        	cmd = SqlCeCommand(sql, cn)
        	rdr = cmd.ExecuteNonQuery()

        self.update_database(cn, self.networkFileBox.Text, "Network_File")
        self.update_database(cn, self.speedBox.Text, "Speed")
        self.update_database(cn, self.speedBox2.Text, "Speed2")
        self.update_database(cn, self.laneBox.Text, "Lanes")
        self.update_database(cn, self.laneBox2.Text, "Lanes2")
        self.update_database(cn, self.functionalClassBox.Text, "Functional_Class")
        self.update_database(cn, self.functionalClassBox2.Text, "Functional_Class2")
        self.update_database(cn, self.matrixFileBox.Text, "Matrix_File")
        self.update_database(cn, self.coreBox.Text, "Matrix_Core")
        self.update_database(cn, self.truckFileBox.Text, "Trucks_File")
        self.update_database(cn, self.coreTBox.Text, "Trucks_Core")
        self.update_database(cn, self.PHFBox.Text, "PHF")

        #checkbox update
        state = self.directionCheckBox.CheckState
        if state == CheckState.Checked:
        	sql = (
                "update TimePeriods set Direction='1'"
                + " where T_Name='"+ self.previousValue + "';"
                )
        elif state == CheckState.Unchecked:
        	sql = (
                "update TimePeriods set Direction='2'"
                + " where T_Name='"+ self.previousValue + "';"
                )
        cmd = SqlCeCommand(sql, cn)
        rdr = cmd.ExecuteNonQuery()

        self.networkFileBox.Text = self.update_box(cn, self.networkFileBox.Text, "Network_File")
        self.speedBox.Text = self.update_box(cn, self.speedBox.Text, "Speed")
        self.speedBox2.Text = self.update_box(cn, self.speedBox2.Text, "Speed2")
        self.laneBox.Text = self.update_box(cn, self.laneBox.Text, "Lanes")
        self.laneBox2.Text = self.update_box(cn, self.laneBox2.Text, "Lanes2")
        self.functionalClassBox.Text = self.update_box(cn, self.functionalClassBox.Text, "Functional_Class")
        self.functionalClassBox2.Text = self.update_box(cn, self.functionalClassBox2.Text, "Functional_Class2")
        self.matrixFileBox.Text = self.update_box(cn, self.matrixFileBox.Text, "Matrix_File")
        self.coreBox.Text = self.update_box(cn, self.coreBox.Text, "Matrix_Core")
        self.truckFileBox.Text = self.update_box(cn, self.truckFileBox.Text, "Trucks_File")
        self.coreTBox.Text = self.update_box(cn, self.coreTBox.Text, "Trucks_Core")
        self.PHFBox.Text = self.update_box(cn, self.PHFBox.Text, "PHF")

        sql = (
            "select Direction from TimePeriods where T_Name='" + str(self.listBox.SelectedItem) + "';"
            )
        cmd = SqlCeCommand(sql, cn)
        rdr = cmd.ExecuteReader()

        oldCheckedState = self.directionCheckBox.CheckState

        if(rdr.Read()):
            if rdr.GetString(0) == '1':
                self.directionCheckBox.CheckState = CheckState.Checked
                self.display_fields(True)
            elif rdr.GetString(0) == '2':
                self.directionCheckBox.CheckState = CheckState.Unchecked
                self.display_fields(False)
            elif rdr.GetString(0) == '':
                self.directionCheckBox.CheckState = oldCheckedState
                state = self.directionCheckBox.CheckState
                if state == CheckState.Checked:
                    sql = (
                    "update TimePeriods set Direction='1'"
                    + " where T_Name='"+ self.previousValue + "';"
                    )
                    self.display_fields(True)
                elif state == CheckState.Unchecked:
                    sql = (
                    "update TimePeriods set Direction='2'"
                    + " where T_Name='"+ self.previousValue + "';"
                    )
                    self.display_fields(False)
                cmd = SqlCeCommand(sql, cn)
                rdr = cmd.ExecuteNonQuery()

        self.previousValue = self.listBox.SelectedItem

    def display_fields(self,value):
    	self.speedLabel2.Visible=value
    	self.laneLabel2.Visible=value
    	self.functionalClassLabel2.Visible=value
    	self.speedBox2.Visible=value
    	self.laneBox2.Visible=value
    	self.functionalClassBox2.Visible=value
    	if(value == False):
    		self.speedBox2.ResetText()
    		self.laneBox2.ResetText()
    		self.functionalClassBox2.ResetText()

    def get_core_names(self, sender, args):
    	matrix_name = self.matrixFileBox.Text

    	Gisdk = CaliperForm.Connection()
    	Gisdk.MappingServer = "TransCAD"
    	Gisdk.Open()

    	try:
    		mTrips = Gisdk.DoFunction("OpenMatrix",matrix_name,"Auto")
    		core_names = Gisdk.DoFunction("GetMatrixCoreNames", mTrips)
    	except:	#invalid filename
    		core_names = []

    	Gisdk.Close()

    	self.coreBox.Items.Clear()
        
        for item in core_names:
            self.coreBox.Items.Add(item)

    def get_Tcore_names(self, sender, args):
    	matrix_name = self.truckFileBox.Text

    	Gisdk = CaliperForm.Connection()
    	Gisdk.MappingServer = "TransCAD"
    	Gisdk.Open()

    	try:
    		mTrips = Gisdk.DoFunction("OpenMatrix",matrix_name,"Auto")
    		core_names = Gisdk.DoFunction("GetMatrixCoreNames", mTrips)
    	except:	#invalid filename
    		core_names = []

    	Gisdk.Close()

    	self.coreTBox.Items.Clear()
        
        for item in core_names:
            self.coreTBox.Items.Add(item)

    def get_field(self):
    	output_name = self.select_Pname.Text + "\\" + str(self.listBox.SelectedItem) + "\\links.csv"

    	#open csv and read first line
    	f = open(output_name,'r')
    	line = f.readline()
    	tokens = line.split(',')
    	f.close()

    	return tokens

    def get_field_speed(self, sender, args):
    	tokens = self.get_field()

    	self.speedBox.Items.Clear()
        
    	for item in tokens:
    		self.speedBox.Items.Add(item)

    def get_field_speed2(self, sender, args):
    	tokens = self.get_field()

    	self.speedBox2.Items.Clear()
        
    	for item in tokens:
    		self.speedBox2.Items.Add(item)

    def get_field_lanes(self, sender, args):
    	tokens = self.get_field()

    	self.laneBox.Items.Clear()
        
    	for item in tokens:
    		self.laneBox.Items.Add(item)

    def get_field_lanes2(self, sender, args):
    	tokens = self.get_field()

    	self.laneBox2.Items.Clear()
        
    	for item in tokens:
    		self.laneBox2.Items.Add(item)

    def get_field_functional_class(self, sender, args):
    	tokens = self.get_field()

    	self.functionalClassBox.Items.Clear()
        
    	for item in tokens:
    		self.functionalClassBox.Items.Add(item)

    def get_field_functional_class2(self, sender, args):
    	tokens = self.get_field()

    	self.functionalClassBox2.Items.Clear()
        
    	for item in tokens:
    		self.functionalClassBox2.Items.Add(item)

    def check_time_period_folder(self, item):
    	if self.select_Pname.Text != None and item != None:
    		directory = self.select_Pname.Text + "\\" + item
    		if not os.path.isdir(directory):
    			os.mkdir(directory)
    			self.copy_shared_files(directory,item)

    def Export_Network(self, sender, args):
        cn = SqlCeConnection(self.connectionString)
        cn.Open()
        for item in self.listBox.SelectedItems:
            dbd_name = self.query(cn, item, "Network_File")
            out_links = self.select_Pname.Text + "\\" + item + "\\links.csv"
            out_nodes = self.select_Pname.Text + "\\" + item + "\\nodes.csv"
            ui_database = os.getcwd() + "\\smartdta.dbd"

            self.check_time_period_folder(item)

            Gisdk = CaliperForm.Connection()
            Gisdk.MappingServer = "TransCAD"
            Gisdk.Open()

            self.opperationLabel.Visible = True
            Gisdk.DoMacro("export_network",ui_database,dbd_name,out_links,out_nodes)
            self.opperationLabel.Visible = False

            Gisdk.Close()
            os.remove(self.select_Pname.Text + "\\" + item + "\\links.DCC")
            os.remove(self.select_Pname.Text + "\\" + item + "\\nodes.DCC")

    def Export_Matrix(self, sender, args):
        cn = SqlCeConnection(self.connectionString)
        cn.Open()
        for item in self.listBox.SelectedItems:
			matrix_name = self.query(cn, item, "Matrix_File")
			time_period = self.query(cn, item, "Matrix_Core")
			directory_name = self.select_Pname.Text + "\\" + item
			output_name = directory_name + "\\" + "Trips.csv"

			self.check_time_period_folder(item)

			Gisdk = CaliperForm.Connection()
			Gisdk.MappingServer = "TransCAD"
			Gisdk.Open()

			self.opperationLabel.Visible = True
			mTrips = Gisdk.DoFunction("OpenMatrix",matrix_name,"Auto")

			base_indices = Gisdk.DoFunction("GetMatrixBaseIndex",mTrips)
			row_index = base_indices[0]
			col_index = base_indices[1]
			
			mcTrips = Gisdk.DoFunction("CreateMatrixCurrency",mTrips,time_period,row_index,col_index,None)
			rows = Gisdk.DoFunction("GetMatrixRowLabels",mcTrips)

			Gisdk.DoFunction("ExportMatrix",mcTrips, rows, col_index,"csv",output_name,None)
			self.opperationLabel.Visible = False

			Gisdk.Close()

			self.generate_input_files(directory_name, output_name)

    def Export_Trucks(self, sender, args):
    	cn = SqlCeConnection(self.connectionString)
    	cn.Open()
    	for item in self.listBox.SelectedItems:
    		matrix_name = self.query(cn, item, "Trucks_File")
    		time_period = self.query(cn, item, "Trucks_Core")
    		output_name = self.select_Pname.Text + "\\" + item + "\\" + "TruckTrips.csv"

    		self.check_time_period_folder(item)

    		Gisdk = CaliperForm.Connection()
    		Gisdk.MappingServer = "TransCAD"
    		Gisdk.Open()

    		self.opperationLabel.Visible = True
    		mTrips = Gisdk.DoFunction("OpenMatrix",matrix_name,"Auto")

    		base_indices = Gisdk.DoFunction("GetMatrixBaseIndex",mTrips)
    		row_index = base_indices[0]
    		col_index = base_indices[1]
			
    		mcTrips = Gisdk.DoFunction("CreateMatrixCurrency",mTrips,time_period,row_index,col_index,None)
    		rows = Gisdk.DoFunction("GetMatrixRowLabels",mcTrips)

    		Gisdk.DoFunction("ExportMatrix",mcTrips, rows, col_index,"csv",output_name,None)
    		self.opperationLabel.Visible = False
    		Gisdk.Close()

    def generate_link_template(self, item):
    	cn = SqlCeConnection(self.connectionString)
    	cn.Open()
    	row1 = "name,link_id,from_node_id,to_node_id,link_type_name,direction,length,number_of_lanes,speed_limit_in_mph,saturation_flow_rate_in_vhc_per_hour_per_lane,lane_capacity_in_vhc_per_hour,link_type,jam_density_in_vhc_pmpl,wave_speed_in_mph,effective_green_time_length_in_second,green_start_time_in_second,AADT_conversion_factor,mode_code,grade,geometry"
    	row2 = ",ID,From_Node,To_Node,,Dir,Length,"+self.query(cn, item, "Lanes")+","+self.query(cn, item, "Speed")+",,,"+self.query(cn, item, "Functional_Class")+",,,,,,,,x"
    	row3 = ",,,,,,,"+self.query(cn, item, "Lanes2")+","+self.query(cn, item, "Speed2")+",,,"+self.query(cn, item, "Functional_Class2")+",,,,,,,,x"

    	self.check_time_period_folder(item)

    	template = open(self.select_Pname.Text + "\\" + item + "\\CCMPO_link_template.csv","w")
    	template.write(row1 + "\n" + row2 + "\n" + row3)
    	template.close()

    def copy_shared_files(self, target_directory,target_name):
    	print "copying shared files"
    	base_path = os.getcwd()
    	shared_folder = "Shared"
    	tnp_file = "period.tnp"
    	#bat_file = "period.bat"
    	additional_files = [
    		"CCMPO_node_template.csv",
    		"DTASettings.txt",
    		"input_base_cycle_fraction_of_OpMode.csv",
    		"input_cycle_emission_factor.csv",
    		"input_demand_file_list.csv",
    		"input_demand_meta_data.csv",
    		"input_demand_type.csv",
    		"input_MOE_settings.csv",
    		"input_node_control_type.csv",
    		"input_pricing_type.csv",
    		"input_scenario_settings.csv",
    		"input_vehicle_emission_rate.csv",
    		"input_vehicle_type.csv",
    		"input_VOT.csv",
    		"Scenario_Incident.csv",
    		"input_link_type.csv"
    	]
    	shutil.copy(base_path + "\\" + shared_folder + "\\" + tnp_file, target_directory + "\\" + target_name + ".tnp")
    	#shutil.copy(base_path + "\\" + shared_folder + "\\" + bat_file, target_directory + "\\" + target_name + ".bat")
    	for file_name in additional_files:
    		shutil.copy(base_path + "\\" + shared_folder + "\\" + file_name, target_directory)


    def generate_input_files(self, target_directory, trips_file):
    	activity_location = "input_activity_location.csv"  #TODO: Don't write this file
    	zone = "input_zone.csv"
    	zone_ids = []

    	f = open(trips_file, "r")
    	i = 0
    	for line in f:
    		zone_ids.append(line.split(",")[0])	#get the first field in the csv (id)
    		i+=1
    	f.close()

    	f = open(target_directory + "\\" + activity_location, "w")
    	f.write("zone_id,node_id,external_OD_flag\n")
    	for i in range(0, len(zone_ids)):
    		f.write(zone_ids[i] + "," + zone_ids[i] + ",0\n")
    	f.close()

    	f = open(target_directory + "\\" + zone, "w")
    	f.write("zone_id,production,attraction,geometry\n")
    	for i in range(0, len(zone_ids)):
    		f.write(zone_ids[i] + ",0,0,<Polygon><outerBoundaryIs><LinearRing><coordinates>\n")
    	f.close()

    def run_java(self, sender, args):
    	project_folder = self.select_Pname.Text

    	self.opperationLabel.Visible = True

    	for item in self.listBox.SelectedItems:
	    	self.generate_link_template(item)
	    	cmd = 'java -cp "SmartDTAJava.jar" "smartdtajava.Read_TransCAD_trip_tables" "'+project_folder+ "\\" + item +'" Trips.csv TruckTrips.csv '
	    	subprocess.call(cmd)
	    	cmd = 'java -cp "SmartDTAJava.jar" "smartdtajava.Read_TransCAD_network" "'+project_folder+ "\\" + item +'\\\\"'
	    	subprocess.call(cmd)
    
    	self.opperationLabel.Visible = False

    def run_dta_lite(self, sender, args):
    	project_folder = self.select_Pname.Text

    	self.opperationLabel.Visible = True

    	for item in self.listBox.SelectedItems:
    		cmd = 'DTALite.exe'
    		arg = item + '.tnp'
    		cwd = project_folder + '\\' +  item + '\\'
    		p = subprocess.Popen([cmd, arg], cwd=cwd)
    		p.wait()
	    	subprocess.call(cmd)
    
    	self.opperationLabel.Visible = False

    def write_load_to_transcad(self, sender, args):
        project_folder = self.select_Pname.Text
        try:
            os.remove(self.select_Pname.Text + "\\load.DCC")
        except:
            pass
        self.opperationLabel.Visible = True
        cn = SqlCeConnection(self.connectionString)
        cn.Open()
        phfs = []
        time_periods = []
        for item in self.listBox.SelectedItems:
            phfs.append(self.query(cn, item, "PHF"))
            time_periods.append(item)

        cmd = 'java -cp "SmartDTAJava.jar" "smartdtajava.Write_Load_to_TransCAD" "'+project_folder+ '\\\\" "' + str(time_periods) + '" "' + str(phfs) + '"'
        print cmd
        subprocess.call(cmd)

        self.opperationLabel.Visible = False


form = SmartDTA()
Application.Run(form)
Macro "main"

	RunMacro("TCB Init")
	
	dbd_name = "D:\\CCMPO\\1_Networks\\CCMPO_Highways_2005.dbd"
	out_links ="D:\\DTA\\links.csv"
	out_nodes ="D:\\DTA\\nodes.csv"

	RunMacro("export_network",dbd_name,out_links,out_nodes)

endMacro
	
Macro "export_network" (dbd_name,out_links,out_nodes)

	// open network map and get layer names
	info = GetDBInfo(dbd_name)
	scope = info[1]
    	newmap = CreateMap("TempMap",{{"Scope",info[1]}})
	lyrs = GetDBLayers(dbd_name)
	SetMapUnits("Miles")
	
	// export nodes
	node_lyr = AddLayer("TempMap", "nodes", dbd_name, lyrs[1])
	SetLayerVisibility(node_lyr,"True")
    	SetLayer(node_lyr)
	ExportView(node_lyr + "|", "CSV", out_nodes,,{{"CSV Header", "True"}})
	
	// add From and To nodes and export links
	link_lyr = AddLayer("TempMap", "links", dbd_name, lyrs[2])
	SetLayerVisibility(link_lyr,"True")
	SetLayer(link_lyr)
	from_fld =CreateNodeField(link_lyr, "From_Node", node_lyr + ".ID", "From", )	
	to_fld = CreateNodeField(link_lyr, "To_Node", node_lyr + ".ID", "To", )	
	ExportView(link_lyr + "|", "CSV", out_links,,{{"CSV Header", "True"}})
	
	// close map
	CloseMap("TempMap")

endMacro
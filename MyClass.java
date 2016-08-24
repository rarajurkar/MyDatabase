import java.io.RandomAccessFile;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;



/**
 * @author Chris Irwin Davis
 * @version 1.0
 * <b>This is an example of how to read/write binary data files using RandomAccessFile class</b>
 *
 */
public class MyClass {
	// This can be changed to whatever you like
	static String prompt = "davisql> ";
	
	/*
	 *  This example does not dynamically load a table schema to be able to 
	 *  read/write any table -- there is exactly ONE hardcoded table schema.
	 *  These are the variables associated with that hardecoded table schema.
	 *  Your database engine will need to define these on-the-fly from
	 *  whatever table schema you use from your information_schema
	 */
	static String widgetTableFileName = "widgets.dat";
	static String tableIdIndexName = "widgets.id.ndx";
	static int id;
	static String name;
	static short quantity;
	static float probability;
	static String schemaName="information_schema";
	

    public static void main(String[] args) throws IOException {
    	/*load the data initially*/
    	loadInitialData();
		/* Display the welcome splash screen */
		splashScreen();
		
		/* 
		 *  Manually create a binary data file for the single hardcoded 
		 *  table. It inserts 5 hardcoded records. The schema is inherent 
		 *  in the code, pre-defined, and static.
		 *  
		 *  An index file for the ID field is created at the same time.
		 */
		hardCodedCreateTableWithIndex();

		/* 
		 *  The Scanner class is used to collect user commands from the prompt
		 *  There are many ways to do this. This is just one.
		 *
		 *  Each time the semicolon (;) delimiter is entered, the userCommand String
		 *  is re-populated.
		 */
		Scanner scanner = new Scanner(System.in).useDelimiter(";");
		String userCommand; // Variable to collect user input from the prompt

		do {  // do-while !exit
			System.out.print(prompt);
			userCommand = scanner.next().trim();
			/*
			 *  This switch handles a very small list of commands of known syntax.
			 *  You will probably want to write a parse(userCommand) method to
			 *  to interpret more complex commands. 
			 */
			switch (userCommand) {
				case "show tables":
				case "SHOW TABLES":
					showTables(userCommand);
					break;
				case "show schemas":
				case "SHOW SCHEMAS":
					showSchemas();
					break;
				case "help":
				case "HELP":
					help();
					break;
				case "version": 
				case "VERSION":
					version();
					break;
				default:
					if(userCommand.contains("use") || userCommand.contains("USE")){
						useSchema(userCommand);
					}
					else if(userCommand.contains("create schema") || userCommand.contains("CREATE SCHEMA")){
						createSchema(userCommand);
					}
					else if(userCommand.contains("select * from") || userCommand.contains("SELECT * FROM")){
						selectOperation(userCommand);
					}
					else if(userCommand.contains("create table") || userCommand.contains("CREATE TABLE")){
						createTable(userCommand);
					}
					else if(userCommand.contains("insert into table") || userCommand.contains("INSERT INTO TABLE")){
						insertInto(userCommand);
					}
					else if(userCommand.contains("drop table") || userCommand.contains("DROP TABLE"))
						dropTable(userCommand);
					else{
						System.out.println("I didn't understand the command: \"" + userCommand + "\"");
					}
			}
		} while(!userCommand.equals("exit"));
		System.out.println("Exiting...");
	    
    } /* End main() method */


    private static void showTables(String userCommand) {
		// TODO Auto-generated method stub
    	try {
    		RandomAccessFile tablesTableFile = new RandomAccessFile("information_schema.tables.tbl", "rw");
    		while(tablesTableFile.getFilePointer()+1 < tablesTableFile.length()){
    			byte varcharLength = tablesTableFile.readByte();
    			StringBuffer s=new StringBuffer() ;
    			StringBuffer k=new StringBuffer();
				for(int i = 0; i < varcharLength; i++)
						s.append((char)tablesTableFile.readByte());//System.out.print((char)schemataTableFile.readByte());
				byte tableLength = tablesTableFile.readByte();
				for(int i = 0; i < tableLength; i++){
						k.append((char)tablesTableFile.readByte());//System.out.print((char)schemataTableFile.readByte());
				}
				if(s.toString().equalsIgnoreCase(schemaName)){
					
					System.out.println(k.toString());
				}
				tablesTableFile.readLong();
			}
		} catch(EOFException e){
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}

		
	}


	private static void selectOperation(String userCommand) throws IOException {
		// TODO Auto-generated method stub
		String withoutSelect = userCommand.substring(14);
		//System.out.println(withoutSelect);
		if(withoutSelect.contains("where")){
			 String tableNm= withoutSelect.substring(0, withoutSelect.indexOf(" ")).trim();
			 //System.out.println("Table Name :"+tableNm);
			 String remaining = withoutSelect.substring(withoutSelect.indexOf(" ")+1);
			 //System.out.println(remaining);
			 String afterWhere = remaining.substring(remaining.indexOf(" ")+1);
			 //System.out.println(afterWhere);
			 RandomAccessFile columnsTableFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
			 List<String> allColumns = new ArrayList<String>();
			 List<String> dataTypes = new ArrayList<String>();
			 while(columnsTableFile.getFilePointer()+1 < columnsTableFile.length()){
				//SCHEMA_NAME
				byte varcharLength = columnsTableFile.readByte();
				StringBuffer schema = new StringBuffer() ;
				for(int i = 0; i < varcharLength; i++)
					schema.append((char)columnsTableFile.readByte());
				//TABLE_NAME
				StringBuffer tblname = new StringBuffer();
				byte tableLength = columnsTableFile.readByte();
				for(int i = 0; i < tableLength; i++){
					tblname.append((char)columnsTableFile.readByte());
				}
				//COLUMN_NAME
				StringBuffer columnname = new StringBuffer();
				byte columnLength = columnsTableFile.readByte();
				for(int i = 0; i < columnLength; i++){
					columnname.append((char)columnsTableFile.readByte());
				}
				//ORDINAL_POSITION
				columnsTableFile.readInt();
				//COLUMN_TYPE
				StringBuffer type = new StringBuffer();
				byte typeLength = columnsTableFile.readByte();
				for(int i = 0; i < typeLength; i++){
					type.append((char)columnsTableFile.readByte());
				}
				if(tblname.toString().trim().equalsIgnoreCase(tableNm.trim())){
					allColumns.add(columnname.toString());
					dataTypes.add(type.toString());
				}
				//IS_NULLABLE
				StringBuffer isnull = new StringBuffer();
				byte isnullLength = columnsTableFile.readByte();
				for(int i = 0; i < isnullLength; i++){
					isnull.append((char)columnsTableFile.readByte());
				}
				//COLUMN_KEY
				StringBuffer key = new StringBuffer();
				byte keyLength = columnsTableFile.readByte();
				for(int i = 0; i < keyLength; i++){
					key.append((char)columnsTableFile.readByte());
				}
			 }
			 //printing
			 //check for column name
			 String conditionColumn =""; 
			 int index=-1;
			 String conditionColumnDT = "";
			 for(int k=0;k<allColumns.size();k++){
				 if(afterWhere.contains(allColumns.get(k).trim())){
					 conditionColumn = allColumns.get(k).trim();
					 conditionColumnDT = dataTypes.get(k).trim();
					 index = k;
				 }
			 }
			 String operator = afterWhere.substring(conditionColumn.length(), conditionColumn.length()+1);
			 String value = afterWhere.substring(conditionColumn.length()+1);
			 //check for datatypes 
			 //check for operators
			 String fileName = schemaName + "."+ tableNm +".tbl";	
				//System.out.print(fileName);
			 	List<Long> allData = new ArrayList<Long>();
				RandomAccessFile tableFile = new RandomAccessFile(fileName, "rw");
				tableFile.seek(0);
				int indexOfPointer=-1;
				boolean flag = false; 
				long initialPointer=0;
				while(tableFile.getFilePointer()+1 < tableFile.length()){
					allData.add(tableFile.getFilePointer());
					initialPointer=tableFile.getFilePointer();
					indexOfPointer++;
					for(int i=0;i<allColumns.size();i++){	
						if(i==index){
							//get the value in its original form
							int queryInt =-1,whereInt=-1;
							String queryStr="", whereStr = "";
							long queryLong=-1, whereLong = -1; 
							if(dataTypes.get(i).contains("varchar")){
								StringBuffer str = new StringBuffer();
								byte strLength = tableFile.readByte();
								for(int j = 0; j < strLength; j++){
									str.append((char)tableFile.readByte());
								}
								whereStr = str.toString();
								queryStr=value.substring(1, value.length()-1).trim();
								if(operator.equals("=")){
									if(whereStr.equalsIgnoreCase(queryStr)){
										flag = true;
									}
								}
							}
							else if(dataTypes.get(i).contains("long")){
								whereLong=tableFile.readLong();
								queryLong = Long.parseLong(value.trim());
								if(operator.equals("=")){
									if(whereLong==queryLong){
										flag=true;
									}
								}
								else if(operator.equals(">")){
									if(whereLong > queryLong){
										flag=true;
									}
								}
								else if(operator.equals("<")){
									if(whereLong < queryLong){
										flag=true;
									}
								}
							}
							else if(dataTypes.get(i).contains("int")){
								whereInt=tableFile.readInt();
								queryInt=Integer.parseInt(value.trim());
								if(operator.equals("=")){
									if(whereInt==queryInt){
										flag=true;
									}
								}
								else if(operator.equals(">")){
									if(whereInt > queryInt){
										flag=true;
									}
								}
								else if(operator.equals("<")){
									if(whereInt < queryInt){
										flag=true;
									}
								}
							}
							//check for the condition
							
						}
						else{
							if(dataTypes.get(i).contains("varchar")){
								StringBuffer str = new StringBuffer();
								byte strLength = tableFile.readByte();
								for(int j = 0; j < strLength; j++){
									str.append((char)tableFile.readByte());
								}
							}
							else if(dataTypes.get(i).contains("long")){
								tableFile.readLong();
							}
							else if(dataTypes.get(i).contains("int")){
								tableFile.readInt();
							}	
						}
					}
					if(flag){
						tableFile.seek(initialPointer);
						for(int i=0;i<allColumns.size();i++){
							if(dataTypes.get(i).contains("varchar")){
								StringBuffer str = new StringBuffer();
								byte strLength = tableFile.readByte();
								for(int j = 0; j < strLength; j++){
									str.append((char)tableFile.readByte());
								}
								System.out.print("||"+str.toString());
							}
							else if(dataTypes.get(i).contains("long")){
								System.out.print("||"+tableFile.readLong());
							}
							else if(dataTypes.get(i).contains("int")){
								System.out.print("||"+tableFile.readInt());
							}
							else if(dataTypes.get(i).contains("date")){
								StringBuffer str = new StringBuffer();
								byte strLength = tableFile.readByte();
								for(int j = 0; j < strLength; j++){
									str.append((char)tableFile.readByte());
								}
								System.out.print("||"+str.toString());
							}
							else if(dataTypes.get(i).contains("double")){
								System.out.print("||"+tableFile.readDouble());
							}
							else if(dataTypes.get(i).contains("float")){
								System.out.print("||"+tableFile.readFloat());
							}
							
							System.out.print("\t");
						}
						System.out.println();
					
					}
				}
		}
		else{
			String tableNm= withoutSelect.substring(0);
			//System.out.println(tableNm);
			RandomAccessFile columnsTableFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
			 List<String> allColumns = new ArrayList<String>();
			 List<String> dataTypes = new ArrayList<String>();
			 while(columnsTableFile.getFilePointer()+1 < columnsTableFile.length()){
				//SCHEMA_NAME
				byte varcharLength = columnsTableFile.readByte();
				StringBuffer schema = new StringBuffer() ;
				for(int i = 0; i < varcharLength; i++)
					schema.append((char)columnsTableFile.readByte());
				//TABLE_NAME
				StringBuffer tblname = new StringBuffer();
				byte tableLength = columnsTableFile.readByte();
				for(int i = 0; i < tableLength; i++){
					tblname.append((char)columnsTableFile.readByte());
				}
				//COLUMN_NAME
				StringBuffer columnname = new StringBuffer();
				byte columnLength = columnsTableFile.readByte();
				for(int i = 0; i < columnLength; i++){
					columnname.append((char)columnsTableFile.readByte());
				}
				//ORDINAL_POSITION
				columnsTableFile.readInt();
				//COLUMN_TYPE
				StringBuffer type = new StringBuffer();
				byte typeLength = columnsTableFile.readByte();
				for(int i = 0; i < typeLength; i++){
					type.append((char)columnsTableFile.readByte());
				}
				if(tblname.toString().trim().equalsIgnoreCase(tableNm.trim())){
					allColumns.add(columnname.toString());
					dataTypes.add(type.toString());
				}
				//IS_NULLABLE
				StringBuffer isnull = new StringBuffer();
				byte isnullLength = columnsTableFile.readByte();
				for(int i = 0; i < isnullLength; i++){
					isnull.append((char)columnsTableFile.readByte());
				}
				//COLUMN_KEY
				StringBuffer key = new StringBuffer();
				byte keyLength = columnsTableFile.readByte();
				for(int i = 0; i < keyLength; i++){
					key.append((char)columnsTableFile.readByte());
				}
			 }
			 //System.out.println(allColumns);
			 //System.out.println(dataTypes);
			 //printing
			String fileName = schemaName + "."+ tableNm +".tbl";	
			//System.out.print(fileName);
			RandomAccessFile tableFile = new RandomAccessFile(fileName, "rw");
			tableFile.seek(0);
			while(tableFile.getFilePointer()+1 < tableFile.length()){
				for(int i=0;i<allColumns.size();i++){
					if(dataTypes.get(i).contains("varchar")){
						StringBuffer str = new StringBuffer();
						byte strLength = tableFile.readByte();
						for(int j = 0; j < strLength; j++){
							str.append((char)tableFile.readByte());
						}
						System.out.print("||"+str.toString());
					}
					else if(dataTypes.get(i).contains("long")){
						System.out.print("||"+tableFile.readLong());
					}
					else if(dataTypes.get(i).contains("int")){
						System.out.print("||"+tableFile.readInt());
					}
					else if(dataTypes.get(i).contains("date")){
						StringBuffer str = new StringBuffer();
						byte strLength = tableFile.readByte();
						for(int j = 0; j < strLength; j++){
							str.append((char)tableFile.readByte());
						}
						System.out.print("||"+str.toString());
					}
					else if(dataTypes.get(i).contains("double")){
						System.out.print("||"+tableFile.readDouble());
					}
					else if(dataTypes.get(i).contains("float")){
						System.out.print("||"+tableFile.readFloat());
					}
					
					System.out.print("\t");
				}
				System.out.println();
			}
			
		}
		
	}


	private static void dropTable(String userCommand) throws IOException {
		// TODO Auto-generated method stub
		
    	System.out.println("inside drop table");
    	//System.out.println(userCommand);
    	String dropTableName = userCommand.substring(11);
    	/*
    	String fileName =  schemaName + "."+ dropTableName +".tbl";
    	RandomAccessFile tablesTableFile = new RandomAccessFile("information_schema.tables.tbl", "rw");
		//RandomAccessFile columnsTableFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
		RandomAccessFile tempFile = new RandomAccessFile("tempFile.tbl", "rw");
    	//delete this file
		while(tablesTableFile.getFilePointer()+1 < tablesTableFile.length()){
			byte varcharLength = tablesTableFile.readByte();
			StringBuffer schema = new StringBuffer() ;
			for(int i = 0; i < varcharLength; i++)
				schema.append((char)tablesTableFile.readByte());
			String scNm = schema.toString();
			//TABLE_NAME
			StringBuffer tblname = new StringBuffer();
			byte tableLength = tablesTableFile.readByte();
			for(int i = 0; i < tableLength; i++){
				tblname.append((char)tablesTableFile.readByte());
			}
			String tbNm = tblname.toString();
			if(!(tbNm.trim().equalsIgnoreCase(dropTableName.trim()))){
				tempFile.writeByte(scNm.length()); // TABLE_SCHEMA
				tempFile.writeBytes(scNm);
				tempFile.writeByte(tbNm.length()); // TABLE_NAME
				tempFile.writeBytes(tbNm);
				tempFile.writeLong(tablesTableFile.readLong()); // TABLE_ROWS
			}
			else{
				tablesTableFile.readLong();
			}
		}
		File fDelete = new File("information_schema.tables.tbl");
		fDelete.delete();
		File fOld = new File("tempFile.tbl");
		File fNew = new File("information_schema.tables.tbl");
		fOld.renameTo(fDelete);
		*/

		String fileName = "information_schema.table.tbl";
		RandomAccessFile tablesTableFile = new RandomAccessFile(fileName, "rw");
		String newfileName = "information_schema.tabl.tbl";
		RandomAccessFile newtablesTable = new RandomAccessFile(newfileName, "rw");
		while(tablesTableFile.getFilePointer()+1 < tablesTableFile.length())
		{
			byte vLength = tablesTableFile.readByte();
			StringBuffer tempstr = new StringBuffer();
			for(int i = 0; i < vLength; i++)
				tempstr.append((char)tablesTableFile.readByte());
			byte varcharLength = tablesTableFile.readByte();
			StringBuffer tempstr1 = new StringBuffer();
			for(int i = 0; i < varcharLength; i++)
			{
				tempstr1.append((char)tablesTableFile.readByte());
			}
			long rows = tablesTableFile.readLong();
			if (tempstr.toString().equalsIgnoreCase(schemaName) && tempstr1.toString().equalsIgnoreCase(dropTableName))
			{
				System.out.println("Deleted rows");
			}
			else
			{
			newtablesTable.writeByte(vLength);
			newtablesTable.writeBytes(tempstr.toString());
			newtablesTable.writeByte(varcharLength);
			newtablesTable.writeBytes(tempstr1.toString());
			newtablesTable.writeLong(rows);
			}
		}
		tablesTableFile.close();
		newtablesTable.close();
		File file = new File("information_schema.tabl.tbl");
		File file2 = new File("information_schema.table.tbl");
		file2.delete();
		file.renameTo(file2);
		
		RandomAccessFile columnsTableFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
		RandomAccessFile newcolumnsTable = new RandomAccessFile("information_schema.column.tbl", "rw");
		while (columnsTableFile.getFilePointer()+1 < columnsTableFile.length())
		{
			Byte schemaLength = columnsTableFile.readByte(); // TABLE_SCHEMA
			StringBuffer schema = new StringBuffer();
			for(int i = 0; i < schemaLength; i++)
				schema.append((char)columnsTableFile.readByte());
			
			Byte tableLength = columnsTableFile.readByte(); // TABLE_NAME
			StringBuffer table = new StringBuffer();
			for(int i = 0; i < tableLength; i++)
				table.append((char)columnsTableFile.readByte());
			Byte columnlen = columnsTableFile.readByte(); // COLUMN_NAME
			StringBuffer colname = new StringBuffer();
			for(int i = 0; i < columnlen; i++)
				colname.append((char)columnsTableFile.readByte());
			int order= columnsTableFile.readInt(); // ORDINAL_POSITION
			Byte columntype = columnsTableFile.readByte(); // COLUMN_TYPE
			StringBuffer coltype= new StringBuffer();
			for(int i = 0; i < columntype; i++)
				coltype.append((char)columnsTableFile.readByte());
			Byte nullable = columnsTableFile.readByte(); // IS_NULLABLE
			StringBuffer nultype= new StringBuffer();
			for(int i = 0; i < nullable; i++)
				nultype.append((char)columnsTableFile.readByte());
			Byte key = columnsTableFile.readByte(); // COLUMN_KEY
			StringBuffer keyyn= new StringBuffer();
			for(int i = 0; i < key; i++)
				keyyn.append((char)columnsTableFile.readByte());
			
			if (schema.toString().equalsIgnoreCase(schemaName) && table.toString().equalsIgnoreCase(dropTableName))
			{
				System.out.println("Deleted rows");
			}
			else
			{
				newcolumnsTable.writeByte(schemaLength);
				newcolumnsTable.writeBytes(schema.toString());
				newcolumnsTable.writeByte(tableLength);
				newcolumnsTable.writeBytes(table.toString());
				newcolumnsTable.writeByte(columnlen);
				newcolumnsTable.writeBytes(colname.toString());
				newcolumnsTable.writeInt(order);
				newcolumnsTable.writeByte(columntype);
				newcolumnsTable.writeBytes(coltype.toString());
				newcolumnsTable.writeByte(nullable);
				newcolumnsTable.writeBytes(nultype.toString());
				newcolumnsTable.writeByte(key);
				newcolumnsTable.writeBytes(keyyn.toString());
			}
		}
		columnsTableFile.close();
		newcolumnsTable.close();
		File file3 = new File("information_schema.column.tbl");
		File file4 = new File("information_schema.columns.tbl");	
		file4.delete();
		file3.renameTo(file4);
	}


	private static void insertInto(String userCommand) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("inside insert into");
		System.out.println(userCommand);
		String sub = userCommand.substring(18);
		System.out.println(sub);
		String tableName = sub.substring(0,sub.indexOf(" ")).trim();
		System.out.println(tableName);
		String values = sub.substring(sub.indexOf("(")+1, sub.indexOf(")"));
		//System.out.println(values);
		String arr[]=values.split(",");
		//System.out.println(arr[0]+arr[1]);
		RandomAccessFile columnsTableFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
		List<String> allColumns = new ArrayList<String>();
		List<String> dataTypes = new ArrayList<String>();
		while(columnsTableFile.getFilePointer()+1 < columnsTableFile.length()){
			//SCHEMA_NAME
			byte varcharLength = columnsTableFile.readByte();
			StringBuffer schema = new StringBuffer() ;
			for(int i = 0; i < varcharLength; i++)
				schema.append((char)columnsTableFile.readByte());
			//TABLE_NAME
			StringBuffer tblname = new StringBuffer();
			byte tableLength = columnsTableFile.readByte();
			for(int i = 0; i < tableLength; i++){
				tblname.append((char)columnsTableFile.readByte());
			}
			//COLUMN_NAME
			StringBuffer columnname = new StringBuffer();
			byte columnLength = columnsTableFile.readByte();
			for(int i = 0; i < columnLength; i++){
				columnname.append((char)columnsTableFile.readByte());
			}
			//ORDINAL_POSITION
			columnsTableFile.readInt();
			//COLUMN_TYPE
			StringBuffer type = new StringBuffer();
			byte typeLength = columnsTableFile.readByte();
			for(int i = 0; i < typeLength; i++){
				type.append((char)columnsTableFile.readByte());
			}
			if(tblname.toString().trim().equalsIgnoreCase(tableName.trim())){
				allColumns.add(columnname.toString());
				dataTypes.add(type.toString());
			}
			//IS_NULLABLE
			StringBuffer isnull = new StringBuffer();
			byte isnullLength = columnsTableFile.readByte();
			for(int i = 0; i < isnullLength; i++){
				isnull.append((char)columnsTableFile.readByte());
			}
			//COLUMN_KEY
			StringBuffer key = new StringBuffer();
			byte keyLength = columnsTableFile.readByte();
			for(int i = 0; i < keyLength; i++){
				key.append((char)columnsTableFile.readByte());
			}
		 } 
		System.out.println(allColumns);
		System.out.println(dataTypes);
		//generate file name
		String fileName = schemaName + "."+ tableName +".tbl";
		String indexFileName = schemaName + "."+ tableName +".id.ndx";
		RandomAccessFile tableFile = new RandomAccessFile(fileName, "rw");
		RandomAccessFile tableIdIndex = new RandomAccessFile(indexFileName, "rw");
		
		//check data types
		for(int i=0;i<arr.length;i++){
			if(dataTypes.get(i).contains("varchar")){
				String substr = (arr[i].trim()).substring(1, arr[i].length()-1);
				tableFile.writeByte(substr.length());
				tableFile.writeBytes(substr);
			}
			else if(dataTypes.get(i).contains("long")){
				tableFile.writeLong(Long.parseLong(arr[i].trim()));
			}
			else if(dataTypes.get(i).contains("int")){
				tableFile.writeInt(Integer.parseInt(arr[i].trim()));
			}
			else if(dataTypes.get(i).contains("double")){
				tableFile.writeDouble(Double.parseDouble(arr[i].trim()));
			}
			else if(dataTypes.get(i).contains("date")){
				String substr = (arr[i].trim()).substring(1, arr[i].length()-1);
				tableFile.writeByte(substr.length());
				tableFile.writeBytes(substr);
			}
		}
		
	}


	private static void createTable(String userCommand) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println("create table");
		try {
			RandomAccessFile tablesTableFile = new RandomAccessFile("information_schema.tables.tbl", "rw");
			RandomAccessFile columnsTableFile = new RandomAccessFile("information_schema.columns.tbl", "rw");
			tablesTableFile.seek(tablesTableFile.length());
			columnsTableFile.seek(columnsTableFile.length());
			String tableName = userCommand.substring(13,userCommand.indexOf("(")).trim();
			System.out.println(tableName);
			String attributes = userCommand.substring(userCommand.indexOf("(")+1,userCommand.length()-1);
			System.out.println(attributes);
			int ordPos = 1;
			for(String str : attributes.split(",")){
				System.out.println(str);
				String[] arr = str.trim().split(" ");
				//System.out.println(arr[0]+" "+arr[1]);
				//arr.size();
				columnsTableFile.writeByte(schemaName.length()); // TABLE_SCHEMA
				columnsTableFile.writeBytes(schemaName);
				columnsTableFile.writeByte(tableName.length()); // TABLE_NAME
				columnsTableFile.writeBytes(tableName);
				columnsTableFile.writeByte(arr[0].length()); // COLUMN_NAME
				columnsTableFile.writeBytes(arr[0]);
				columnsTableFile.writeInt(ordPos++); // ORDINAL_POSITION
				columnsTableFile.writeByte(arr[1].length()); // COLUMN_TYPE
				columnsTableFile.writeBytes(arr[1]);
				if(str.contains("NOT") || str.contains("not"))
				{
					columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
					columnsTableFile.writeBytes("NO");
				}
				else{
					columnsTableFile.writeByte("YES".length()); // IS_NULLABLE
					columnsTableFile.writeBytes("YES");
				}
				//columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
				//columnsTableFile.writeBytes("NO");
				if(str.contains("PRIMARY") || str.contains("primary")){
					columnsTableFile.writeByte("PRI".length()); // COLUMN_KEY
					columnsTableFile.writeBytes("PRI");
				}
				else{
					columnsTableFile.writeByte("".length()); // COLUMN_KEY
					columnsTableFile.writeBytes("");
				}
				
			}
			tablesTableFile.writeByte(schemaName.length()); // TABLE_SCHEMA
			tablesTableFile.writeBytes(schemaName);
			tablesTableFile.writeByte(tableName.length()); // TABLE_NAME
			tablesTableFile.writeBytes(tableName);
			tablesTableFile.writeLong(0); // TABLE_ROWS
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(userCommand);
		
	}


	private static void useSchema(String userCommand) {
		// TODO Auto-generated method stub
		//System.out.println("inside use schema");
		schemaName = userCommand.substring(4);
		System.out.println(schemaName);
	}


	private static void createSchema(String userCommand) throws IOException {
    	try {
			RandomAccessFile schemataTableFile = new RandomAccessFile("information_schema.schemata.tbl", "rw");
			String name =  userCommand.substring(14);
			System.out.println(name);
			schemataTableFile.seek(schemataTableFile.length());
			schemataTableFile.writeByte(name.length());
			schemataTableFile.writeBytes(name);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private static void showSchemas() {
    	try {
			RandomAccessFile schemataTableFile = new RandomAccessFile("information_schema.schemata.tbl", "rw");
			while(schemataTableFile.getFilePointer()+1 < schemataTableFile.length()){
				byte varcharLength = schemataTableFile.readByte();
				StringBuffer s=new StringBuffer();
				for(int i = 0; i < varcharLength; i++)
						s.append((char)schemataTableFile.readByte());
				System.out.println(s.toString());
			}	
				//System.out.println();
			}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}


//  ===========================================================================
//  STATIC METHOD DEFINTIONS BEGIN HERE
//  ===========================================================================


	/**
	 *  Help: Display supported commands
	 */
	public static void help() {
		System.out.println(line("*",80));
		System.out.println();
		System.out.println("\tversion;       Show the program version.");
		System.out.println("\thelp;          Show this help information");
		System.out.println("\tshow schemas;  Display all schemas.");
		System.out.println("\tuse schema;    Use selected schema");
		System.out.println("\tshow tables;   Show the tables in given schema.");
		System.out.println("\tcreate schema  Creates new schema.");
		System.out.println("\tcreate table   Creates new table.");
		System.out.println("\tinsert into    Insert into the table.");
		System.out.println("\tdrop table     Delete the table.");
		System.out.println("\tselect * from  displays from table.");
		System.out.println("\texit;          Exit the program");
		System.out.println();
		System.out.println();
		System.out.println(line("*",80));
	}
	
	/**
	 *  Display the welcome "splash screen"
	 */
	public static void splashScreen() {
		System.out.println(line("*",80));
        System.out.println("Welcome to DavisBaseLite"); // Display the string.
		version();
		System.out.println("Type \"help;\" to display supported commands.");
		System.out.println(line("*",80));
	}

	/**
	 * @param s The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num times.
	 */
	public static String line(String s,int num) {
		String a = "";
		for(int i=0;i<num;i++) {
			a += s;
		}
		return a;
	}
	
	/**
	 * @param num The number of newlines to be displayed to <b>stdout</b>
	 */
	public static void newline(int num) {
		for(int i=0;i<num;i++) {
			System.out.println();
		}
	}
	
	public static void version() {
		System.out.println("DavisBaseLite v1.0\n");
	}


	/**
	 *  This method reads a binary table file using a hard-coded table schema.
	 *  Your query must be able to read a binary table file using a dynamically 
	 *  constructed table schema from the information_schema
	 */
	public static void displayAllRecords() {
		try {
			/* Open the widget table binary data file */
			RandomAccessFile widgetTableFile = new RandomAccessFile(widgetTableFileName, "rw");

			/*
			 *  Navigate throught the binary data file, displaying each widget record
			 *  in the order that it physically appears in the file. Convert binary data
			 *  to appropriate data types for each field.
			 */
			for(int record = 0;record < 5; record++) {
				System.out.print(widgetTableFile.readInt());
				System.out.print("\t");
				byte varcharLength = widgetTableFile.readByte();
				for(int i = 0; i < varcharLength; i++)
					System.out.print((char)widgetTableFile.readByte());
				System.out.print("\t");
				System.out.print(widgetTableFile.readShort());
				System.out.print("\t");
				System.out.println(widgetTableFile.readFloat());
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}

	public static void displayRecordID(int id) {
		try {
			int indexFileLocation = 0;
			long indexOfRecord = 0;
			boolean recordFound = false;

			RandomAccessFile widgetTableFile = new RandomAccessFile(widgetTableFileName, "rw");
			RandomAccessFile tableIdIndex = new RandomAccessFile(tableIdIndexName, "rw");

			/*
			 *  Use exhaustive brute force seach over the binary index file to locate
			 *  the requested ID values. Then use its assoicated address to seek the 
			 *  record in the widget table binary data file.
			 *
			 *  You may instead want to load the binary index file into a HashMap
			 *  or similar key:value data structure for efficient index-address lookup,
			 *  but this is not required.
			 */
			while(!recordFound) {
				tableIdIndex.seek(indexFileLocation);
				if(tableIdIndex.readInt() == id) {
					tableIdIndex.seek(indexFileLocation+4);
					indexOfRecord = tableIdIndex.readLong();
					recordFound = true;
				}
				/* 
				 *  Each index entry uses 12 bytes: ID=4-bytes + address=8-bytes
				 *  Move ahead 12 bytes in the index file for each while() loop
				 *  iteration to increment through index entries.
				 * 
				 */
				indexFileLocation += 12;
			}

			widgetTableFile.seek(indexOfRecord);
			System.out.print(widgetTableFile.readInt());
			System.out.print("\t");
			byte varcharLength = widgetTableFile.readByte();
			for(int i = 0; i < varcharLength; i++)
				System.out.print((char)widgetTableFile.readByte());
			System.out.print("\t");
			System.out.print(widgetTableFile.readShort());
			System.out.print("\t");
			System.out.println(widgetTableFile.readFloat());
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}

	/**
	 *  This method is hard-coded to create a binary table file with 5 records
	 *  It also creates an index file for the ID field
	 *  It is based on the following table schema:
	 *  
	 *  CREATE TABLE table (
	 *      id unsigned int primary key,
	 *      name varchar(25),
	 *      quantity unsigned short,
	 *      probability float
	 *  );
	 */
	public static void hardCodedCreateTableWithIndex() {
		long recordPointer;
		try {
			RandomAccessFile widgetTableFile = new RandomAccessFile(widgetTableFileName, "rw");
			RandomAccessFile tableIdIndex = new RandomAccessFile(tableIdIndexName, "rw");
			
			id = 1;
			name = "alpha";
			quantity = 847;
			probability = 0.341f;
			
			tableIdIndex.writeInt(id);
			tableIdIndex.writeLong(widgetTableFile.getFilePointer());
			widgetTableFile.writeInt(id);
			widgetTableFile.writeByte(name.length());
			widgetTableFile.writeBytes(name);
			widgetTableFile.writeShort(quantity);
			widgetTableFile.writeFloat(probability);
			
			id = 2;
			name = "beta";
			quantity = 1472;
			probability = 0.89f;
			
			tableIdIndex.writeInt(id);
			tableIdIndex.writeLong(widgetTableFile.getFilePointer());
			widgetTableFile.writeInt(id);
			widgetTableFile.writeByte(name.length());
			widgetTableFile.writeBytes(name);
			widgetTableFile.writeShort(quantity);
			widgetTableFile.writeFloat(probability);

			id = 3;
			name = "gamma";
			quantity = 41;
			probability = 0.5f;
			
			tableIdIndex.writeInt(id);
			tableIdIndex.writeLong(widgetTableFile.getFilePointer());
			widgetTableFile.writeInt(id);
			widgetTableFile.writeByte(name.length());
			widgetTableFile.writeBytes(name);
			widgetTableFile.writeShort(quantity);
			widgetTableFile.writeFloat(probability);

			id = 4;
			name = "delta";
			quantity = 4911;
			probability = 0.4142f;
			
			tableIdIndex.writeInt(id);
			tableIdIndex.writeLong(widgetTableFile.getFilePointer());
			widgetTableFile.writeInt(id);
			widgetTableFile.writeByte(name.length());
			widgetTableFile.writeBytes(name);
			widgetTableFile.writeShort(quantity);
			widgetTableFile.writeFloat(probability);

			id = 5;
			name = "epsilon";
			quantity = 6823;
			probability = 0.618f;
			
			tableIdIndex.writeInt(id);
			tableIdIndex.writeLong(widgetTableFile.getFilePointer());
			widgetTableFile.writeInt(id);
			widgetTableFile.writeByte(name.length());
			widgetTableFile.writeBytes(name);
			widgetTableFile.writeShort(quantity);
			widgetTableFile.writeFloat(probability);
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	/* initial database values loaded at starting*/
	public static void loadInitialData(){

		try {
			/* FIXME: Put all binary data files in a separate subdirectory (subdirectory tree?) */
			/* FIXME: Should there not be separate Class static variables for the file names? 
			 *        and just hard code them here?
			 */
			/* TODO: Should there be separate methods to checkfor and subsequently create each file 
			 *       granularly, instead of a big bang all or nothing? 
			 */
			RandomAccessFile schemataTableFile = new RandomAccessFile("information_schema.schemata.tbl", "rw");
			RandomAccessFile tablesTableFile = new RandomAccessFile("information_schema.tables.tbl", "rw");
			RandomAccessFile columnsTableFile = new RandomAccessFile("information_schema.columns.tbl", "rw");

			/*	
			 *  Create the SCHEMATA table file.
			 *  Initially it has only one entry:
			 *      information_schema
			 */
			// ROW 1: information_schema.schemata.tbl
			schemataTableFile.writeByte("information_schema".length());
			schemataTableFile.writeBytes("information_schema");

			/*
			 *  Create the TABLES table file.
			 *  Remember!!! Column names are not stored in the tables themselves
			 *              The column names (TABLE_SCHEMA, TABLE_NAME, TABLE_ROWS)
			 *              and their order (ORDINAL_POSITION) are encoded in the
			 *              COLUMNS table.
			 *  Initially it has three rows (each row may have a different length):
			 */
			// ROW 1: information_schema.tables.tbl
			tablesTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			tablesTableFile.writeBytes("information_schema");
			tablesTableFile.writeByte("SCHEMATA".length()); // TABLE_NAME
			tablesTableFile.writeBytes("SCHEMATA");
			tablesTableFile.writeLong(1); // TABLE_ROWS

			// ROW 2: information_schema.tables.tbl
			tablesTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			tablesTableFile.writeBytes("information_schema");
			tablesTableFile.writeByte("TABLES".length()); // TABLE_NAME
			tablesTableFile.writeBytes("TABLES");
			tablesTableFile.writeLong(3); // TABLE_ROWS

			// ROW 3: information_schema.tables.tbl
			tablesTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			tablesTableFile.writeBytes("information_schema");
			tablesTableFile.writeByte("COLUMNS".length()); // TABLE_NAME
			tablesTableFile.writeBytes("COLUMNS");
			tablesTableFile.writeLong(7); // TABLE_ROWS

			/*
			 *  Create the COLUMNS table file.
			 *  Initially it has 11 rows:
			 */
			// ROW 1: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("SCHEMATA".length()); // TABLE_NAME
			columnsTableFile.writeBytes("SCHEMATA");
			columnsTableFile.writeByte("SCHEMA_NAME".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("SCHEMA_NAME");
			columnsTableFile.writeInt(1); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(64)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 2: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("TABLES".length()); // TABLE_NAME
			columnsTableFile.writeBytes("TABLES");
			columnsTableFile.writeByte("TABLE_SCHEMA".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("TABLE_SCHEMA");
			columnsTableFile.writeInt(1); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(64)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 3: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("TABLES".length()); // TABLE_NAME
			columnsTableFile.writeBytes("TABLES");
			columnsTableFile.writeByte("TABLE_NAME".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("TABLE_NAME");
			columnsTableFile.writeInt(2); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(64)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 4: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("TABLES".length()); // TABLE_NAME
			columnsTableFile.writeBytes("TABLES");
			columnsTableFile.writeByte("TABLE_ROWS".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("TABLE_ROWS");
			columnsTableFile.writeInt(3); // ORDINAL_POSITION
			columnsTableFile.writeByte("long int".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("long int");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 5: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length()); // TABLE_NAME
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("TABLE_SCHEMA".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("TABLE_SCHEMA");
			columnsTableFile.writeInt(1); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(64)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 6: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length()); // TABLE_NAME
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("TABLE_NAME".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("TABLE_NAME");
			columnsTableFile.writeInt(2); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(64)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 7: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length()); // TABLE_NAME
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("COLUMN_NAME".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("COLUMN_NAME");
			columnsTableFile.writeInt(3); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(64)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 8: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length()); // TABLE_NAME
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("ORDINAL_POSITION".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("ORDINAL_POSITION");
			columnsTableFile.writeInt(4); // ORDINAL_POSITION
			columnsTableFile.writeByte("int".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("int");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 9: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length()); // TABLE_NAME
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("COLUMN_TYPE".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("COLUMN_TYPE");
			columnsTableFile.writeInt(5); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(64)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(64)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 10: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length()); // TABLE_NAME
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("IS_NULLABLE".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("IS_NULLABLE");
			columnsTableFile.writeInt(6); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(3)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(3)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

			// ROW 11: information_schema.columns.tbl
			columnsTableFile.writeByte("information_schema".length()); // TABLE_SCHEMA
			columnsTableFile.writeBytes("information_schema");
			columnsTableFile.writeByte("COLUMNS".length()); // TABLE_NAME
			columnsTableFile.writeBytes("COLUMNS");
			columnsTableFile.writeByte("COLUMN_KEY".length()); // COLUMN_NAME
			columnsTableFile.writeBytes("COLUMN_KEY");
			columnsTableFile.writeInt(7); // ORDINAL_POSITION
			columnsTableFile.writeByte("varchar(3)".length()); // COLUMN_TYPE
			columnsTableFile.writeBytes("varchar(3)");
			columnsTableFile.writeByte("NO".length()); // IS_NULLABLE
			columnsTableFile.writeBytes("NO");
			columnsTableFile.writeByte("".length()); // COLUMN_KEY
			columnsTableFile.writeBytes("");

		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
}



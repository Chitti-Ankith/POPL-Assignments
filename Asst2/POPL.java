import java.io.*;
import java.util.*;

public class POPL {
	ArrayList<String[]> nE = new ArrayList<>();	// nE is for name equivalence arraylist
	static ArrayList<String[]> iNE = new ArrayList<>(); //iNE is for internal name equivalence arraylist
	LinkedHashMap<String, Integer> vTol = new LinkedHashMap<>(); // for index in sEM
	boolean[][] sEM, sEM2;	
	// name equivalences like [a,b,c] or [aa,bb]
	LinkedHashMap<String,String> vars = new LinkedHashMap<>();
	//to store variable and there type
	LinkedHashMap<String,String> str = new LinkedHashMap<>();
	//to store name and struct definition
	
	// CONSTRUCTOR
	public POPL(String inputFile) throws IOException {
		readFile(inputFile);
		findSE();
	}
	/*
	 * to get variable types like int *a gives (a,ptr_int)
	 */
	private LinkedHashMap<String,String> getVType(boolean findINE, String s) {
		LinkedHashMap<String,String> varsTemp = new LinkedHashMap<>();
		int space;
		String kind;
		if(s.startsWith("struct"))
			space = s.indexOf( " " , s.indexOf(" ")+1 );
		else
			space = s.indexOf(" ");
		
		int star = s.indexOf("*"); // star will be -1 if * is not found
		if(star==-1 || star > space) { 
			// s = int a; or int *a; or struct xyz *a;
			kind = s.substring(0, space);
			s = s.substring(space+1);
		}else {  
			// s = int*a; and struct xyz* a;
			kind = s.substring(0, star);
			s = s.substring(star);
		}
		
		/*
		 * checks internal name equivalence
		 * if x ,y are of int * then ptr_int (x,y) 
		 */
		LinkedHashMap<String,ArrayList<String>> iNEArrayPtr = new LinkedHashMap<>();
		s = s.replace(" ", "");
		s = s.replace(";","");
		String[] var = s.split(",");
		for(String t : var) {
			String mapVal = "";
			if(t.contains("[")) { 
				mapVal+="array:";
				String dims = t.substring(t.indexOf("["),t.lastIndexOf("]")+1);
				for(String d : dims.replace("[","").split("]")) {
					mapVal+=d+"_";
				}
				mapVal = mapVal.substring(0,mapVal.length()-1)+":";
				t = t.substring(0,t.indexOf("[")); 
			}
			
			if(t.contains("*")) {
				String dup = new String(t);
				int ptrs = dup.length() - dup.replace("*", "").length();
				for(int i = 0; i < ptrs; i++)	mapVal+="ptr_";
				t = t.substring(t.lastIndexOf("*")+1);
			}
			
			mapVal += kind;
			varsTemp.put(t, mapVal);
			
			if(mapVal.startsWith("ptr") || mapVal.startsWith("array")) {
				if(!iNEArrayPtr.containsKey(mapVal))
					iNEArrayPtr.put(mapVal, new ArrayList<>());
				iNEArrayPtr.get(mapVal).add(t);
			}
		}
		
		// if(findINE)
		// 	for(ArrayList<String> INEArrays : iNEArrayPtr.values()) {
		// 		if(INEArrays.size()==1)	continue;
		// 		iNE.add( (String[])INEArrays.toArray(new String[INEArrays.size()]) );
		// 	}
		
		return varsTemp;
	}
	
	/*
	 * s = struct definition
	 */
	private void strDef(String s) {
		LinkedHashMap<String, String> varsTemp;

		String strName = s.substring(0,s.indexOf("{")).trim(), strType="";
		s = s.substring(s.indexOf("{")+1,s.indexOf("}")).trim();
		
		String[] arg = s.split(";");
		for(String argDup : arg) {
			varsTemp = getVType(false,argDup);
			// System.out.println(varsTemp.keySet());
			for(String t : varsTemp.keySet()) {
				strType+=varsTemp.get(t)+"!";
			}
		}
		strType = strType.substring(0,strType.length()-1);
		// System.out.println(strType);
		str.put(strName, strType);
		// System.out.println(strName);
	}
	
	/*
	 * s = func definition
	 */
	private void funType(String s) {
		LinkedHashMap<String, String> varsTemp;
		s = s.replace(";","");
		String returnType="", funName="";
		
		varsTemp = getVType(false,s.substring(0,s.indexOf("(")));
		if(varsTemp.size()!=1) System.err.println("Something went wrong");
		for(String t : varsTemp.keySet()) {
			funName = t;
			returnType = varsTemp.get(t);
		}

		String[] arg = s.substring(s.indexOf("(")+1,s.indexOf(")")).split(",");
		
		String funType = "func|"+returnType+"|";
		for(int i = 0; i < arg.length; i++) {
			varsTemp = getVType(false,arg[i]);
			if(varsTemp.size()!=1)	System.err.println("Something went wrong");
			for(String t : varsTemp.keySet()) {
				funType+=varsTemp.get(t)+"!";
			}
		}
		if(arg.length>0)	funType = funType.substring(0, funType.length()-1); // to remove last !
		vars.put(funName, funType);
	}
	/*
		For internal name equivalence
	*/

	static void iNeq(String inputFile) throws IOException {
		ArrayList<String[]> vars = new ArrayList<>();	
		FileReader fr = new FileReader(inputFile);
		BufferedReader br = new BufferedReader(fr);

		String s;int j=0;
		while((s = br.readLine())!=null) {
				//processing to remove extra spaces
				s = s.trim();
				s = s.replaceAll(" +", " "); 
				// s = s.replace("*","");
				s = s.replace(", ", ",");
				s = s.replace(" ,",",");
				s = s.replace(") ", ")");
				s = s.replace("( ", "(");
				s = s.replace(" (","(");
				s = s.replace(" )",")");
				s = s.replace("; ", ";");
				s = s.replace(" ;",";"); 
				s = s.trim();
				if(s.length()==0)	continue;
				if(s.contains("struct") && (s.contains("{") || !s.contains(";"))) { 
				// for structs
				if(!s.contains("}")) {
					do {
						s = br.readLine();
					}while(!s.contains("}"));
				}
				
				
			}


				if(s.contains("(") &&s.contains("{"))
				{
					if(!s.contains("}")) {
						do {
							s = br.readLine();
						}while(!s.contains("}"));
					}
					else
					{
						continue;
					}
				}
				if(s.contains("(")&&s.contains(")"))
					continue;

				
				int flag1= 0 ;int flag2 = 0;		//For strcuture variables
				if(s.contains("struct")) 
				{flag1=1; flag2 =0;}
				int flag3=0;				//For pointers
				if (s.contains("*"))
					{flag3=1;}


				String k = new String();
				for(int i=0;i<s.length();i++)
				{	
					if(flag1 ==1)
					{
						if(s.charAt(i)!=' ' && flag2<2)
							{ continue;}
						else if(flag2>=2)
						{
							k=s.substring(i,s.length()-1);
							// System.out.println(t);
							String[] var = k.split(",");
							iNE.add(var);
							// System.out.println(var);
							break;
						}
						else
						{
							flag2++;continue;
						}

					}
					else if(flag3==1) {
						if(s.charAt(i)!=' ')
							continue;
						else
						{
							i++;
							k=s.substring(i,s.length()-1);
							// System.out.println(t);
							k.replace("*","");
							// System.out.println(k + " k is here");
							String[] var = k.split(",");
							ArrayList<String> var3 = new ArrayList<>();
							for(String t : var)
							{	
								// System.out.println(t);
								var3.add(t.substring(1,t.length()));
							}
							String[] var4= new String[var3.size()];
							var4=var3.toArray(var4);
							iNE.add(var4);
							// System.out.println(var);
							break;
						}


					}


					else {
						if(s.charAt(i)!=' ')
							continue;
						else
						{
							// i++;
							// k=s.substring(i,s.length()-1);
							// // System.out.println(t);
							// String[] var = k.split(",");
							// iNE.add(var);
							// // System.out.println(var);
							// break;
						}
					}
					 
				}
				
			}
		}


	/*
	 * hoping file is syntactically correct
	 */
	private void readFile(String inputFile) throws IOException {
		FileReader fr = new FileReader(inputFile);
		BufferedReader br = new BufferedReader(fr);
		
		
		iNeq(inputFile);
		String s;
		while((s = br.readLine())!=null) {
			//processing to remove extra spaces
			s = s.trim();
			s = s.replaceAll(" +", " "); 
			s = s.replace(", ", ",");
			s = s.replace(" ,",",");
			s = s.replace(") ", ")");
			s = s.replace("( ", "(");
			s = s.replace(" (","(");
			s = s.replace(" )",")");
			s = s.replace("; ", ";");
			s = s.replace(" ;",";"); 
			s = s.trim();
			if(s.length()==0)	continue;
			
			if(s.contains("(")&&!s.contains("int")&&!s.contains("char")&&!s.contains("float")&&!s.contains("double")&&!s.contains("char")&&!s.contains("string")&&!s.contains("[]")&&!s.contains("*"))
			{
				continue;
			}
			else if(s.contains("(")) { 
				// for function types
				if(!s.contains("}")) {
					do {
						String next = br.readLine().trim();
						s+=next;
					}while(!s.contains("}"));
				}
				funType(s);
			}
			else if(s.contains("struct") && (s.contains("{") || !s.contains(";"))) { 
				// for structs
				if(!s.contains("}")) {
					do {
						String next = br.readLine().trim();
						s+=next;
					}while(!s.contains("}"));
				}
				// System.out.println(s);
				strDef(s);
			}else {
				// for variables
				LinkedHashMap<String,String> varsTemp = getVType(true,s);
				for(String v : varsTemp.keySet())	vars.put(v, varsTemp.get(v));
			}
		}
		
		// stores basic types
		HashSet<String> type = new HashSet<>(); 
		for(String t : vars.values()) {
			if(type.contains(t))	continue;
			if(t.startsWith("array") || t.startsWith("func") || t.startsWith("ptr")|| t.startsWith("struct"))	continue;
			type.add(t);
		}
		for(String t : type) {
			ArrayList<String> al = new ArrayList<>();
			for(String key : vars.keySet()) {
				if(vars.get(key).equals(t))	al.add(key);
			}
			String[] arr = (String[]) al.toArray(new String[al.size()]);
			if(arr.length == 1)	continue;
			nE.add(arr);
			iNE.add(arr);
		}
		
	}
	
	/*
	 * to find structural equivalence
	 * first find equivalent structs and replace with common struct
	 * and then do structural equivalence matrix.
	 */
	private void findSE(){
		

		LinkedHashMap<Integer,String> integerToType = new LinkedHashMap<>();
		LinkedHashMap<String,Integer> typeToInteger = new LinkedHashMap<>();
		int countOfStr = 0;
		for(String t : str.keySet()) {
			typeToInteger.put(t, countOfStr);
			integerToType.put(countOfStr, t);
			countOfStr++;
		}
		
		sEM2 = new boolean[countOfStr][countOfStr];
		for(int i = 0; i < countOfStr; i++)	Arrays.fill(sEM2[i], true);
		boolean diff = true;
		while(diff) {
			diff = false;
			for (int i = 0; i < countOfStr; i++) {
				for (int j = i+1; j < countOfStr; j++) {
					if(sEM2[i][j] == false)	continue;
					String s1 = str.get(integerToType.get(i));
					String s2 = str.get(integerToType.get(j));
					String[] first = s1.split("!");
					String[] second = s2.split("!");
					if(first.length != second.length) {
						diff = true;
						sEM2[i][j] = false;
						continue;
					}
					for(int k = 0; k < first.length; k++) {
						String e = first[k];
						String f = second[k];
						if(e.equals(f))	continue;
						if(e.contains("struct") && f.contains("struct")) {
								
							int idx1 = e.indexOf("struct"), idx2 = f.indexOf("struct");
							if(!e.substring(0, idx1).equals(f.substring(0, idx2))) { 
								sEM2[i][j] = false;
								diff = true;
								break;
							}else { 
								e = e.substring(idx1); 
								f = f.substring(idx2);
								if(sEM2[typeToInteger.get(e)][typeToInteger.get(f)] == false) {
									sEM2[i][j] = false;
									diff = true;
									break;
								}
							}
						}else {
							diff = true;
							sEM2[i][j] = false;
							break;
						}
					}
				}
			}
		}
		for(int i = 0; i < countOfStr; i++)
			for(int j = 0; j < i; j++)	sEM2[i][j] = sEM2[j][i];
		
		boolean[] point = new boolean[countOfStr];
		for(int i = 0; i < countOfStr; i++) {
			if(point[i])	continue;
			point[i] = true;
			String z1 = integerToType.get(i); 
			for(int j = i+1; j < countOfStr; j++) {
				if(point[j])	continue;
				if(sEM2[i][j]) {
					point[j] = true;
					String z2 = integerToType.get(j);
					// System.out.println(vars.keySet());
					for(String v : vars.keySet()) {
						// System.out.println(vars.get(v));
						vars.put(v,vars.get(v));
						// System.out.println(vars.get(v));

					}
				}
			}
		}
		
		LinkedHashMap<Integer,String> IntegerToVariable = new LinkedHashMap<>();
		int CountofVars = 0;	
		for(String kind : vars.values())	IntegerToVariable.put(CountofVars++, kind);
		
		CountofVars = 0;	
		for(String t : vars.keySet())	
			vToI.put(t,CountofVars++);
		
		sEM = new boolean[CountofVars][CountofVars];
		for(int i = 0; i < CountofVars; i++)	
			Arrays.fill(sEM[i],true);
		
		for(int i = 0; i < CountofVars; i++) {
			for(int j = i+1; j < CountofVars; j++) {
				sEM[i][j] = IntegerToVariable.get(i).equals(IntegerToVariable.get(j));
				sEM[j][i] = sEM[i][j];
			}
		}
	}
	
	public void output() {
		LinkedHashMap<Integer,String> varNames=new LinkedHashMap<Integer,String>();
		System.out.println("str:");
		int countOfStr = 0;
		for(String t : str.keySet()) {
			System.out.printf("%-3d: %-20s --> %s\n",countOfStr++, t, str.get(t));
		}
		System.out.print("   |");
		for(int i = 0; i < countOfStr; i++)		
			System.out.printf("%-3d|",i);
		System.out.println();
		for(int i = 0; i < countOfStr; i++) {
			System.out.printf("%-3d|",i);	
			for (int j = 0; j <countOfStr; j++)
			{
				if(i>j)
				{
					System.out.print("    ");
					continue;
				}
				System.out.print(sEM2[i][j]?" Y |":" N |");
			}		
				
			System.out.println();
		}
		
		System.out.println("\nName Equivalence:");
		for(String[] arr : nE) {
			System.out.println(Arrays.toString(arr));
		}
		
		System.out.println("\nInternal Name Equivalence:");
		for(String[] arr : iNE) {
			if(arr.length>1)
			System.out.println(Arrays.toString(arr));
		}
		
		System.out.println("\nVariables:");
		int CountofVars = 0;
		for(String t : vars.keySet()) {
			varNames.put(CountofVars,t);
			System.out.printf("%-3d: %-20s --> %s\n",CountofVars++, t, vars.get(t));
		}
		
		System.out.println("\nStructural Equivalence Matrix:");
		System.out.print("   |");
		for(int i = 0; i < CountofVars; i++)		
			System.out.printf("%s|",varNames.get(i));
		System.out.println();
		for(int i = 0; i < CountofVars; i++) {
			System.out.printf("%s|",varNames.get(i));	
			for (int j = 0; j < CountofVars; j++)	
			{
				if(i>j)
				{
					System.out.print("    ");
					continue;
				}
				System.out.print(sEM[i][j]?" Y |":" N |");
			}	
				
			System.out.println();
		}
		
	}
	

/**************************************			CODE FOR PART : 2		*********************************************/

void checkEq(String v1, String v2) { 
		System.out.println(v1);
		System.out.println(v2);
		
		String t1 = (vars.get(v1)); // to get types of variables
		System.out.println(v1+"-"+t1);
		String t2 = (vars.get(v2)); // to get types of variables
		System.out.println(v2+"-"+t2);
		if(!t1.equals(t2)) 	//types not equal then FALSE
			System.out.println("False as "+v1+" and "+v2+" "+ "are Not Name Equivalent");
		else
		{
			System.out.println("True as "+v1+" and "+v2+" "+ "are Name Equivalent");
		}
			// if(t1.equals("struct")) {
			// 	// checks structural equivalence
		System.out.println(sEM[vToI.get(v1)][vToI.get(v2)]?"True as "+v1+" and "+v2+" "+ "are Structurally Equivalent":"False as "+v1+" and "+v2+" "+ "are not Structurally Equivalent");
			// }else {
				// checks name equivalence
				// for(String[] nameEq : nE) {
				// 	boolean found1 = false, found2 = false;
				// 	for(int i = 0; i < nameEq.length; i++) {
				// 		if(nameEq[i].equals(v1))
				// 			found1 = true;
				// 		else if(nameEq[i].equals(v2))
				// 			found2 = true;
				// 	}
				// 	if(found1 || found2) {
				// 		System.out.println(found1 && found2 ? "TRUE" : "FALSE");
				// 		return;
				// 	}
				// }
				// System.out.println("FALSE");
			// }
		}
	
	public static void main(String[] args) throws IOException {
		POPL tc = new POPL("input2.txt");
		tc.output();
		// tc.checkEq("aa","cc");
	}
}

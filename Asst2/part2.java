import java.io.*;
import java.util.*;

public class part2
{
	public static void main(String[] args) throws IOException
	{
		Scanner sc= new Scanner(System.in);
		String line=sc.nextLine();
		// codeObj f1=new codeObj("input1.txt");
		line=line.trim();
		System.out.println(line);
		String[] vars=line.split(",");
		POPL f=new POPL("input3.txt");
		f.checkEq(vars[0], vars[1]);
		}
}
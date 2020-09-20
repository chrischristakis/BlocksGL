package chrischristakis;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileUtils 
{
	
private FileUtils() {}
	
	public static String readFileAsString(String path)
	{
		String result = "";
		try
		{
			String next;
			BufferedReader reader = new BufferedReader(new FileReader(path));
			while((next = reader.readLine()) != null)
				result += next + "\n";
			reader.close();
		}
		catch (IOException e) {e.printStackTrace();}
		return result;
	}

}

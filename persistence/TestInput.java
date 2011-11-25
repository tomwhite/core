import java.beans.*;
import java.io.*;

public class TestInput { 
    public TestInput(InputStream is) { 
        XMLDecoder d = new XMLDecoder(is); 
	System.out.println("Read: " + d.readObject()); 
	d.close();
    }

    public static void main(String[] args) throws IOException { 
    	String fileName = (args.length == 0) ? "TestInput.xml" : args[0]; 
        InputStream is = new BufferedInputStream(
	                     new FileInputStream(fileName)); 
        new TestInput(is); 
    }
}
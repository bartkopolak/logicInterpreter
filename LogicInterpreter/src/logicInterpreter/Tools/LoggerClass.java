package logicInterpreter.Tools;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerClass {
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				 "%5$s");
	}
	public static final Logger LOGGER = Logger.getLogger("log:"+System.currentTimeMillis());
	static {
		FileHandler fh;
		try {
			File f = new File("log/"+String.valueOf(System.currentTimeMillis())+".log");
			fh = new FileHandler(f.getAbsolutePath());
			Formatter formatter = new SimpleFormatter();  
			 
			fh.setFormatter(formatter);
			LOGGER.addHandler(fh);
			
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

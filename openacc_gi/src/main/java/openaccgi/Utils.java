package openaccgi;

import java.io.*;
import java.util.Optional;

public class Utils {
	public static void appendToFile(File file, String toAppend) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		out.println(toAppend);
		out.close();
	}

	public static void appendToFile(Optional<File> file, String toAppend) throws IOException {
		if(file.isPresent()){
			appendToFile(file.get(), toAppend);
		}
	}
}

package openaccgi.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EvaluationExecutables {
	private static final String bashScript = "#!/bin/bash\n" +
											 "echo $(( $RANDOM%1000 ))";
											
	
	public static final File getBashScript(){
		try {
			File bashFile = File.createTempFile("bashScript", ".tmp");
			BufferedWriter bw = new BufferedWriter(new FileWriter(bashFile));
			bw.write(EvaluationExecutables.bashScript);
			bw.close();
			bashFile.setExecutable(true);
			return bashFile;
		} catch (IOException e) { // No good reason this should happen
			e.printStackTrace();
			System.exit(1);
		}
		
		assert(false); //Should never be reached
		return null;
	}
}

package openaccgi.application;

import org.apache.commons.cli.ParseException;
import org.junit.Test;
import openaccgi.utils.EvaluationExecutables;
import openaccgi.utils.InputFileGenerator;

import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApplicationTest {

	@Test //Mostly run to check no exceptions are thrown. Does not verify output
	public void testRun_evolutionary() throws IOException, AccessControlException, ParseException {
		File script = EvaluationExecutables.getBashScript();
		File csvInput = InputFileGenerator.getRealF1();
		File logDirectory = File.createTempFile("openacc_gi_log_", "");
		logDirectory.delete();
		logDirectory.mkdir();
		
		String input = "-f " + csvInput.getAbsolutePath() + " -x" + script.getAbsolutePath()
			+ " -e 100 -s 100 -lCPP -i -L" + logDirectory.getAbsolutePath()
			+ ApplicationCommandLineParserTest.TOOL_ARGUMENTS;
		
		String[] inputSplit = input.split("\\s+");
		Application.main(inputSplit);

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "original_fitness.dat").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "original_fitness.dat").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "log.dat").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "log.dat").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "original_fitness.dat").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "original_fitness.dat").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "loop_selection_fitness.dat").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "loop_selection_fitness.dat").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "loop_selection.patch").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "loop_selection.patch").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "data_optimiser_fitness.dat").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "data_optimiser_fitness.dat").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "data_optimiser.patch").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "data_optimiser.patch").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "directive_pruning_fitness.dat").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "directive_pruning_fitness.dat").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "directive_pruning.patch").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "directive_pruning.patch").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "final.patch").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "final.patch").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "final_fitness.dat").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "final_fitness.dat").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "total_evaluations.dat").exists());
		assertFalse(new File(logDirectory.getAbsolutePath() + File.separator + "total_evaluations.dat").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "loop_selection").exists());
		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "loop_selection").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "data_optimiser").exists());
		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "data_optimiser").isDirectory());

		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "directive_pruning").exists());
		assertTrue(new File(logDirectory.getAbsolutePath() + File.separator + "directive_pruning").isDirectory());
	}
}

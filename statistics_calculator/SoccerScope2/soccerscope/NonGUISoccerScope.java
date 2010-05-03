package soccerscope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import soccerscope.file.LogFileReader;
import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.SceneSetMaker;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.util.GameAnalyzer;
import soccerscope.util.analyze.SceneAnalyzer;
import soccerscope.util.analyze.Xmling;

import com.jamesmurty.utils.XMLBuilder;

public class NonGUISoccerScope {

	public static void run(final String filename, final String xmlFilename)
	throws Exception {
		// initiate the world model...
		final WorldModel wm = WorldModel.getInstance();
		wm.clear(); // don't think this is necessary... but ok...
		final File file = new File(filename);
		if (file == null || !file.canRead()) {
			throw new FileNotFoundException(String.format(
					"invalid file '%s' or permissions...\n", filename));
		}
		// open the log file and analyze it (by doing SceneSet.analyze())
		NonGUISoccerScope.openAndAnalyzeLogFile(wm.getSceneSet(), file
				.getPath());
		System.out.println("final calculations and xml output:");
		NonGUISoccerScope.printXML(wm.getSceneSet(), xmlFilename);
		System.out.println("DONE!");
	}

	private static void openAndAnalyzeLogFile(final SceneSet sceneSet, final String filename)
	throws IOException, InterruptedException {
		final LogFileReader lfr = new LogFileReader(filename);
		final SceneSetMaker ssm = new SceneSetMaker(lfr, sceneSet);
		ssm.run();
	}

	private static void printXML(final SceneSet sceneSet, final String xmlFilename)
	throws Exception {
		final XMLBuilder builder = XMLBuilder.create("analysis");
		builder.attr("version", "1.02");

		final Scene lscene = WorldModel.getInstance().getSceneSet().lastScene();

		final XMLBuilder left = builder.elem("leftteam").attr("name",
				lscene.left.name);
		int[] plindex = Team.firstAndLastPlayerIndexes(Team.LEFT_SIDE);
		for (int iter = plindex[0]; iter < plindex[1]; iter++) {
			final Player p = lscene.player[iter];
			left.elem("player").attr("unum", String.valueOf(p.unum)).attr(
					"viewQuality", p.viewStr()).attr("type", p.typeStr());
		}

		final XMLBuilder right = builder.elem("rightteam").attr("name",
				lscene.right.name);
		plindex = Team.firstAndLastPlayerIndexes(Team.RIGHT_SIDE);
		for (int iter = plindex[0]; iter < plindex[1]; iter++) {
			final Player p = lscene.player[iter];
			right.elem("player").attr("unum", String.valueOf(p.unum)).attr(
					"viewQuality", p.viewStr()).attr("type", p.typeStr());
		}

		for (final SceneAnalyzer analyzer : GameAnalyzer.analyzerList) {
			// all the scene analyzers that can output to XML will do it...
			if (Xmling.class.isInstance(analyzer)) {
				((Xmling) analyzer).xmlElement(builder);
			}
		}

		final Properties outputProperties = new Properties();
		// Explicitly identify the output as an XML document
		outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");

		// Pretty-print the XML output (doesn't work in all cases)
		outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");

		// Get 2-space indenting when using the Apache transformer
		outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");

		// output file...
		final PrintWriter out = new PrintWriter(new File(xmlFilename));

		builder.toWriter(out, outputProperties);
		out.flush();
	}

}

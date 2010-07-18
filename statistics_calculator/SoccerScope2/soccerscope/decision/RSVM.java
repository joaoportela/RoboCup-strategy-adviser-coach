package soccerscope.decision;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

import soccerscope.util.analyze.AttackAnalyzer.AttackType;

class TextConsole implements RMainLoopCallbacks {
	public void rWriteConsole(Rengine re, String text, int oType) {
		System.out.print(text);
	}

	public void rBusy(Rengine re, int which) {
		System.out.println("rBusy(" + which + ")");
	}

	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		System.out.print(prompt);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String s = br.readLine();
			return (s == null || s.length() == 0) ? s : s + "\n";
		} catch (Exception e) {
			System.out.println("jriReadConsole exception: " + e.getMessage());
		}
		return null;
	}

	public void rShowMessage(Rengine re, String message) {
		System.out.println("rShowMessage \"" + message + "\"");
	}

	public String rChooseFile(Rengine re, int newFile) {
		FileDialog fd = new FileDialog(new Frame(),
				(newFile == 0) ? "Select a file" : "Select a new file",
						(newFile == 0) ? FileDialog.LOAD : FileDialog.SAVE);
		fd.setVisible(true);
		String res = null;
		if (fd.getDirectory() != null) {
			res = fd.getDirectory();
		}
		if (fd.getFile() != null) {
			res = (res == null) ? fd.getFile() : (res + fd.getFile());
		}
		return res;
	}

	public void rFlushConsole(Rengine re) {
	}

	public void rLoadHistory(Rengine re, String filename) {
	}

	public void rSaveHistory(Rengine re, String filename) {
	}
}

public class RSVM {
	Rengine re;

	public RSVM() {
		this.re=null;
	}

	public void init(String trainingfile) {
		assert this.re==null;
		// just making sure we have the right version of everything
		if (!Rengine.versionCheck()) {
			System.err
			.println("** Version mismatch - Java files don't match library version.");
			throw new RuntimeException("Rengine versions mismatch");
		}
		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop at first, we'll start it later
		// (that's the "false" as second argument)
		// 3) the callbacks are implemented by the TextConsole class above
		String args[] = new String[] { "--no-restore", "--silent", "--no-save" };
		this.re = new Rengine(args, false, new TextConsole());
		// the engine creates R is a new thread, so we should wait until it's
		// ready
		if (!this.re.waitForR()) {
			System.out.println("Cannot load R");
			return;
		}

		// calculate svm model
		ArrayList<String> initlines = new ArrayList<String>(8);
		initlines.add("cnames<-c('passmisses','goals','outsides','goalkicks','passmisses_offside','fast_attacks','all_attacks','leftwing_1stquarter_possession','leftwing_2ndquarter_possession','middlewing_1stquarter_possession','middlewing_2ndquarter_possession','middlewing_4thquarter_possession','rightwing_3rdquarter_possession','passchains','k')");

		this.re.assign("csvlocation", trainingfile);
		initlines.add("foot <- read.csv(csvlocation, sep=\";\", quote=\"\", dec=\",\", colClasses= c(\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"factor\"),comment.char='#', header=T)");
		initlines.add("foot <- as.data.frame(foot)");
		initlines.add("colnames(foot) <- cnames");
		initlines.add("ind.input.num <- c(1:length(cnames))");
		initlines.add("foot <- na.omit(foot[,ind.input.num])");
		initlines.add("library(e1071)");
		initlines.add("model<-svm(k~.,data=foot)");
		// initlines.add("newdata <- data.frame(passmisses=20, goals=0, outsides=11,goalkicks=0,passmisses_offside=0,fast_attacks=5,all_attacks=10, leftwing_1stquarter_possession= 1, leftwing_2ndquarter_possession=80,middlewing_1stquarter_possession=24,middlewing_2ndquarter_possession=155, middlewing_4thquarter_possession=0, rightwing_3rdquarter_possession=129,passchains=2)");

		for (String line : initlines) {
			this.re.eval(line);
		}
	}

	public void end() {
		this.re.end();
		this.re = null;
	}

	public static String dataframe(float passmisses, float goals,
			float outsides, float goalkicks, float passmissess_offside,
			float fast_attacks, float all_attacks,
			float leftwing_1stquarter_possession,
			float leftwing_2ndquarter_possession,
			float middlewing_1stquarter_possession,
			float middlewing_2ndquarter_possession,
			float middlewing_4thquarter_possession,
			float rightwing_3rdquarter_possession, float passchains) {
		return new String("data.frame(passmisses=" + passmisses + ", goals="
				+ goals + ", outsides=" + outsides + ",goalkicks=" + goalkicks
				+ ",passmisses_offside=" + passmissess_offside
				+ ",fast_attacks=" + fast_attacks + ", all_attacks="
				+ all_attacks + ", leftwing_1stquarter_possession="
				+ leftwing_1stquarter_possession
				+ ", leftwing_2ndquarter_possession="
				+ leftwing_2ndquarter_possession
				+ ", middlewing_1stquarter_possession="
				+ middlewing_1stquarter_possession
				+ ",middlewing_2ndquarter_possession="
				+ middlewing_2ndquarter_possession
				+ ", middlewing_4thquarter_possession="
				+ middlewing_4thquarter_possession
				+ ", rightwing_3rdquarter_possession="
				+ rightwing_3rdquarter_possession + ", passchains="
				+ passchains + ")");
	}

	public int classify(StatisticsAccessFacilitator st) {

		float passmisses = st.passMissesScaled();
		float goals = st.goalsScaled();
		float outsides = st.goalkicksScaled() + st.cornersScaled()
		+ st.kicksinScaled();
		float goalkicks = st.goalkicksScaled();
		float passmissess_offside = st.passMissesOffsideScaled();
		float fast_attacks = st.attacksScaled(AttackType.FAST);
		float all_attacks = st.attacksScaled();
		float leftwing_1stquarter_possession = st
		.ballPossessionScaled(ZoneEnum.leftwing_1stquarter);
		float leftwing_2ndquarter_possession = st
		.ballPossessionScaled(ZoneEnum.leftwing_2ndquarter);
		float middlewing_1stquarter_possession = st
		.ballPossessionScaled(ZoneEnum.middlewing_1stquarter);
		float middlewing_2ndquarter_possession = st
		.ballPossessionScaled(ZoneEnum.middlewing_2ndquarter);
		float middlewing_4thquarter_possession = st
		.ballPossessionScaled(ZoneEnum.middlewing_4thquarter);
		float rightwing_3rdquarter_possession = st
		.ballPossessionScaled(ZoneEnum.rightwing_3rdquarter);
		float passchains = st.passChainsScaled();

		return this.classify(passmisses, goals, outsides, goalkicks,
				passmissess_offside, fast_attacks, all_attacks,
				leftwing_1stquarter_possession, leftwing_2ndquarter_possession,
				middlewing_1stquarter_possession,
				middlewing_2ndquarter_possession,
				middlewing_4thquarter_possession,
				rightwing_3rdquarter_possession, passchains);
	}

	public int classify(float passmisses, float goals, float outsides,
			float goalkicks, float passmissess_offside, float fast_attacks,
			float all_attacks, float leftwing_1stquarter_possession,
			float leftwing_2ndquarter_possession,
			float middlewing_1stquarter_possession,
			float middlewing_2ndquarter_possession,
			float middlewing_4thquarter_possession,
			float rightwing_3rdquarter_possession, float passchains) {

		String dataframe = RSVM.dataframe(passmisses, goals, outsides,
				goalkicks, passmissess_offside, fast_attacks, all_attacks,
				leftwing_1stquarter_possession, leftwing_2ndquarter_possession,
				middlewing_1stquarter_possession,
				middlewing_2ndquarter_possession,
				middlewing_4thquarter_possession,
				rightwing_3rdquarter_possession, passchains);
		this.re.eval("rowdata <- " + dataframe);

		REXP x;
		this.re.eval("res<-predict(model, rowdata)");
		x = this.re.eval("res");
		RFactor rf = x.asFactor();
		return Integer.parseInt(rf.at(0));
	}

	public void mainloop() {
		System.out.println("Now the console is yours... have fun");
		this.re.startMainLoop();
	}

	@Override
	public void finalize() {
		// just to make sure...
		if (this.re != null) {
			this.re.end();
			this.re = null;
		}
	}

	public static void main(String[] args) {
		RSVM r = new RSVM();
		r.init("/home/joao/RoboCup-strategy-adviser-coach/svm_training_csvs/Bahia2D.csv");
		System.out.println("1=="+r.classify(26, 1, 4, 1, 1, 0, 3, 41, 33, 44, 106,
				37, 48, 6));
		//r.mainloop();
		r.end();
	}
}

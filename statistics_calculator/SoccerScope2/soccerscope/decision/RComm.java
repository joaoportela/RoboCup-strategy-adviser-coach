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

class TextConsole implements RMainLoopCallbacks
{
	public void rWriteConsole(Rengine re, String text, int oType) {
		System.out.print(text);
	}

	public void rBusy(Rengine re, int which) {
		System.out.println("rBusy("+which+")");
	}

	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		System.out.print(prompt);
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			String s=br.readLine();
			return (s==null||s.length()==0)?s:s+"\n";
		} catch (Exception e) {
			System.out.println("jriReadConsole exception: "+e.getMessage());
		}
		return null;
	}

	public void rShowMessage(Rengine re, String message) {
		System.out.println("rShowMessage \""+message+"\"");
	}

	public String rChooseFile(Rengine re, int newFile) {
		FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
		fd.setVisible(true);
		String res=null;
		if (fd.getDirectory()!=null) {
			res=fd.getDirectory();
		}
		if (fd.getFile()!=null) {
			res=(res==null)?fd.getFile():(res+fd.getFile());
		}
		return res;
	}

	public void   rFlushConsole (Rengine re) {
	}

	public void   rLoadHistory  (Rengine re, String filename) {
	}

	public void   rSaveHistory  (Rengine re, String filename) {
	}
}

public class RComm {
	public static void main(String[] args) {
		// just making sure we have the right version of everything
		if (!Rengine.versionCheck()) {
			System.err.println("** Version mismatch - Java files don't match library version.");
			System.exit(1);
		}
		System.out.println("Creating Rengine (with arguments)");
		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop at first, we'll start it later
		//    (that's the "false" as second argument)
		// 3) the callbacks are implemented by the TextConsole class above
		Rengine re=new Rengine(args, false, new TextConsole());
		System.out.println("Rengine created, waiting for R");
		// the engine creates R is a new thread, so we should wait until it's ready
		if (!re.waitForR()) {
			System.out.println("Cannot load R");
			return;
		}

		/* High-level API - do not use RNI methods unless there is no other way
			to accomplish what you want */
		try {
			REXP x;
			ArrayList<String> initlines = new ArrayList<String>(8);
			initlines.add("cnames<-c('passmisses','goals','outsides','goalkicks','passmisses_offside','fast_attacks','all_attacks','leftwing_1stquarter_possession','leftwing_2ndquarter_possession','middlewing_1stquarter_possession','middlewing_2ndquarter_possession','middlewing_4thquarter_possession','rightwing_3rdquarter_possession','passchains','k')");
			re.assign("csvlocation", "/home/joao/RoboCup-strategy-adviser-coach/utils/group1_edit.csv");
			initlines.add("foot <- read.csv(csvlocation, sep=\";\", quote=\"\", dec=\",\", colClasses= c(\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"numeric\",\"factor\"),comment.char='#', header=T)");
			initlines.add("foot <- as.data.frame(foot)");
			initlines.add("colnames(foot) <- cnames");
			initlines.add("ind.input.num <- c(1:length(cnames))");
			initlines.add("foot <- na.omit(foot[,ind.input.num])");
			initlines.add("library(e1071)");
			initlines.add("model<-svm(k~.,data=foot)");
			//initlines.add("newdata <- data.frame(passmisses=20, goals=0, outsides=11,goalkicks=0,passmisses_offside=0,fast_attacks=5,all_attacks=10, leftwing_1stquarter_possession= 1, leftwing_2ndquarter_possession=80,middlewing_1stquarter_possession=24,middlewing_2ndquarter_possession=155, middlewing_4thquarter_possession=0, rightwing_3rdquarter_possession=129,passchains=2)");
			initlines.add("newdata <- data.frame(passmisses=18, goals=2, outsides=6,goalkicks=1,passmisses_offside=4,fast_attacks=7,all_attacks=8, leftwing_1stquarter_possession= 158, leftwing_2ndquarter_possession=74,middlewing_1stquarter_possession=12,middlewing_2ndquarter_possession=150, middlewing_4thquarter_possession=82, rightwing_3rdquarter_possession=21,passchains=17)");

			for(String line: initlines) {
				re.eval(line);
			}

			re.eval("res<-predict(model, newdata)");
			x=re.eval("res");
			System.out.println(REXP.xtName(x.rtype));
			System.out.println(x);
			RFactor rf=x.asFactor();
			System.out.println(rf.size());
			for(int i=0; i < rf.size(); i++) {
				System.out.println("is this the one?" + rf.at(i));
			}

			System.out.println("------------------------------------------------");
			x=re.eval("joao");
			System.out.println(REXP.xtName(x.rtype));
			String name = x.asString();
			System.out.println("variable name is: " + name);
			re.assign("listv", new double[]{1,3,5});

			x=re.eval("listv");
			System.out.println(REXP.xtName(x.rtype));
			x.asDoubleArray();
			for(double element: x.asDoubleArray()) {
				System.out.println("element is " + element);
			}

		} catch (Exception e) {
			System.out.println("EX:"+e);
			e.printStackTrace();
		}

		if (true) {
			// so far we used R as a computational slave without REPL
			// now we start the loop, so the user can use the console
			System.out.println("Now the console is yours ... have fun");
			re.startMainLoop();
		} else {
			re.end();
			System.out.println("end");
		}
	}
}

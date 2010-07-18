/**
 * 
 */
package soccerscope.decision;

import java.util.HashMap;
import java.util.Map;

/**
 * @author João Portela
 * 
 *         FLAWS: hard coded training csvs...
 */
public class DecisionTreeFactory {
	public static enum DecisionTreeType {
		SVM_BAHIA(new SVMBahia()), SVM_NEMESIS(new SVMNemesis()), SVM_WRIGHTEAGLE(
				new SVMWrighteagle());

		DecisionTree dt;

		private DecisionTreeType(DecisionTree dt) {
			this.dt = dt;
		}

		public DecisionTree getDecisionTree() {
			return this.dt;
		}

		public static DecisionTreeType typeFor(String opponentname,
				String algorithm) {
			opponentname = opponentname.toLowerCase();

			if (opponentname.startsWith("bahia")) {
				if (algorithm.equalsIgnoreCase("svm")) {
					return SVM_BAHIA;
				}
			} else if (opponentname.startsWith("wrighteagle")) {
				if (algorithm.equalsIgnoreCase("svm")) {
					return SVM_WRIGHTEAGLE;
				}
			} else if (opponentname.startsWith("nemesis")) {
				if (algorithm.equalsIgnoreCase("svm")) {
					return SVM_NEMESIS;
				}
			}
			throw new AssertionError("unkown algorithm, opponent combination "
					+ opponentname + " " + algorithm);
		}
	}

	/**
	 * Abstract decision tree, 'nuff said.
	 * 
	 * @author João Portela
	 * 
	 */
	public static abstract class DecisionTree {
		Map<Integer, Integer> k_to_tactic_map;
		String trainingcsv;
		RSVM svm;

		protected DecisionTree(String trainingcsv) {
			this.k_to_tactic_map = new HashMap<Integer, Integer>();
			this.trainingcsv = trainingcsv;
			this.svm = new RSVM();
			// should be filled like this:
			// this.k_to_tactic_map.put(K, tactic);
			// for every possible K
		}

		public int tactic(StatisticsAccessFacilitator s) {
			int k = this.whichK(s);
			if (this.k_to_tactic_map.containsKey(k)) {
				return this.k_to_tactic_map.get(k);
			}
			return this.defaultTactic();
		}

		public int defaultTactic() {
			return this.k_to_tactic_map.get(this.defaultK());
		}

		protected int whichK(StatisticsAccessFacilitator s) {
			return this.svm.classify(s);
		}

		public abstract int defaultK();

		public void init() {
			// sucks to have init and end but this is not c++ :p
			this.svm.init(this.trainingcsv);
		}

		public void end() {
			// sucks to have init and end but this is not c++ :p
			this.svm.end();
		}

	}

	public static class SVMBahia extends DecisionTree {

		public SVMBahia() {
			super(
			"/home/joao/RoboCup-strategy-adviser-coach/svm_training_csvs/Bahia2D.csv");
			this.k_to_tactic_map.put(1, 15);
			this.k_to_tactic_map.put(2, 26);
			this.k_to_tactic_map.put(3, 1);
			this.k_to_tactic_map.put(4, 22);
			this.k_to_tactic_map.put(5, 17);
			this.k_to_tactic_map.put(7, 8);
			this.k_to_tactic_map.put(9, 26);
		}

		@Override
		public int defaultK() {
			return 9;
		}
	}

	public static class SVMWrighteagle extends DecisionTree {

		public SVMWrighteagle() {
			super(
			"/home/joao/RoboCup-strategy-adviser-coach/svm_training_csvs/WrightEagle.csv");
			this.k_to_tactic_map.put(1, 18);
			this.k_to_tactic_map.put(4, 18);
			this.k_to_tactic_map.put(5, 21);
			this.k_to_tactic_map.put(6, 8);
			this.k_to_tactic_map.put(7, 32);
			this.k_to_tactic_map.put(9, 5);
		}

		@Override
		public int defaultK() {
			return 4;
		}
	}

	public static class SVMNemesis extends DecisionTree {

		public SVMNemesis() {
			super(
			"/home/joao/RoboCup-strategy-adviser-coach/svm_training_csvs/NemesisRC09.csv");
			this.k_to_tactic_map.put(1, 23);
			this.k_to_tactic_map.put(4, 4);
			this.k_to_tactic_map.put(6, 13);
			this.k_to_tactic_map.put(7, 25);
		}

		@Override
		public int defaultK() {
			return 6;
		}
	}

	/**
	 * Fetches the decision tree for $treetype$.
	 * 
	 * @return the decision tree
	 */
	public static DecisionTree getDecisionTree(DecisionTreeType treetype) {
		return treetype.getDecisionTree();
	}

	public static void main(String[] args) {
		for (DecisionTreeType dtt : DecisionTreeType.values()) {
			DecisionTree dt = dtt.getDecisionTree();
			System.out.println(dt.toString());
			System.out.println(dt.getClass().toString());
			System.out.println("-");
		}
	}

}

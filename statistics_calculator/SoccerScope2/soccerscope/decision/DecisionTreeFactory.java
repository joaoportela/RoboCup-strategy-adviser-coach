/**
 * 
 */
package soccerscope.decision;

import java.util.HashMap;
import java.util.Map;

/**
 * @author João Portela
 * 
 */
public class DecisionTreeFactory {
	public static enum DecisionTreeType {
		RANDOMFOREST_BAHIA(new RandomForestBahia()),
		SVM_BAHIA(new SVMBahia()),
		BAGGING_BAHIA(new BaggingBahia()),

		RANDOMFOREST_NEMESIS(new RandomForestNemesis()),
		SVM_NEMESIS(new SVMNemesis()),
		BAGGING_NEMESIS(new BaggingNemesis()),

		RANDOMFOREST_WRIGHTEAGLE(new RandomForestWrighteagle()),
		SVM_WRIGHTEAGLE(new SVMWrighteagle()),
		BAGGING_WRIGHTEAGLE(new BaggingWrighteagle());

		DecisionTree dt;
		private DecisionTreeType(DecisionTree dt) {
			this.dt=dt;
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
				} else if (algorithm.equalsIgnoreCase("bagging")) {
					return BAGGING_BAHIA;
				} else if (algorithm.equalsIgnoreCase("randomforest")) {
					return RANDOMFOREST_BAHIA;
				}
			} else if (opponentname.startsWith("wrighteagle")) {
				if (algorithm.equalsIgnoreCase("svm")) {
					return SVM_WRIGHTEAGLE;
				} else if (algorithm.equalsIgnoreCase("bagging")) {
					return BAGGING_WRIGHTEAGLE;
				} else if (algorithm.equalsIgnoreCase("randomforest")) {
					return RANDOMFOREST_WRIGHTEAGLE;
				}
			} else if (opponentname.startsWith("nemesis")) {
				if (algorithm.equalsIgnoreCase("svm")) {
					return SVM_NEMESIS;
				} else if (algorithm.equalsIgnoreCase("bagging")) {
					return BAGGING_NEMESIS;
				} else if (algorithm.equalsIgnoreCase("randomforest")) {
					return RANDOMFOREST_NEMESIS;
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

		protected DecisionTree(int map_size_hint) {
			this.k_to_tactic_map=new HashMap<Integer, Integer>(map_size_hint);
		}

		public int Tactic(StatisticsAccessFacilitator s) {
			return this.k_to_tactic_map.get(this.whichK(s));
		}

		protected abstract int whichK(StatisticsAccessFacilitator s);
	}

	public static class RandomForestBahia extends DecisionTree {

		public RandomForestBahia() {
			super(1);
			this.k_to_tactic_map.put(9, 19); // could also be tactic 26
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			return 9;
		}

	}

	public static class RandomForestWrighteagle extends DecisionTree {

		public RandomForestWrighteagle() {
			super(1);
			// this.k_to_tactic_map.put(...);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			throw new AssertionError(); // TODO
		}
	}

	public static class RandomForestNemesis extends DecisionTree {

		public RandomForestNemesis() {
			super(1);
			// this.k_to_tactic_map.put(...);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			throw new AssertionError(); // TODO
		}
	}

	public static class SVMBahia extends DecisionTree {

		public SVMBahia() {
			super(1);
			// this.k_to_tactic_map.put(...);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			throw new AssertionError(); // TODO
		}
	}

	public static class SVMWrighteagle extends DecisionTree {

		public SVMWrighteagle() {
			super(1);
			// this.k_to_tactic_map.put(...);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			throw new AssertionError(); // TODO
		}
	}

	public static class SVMNemesis extends DecisionTree {

		public SVMNemesis() {
			super(1);
			// this.k_to_tactic_map.put(...);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			throw new AssertionError(); // TODO
		}
	}

	public static class BaggingBahia extends DecisionTree {

		public BaggingBahia() {
			super(1);
			// this.k_to_tactic_map.put(...);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			throw new AssertionError(); // TODO
		}
	}


	public static class BaggingWrighteagle extends DecisionTree {

		public BaggingWrighteagle() {
			super(1);
			// this.k_to_tactic_map.put(...);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			throw new AssertionError(); // TODO
		}
	}

	public static class BaggingNemesis extends DecisionTree {

		public BaggingNemesis() {
			super(1);
			// this.k_to_tactic_map.put(...);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			throw new AssertionError(); // TODO
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

	public static void main(String [] args) {
		for(DecisionTreeType dtt: DecisionTreeType.values()) {
			DecisionTree dt=dtt.getDecisionTree();
			System.out.println(dt.toString());
			System.out.println(dt.getClass().toString());
			System.out.println("-");
		}
	}

}

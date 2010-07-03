/**
 * 
 */
package soccerscope.decision;

/**
 * @author João Portela
 * 
 */
public class DecisionTreeFactory {
	public static enum DecisionTreeType {
		RANDOMFOREST_BAHIA, SVM_BAHIA, BAGGING_BAHIA,
		RANDOMFOREST_NEMESIS, SVM_NEMESIS, BAGGING_NEMESIS,
		RANDOMFOREST_WRIGHTEAGLE, SVM_WRIGHTEAGLE, BAGGING_WRIGHTEAGLE;

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
	 * Decision tree interface, 'nuff said.
	 * 
	 * @author João Portela
	 * 
	 */
	public static interface DecisionTree {
		public int whichK(StatisticsAccessFacilitator s);
	}

	public static class RandomForestBahia implements DecisionTree {
		public int whichK(StatisticsAccessFacilitator s) {
			return 9;
		}

	}

	/**
	 * Fetches the decision tree for $treetype$.
	 * 
	 * @return the decision tree or null if the DecisionTreeType is unknown
	 */
	public static DecisionTree getDecisionTree(DecisionTreeType treetype) {
		switch (treetype) {
		case RANDOMFOREST_BAHIA:
			return new RandomForestBahia();
		}
		return null;
	}

}

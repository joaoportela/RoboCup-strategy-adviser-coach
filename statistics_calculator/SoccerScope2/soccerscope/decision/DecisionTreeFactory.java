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
		RANDOMFOREST_BAHIA(new RandomForestBahia()), SVM_BAHIA(new SVMBahia()), BAGGING_BAHIA(
				new BaggingBahia()),

				RANDOMFOREST_NEMESIS(new RandomForestNemesis()), SVM_NEMESIS(
						new SVMNemesis()), BAGGING_NEMESIS(new BaggingNemesis()),

						RANDOMFOREST_WRIGHTEAGLE(new RandomForestWrighteagle()), SVM_WRIGHTEAGLE(
								new SVMWrighteagle()), BAGGING_WRIGHTEAGLE(
										new BaggingWrighteagle());

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
			this.k_to_tactic_map = new HashMap<Integer, Integer>(map_size_hint);
			// should be filled like this:
			// this.k_to_tactic_map.put(K, tactic);
			// for every possible K
		}

		public int tactic(StatisticsAccessFacilitator s) {
			return this.k_to_tactic_map.get(this.whichK(s));
		}

		public int defaultTactic() {
			return this.k_to_tactic_map.get(this.defaultK());
		}

		protected abstract int whichK(StatisticsAccessFacilitator s);

		public abstract int defaultK();

	}

	public static class RandomForestBahia extends DecisionTree {

		public RandomForestBahia() {
			super(1);
			this.k_to_tactic_map.put(9, 9);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			return 9;
		}

		@Override
		public int defaultK() {
			return 9;
		}

	}

	public static class RandomForestWrighteagle extends DecisionTree {

		public RandomForestWrighteagle() {
			super(2);
			this.k_to_tactic_map.put(6, 13);
			this.k_to_tactic_map.put(7, 25);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			if (s.ballPossessionScaled(ZoneEnum.leftwing_1stquarter) < 36.5) {
				if (s.ballPossessionScaled(ZoneEnum.leftwing_1stquarter) < 30.5) {
					return 6;
				} else {
					if (s.ballPossessionScaled(ZoneEnum.middlewing_4thquarter) >= 128) {
						return 6;
					} else {
						return 7;
					}
				}
			} else {
				return 7;
			}
		}

		@Override
		public int defaultK() {
			return 6;
		}
	}

	public static class RandomForestNemesis extends DecisionTree {

		public RandomForestNemesis() {
			super(1);
			this.k_to_tactic_map.put(4, 18);
			this.k_to_tactic_map.put(6, 16);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			if (s.ballPossessionScaled(ZoneEnum.rightwing_3rdquarter) >= 182) {
				if (s.passMissesScaled() >= 17.5) {
					return 4;
				} else {
					if (s.ballPossessionScaled(ZoneEnum.leftwing_2ndquarter) >= 74.5) {
						if (s
								.ballPossessionScaled(ZoneEnum.rightwing_3rdquarter) >= 243) {
							return 4;
						} else {
							return 6;
						}
					} else {
						if (s
								.ballPossessionScaled(ZoneEnum.rightwing_3rdquarter) >= 358.5) {
							return 4;
						} else {
							return 6;
						}
					}
				}
			} else {
				return 6;
			}
		}

		@Override
		public int defaultK() {
			return 4;
		}
	}

	public static class SVMBahia extends DecisionTree {

		public SVMBahia() {
			super(4);
			this.k_to_tactic_map.put(3, 3);
			this.k_to_tactic_map.put(1, 25);
			this.k_to_tactic_map.put(9, 9);
			this.k_to_tactic_map.put(5, 17);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			if (s.ballPossessionScaled(ZoneEnum.middlewing_2ndquarter) < 166.5) {
				if (s.ballPossessionScaled(ZoneEnum.middlewing_1stquarter) >= 103) {
					return 3;
				} else {
					if (s.passMissesScaled() < 20.5) {
						return 1;
					} else {
						return 9;
					}
				}
			} else {
				if (s.ballPossessionScaled(ZoneEnum.leftwing_2ndquarter) >= 141) {
					return 5;
				} else {
					return 9;
				}
			}
		}

		@Override
		public int defaultK() {
			return 9;
		}
	}

	public static class SVMWrighteagle extends DecisionTree {

		public SVMWrighteagle() {
			super(2);
			this.k_to_tactic_map.put(6, 13);
			this.k_to_tactic_map.put(7, 25);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			if (s.ballPossessionScaled(ZoneEnum.leftwing_1stquarter) < 37.5) {
				if (s.ballPossessionScaled(ZoneEnum.leftwing_1stquarter) < 30.5) {
					return 6;
				} else {
					if (s.passMissesOffsideScaled() < 2.5) {
						return 6;
					} else {
						return 7;
					}
				}
			} else {
				return 7;
			}
		}

		@Override
		public int defaultK() {
			return 6;
		}
	}

	public static class SVMNemesis extends DecisionTree {

		public SVMNemesis() {
			super(1);
			this.k_to_tactic_map.put(4, 18);
			this.k_to_tactic_map.put(6, 8);
			this.k_to_tactic_map.put(1, 18);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			if (s.ballPossessionScaled(ZoneEnum.rightwing_3rdquarter) > 250.5) {
				if (s.ballPossessionScaled(ZoneEnum.rightwing_3rdquarter) > 355.5) {
					return 4;
				} else {
					if (s.ballPossessionScaled(ZoneEnum.middlewing_2ndquarter) >= 89.5) {
						return 4;
					} else {
						if (s.passMissesScaled() >= 18.5) {
							return 4;
						} else {
							return 6;
						}
					}
				}
			} else {
				if (s.passMissesScaled() > 19.5) {
					if (s.ballPossessionScaled(ZoneEnum.middlewing_4thquarter) < 107.5) {
						if (s.goalkicksScaled() >= 0.5) {
							return 1;
						} else {
							return 4;
						}
					}else {
						return 6;
					}
				} else {
					return 6;
				}
			}
		}

		@Override
		public int defaultK() {
			return 4;
		}
	}

	public static class BaggingBahia extends DecisionTree {

		public BaggingBahia() {
			super(1);
			this.k_to_tactic_map.put(1, 9);
			this.k_to_tactic_map.put(3, 23);
			this.k_to_tactic_map.put(2, 1);
			this.k_to_tactic_map.put(9, 26);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			if (s.passChainsScaled() < 8.5) {
				if (s.goalkicksScaled() < 2.5) {
					if (s.ballPossessionScaled(ZoneEnum.middlewing_1stquarter) < 84) {
						return 1;
					} else {
						return 3;
					}
				} else {
					return 2;
				}
			} else {
				return 9;
			}
		}

		@Override
		public int defaultK() {
			return 9;
		}
	}

	public static class BaggingWrighteagle extends DecisionTree {

		public BaggingWrighteagle() {
			super(1);
			this.k_to_tactic_map.put(6, 13);
			this.k_to_tactic_map.put(7, 25);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			if (s.ballPossessionScaled(ZoneEnum.leftwing_1stquarter) < 34.5) {
				return 6;
			} else {
				return 7;
			}
		}

		@Override
		public int defaultK() {
			return 6;
		}
	}

	public static class BaggingNemesis extends DecisionTree {

		public BaggingNemesis() {
			super(1);
			this.k_to_tactic_map.put(4, 9);
			this.k_to_tactic_map.put(8, 16);
			this.k_to_tactic_map.put(6, 15);
			this.k_to_tactic_map.put(7, 32);
		}

		@Override
		protected int whichK(StatisticsAccessFacilitator s) {
			if (s.ballPossessionScaled(ZoneEnum.rightwing_3rdquarter) > 252.5) {
				if (s.attacksScaled() < 18.5) {
					return 4;
				} else {
					return 8;
				}
			} else {
				if (s.ballPossessionScaled(ZoneEnum.middlewing_4thquarter) < 46.5) {
					return 4;
				} else {
					if (s.ballPossessionScaled(ZoneEnum.leftwing_1stquarter) < 33) {
						return 6;
					} else {
						return 7;
					}
				}
			}
		}

		@Override
		public int defaultK() {
			return 4;
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

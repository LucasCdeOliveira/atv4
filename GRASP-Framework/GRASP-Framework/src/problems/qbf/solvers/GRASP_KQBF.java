package problems.qbf.solvers;

import metaheuristics.grasp.AbstractGRASP;
import problems.qbf.BiasFunction;
import problems.qbf.KQBF;
import problems.qbf.KQBF_Inverse;
import problems.qbf.QBF_Inverse;
import solutions.Solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Metaheuristic GRASP (Greedy Randomized Adaptive Search Procedure) for
 * obtaining an optimal solution to a QBF (Quadractive Binary Function --
 * {@link #QuadracticBinaryFunction}). Since by default this GRASP considers
 * minimization problems, an inverse QBF function is adopted.
 * 
 * @author ccavellucci, fusberti
 */
public class GRASP_KQBF extends AbstractGRASP<Integer> {

	/**
	 * Constructor for the GRASP_QBF class. An inverse QBF objective function is
	 * passed as argument for the superclass constructor.
	 *
	 * @param alpha
	 *            The GRASP greediness-randomness parameter (within the range
	 *            [0,1])
	 * @param iterations
	 *            The number of iterations which the GRASP will be executed.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             necessary for I/O operations.
	 */
	public GRASP_KQBF(Double alpha, Integer iterations, String filename) throws IOException {
		super(new KQBF_Inverse(filename), alpha, iterations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeCL()
	 */
	@Override
	public ArrayList<Integer> makeCL() {

		ArrayList<Integer> _CL = new ArrayList<Integer>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = i;
			_CL.add(cand);
		}

		return _CL;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeRCL()
	 */
	@Override
	public ArrayList<Integer> makeRCL() {

		ArrayList<Integer> _RCL = new ArrayList<Integer>();

		return _RCL;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#updateCL()
	 */
	@Override
	public void updateCL() {

		// do nothing since all elements off the solution are viable candidates.

	}

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}

	@Override
	public Solution<Integer> constructiveHeuristic() {

		CL = makeCL();
		RCL = makeRCL();
		sol = createEmptySol();
		cost = Double.POSITIVE_INFINITY;

		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {

			double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			cost = ObjFunction.evaluate(sol);
			updateCL();

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (Integer c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, sol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance using parameter alpha as threshold.
			 */
			for (Integer c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, sol);
				if (deltaCost <= minCost + alpha * (maxCost - minCost)) {
					RCL.add(c);
				}
			}

			/* Choose a candidate from the Bias Function from the RCL */
			BiasFunction biasFunction = new BiasFunction(RCL);
			Integer inCand = biasFunction.getElement();
			//Change to adapt to KQBF, with QBF this is always true
			if(ObjFunction.shouldInsert(inCand, sol)){
				CL.remove(inCand);
				sol.add(inCand);
				ObjFunction.evaluate(sol);
				RCL.clear();
			}

		}
		return sol;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer> localSearch() {

		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		do {
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();
				
			// Evaluate insertions
			for (Integer candIn : CL) {
				double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}
			// Evaluate removals
			for (Integer candOut : sol) {
				double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
				}
			}
			// Evaluate exchanges
			for (Integer candIn : CL) {
				for (Integer candOut : sol) {
					double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
			// Implement the best move, if it reduces the solution cost.
			if (minDeltaCost < -Double.MIN_VALUE) {
				//I can remove any candidate
				if (bestCandOut != null) {
					sol.remove(bestCandOut);
					CL.add(bestCandOut);
				}
				//I can only add if it doesn't exceed the capacity
				if (bestCandIn != null && ObjFunction.shouldInsert(bestCandIn, sol)) {
					sol.add(bestCandIn);
					CL.remove(bestCandIn);
				} else {
					//If I cant add I just remove from the candidate list
					CL.remove(bestCandIn);
				}
				ObjFunction.evaluate(sol);
			}
		} while (minDeltaCost < -Double.MIN_VALUE);

		return null;
	}

	/**
	 * A main method used for testing the GRASP metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {
		List<String> instancias = List.of("020", "040", "060", "080", "100", "200", "400");
		for(String instancia: instancias) {
			System.out.println("Running for instance " + instancia);
			long startTime = System.currentTimeMillis();
			GRASP_KQBF grasp = new GRASP_KQBF(0.05, 1000, "instances/kqbf/kqbf" + instancia);
			Solution<Integer> bestSol = grasp.solve();
			System.out.println("maxVal = " + bestSol);
			KQBF evaluateCost = new KQBF("instances/kqbf/kqbf" + instancia);
			System.out.println("weight of solution = " + evaluateCost.evaluateWeight(bestSol));
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
			System.out.println("#########################################################################################\n");
		}

	}



}

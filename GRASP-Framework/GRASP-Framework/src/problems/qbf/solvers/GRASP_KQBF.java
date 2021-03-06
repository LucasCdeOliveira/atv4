package problems.qbf.solvers;

import metaheuristics.grasp.AbstractGRASP;
import problems.qbf.KQBF;
import problems.qbf.KQBF_Inverse;
import solutions.Solution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

	/**
	 * {@inheritDoc}
	 * 
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer> localSearch() {
		//return firstLocalSearch();
		return bestLocalSearch();
	}

	/**
	 * Best improving method
	 */
	public Solution<Integer> bestLocalSearch() {
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
	 * First improving method
	 */
	public Solution<Integer> firstLocalSearch() {
		Double minDeltaCost;
		Integer firstCandIn = null, firstCandOut = null;

		minDeltaCost = Double.POSITIVE_INFINITY;
		updateCL();
		Boolean needRemoveCandIn = false;
		// Evaluate insertions
		do {
			for (Integer candIn : CL) {
				double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					firstCandIn = candIn;
					firstCandOut = null;
					if(ObjFunction.shouldInsert(firstCandIn, sol)) {
						break;
					}
					else {
						needRemoveCandIn = true;
						break;
					}
				}
			}
			if(firstCandIn != null) {
				if(!needRemoveCandIn) {
					sol.add(firstCandIn);
					CL.remove(firstCandIn);
					return null;
				}
				CL.remove(firstCandIn);
				needRemoveCandIn = false;
			}
			// Evaluate removals
			for (Integer candOut : sol) {
				double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					firstCandIn = null;
					firstCandOut = candOut;
					break;
				}
			}
			if(firstCandOut != null) {
				sol.remove(firstCandOut);
				CL.add(firstCandOut);
				return null;
			}
			// Evaluate exchanges
			boolean found = false;
			for (Integer candIn : CL) {
				if(found) {
					break;
				}
				for (Integer candOut : sol) {
					double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						firstCandIn = candIn;
						firstCandOut = candOut;
						found = true;
						break;
					}
				}
			}
			if(firstCandIn != null && firstCandOut != null) {
				//Remove
				sol.remove(firstCandOut);
				if (ObjFunction.shouldInsert(firstCandIn, sol)) {
					sol.add(firstCandIn);
					CL.remove(firstCandIn);
					CL.add(firstCandOut);
					return null;
				} else {
					sol.add(firstCandOut);
				}
			}
		} while(minDeltaCost < -Double.MIN_VALUE);

		return null;
	}

	/**
	 * A main method used for testing the GRASP metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {
		List<String> instancias = Arrays.asList(new String[]{"020", "040", "060", "080", "100", "200", "400"});

		for(String instancia: instancias) {
			try {
				File myObj = new File("C:\\Users\\rebec\\Documents\\GitHub\\atv4\\GRASP-Framework\\GRASP-Framework\\solutions\\scenario8\\instance_" + instancia + ".txt");
				myObj.createNewFile();
				// Create a FileWriter object
				// to write in the file
				FileWriter fWriter = new FileWriter(
					"C:\\Users\\rebec\\Documents\\GitHub\\atv4\\GRASP-Framework\\GRASP-Framework\\solutions\\scenario8\\instance_" + instancia + ".txt");
	 
				fWriter.write("Running for instance " + instancia+"\n");
				long startTime = System.currentTimeMillis();
				GRASP_KQBF grasp = new GRASP_KQBF(0.85, 3000, "instances/kqbf/kqbf" + instancia);
				Solution<Integer> bestSol = grasp.solve();
				fWriter.write("maxVal = " + bestSol+"\n");
				KQBF evaluateCost = new KQBF("instances/kqbf/kqbf" + instancia);
				fWriter.write("weight of solution = " + evaluateCost.evaluateWeight(bestSol) + "\n");
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				fWriter.write("Time = "+(double)totalTime/(double)1000+" seg\n");
				fWriter.write("#########################################################################################\n");	
	 
				// Closing the file writing connection
				fWriter.close();
	 
				// Display message for successful execution of
				// program on the console
				System.out.println(
					"File is created successfully with the content.");				
			}
			catch (IOException e) {
				// Print the exception
				System.out.print(e.getMessage());
			}

		}

	}



}

package problems.qbf;

import java.io.IOException;
import java.util.*;

public class BiasFunction {

    private ArrayList<Map.Entry<Integer, Double>> candidates;

    public BiasFunction(ArrayList<Integer> candidates) {
        this.candidates = new ArrayList<>();
        candidates.forEach(c -> this.candidates.add(Map.entry(c, bias(c))));
    }

    public void addCandidate(Integer candidate){
        Double biasC = bias(candidate);
        candidates.add(Map.entry(candidate, biasC));
    }

    public void removeCandidate(Integer candidate){
        candidates.removeIf(c -> Objects.equals(c.getKey(), candidate));
    }

    private Double bias(Integer candidate){
        //to avoid negative values
        //bias(r) = 1/|r|
        return 1.0/(candidates.indexOf(candidate) + 1);
    }

    public Integer getElement(){
        //I'm expressing the bias(r) in a percentage mode. X% = bias(r)/totalBias
        Double totalBias = candidates.stream().mapToDouble(Map.Entry::getValue).sum();
        //Creating the percentile for each element
        List<Double> percetile = candidates.stream().map(c -> c.getValue() * 100 / totalBias).toList();
        //Getting a value from 0 to 100
        Double random = Math.random() * 100;
        Double total = percetile.get(0);
        //Finding the value that represents the percetile
        //To control this I'll sum all the percetile values in total variable to comparate
        //O to X -> Element 1; X to Y -> Element 2; ....; Z to 100 -> last element
        for (int i = 0; i < percetile.size(); i++) {
            if(random <= total + percetile.get(i)){
                return candidates.get(i).getKey();
            }else {
                total += percetile.get(0);
            }
        }
        //Shouldn't get here because the max is 100
        return 0;
    }

    public static void main(String[] args) throws IOException {
        ArrayList<Integer> testList = new ArrayList<>();
        testList.add(3);
        testList.add(-8);
        testList.add(10);
        testList.add(13);
        testList.add(12);
        testList.add(15);
        BiasFunction biasFunction = new BiasFunction(testList);
        System.out.println(biasFunction.getElement());
        System.out.println(biasFunction.getElement());
        System.out.println(biasFunction.getElement());
    }
}

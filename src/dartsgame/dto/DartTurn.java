package dartsgame.dto;

import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

public class DartTurn {
    @Pattern(regexp = "[1-3]:(1?[0-9]|20)|2:25", message = "Wrong throws!")
    private String first;
    @Pattern(regexp = "[1-3]:(1?[0-9]|20)|2:25|none", message = "Wrong throws!")
    private String second;
    @Pattern(regexp = "[1-3]:(1?[0-9]|20)|2:25|none", message = "Wrong throws!")
    private String third;

    public void setFirst(String first) {
        this.first = first;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public void setThird(String third) {
        this.third = third;
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    public String getThird() {
        return third;
    }

    public List<Integer> retrieveThrowsScores(){
        List<Integer> scores = new ArrayList<>();
        scores.add(scoreOf(first));
        if(!"none".equals(second)) scores.add(scoreOf(second));
        if(!"none".equals(third)) scores.add(scoreOf(third));

        return scores;
    }

    private int scoreOf(String dartThrow){
        // double throws are indicated by negative numbers
        String[] values = dartThrow.split(":");
        if("2".equals(values[0])){
            return -2*Integer.parseInt(values[1]);
        }
        return Integer.parseInt(values[0]) * Integer.parseInt(values[1]);
    }

    public static boolean checkDoubleScore(int score){
        return score < 0;
    }
}

package com.example.challenge_3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Calcuations {

    public double SMV(double x, double y, double z){
        return Math.sqrt(x*x + y*y + z*z);
    }


    double Minimum(List<Double> list){
        Collections.sort(list);
        return list.get(0);
    }

     double Maximum(List<Double> list){
        Collections.sort(list);
        return list.get(list.size()-1);
    }

    double Mean(List<Double> list){
        if(list.size() == 0)
            return 0;
        else{
            double sum = 0;
            for(int i = 0; i < list.size(); i++){
                sum+= list.get(i);
            }
            return sum/list.size();
        }

    }

    double Mode(List<Double> list){
        double maxvalue =0;
        int maxCount=0;

        for(int i = 0; i< list.size(); i++){
            int count = 0;
            for(int j = 0; j< list.size(); j++){
                if((int) Math.round(list.get(i)) == (int) Math.round(list.get(j)))
                    count++;
            }
            if(count > maxCount){
                maxCount = count;
                maxvalue = list.get(i);
            }
        }
        return maxvalue;
    }

    String ModeString(List<String> list){
        String maxValue = "";
        int maxCount = 0;

        for (int i = 0; i < list.size(); i++) {
            int count =0;
            for (int j = 0; j < list.size(); j++) {
                if(list.get(i).equals(list.get(j)))
                    count++;
            }
            if(count > maxCount){
                maxCount = count;
                maxValue = list.get(i);
            }
        }

        return maxValue;
    }


    public double StandardDev(List<Double> list){

        double mean = Mean(list);
        double temp = 0;

        for (int i = 0; i < list.size(); i++) {
            double value = list.get(i);
            double powerOfValueMinusMean = Math.pow(value - mean, 2);
            temp+= powerOfValueMinusMean;
        }

        double variance = temp/list.size();


        return Math.sqrt(variance);
    }

    public <T> boolean IsEmpty(List<T> list){
        return list.size()==0;
    }
}

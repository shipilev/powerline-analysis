package net.shipilev.powerline.analyzers;

import net.shipilev.powerline.Period;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class HomogeneityAnalyzer implements Analyzer {

    private final int sigma;

    public HomogeneityAnalyzer(int sigma) {
        this.sigma = sigma;
    }

    @Override
    public boolean analyze(Period p) {
        short[] data = p.getData();

        SummaryStatistics stat = new SummaryStatistics();

        for (int i = 0; i < data.length - 1; i++) {
            short l1 = data[i];
            short l2 = data[i + 1];
            stat.addValue(Math.abs(l2 - l1));
        }

        for (int i = 0; i < data.length - 1; i++) {
            short l1 = data[i];
            short l2 = data[i + 1];

            if (Math.abs(l2 - l1) > stat.getStandardDeviation() * sigma) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "Homogeneity (" + sigma + " sigmas)";
    }

}

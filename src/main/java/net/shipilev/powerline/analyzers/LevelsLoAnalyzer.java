package net.shipilev.powerline.analyzers;

import net.shipilev.powerline.Period;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class LevelsLoAnalyzer implements Analyzer {

    private final DescriptiveStatistics stat;
    private final int window;
    private final int sigma;

    public LevelsLoAnalyzer(int window, int sigma) {
        this.window = window;
        this.sigma = sigma;
        this.stat = new DescriptiveStatistics(window);
    }

    @Override
    public boolean analyze(Period p) {
        stat.addValue(p.getLoLevel());
        return Math.abs(p.getLoLevel() - stat.getMean()) > stat.getStandardDeviation() * sigma;
    }

    @Override
    public String toString() {
        return "Amplitude Low (" + window + " ticks, " + sigma + " sigmas)";
    }

}

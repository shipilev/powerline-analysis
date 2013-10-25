package net.shipilev.powerline;

public class Period {
    private final float ms;
    private final float duration;
    private final int srcIndex;
    private short[] data;
    private short loLevel;
    private short hiLevel;

    public Period(float ms, float duration, short[] srcData, int srcIndex) {
        this.ms = ms;
        this.duration = duration;
        this.srcIndex = srcIndex;
        this.data = new short[srcIndex];
        System.arraycopy(srcData, 0, this.data, 0, srcIndex);

        loLevel = Short.MAX_VALUE;
        hiLevel = Short.MIN_VALUE;
        for (short level : this.data) {
            loLevel = (loLevel < level) ? loLevel : level;
            hiLevel = (hiLevel > level) ? hiLevel : level;
        }
    }

    public double getHz() {
        return 1.0/duration;
    }

    @Override
    public String toString() {
        return String.format("%.4f Hz, range = (%.4f, %.4f)", 1.0/duration, 1.0 * loLevel / Short.MAX_VALUE, 1.0 * hiLevel / Short.MAX_VALUE);
    }

    public short getLoLevel() {
        return loLevel;
    }

    public short getHiLevel() {
        return hiLevel;
    }

    public short[] getData() {
        return data;
    }

    public float getMs() {
        return ms;
    }
}

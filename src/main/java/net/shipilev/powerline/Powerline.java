package net.shipilev.powerline;

import net.shipilev.powerline.analyzers.Analyzer;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Powerline {

    private final List<Analyzer> analyzers;
    private final AudioInputStream is;
    private int eventIdx = 1;
    private long lastProgress = System.currentTimeMillis();

    public Powerline(AudioInputStream is) {
        this.is = is;
        this.analyzers = new ArrayList<Analyzer>();
    }

    public void addAnalyzer(Analyzer analyzer) {
        analyzers.add(analyzer);
    }

    void run() throws IOException {
        int bytesPerFrame = is.getFormat().getFrameSize();
        if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
            bytesPerFrame = 1;
        }
        byte[] audioBytes = new byte[bytesPerFrame];

        float frameRate = is.getFormat().getFrameRate();

        int frame = 0;

        int prevZeroFrame = 0;
        short prevLevel = 0;

        int periodSize = 10;
        int periodIdx = 0;
        short[] periodBuf = new short[periodSize];

        int numBytesRead;
        while ((numBytesRead = is.read(audioBytes)) != -1) {
            short level = (short) ((audioBytes[0] & 0xFF) + (audioBytes[1] & 0xFF) * 256);

            int curIdx = periodIdx++;
            if (curIdx >= periodSize) {
                int newPeriodSize = (int) (periodSize * 1.2);
                short[] newPeriod = new short[newPeriodSize];

                System.arraycopy(periodBuf, 0, newPeriod, 0, periodSize);
                periodBuf = newPeriod;
                periodSize = newPeriodSize;
            }
            periodBuf[curIdx] = level;

            if (level >= 0 && prevLevel < 0) {
                float ms = frame / frameRate;
                float duration = (frame - prevZeroFrame) / frameRate;
                Period p = new Period(ms, duration, periodBuf, periodIdx);

                List<Analyzer> interestingAnalyzers = new ArrayList<Analyzer>();
                for (Analyzer analyzer : analyzers) {
                    if (analyzer.analyze(p)) {
                        interestingAnalyzers.add(analyzer);
                    }
                }

                if (!interestingAnalyzers.isEmpty()) {
                    System.out.println();
                    System.out.printf("%.2f sec: (triggered) %s%n", ms, p.toString());
                    for (Analyzer analyzer : interestingAnalyzers) {
                        System.out.printf("%.2f sec:      %s%n", ms, analyzer.toString());
                    }
                    System.out.println();

                    dump(p, interestingAnalyzers);
                } else {
                    if (System.currentTimeMillis() - lastProgress > 5000) {
                        System.out.printf("%.2f sec: (current) %s%n", ms, p.toString());
                        lastProgress = System.currentTimeMillis();
                    }
                }

                prevZeroFrame = frame;
                periodIdx = 0;
            }

            prevLevel = level;
            frame++;
        }

    }

    private void dump(Period p, List<Analyzer> anals) throws IOException {
        final int WIDTH = 1000;
        final int HEIGHT = 500;
        final int GAP = 20;

        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D g = img.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.BLACK);

        int samples = p.getData().length;

        short[] data = p.getData();
        for (int i = 0; i < data.length; i++) {
            short level = data[i];
            int x = GAP + i * (WIDTH - 2*GAP) / samples;
            int y = GAP + (HEIGHT - 2*GAP) * (level - Short.MIN_VALUE) / (Short.MAX_VALUE - Short.MIN_VALUE);
            g.fillRect(x, y, 1, 1);
        }

        g.drawString(String.format("%.2f sec, %s", p.getMs(), p.toString()), 10, 20);
        for (int c = 0; c < anals.size(); c++) {
            g.drawString(anals.get(c).toString(), 10, 25 + 15*(c+1));
        }

        ImageIO.write(img, "png", new File("event-" + (eventIdx++) + ".png"));
    }
}

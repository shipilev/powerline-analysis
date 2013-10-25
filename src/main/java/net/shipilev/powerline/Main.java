package net.shipilev.powerline;

import net.shipilev.powerline.analyzers.HomogeneityAnalyzer;
import net.shipilev.powerline.analyzers.HzGapAnalyzer;
import net.shipilev.powerline.analyzers.LevelsHiAnalyzer;
import net.shipilev.powerline.analyzers.LevelsLoAnalyzer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(
                (float) 44100,
                16,
                1,
                true,
                false);

        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        targetDataLine.open(audioFormat);
        targetDataLine.start();

        Powerline pl = new Powerline(new AudioInputStream(targetDataLine));

        pl.addAnalyzer(new HzGapAnalyzer(100, 5));
        pl.addAnalyzer(new LevelsLoAnalyzer(100, 5));
        pl.addAnalyzer(new LevelsHiAnalyzer(100, 5));
        pl.addAnalyzer(new HomogeneityAnalyzer(5));

        pl.run();
    }

}

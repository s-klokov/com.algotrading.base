package com.algotrading.base.lib.nnets.test;

import com.algotrading.base.lib.Rnd;
import com.algotrading.base.lib.nnets.NNet3L;
import com.algotrading.base.lib.nnets.NNetData;
import com.algotrading.base.lib.nnets.NNetTrainer;
import com.algotrading.base.lib.nnets.ParabolicNNetTrainer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class NNetTrainerTestCase {

    public static void main(final String[] args) throws IOException {
        final Rnd rnd = new Rnd(1L);
        final NNetData data = new NNetData(rnd, 50 * 1000, 50 * 1000);
        data.load("max.txt");
        final NNet3L net = new NNet3L(data.adviseNetConfiguration(1.8, 5, 1000), true);
//        final NNet3L net = new NNet3L(2, 4, 1, true);
        data.normalize();
        data.normalizeNet(net);
        net.setRandomWeights(rnd);
//        train(net.getCopy(), data, new BfgsGoldenSectNNetTrainer());
//        train(net.getCopy(), data, new BfgsParabolicNNetTrainer());
//        train(net.getCopy(), data, new BpNNetTrainer());
//        train(net.getCopy(), data, new ConjugateGradientNNetTrainer());
//        train(net.getCopy(), data, new GoldenSectNNetTrainer());
        train(net.getCopy(), data, new ParabolicNNetTrainer());
    }

    private static void train(final NNet3L net, final NNetData data, final NNetTrainer trainer) throws IOException {
        trainer.logPeriod = 1;
        String trainerName = trainer.getClass().getName();
        trainerName = trainerName.substring(trainerName.lastIndexOf('.') + 1);
        try (final PrintStream log = new PrintStream(trainerName + ".csv", StandardCharsets.UTF_8)) {
            trainer.initialize(net, data);
            trainer.train(0, log);
        }
    }
}

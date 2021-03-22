package com.algotrading.base.lib.nnets.test;

import com.algotrading.base.lib.Rnd;
import com.algotrading.base.lib.nnets.NNet3L;
import com.algotrading.base.lib.nnets.NNetData;
import com.algotrading.base.lib.nnets.NNetMetaTrainer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class NNetMetaTrainerTestCase {
    public static void main(final String[] args) throws IOException {
        final Rnd rnd = new Rnd(1);
        final NNetData data = new NNetData(rnd, 50 * 1000, 50 * 1000);
        data.load("max.txt");
        final NNet3L net = new NNet3L(data.adviseNetConfiguration(1.8, 20, 1000), false);
        train(net, data);
    }

    private static void train(final NNet3L net, final NNetData data) throws IOException {
        try (final PrintStream log = new PrintStream("log.txt", StandardCharsets.UTF_8)) {
            final NNetMetaTrainer metaTrainer = new NNetMetaTrainer();
            metaTrainer.train(net, data, new double[]{0.0}, log);
        }
    }
}

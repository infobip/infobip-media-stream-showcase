package com.infobip.calls.mediastream.infobipcallsmediastreamclient.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class AudioParser {
    private final int samplesPerFrame;
    private final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    private short[] bytesToShorts(ByteBuffer byteBuffer) {
        var shorts = new short[samplesPerFrame];
        byteBuffer.order(byteOrder);
        byteBuffer.asShortBuffer().get(shorts);
        return shorts;
    }

    private ByteBuffer shortsToBytes(short[] shorts) {
        var byteBuffer = ByteBuffer.allocate(shorts.length * 2);
        byteBuffer.order(byteOrder);
        byteBuffer.asShortBuffer().put(shorts);
        return byteBuffer;
    }

    private double[] shortsToDoubles(short[] shorts) {
        var doubles = new double[shorts.length];
        IntStream.range(0, doubles.length).forEach(i -> doubles[i] = shorts[i] / 32768f);
        return doubles;
    }

    private short[] doublesToShorts(double[] doubles) {
        var shorts = new short[doubles.length];
        IntStream.range(0, doubles.length).forEach(i -> {
            // clip values between -1 and 1
            doubles[i] = Math.min(doubles[i], 1);
            doubles[i] = Math.max(doubles[i], -1);
            shorts[i] = (short) (doubles[i] * 32767);
        });
        return shorts;
    }

    public double[] unpackAudio(ByteBuffer byteBuffer) {
        return shortsToDoubles(bytesToShorts(byteBuffer));
    }

    public ByteBuffer packAudio(double[] rawAudio) {
        return shortsToBytes(doublesToShorts(rawAudio));
    }
}


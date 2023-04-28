package com.infobip.calls.mediastream.infobipcallsmediastreamclient.utils;

import com.github.psambit9791.jdsp.filter.Butterworth;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.nio.ByteBuffer;

@Slf4j
public class AudioFilter {
    @Getter
    private final int samplesPerFrame;
    private final int sampleRate;
    private final AudioParser audioParser;
    private final Butterworth butterworthFilter;

    public AudioFilter(int samplesPerFrame, int sampleRate) {
        this.sampleRate = sampleRate;
        this.samplesPerFrame = samplesPerFrame;
        this.audioParser = new AudioParser(samplesPerFrame);
        this.butterworthFilter = new Butterworth(this.sampleRate);
    }

    // Method implements bandpass filter from 300Hz to 3.4kHz.
    private double[] voiceFilter(double[] audio) {
        return this.butterworthFilter.bandPassFilter(audio, 6, 300, 3400);
    }

    public ByteBuffer processFrame(ByteBuffer frame) {
        var rawAudio = audioParser.unpackAudio(frame);
        var modifiedAudio = voiceFilter(rawAudio);
        return audioParser.packAudio(modifiedAudio);
    }

}

package com.infobip.calls.mediastream.infobipcallsmediastreamclient.socketserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infobip.calls.mediastream.infobipcallsmediastreamclient.utils.AudioFilter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Component
public class WebSocketServerImpl extends WebSocketServer {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<WebSocket, AudioFilter> openConnections = new HashMap<>();

    public WebSocketServerImpl(@Value("${media-stream-client.address}") String address,
                               @Value("${media-stream-client.port}") int port,
                               @Value("${media-stream-client.max-threads}") int maxThreads) {
        super(new InetSocketAddress(address, port), maxThreads);
    }

    @PostConstruct
    private void startWebSocketServer() {
        this.start();
    }

    private void parseMultipleFrames(WebSocket webSocket, ByteBuffer byteBuffer) {
        var audioFilter = openConnections.get(webSocket);
        var nBytesPerFrame = audioFilter.getSamplesPerFrame() * 2;
        var nFrames = byteBuffer.capacity() / nBytesPerFrame;

        IntStream.range(0, nFrames).forEach(i -> {
            var singleFrameBuffer = byteBuffer.duplicate();
            singleFrameBuffer.position(i * nBytesPerFrame);
            singleFrameBuffer.limit((i + 1) * nBytesPerFrame);
            var returnBuffer = audioFilter.processFrame(singleFrameBuffer);
            log.info("Sending back {} bytes. ", returnBuffer.capacity());
            webSocket.send(returnBuffer);
        });
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log.info("New connection: {} {}", clientHandshake.getResourceDescriptor(), webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        log.info("Closed connection: {} with exit code: {} additional info: {}", webSocket.getRemoteSocketAddress(), code, reason);
        openConnections.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        log.info("Received message from {}: {}", webSocket.getRemoteSocketAddress(), message);
        try {
            JsonNode jsonNode = mapper.readTree(message);
            var sampleRate = jsonNode.get("sampleRate").asInt();
            var packetizationTime = jsonNode.get("packetizationTime").asInt();
            var samplesPerFrame = sampleRate * packetizationTime / 1000;

            openConnections.put(webSocket, new AudioFilter(samplesPerFrame, sampleRate));

            log.info("Initialized audioFilter with {} samples per frame.", samplesPerFrame);
        } catch (JsonProcessingException e) {
            log.warn("Error while parsing received JSON from FreeSwitch. Closing connection.", e);
            webSocket.close(1011, "Could not parse initialMetadata JSON.");
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteBuffer message) {
        log.info("Received raw bytes from: " + webSocket.getRemoteSocketAddress());
        if (message.capacity() != openConnections.get(webSocket).getSamplesPerFrame() * 2 ){
            log.info("Number of bytes in message = {}, parsing out and processing individual frames.", message.capacity());
            parseMultipleFrames(webSocket, message);
        } else {
            ByteBuffer returnBuffer = openConnections.get(webSocket).processFrame(message);
            log.info("Sending back {} bytes. ", returnBuffer.capacity());
            webSocket.send(returnBuffer);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        log.error("Error occurred on connection {}: ", webSocket.getRemoteSocketAddress(), e);
    }

    @Override
    public void onStart() {
        log.info("WebSocketServer started successfully at {}", getAddress());
    }
}

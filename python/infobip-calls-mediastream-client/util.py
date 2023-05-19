import numpy as np
import json

DEFAULT_BYTE_SIZE = 2


def parse_string_message(websocket, message):
    message_json = json.loads(message)

    call_id = message_json['callId']
    sample_rate = int(message_json['sampleRate'])
    packetization_time = int(message_json['packetizationTime'])
    samples_per_packet = sample_rate * packetization_time * DEFAULT_BYTE_SIZE // 1000

    return call_id, sample_rate, packetization_time, samples_per_packet


def float_to_int(audio):
    return np.array([np.uint16(sample*32768) for sample in audio], dtype=np.uint16)


def buf_to_float(buffer, n_bytes=2, dtype=np.float32):
    scale = 1.0 / float(1 << ((8 * n_bytes) - 1))
    fmt = f"<i{n_bytes:d}"
    return scale * np.frombuffer(buffer, fmt).astype(dtype)


# Split bytearray to multiple packets in case reading/transferring audio is slow,
# and we receive multiple packets at once.
def split_audio(buffer, samples_per_packet):
    return [buffer[i:i + samples_per_packet] for i in range(0, len(buffer), samples_per_packet)]

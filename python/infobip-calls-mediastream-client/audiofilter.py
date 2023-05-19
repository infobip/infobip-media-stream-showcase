import numpy as np
import util
from scipy import signal


class AudioFilter:
	def __init__(self, filter_order, cut_off, filter_type='bandpass', sample_rate=8000,
				 packetization_time=20, samples_per_packet=320 ):
		self.sample_rate = sample_rate
		self.packetization_time = packetization_time
		self.samples_per_packet = samples_per_packet
		self.filter_inst = signal.butter(filter_order, cut_off, filter_type, fs=sample_rate, output='sos')

	def filter(self, audio):
		return signal.sosfilt(self.filter_inst, audio)

	def filter_bytes(self, audio_bytes):
		chunk_float = util.buf_to_float(audio_bytes, n_bytes=2, dtype=np.float32)
		filtered = self.filter(chunk_float)
		filtered_int = util.float_to_int(filtered)
		return filtered_int

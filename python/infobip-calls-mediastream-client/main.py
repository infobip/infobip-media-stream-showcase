#!/usr/bin/env python3

import yaml
import asyncio
import json
import util
import os
import websockets.exceptions
from audiofilter import AudioFilter
from websocketserver import WebSocketServer


# Configuration loading
media_stream_client_config = {}
try:
	with open('config.yml', 'r') as config_stream:
		config = yaml.safe_load(os.path.expandvars(config_stream.read()))
		media_stream_client_config = config["media-stream-client"]
except yaml.YAMLError as exc:
	print(exc)
except FileNotFoundError as exc:
	print(exc)

HOSTNAME = media_stream_client_config.get("address", "0.0.0.0")
PORT = media_stream_client_config.get("port", "3001")
MAX_THREADS = media_stream_client_config.get("max-threads", 5)


async def handler(websocket, path):
	audio_filter = None

	try:
		async for message in websocket:
			if type(message) is str:
				print("Received string from client: {}.".format(json.loads(message)))

				try:
					call_id, sample_rate, packetization_time, samples_per_packet = \
						util.parse_string_message(websocket, message)

					audio_filter = AudioFilter(sample_rate=sample_rate, packetization_time=packetization_time,
											   samples_per_packet=samples_per_packet, filter_order=6, cut_off=[300, 3400],
											   filter_type='bandpass')
				except Exception as e:
					print(f"Error parsing initialization message.")
					return

			elif type(message) is bytes:
				if audio_filter is None:
					print(f"Did not receive initialization message. Stopping the socket.")
					return
				print(f"Received {len(message)} bytes from client.")

				try:
					audio = util.split_audio(buffer=message, samples_per_packet=audio_filter.samples_per_packet)
					if audio is not None:
						for chunk in audio:
							filtered_audio = audio_filter.filter_bytes(audio_bytes=chunk)
							await websocket.send(filtered_audio.tobytes())
				except Exception as e:
					print(f"Error filtering received data.")
	except websockets.exceptions.ConnectionClosedOK as e:
		print(f"Connection closed gracefully: {e}")
	except Exception as e:
		print(f"Exception occurred: {e}")


if __name__ == "__main__":
	websocket_server = WebSocketServer(HOSTNAME, PORT, handler)
	loop = asyncio.get_event_loop()
	loop.run_until_complete(websocket_server.start_server())


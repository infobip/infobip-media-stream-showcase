import websockets
import asyncio


class WebSocketServer:
	def __init__(self, host, port, handler):
		self.host = host
		self.port = port
		self.handler = handler

	async def start_server(self):
		async with websockets.serve(self.handler, self.host, self.port):
			await asyncio.Future()

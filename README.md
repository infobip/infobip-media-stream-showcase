# Infobip media stream showcase

This simple application is an example of a websocket server that parses incoming media streamed from Infobip.
Received media is filtered using a bandpass [Butterworth filter](https://en.wikipedia.org/wiki/Butterworth_filter)
from 300 to 3400Hz.

### Setup

1. Have an Infobip account with access to Infobip API.
2. Have an established call using Infobip calls
   API ([example guide](https://github.com/infobip/infobip-calls-showcase)).
3. Start the Python or Java application from this repository.
4. Expose the application's websocket port to public internet. Easiest way to do so is via [ngrok](https://ngrok.com/).
   After installing ```ngrok```, just run: ```ngrok tcp [portNumber]```.
5. Have media streaming enabled and an appropriate `media-stream configuration` created. The configuration should point
   to the address exposed by ```ngrok```.
6. Start streaming media

### Some notes

- Due to GIL, the Python application can only handle one connection in parallel. If you need more connections, you'll
  need to start them as separate processes.
- The Java server can handle multiple connections in parallel. To ensure each connection has its separate filter, every
  websocket connection is kept in a map, paired with its appropriate filter.
- By default, the first message you'll receive from Infobip will be a string containing metadata about the incoming
  media:
  ```json
  {
    "callId": "d8d84155-3831-43fb-91c9-bb897149a79d",
    "sampleRate": 48000,
    "packetizationTime": 20
  }
  ```
  Packetization time is expressed in milliseconds, sampling rate in Hz. The remaining incoming messages will be bytes.
- If media replacement is not enabled in the `media-stream configuration`, any media sent to Infobip will be ignored.
  This means you can use media-streaming to log data for processing later.

### Effect

This application implements a bandpass filter from 300-3400Hz. Specifically, the Butterworth filter is implemented,
as it is one of the simplest filters that can achieve good results in the bandpass. The filter will significantly
attenuate frequencies outside the bandpass. The 300-3400Hz range was very commonly used in old telephony systems and is
referred to as the [voice frequency](https://en.wikipedia.org/wiki/Voice_frequency), so you can expect to hear lower
quality audio than what you normally get from Infobip and a nostalgic reminder of old telephony calls.

If you want to use a different filter or algorithm for modifying the media, all you have to do is change the filtering
method from this application. The rest of the code can remain as is.


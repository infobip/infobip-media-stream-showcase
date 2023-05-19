# Starting the client

### Prerequisites

- Python 3.x

### Running the python-app

### Step 1

Clone this application from [GitHub repository](https://github.com/infobip/infobip-media-stream-showcase):

`git clone git@github.com:infobip/infobip-media-stream-showcase.git`

### Step 2

Edit the `config.yml` file. You can modify the values of the hostname, exposed port and maximum number of threads.


### Step 3

Start your application:

`python3 main.py`


### Modifying the application filter

As already mentioned, our example applies a simple Butterworth filter onto the incoming media. If you want to implement
another filter, or any arbitrary/custom media processing all you have to do is modify the
[AudioFilter](./audiofilter.py#L18)
class. More specifically, the [filter_bytes](./audiofilter.py#L27)
method of the given class unpacks the received media, applies an arbitrary filter and then packs the filtered media
back. It's sufficient to just change the method which modifies the media.

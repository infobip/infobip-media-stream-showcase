# Starting the client

### Prerequisites

- Installed [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
- Installed JDK 17 or newer. Check version with `java -version`
- Installed [maven](https://maven.apache.org/install.html)

### Running the java-app

### Step 1

Clone this application from [GitHub repository](https://github.com/infobip/infobip-media-stream-showcase):

`git clone git@github.com:infobip/infobip-media-stream-showcase.git`

### Step 2

Edit the `application.yml` file. You can modify the values of the hostname, exposed port and maximum number of threads.


### Step 3

Compile and build application using this command:

`mvn clean package`

### Step 4

Start your application:

`java -jar target/infobip-calls-mediastream-client-1.0.0.jar`


### Modifying the application filter

As already mentioned, our example applies a simple Butterworth filter onto the incoming media. If you want to implement
another filter, or any arbitrary/custom media processing all you have to do is modify the
[AudioFilter](./src/main/java/com/infobip/calls/mediastream/infobipcallsmediastreamclient/utils/AudioFilter.java#L9)
class. More specifically, the [processFrame](./src/main/java/com/infobip/calls/mediastream/infobipcallsmediastreamclient/utils/AudioFilter.java#L28)
method of the given class unpacks the received media, applies an arbitrary filter and then packs the filtered media
back. It's sufficient to just change the method which modifies the media.

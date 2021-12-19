import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import lib.Network;
import lib.log;

public abstract class Server {
	static boolean dynamic = true; // Dynamic mode?
	static int port = 443; // Port Number
	static int MaxConcurrentRequests = 10000; // MaxConcurrentReqs in settings.conf
	static int CurrentConcurrentRequests = 0; // Current Concurrent Requests
	static boolean gzip = true; // GZip compression? (default false)
	static int MAX_REQ_SIZE = 1000000; // Max bytes to read in kb. (default 1000MB)
	static String defaultResponseMIME = "text/html"; // default content type
	static String defaultResponseCode = "HTTP/1.1 200 OK"; // default HTTP code
	static String addedResHeaders = "";
	static SSLServerSocket SS = null;

	/*
	 * SSL Context
	 */
	SSLContext getSSLContext(Path keyStorePath, char[] keyStorePass) {
		try {
			var keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream(keyStorePath.toFile()), keyStorePass);
			var keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, keyStorePass);
			var sslContext = SSLContext.getInstance("TLSv1.3");
			sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
			return sslContext;
		} catch (Exception e) {
			log.e(e, Server.class.getName(), "getSSLContext");
			return null;
		}
	}

	/*
	 * SSL Server Socket
	 */
	ServerSocket getServerSocket(InetSocketAddress address) {
		try {
			int backlog = MaxConcurrentRequests * 10;
			var keyStorePath = Path.of("./keystore.jks");
			char[] keyStorePassword = "ofels1250".toCharArray();
			var serverSocket = getSSLContext(keyStorePath, keyStorePassword).getServerSocketFactory()
					.createServerSocket(address.getPort(), backlog, address.getAddress());
			Arrays.fill(keyStorePassword, '0');
			return serverSocket;
		} catch (Exception e) {
			log.e(e, Server.class.getName(), "getServerSocket");
			return null;
		}
	}

	/*
	 * Starts the Server. The entry point, basically.
	 */
	void start() {
		try {
			SS = (SSLServerSocket) getServerSocket(new InetSocketAddress("0.0.0.0", port));
			log.s("Server started on port " + port);
			while (true) {
				/*
				 * Number of requests processed at the same time MUST be limited!
				 */
				// Retries for 20 times, interval is 1x ms.
				int tries = 0; // current tries
				inner: while (tries < 21) {
					if (tries > 0)
						Thread.sleep(1);
					if (CurrentConcurrentRequests <= MaxConcurrentRequests) {
						SSLSocket S = (SSLSocket) SS.accept();
						Engine e = new Engine(S);
						e.start();
						CurrentConcurrentRequests++;
						break inner;
					} else {
						tries++;
					}
				}
			}
		} catch (Exception e) {
			log.e(e, Server.class.getName(), "start");
		}
	}

	/*
	 * Reads the HTTP request, then makes a hashmap to store headers n' stuff.
	 */
	HashMap<String, String> reqTranslator(String req) {
		HashMap<String, String> data = new HashMap<String, String>();
		String[] lines = req.split("\r\n");
		String[] fir_data = lines[0].split(" ");
		data.put("method", fir_data[0]);
		data.put("path", fir_data[1]);
		for (int i = 1; i < lines.length; i++) {
			String[] temp = lines[i].split(": ");
			data.put(temp[0], temp[1]);
		}
		return data;
	}

	static byte[] toPrimitives(Byte[] oBytes) {
		byte[] bytes = new byte[oBytes.length];
		for (int i = 0; i < oBytes.length; i++) {
			bytes[i] = oBytes[i];
		}
		return bytes;
	}

	/*
	 * Main HTTP Engine
	 */
	public class Engine extends Thread {
		DataInputStream DIS;
		DataOutputStream DOS;
		SSLSocket s;

		Engine(SSLSocket i) {
			try {
				s = i;
				DIS = new DataInputStream(i.getInputStream());
				DOS = new DataOutputStream(i.getOutputStream());
			} catch (IOException e) {
				log.e(e, Engine.class.getName(), "Constructor");
			}
		}

		/*
		 * Where it all begins!
		 */
		@SuppressWarnings("deprecation")
		public void run() {
			// Read from Socket
			ArrayList<Byte> reqq = Network.read(DIS, MAX_REQ_SIZE);
			byte[] req = toPrimitives(reqq.toArray(Byte[]::new));

			// From ArrayList to String, through Byte array!
			long f = System.nanoTime();
			/*
			 * If we're on dynamic mode, go and read output from main(..) Else, go static!
			 */
			List<byte[]> headerPost = tokens(req, new byte[] { 13, 10, 13, 10 }); // split using \r\n
			String o = new String(headerPost.get(0));
			ArrayList<Byte> additional = new ArrayList<Byte>();
			if (headerPost.size() > 2) {
				for (int i = 0; i < headerPost.get(2).length; i++) {
					additional.add(headerPost.get(2)[i]);
				}
				if (reqTranslator(o).containsKey("Content-Length")
						&& (req.length - headerPost.get(0).length) < Integer
								.valueOf(reqTranslator(o).get("Content-Length"))
						&& Integer.valueOf(reqTranslator(o).get("Content-Length")) < 100000000) {
					// Other data available
					byte[] add = Network.ManRead(DIS, Integer.valueOf(reqTranslator(o).get("Content-Length"))
							- (headerPost.get(1).length + headerPost.get(2).length + 4)); // Read n bytes
					for (int i = 0; i < add.length; i++) {
						additional.add(add[i]);
					}
				}
			}
			if (headerPost.size() > 1) {
				if (headerPost.size() > 2) {
					Network.write(DOS, main(o, headerPost.get(1), s, toPrimitives(additional.toArray(Byte[]::new))),
							defaultResponseMIME, defaultResponseCode, gzip, addedResHeaders);
				} else {
					Network.write(DOS, main(o, headerPost.get(1), s, null), defaultResponseMIME, defaultResponseCode,
							gzip, addedResHeaders);
				}
			} else {
				Network.write(DOS, main(o, null, s, null), defaultResponseMIME, defaultResponseCode, gzip,
						addedResHeaders);
			}
			defaultResponseMIME = "text/html";
			defaultResponseCode = "HTTP/1.1 200 OK";
			addedResHeaders = "";
			System.out.println((System.nanoTime() - f) / 1000000.0 );
			CurrentConcurrentRequests--;
			Thread.currentThread().interrupt();
		}
	}

	/*
	 * When you go dynamic, override this in main.java
	 */
	abstract byte[] main(String req, byte[] body, Socket s, byte[] additional);

	/*
	 * File Path Tweaks
	 */
	public String pathfilter(String path) {
		String res = path;
		res = res.replaceAll("\\.\\.", ""); // LFI protection, i guess?
		res = res.replaceAll("//", "/");
		if (res.endsWith("/"))
			res = res + "index.html";
		return res;
	}

	/*
	 * Splits arr according to delimiter sequence
	 */
	public List<byte[]> tokens(byte[] array, byte[] delimiter) {
		List<byte[]> byteArrays = new LinkedList<>();
		int begin = 0;

		outer: for (int i = 0; i < array.length - delimiter.length + 1; i++) {
			for (int j = 0; j < delimiter.length; j++) {
				if (array[i + j] != delimiter[j]) {
					continue outer;
				}
			}
			byteArrays.add(Arrays.copyOfRange(array, begin, i));
			begin = i + delimiter.length;
		}
		byteArrays.add(Arrays.copyOfRange(array, begin, array.length));
		return byteArrays;
	}
}
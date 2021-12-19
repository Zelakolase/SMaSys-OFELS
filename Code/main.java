import java.io.File;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.event.ListSelectionEvent;

import lib.IO;
import lib.SparkDB;
import lib.log;
import lib.TextToHashmap;

/*
 * ./keytool -genkeypair -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass ofels1250 -dname "CN=localhost, OU=OFELS-DEV, O=OFELS, C=EG"
 */

public class main extends Server {
	HashMap<String, String> SessionIDs = new HashMap<String, String>(); // ID, Username
	SparkDB UP = new SparkDB("auth.dat"); // User Pass DB
	SparkDB MIME = new SparkDB("MIME.dat"); // MIME type DB
	/*
	 * Entry Point
	 */

	public static void main(String[] args) {
		main m = new main();
		m.start();
		T t = new T();
		t.start();
	}

	public static class T extends Thread {
		@Override
		public void run() {
			while (true) {
				Runtime rt = Runtime.getRuntime();
				if (((rt.totalMemory() - rt.freeMemory()) / 1024 / 1024) > 700) {
					System.gc();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	// REMEMBER : ADJUST RESPONSE CODE & CONTENT TYPE WHEN DATA IS BYTE[]
	@Override
	byte[] main(String req, byte[] body, Socket s, byte[] additional) {
		try {
			byte[] response = "".getBytes();
			HashMap<String, String> reqHM = reqTranslator(req); // Request HM
			String path = pathfilter(reqHM.get("path")); // requested path
			String method = reqHM.get("method"); // requested method "GET,POST"
			String SessionID = "";

			/*
			 * Get SessionID from Cookies
			 */
			if (reqHM.containsKey("Cookie")) {
				SessionID = reqHM.get("Cookie").replace(";", "").replace("ID=", "").replaceFirst("S", ""); // ID=5; -> 5
			}
			/*
			 * GET main page
			 */
			if (method.equals("GET") && path.equals("/index.html")) {
				if (SessionID.isEmpty()) { // not logged in
					response = IO.read("./www/login.dsp");
				} else {
					if (SessionIDs.containsKey(SessionID)) {
						if (SessionIDs.get(SessionID).equals("admin")) { // is admin
							response = IO.read("./www/home-admin.dsp");
						} else { // is NOT admin
							response = (new String(IO.read("./www/home-user.dsp")).replace("[[full-user]]",
									UP.get("username", SessionIDs.get(SessionID), "name"))).getBytes();
						}
					} else {
						response = new String(IO.read("./www/login.dsp"))
								.replaceFirst("<html>", HTMLMessageFactory("red", "خطأ في تسجيل الدخول")).getBytes();
						addedResHeaders = "Set-Cookie: SID=0;Max-Age=0\r\n"; // Zeroing the SessionID
					}
				}
			}
			/*
			 * Login
			 */
			else if (method.equals("POST") && path.equals("/index.html")) {
				/*
				 * See if its a valid login
				 */
				HashMap<String, String> PostArguments = TextToHashmap.Convert(new String(body), "&", "=");
				/*
				 * Login Functionality
				 */
				if (PostArguments.containsKey("LUser") && PostArguments.containsKey("LPass")) {
					if (UP.Mapper.get("username").contains(PostArguments.get("LUser"))) {
						/*
						 * Valid Username
						 */
						String ValidUsername = UP.get("username", PostArguments.get("LUser"), "password");
						if (ValidUsername.equals(PostArguments.get("LPass"))) {
							/*
							 * Valid Password
							 */
							// 1st. step : assign a sessionid
							String rnd = RandomStringFactory();
							SessionIDs.put(rnd, PostArguments.get("LUser"));

							// 2nd. step : set custom header
							addedResHeaders = "Set-Cookie: SID=" + rnd + "\r\n";

							// 3rd. step : redirect to home
							if (SessionIDs.get(rnd).equals("admin")) {
								/*
								 * The user is admin
								 */
								IO.write("log.dat", s.getRemoteSocketAddress() + " is logged in to admin<br>", true);
								response = IO.read("./www/home-admin.dsp");
							} else {
								/*
								 * The user isn't admin
								 */
								IO.write("log.dat",
										s.getRemoteSocketAddress() + " is logged in to "
												+ UP.get("username", PostArguments.get("LUser"), "name") + "<br>",
										true);
								response = (new String(IO.read("./www/home-user.dsp")).replace("[[full-user]]",
										UP.get("username", PostArguments.get("LUser"), "name"))).getBytes();
							}
						} else {
							/*
							 * Invalid Password
							 */
							response = new String(IO.read("./www/login.dsp")).replaceFirst("<html>",
									HTMLMessageFactory("red", "كلمة المرور غير صحيحة") + "</html>").getBytes();
						}
					} else {
						/*
						 * Username not found
						 */
						response = new String(IO.read("./www/login.dsp"))
								.replaceFirst("<html>", HTMLMessageFactory("red", "المستخدم غير موجود") + "</html>")
								.getBytes();
					}
				}
				/*
				 * Logged in users
				 */
				else if (SessionIDs.containsKey(SessionID)) {
					/*
					 * Admin Actions : START
					 */
					if (SessionIDs.get(SessionID).equals("admin")) {
						if (PostArguments.containsKey("gc") && PostArguments.get("gc").equals("1")) {
							/*
							 * (G)arbage (C)ollection
							 */
							IO.write("log.dat", s.getRemoteSocketAddress() + " made a garbage collection action<br>",
									true);
							System.gc();
							response = new String(IO.read("./www/home-admin.dsp"))
									.replaceFirst("<html>",
											HTMLMessageFactory("green", "تم اخلاء ذاكرة الوصول العشوائي") + "</html>")
									.getBytes();
						} else if (PostArguments.containsKey("log") && PostArguments.get("log").equals("1")) {
							/*
							 * View Log
							 */
							IO.write("log.dat", s.getRemoteSocketAddress() + " viewed the log<br>", true);
							response = new String(IO.read("./www/log.dsp"))
									.replace("[[LOG]]", "<p>" + new String(IO.read("log.dat")) + "</p>")
									.replace("[[TITLE]]", "السجل").getBytes();

						} else if (PostArguments.containsKey("add") && PostArguments.get("add").equals("true")) {
							/*
							 * Add a user
							 */
							String username = PostArguments.get("username");
							String name = PostArguments.get("name");
							String password = PostArguments.get("password");
							UP.add(new String[] { username, name, password });
							IO.write("auth.dat", UP.print(), false);
							IO.write("log.dat",
									s.getRemoteSocketAddress() + " added a user, " + username + " " + name + "<br>",
									true);
							File f0 = new File("./mail/" + username + ".inbox");
							File f1 = new File("./mail/" + username + ".outbox");
							f0.createNewFile();
							f1.createNewFile();
							IO.write("./mail/" + username + ".inbox",
									"\"at\",\"from\",\"subject\",\"content\"" + "\n\"0\",\"0\",\"0\",\"0\"", false);
							IO.write("./mail/" + username + ".outbox",
									"\"at\",\"to\",\"subject\",\"content\"" + "\n\"0\",\"0\",\"0\",\"0\"", false);
							response = new String(IO.read("./www/home-admin.dsp")).replaceFirst("<html>",
									HTMLMessageFactory("green", "تم اضافة المستخدم") + "</html>").getBytes();
						} else if (PostArguments.containsKey("delete") && PostArguments.get("delete").equals("true")) {
							/*
							 * Delete a user
							 */
							String username = PostArguments.get("username");
							UP.delete(new String[] { username, UP.get("username", username, "name"),
									UP.get("username", username, "password") });
							IO.write("auth.dat", UP.print(), false);
							IO.write("log.dat", s.getRemoteSocketAddress() + " deleted a user, " + username + "<br>",
									true);
							response = new String(IO.read("./www/home-admin.dsp"))
									.replaceFirst("<html>", HTMLMessageFactory("green", "تم حذف المستخدم") + "</html>")
									.getBytes();

						} else if (PostArguments.containsKey("logout") && PostArguments.get("logout").equals("1")) {
							/*
							 * Admin logs out
							 */
							SessionIDs.remove(SessionID);
							addedResHeaders = "Set-Cookie: SID=0;Max-Age=0\r\n";
							IO.write("log.dat", s.getRemoteSocketAddress() + " logged out.<br>", true);
							response = new String(IO.read("./www/login.dsp"))
									.replaceFirst("<html>", HTMLMessageFactory("red", "تم تسجيل الخروج") + "</html>")
									.getBytes();
						} else if (PostArguments.containsKey("table") && PostArguments.get("table").equals("1")) {
							/*
							 * User List
							 */
							response = new String(IO.read("./www/log.dsp"))
									.replace("[[LOG]]", HTMLSparkTableFactory(UP))
									.replace("[[TITLE]]", "قائمة المستخدمين").getBytes();
							IO.write("log.dat", s.getRemoteSocketAddress() + " viewed the user list<br>", true);
						} else {
							response = new String(IO.read("./www/home-admin.dsp"))
									.replaceFirst("<html>", HTMLMessageFactory("red", "الفعل غير مفهوم") + "</html>")
									.getBytes();
						}
					}
					/*
					 * Admin Actions : END
					 */
					else {
						// USER CONSTRUCTION
						if (PostArguments.containsKey("logout") && PostArguments.get("logout").equals("1")) {
							SessionIDs.remove(SessionID);
							addedResHeaders = "Set-Cookie: SID=0;Max-Age=0\r\n";
							IO.write("log.dat", s.getRemoteSocketAddress() + " logged out.<br>", true);
							response = new String(IO.read("./www/login.dsp"))
									.replaceFirst("<html>", HTMLMessageFactory("red", "تم تسجيل الخروج") + "</html>")
									.getBytes();
						}
					}
				} else {
					response = new String("").replaceFirst("<html>", HTMLMessageFactory("red", "غير مصرح") + "</html>")
							.getBytes();
				}
			}
			// FILES, MAIL, PERSONAL
			/*
			 * Files
			 */
			else if (path.equals("/files") && SessionIDs.containsKey(SessionID)) {
				if (method.equals("GET")) {
					/*
					 * View it
					 */
					response = IO.read("./www/files.dsp");
				} else {
					/*
					 * An action
					 */
					HashMap<String, String> PostArguments = TextToHashmap.Convert(new String(body), "&", "=");
					if (PostArguments.containsKey("id")) {
						String id = PostArguments.get("id").replaceAll("%20", " ");
						SparkDB db = new SparkDB("./files/db.dat");
						String filename = db.get("id", id, "filename");
						response = IO.read("./files/" + filename);
						defaultResponseMIME = MIME.get("extension", filename.split("\\.")[1], "mime");
					}
				}
			} else if (path.equals("/personal") && SessionIDs.containsKey(SessionID)) {
				if (method.equals("GET")) {
					response = new String(IO.read("./www/personal.dsp"))
							.replace("[[Full-Name]]", UP.get("username", SessionIDs.get(SessionID), "name"))
							.replace("[[User-Name]]", SessionIDs.get(SessionID)).getBytes();
				} else {
					HashMap<String, String> PostArguments = TextToHashmap.Convert(new String(body), "&", "=");
					// oldpwd=0,newpwd=0,confirmpwd=0
					if (PostArguments.containsKey("oldpwd") && PostArguments.containsKey("newpwd")
							&& PostArguments.containsKey("confirmpwd")) {
						/*
						 * Change the password
						 */
						if (PostArguments.get("oldpwd")
								.equals(UP.get("username", SessionIDs.get(SessionID), "password"))) {
							/*
							 * Correct Old Pass
							 */
							if (PostArguments.get("newpwd").equals(PostArguments.get("confirmpwd"))) {
								/*
								 * CONFIRM
								 */
								String username = SessionIDs.get(SessionID);
								String name = UP.get("username", SessionIDs.get(SessionID), "name");

								UP.delete(new String[] {
										// username
										SessionIDs.get(SessionID),
										// name
										UP.get("username", SessionIDs.get(SessionID), "name"),
										// password
										UP.get("username", SessionIDs.get(SessionID), "password") });
								UP.add(new String[] {
										// username
										username,
										// name
										name,
										// password
										PostArguments.get("newpwd") });

								IO.write("./auth.dat", UP.print(), false);
								IO.write("log.dat", s.getRemoteSocketAddress() + " changed his password.<br>", true);
								response = new String(IO.read("./www/personal.dsp"))
										.replace("[[Full-Name]]", UP.get("username", SessionIDs.get(SessionID), "name"))
										.replace("[[User-Name]]", SessionIDs.get(SessionID))
										.replace("<html>", HTMLMessageFactory("green", "تم تغيير كلمة المرور"))
										.getBytes();
							} else {
								response = new String(IO.read("./www/personal.dsp"))
										.replace("[[Full-Name]]", UP.get("username", SessionIDs.get(SessionID), "name"))
										.replace("[[User-Name]]", SessionIDs.get(SessionID))
										.replace("<html>", HTMLMessageFactory("red",
												"كلمة المرور الجديدة ليست متطابقة مع التأكيد"))
										.getBytes();
							}
						} else {
							/*
							 * Incorrect Old pass
							 */
							response = new String(IO.read("./www/personal.dsp"))
									.replace("[[Full-Name]]", UP.get("username", SessionIDs.get(SessionID), "name"))
									.replace("[[User-Name]]", SessionIDs.get(SessionID))
									.replace("<html>", HTMLMessageFactory("red", "خطأ في كلمة المرور القديمة"))
									.getBytes();
						}
					} else {
						response = HTMLMessageFactory("red", "فعل غير مفهوم").getBytes();
					}

				}
			}

			else if (path.equals("/mail") && SessionIDs.containsKey(SessionID)) {
				if (method.equals("GET")) {

					response = new String(IO.read("./www/mail.dsp"))
							.replace("[[InboxTable]]",
									HTMLSparkTableFactory(
											new SparkDB("./mail/" + SessionIDs.get(SessionID) + ".inbox")))
							.replace("[[OutboxTable]]", HTMLSparkTableFactory(
									new SparkDB("./mail/" + SessionIDs.get(SessionID) + ".outbox")))
							.getBytes();

				} else {
					HashMap<String, String> PostArguments = TextToHashmap.Convert(new String(body), "&", "=");
					if (PostArguments.containsKey("to") && PostArguments.containsKey("subject")
							&& PostArguments.containsKey("content")) {
						/*
						 * Send mail
						 */
						String dest = PostArguments.get("to");
						String subj = PostArguments.get("subject");
						String cont = PostArguments.get("content");
						if (UP.Mapper.get("username").contains(PostArguments.get("to"))) {
							// write : my outbox
							SparkDB outbox = new SparkDB("./mail/" + SessionIDs.get(SessionID) + ".outbox");
							outbox.add(new String[] {
									// at
									LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
									// to
									dest,
									// subject
									subj,
									// content
									cont

							});
							IO.write("./mail/" + SessionIDs.get(SessionID) + ".outbox", outbox.print(), false);
							// write : his inbox
							SparkDB inbox = new SparkDB("./mail/" + dest + ".inbox");
							inbox.add(new String[] {
									// at
									LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
									// from
									SessionIDs.get(SessionID),
									// subj
									subj,
									// cont
									cont });
							IO.write("./mail/" + dest + ".inbox", inbox.print(), false);
							IO.write("log.dat", s.getRemoteSocketAddress() + " sent a mail to " + dest + ". <br>",
									true);
							response = (new String(IO.read("./www/mail.dsp"))
									.replace("[[InboxTable]]",
											HTMLSparkTableFactory(
													new SparkDB("./mail/" + SessionIDs.get(SessionID) + ".inbox")))
									.replace("[[OutboxTable]]",
											HTMLSparkTableFactory(
													new SparkDB("./mail/" + SessionIDs.get(SessionID) + ".outbox")))
									.replaceFirst("<html>", HTMLMessageFactory("green", "تم الأرسال"))).getBytes();
						} else {
							/*
							 * Dest. doesn't exist
							 */
							response = new String(IO.read("./www/mail.dsp"))
									.replace("[[InboxTable]]",
											HTMLSparkTableFactory(
													new SparkDB("./mail/" + SessionIDs.get(SessionID) + ".inbox")))
									.replace("[[OutboxTable]]",
											HTMLSparkTableFactory(
													new SparkDB("./mail/" + SessionIDs.get(SessionID) + ".outbox")))
									.replaceFirst("<html>", HTMLMessageFactory("red", "المستخدم غير موجود")).getBytes();

						}
					} else {
						response = HTMLMessageFactory("red", "فعل غير مفهوم").getBytes();
					}
				}
			} else if (path.equals("/upload") && SessionIDs.containsKey(SessionID)) {
				/*
				 * It's an upload!
				 */
				List<byte[]> multipart = tokens(body, new byte[] { 13, 10, 13, 10 });
				String[] fileMetadata = new String(multipart.get(0)).split("\r\n");
				String endBoundary = fileMetadata[0] + "--"; // after end file
				fileMetadata[1] = fileMetadata[1]
						.replaceFirst("Content-Disposition: form-data; name=\"filename\"; filename=\"", "");
				fileMetadata[1] = fileMetadata[1].substring(0, fileMetadata[1].length() - 1);

				File f = new File("./files/" + fileMetadata[1]);
				f.createNewFile();
				IO.write("./files/" + fileMetadata[1], tokens(additional, endBoundary.getBytes()).get(0), false);
				SparkDB files = new SparkDB("./files/db.dat");
				String id = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999 + 1));
				files.add(new String[] { fileMetadata[1], id });
				IO.write("./files/db.dat", files.print(), false);
				response = HTMLMessageFactory("green", "ID: " + id + ". Go back to home").getBytes();
			} else {
				defaultResponseMIME = MIME.get("extension", path.split("\\.")[1], "mime");
				response = IO.read("./www" + path);
			}

			return response;
		} catch (Exception e) {
			log.e(e, main.class.getName(), "main");
			return HTMLMessageFactory("red", "خطأ حدث بالخادم").getBytes();
		}
	}

	static String AbsMsg = "<center><h2 style=\"color: [[COLOR]];\"><b>[[MSG]]</b></h2></center>";

	public static String HTMLMessageFactory(String color, String Message) {
		return AbsMsg.replace("[[COLOR]]", color).replace("[[MSG]]", Message);
	}

	public static String HTMLSparkTableFactory(SparkDB e) {
		String out = "<table style=\"width:100%; table-layout: fixed;\">";
		out += "<tr>"; // HEADER START
		for (int i = 0; i < e.num_header; i++) {
			out += "<th>" + e.Headers.get(i) + "</th>";
		}
		out += "</tr>"; // HEADER END

		// Data Start
		for (int i = 0; i < e.num_queries; i++) {
			out += "<tr>";
			String[] data = e.getbyindex(i).split(",");
			for (int o = 0; o < data.length; o++) {
				out += "<td style=\"word-wrap: break-word\">";
				out += data[o];
				out += "</td>";
			}
			out += "</tr>";
		}
		out += "<table>";
		return out;
	}

	public static String RandomStringFactory() {
		char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890QWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
		StringBuilder sb = new StringBuilder(20);
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
}
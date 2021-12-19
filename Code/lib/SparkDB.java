package lib;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class SparkDB {
	public HashMap<String, ArrayList<String>> Mapper = new HashMap<String, ArrayList<String>>();
	public ArrayList<String> Headers = new ArrayList<String>();
	public int num_queries = 0;
	public int num_header = 0;

	public SparkDB(String file) {
		readfromfile(file);
	}

	public void readfromfile(String filename) {
		zero();
		boolean headerisprocessed = false;
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			String[] header = null;
			String temp_1 = "";
			String temp_0 = "";
			while ((line = br.readLine()) != null) {
				if (!headerisprocessed) {
					header = line.split("\",\""); // ","
					temp_1 = header[header.length - 1].substring(0, header[header.length - 1].length() - 1);
					temp_0 = header[0].substring(1);
					Mapper.put(temp_0, new ArrayList<String>());
					Headers.add(temp_0);
					for (int i = 1; i < header.length - 1; i++) {
						Mapper.put(header[i], new ArrayList<String>());
						Headers.add(header[i]);
					}
					Mapper.put(temp_1, new ArrayList<String>());
					Headers.add(temp_1);
					num_header = header.length;
					headerisprocessed = true;
				} else {
					num_queries++;
					String[] single_col = line.split("\",\""); // ","
					Mapper.get(temp_0).add(single_col[0].substring(1));
					for (int x = 1; x < num_header - 1; x++) {
						Mapper.get(header[x]).add(single_col[x]);
					}
					Mapper.get(temp_1)
							.add(single_col[num_header - 1].substring(0, single_col[num_header - 1].length() - 1));

				}
			}
			br.close();
		} catch (Exception e) {
			log.e(e,SparkDB.class.getName(),"readfromfile");
		}
	}

	public String get(String FromCol, String ColVal, String ColToFind) {
		try {
			return Mapper.get(ColToFind).get(Mapper.get(FromCol).indexOf(ColVal));
		} catch (Exception e) {
			log.e(e,SparkDB.class.getName(),"get");
			return null;
		}
	}

	public void add(String[] in) {
		try {
			for (int i = 0; i < num_header; i++) {
				Mapper.get(Headers.get(i)).add(in[i].replaceAll("\",", ""));
			}
			num_queries++;
		} catch (Exception e) {
			log.e(e,SparkDB.class.getName(),"add");
		}
	}
	public String getbyindex(int index) {
		String out = "";
		for(int i = 0;i<num_header;i++) {
		String temp = Headers.get(i);
		out += Mapper.get(temp).get(index);
		if(i+1<num_header) out += ",";
		}
		return out;
	}
	public void delete(String[] in) {
		try {
			for (int i = 0; i < num_header; i++) {
				Mapper.get(Headers.get(i)).remove(in[i]);
			}
			num_queries--;
		} catch (Exception e) {
			log.e(e,SparkDB.class.getName(),"delete");
		}
	}

	public String print() {
		String out = "";
		// Print Headers
		for (int i = 0; i < num_header; i++) {
			out += "\"" + Headers.get(i) + "\"";
			if (!((i + 1) == num_header)) {
				out += ",";
			}
		}
		out += "\n";
		// Print Data
		for (int i = 0; i < num_queries; i++) {
			for (int x = 0; x < num_header; x++) {
				out += "\"" + Mapper.get(Headers.get(x)).get(i) + "\"";
				if (!((x + 1) == num_header)) {
					out += ",";
				}
			}
			out += "\n";
		}
		return out;
	}

	String multiget(String FromCol, String ColVal, String ColToFind) {
		String out = "";
		for (int i = 0; i < num_queries; i++) {
			String temp = Mapper.get(FromCol).get(i);
			if (temp.equals(ColVal)) {
				out += Mapper.get(ColToFind).get(i) + ",";
			}
		}
		out = out.substring(0, out.length() - 1);
		return out;
	}

	void zero() {
		num_queries = 0;
		num_header = 0;
		Mapper = new HashMap<String, ArrayList<String>>();
		Headers = new ArrayList<String>();
	}
}

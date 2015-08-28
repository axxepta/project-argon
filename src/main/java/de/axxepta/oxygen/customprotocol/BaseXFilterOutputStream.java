/**
 * 
 */
package de.axxepta.oxygen.customprotocol;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.text.MessageFormat;

import de.axxepta.oxygen.api.BaseXClient;

/**
 * @author daltiparmak
 *
 */
public class BaseXFilterOutputStream extends FilterOutputStream {

	//private ArrayList<Byte> bytes = new ArrayList<Byte>();
	private URL url;
	private File temp;

	public String readFile(String filename) {
		File f = new File(filename);
		try {
			byte[] bytes = Files.readAllBytes(f.toPath());
			return new String(bytes, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @param out
	 */
	public BaseXFilterOutputStream(OutputStream out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

	public BaseXFilterOutputStream(OutputStream out, URL url) {
		super(out);
		this.url = url;
	}

	public BaseXFilterOutputStream(OutputStream out, File temp, URL url) {
		super(out);
		this.temp = temp;
		this.url = url;
	}


	/**
	 * @see java.io.FilterOutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
	}

	/**
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		System.out.println("b.length: " + b.length);
		System.out.println("off: " + off);
		System.out.println("len: " + len);

		/*
		for (int i = 0; i < b.length; i++) {
			this.bytes.add(b[i]);
		}
		*/
		super.write(b, off, len);
	}

	/**
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		super.write(b);
	}

	@Override
	public void close() throws IOException {
		super.close();
		// TODO Auto-generated method stub
		System.out.println(" ---- closed ----");

		final BaseXClient session = new BaseXClient("localhost", 1984, "admin",
				"admin");

		//String content = this.readFile(temp);
	
		String content = this.readFile(temp.getAbsolutePath());
		
		// define input stream
		InputStream bais = new ByteArrayInputStream(content.getBytes());

		String argonUrlPath = this.url.getPath();
		argonUrlPath = argonUrlPath.replaceFirst("/", "");
		String[] parts = argonUrlPath.split("/", 2);
		String database = parts[0];
		String path = parts[1];

		System.out.println("----------------------");
		System.out.println("Database: " + database);
		System.out.println("Path: " + path);

		session.execute(MessageFormat.format("open {0}", database));
		System.out.println(session.info());

		/*
		 * byte[] primitive = new byte[this.bytes.size()];
		 * 
		 * for (int i = 0; i < this.bytes.size(); i++) { Byte temp =
		 * this.bytes.get(i); primitive[i] = temp.byteValue(); }
		 * 
		 * String decoded = new String(primitive, "UTF-8");
		 * System.out.println(decoded);
		 * 
		 * InputStream bais = new ByteArrayInputStream(primitive);
		 * 
		 * String argonUrlPath = this.url.getPath(); argonUrlPath =
		 * argonUrlPath.replaceFirst("/", ""); String[] parts =
		 * argonUrlPath.split("/", 2); String database = parts[0]; String path =
		 * parts[1];
		 * 
		 * System.out.println("----------------------");
		 * System.out.println("Database: " + database);
		 * System.out.println("Path: " + path);
		 * 
		 * 
		 * session.execute(MessageFormat.format("open {0}", database));
		 * System.out.println(session.info());
		 */

		try {
			// replace document
			session.replace(path, bais);
			System.out.println(session.info());

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		boolean bool = false;
		try {
			// tries to delete the newly created file
			bool = temp.delete();
			// print
			System.out.println("File deleted: " + bool);

		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}
		session.close();
	
	}

}

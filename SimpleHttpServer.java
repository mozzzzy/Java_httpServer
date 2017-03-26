import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
 
public class SimpleHttpServer {
 
	private static String requestPath;
 
	private static Map<String, String> CONTENT_TYPES = new HashMap<String, String>();
 
	static {
		CONTENT_TYPES.put("html", "text/html");
		CONTENT_TYPES.put("htm", "text/html");
		CONTENT_TYPES.put("css", "text/css");
		CONTENT_TYPES.put("js", "text/javascript");
		CONTENT_TYPES.put("jpg", "image/jpeg");
		CONTENT_TYPES.put("jpeg", "image/jpeg");
		CONTENT_TYPES.put("png", "image/png");
		CONTENT_TYPES.put("gif", "image/gif");
		CONTENT_TYPES.put("pdf", "application/pdf");
		CONTENT_TYPES.put("txt", "text/plain");
		CONTENT_TYPES.put("xml", "text/xml");
		CONTENT_TYPES.put("zip", "application/zip");
		CONTENT_TYPES.put("exe", "application/octet-stream");
	}
 
	public static void main(String[] args) throws IOException {

		if(args.length != 2){
			System.out.println("Usage : SimpleHttpServer DOCUMENT_ROOT PORT");
			System.exit(-1);
		}

		String documentRoot = args[0];
		int port = Integer.parseInt(args[1]);
 
		ServerSocket server = new ServerSocket(port);
 
		while (true) {
			Socket client = server.accept();
 
			// HTTPリクエストを出力
			outputRequest(client);
 
			// HTTPレスポンスを出力
			outputResponse(client, documentRoot);
		}
	}
 
	private static void outputRequest(Socket client) throws IOException {
		System.out.println("------------------------------------------");
 
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
 
		// 1行目からリクエストパスを取得しておく（/でアクセスされた場合はindex.htmlを表示）
		String inline = br.readLine();
		requestPath = inline.split(" ")[1];
		if (requestPath.endsWith("/")) {
			requestPath += "index.html";
		}
 
		while (br.ready() && inline != null) {
			System.out.println(inline);
			inline = br.readLine();
		}
 
		System.out.println("------------------------------------------");
	}
 
	private static void outputResponse(Socket client, String documentRoot) throws IOException {
		PrintStream ps = new PrintStream(client.getOutputStream());
 
		String responseFile = documentRoot + requestPath;
		File file = new File(responseFile);
 
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
 
			// ヘッダー出力
			int bodyLength = (int) file.length();
			ps.println("HTTP/1.1 200 OK");
			ps.println("Content-Length: " + bodyLength);
			ps.println("Content-Type: " + getContentType(file));
			ps.println("");
 
			// ボディ出力
			byte buf[] = new byte[bodyLength];
 
			fis.read(buf);
			ps.write(buf, 0, bodyLength);
			ps.flush();
		} catch (FileNotFoundException e) {
			// ヘッダー出力
			ps.println("HTTP/1.1 404 Not Found");
			ps.println("");
			ps.println("<h1>指定されたファイルは存在しません。</h1>");
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
 
		ps.close();
	}
 
	private static String getContentType(File file) {
		String extension = getExtension(file);
 
		String contentType = CONTENT_TYPES.get(extension);
 
		if (contentType == null) {
			return "text/html";
		} else {
			return contentType;
		}
	}
 
	private static String getExtension(File file) {
		String path = file.getPath();
		int lastDotPosition = path.lastIndexOf(".");
 
		String extension = null;
		if (lastDotPosition == -1) {
			extension = "";
		} else {
			extension = path.substring(lastDotPosition + 1).toLowerCase();
		}
 
		return extension;
	}
}

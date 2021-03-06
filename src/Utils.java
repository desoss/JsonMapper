import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
	
	public static void createFolders(String path){
		new File(path).mkdirs();
	}
	
	public static void copyFolder(String src, String dest) {
		File srcFolder = new File(src);
		File destFolder = new File(dest);

		if (!srcFolder.exists()) {
			System.out.println("Directory does not exist.");
			System.exit(0);
		} else {
			try {
				copyFolder(srcFolder, destFolder);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

	}

	public static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String files[] = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyFolder(srcFile, destFile);
			}
		} else {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		}
	}

	public static void deleteFiles(String[] filesPath) {
		for (int i = 0; i < filesPath.length; i++) {
			File f = new File(filesPath[i]);
			f.delete();
		}
	}

	public static int factorial(int n) {
		if (n == 0)
			return 1;
		else {
			int result = n * factorial(n - 1);
			return result;
		}
	}

}
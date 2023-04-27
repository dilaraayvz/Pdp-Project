
/**
*
* @author Dilara Yavuz - dilara.yavuz@ogr.sakarya.edu.tr
* @since 15.04.2023
* <p>
* Yorumlar icin gerekli metodlarin buludugu class.
* </p>
*/
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentCounter {

	public String getContent(String filePath) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("Dosya okuma hatasi: " + e.getMessage());
		}
		return content;
	}

	public List<String> methodsAndComments(String filePath) {

		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(filePath));
		} catch (FileNotFoundException e) {
			System.out.println("Dosya bulunamadı: " + filePath);
			e.printStackTrace();
		}
		
		boolean inMethod = false;
		boolean inClass = false;
		boolean multiLineComment = false;
		int countBrackets = 0;
		String nameOfMethod = "";
		ArrayList<String> currentComments = new ArrayList<>();
		ArrayList<String> comments = new ArrayList<>();
		String comment = "";
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (isInClass(line)) {
				inClass = true;
			}
			if (inClass) {
				if (line.contains("(") && line.contains(")") && !line.contains(";")) {
					nameOfMethod = line.substring(0, line.indexOf(")") + 1);
					inMethod = true;
				}
				if (inMethod == false) {
					if (line.contains("/**")) {
						multiLineComment = true;
					}
					if (multiLineComment) {
						if (line.contains("*/")) {
							multiLineComment = false;
						}
						comment += line + "\n";
					} else if (((line.contains("{") || line.contains("}")) && !line.contains("class"))) {
						if (line.contains("{")) {
							countBrackets++;
						} else if (line.contains("}")) {
							countBrackets--;
						}
						if (countBrackets == 0) {
							// fonksiyon bitti demektir
							currentComments.add(comment);
							for (String content : currentComments) {
								comments.add(content + "\nFonksiyon : " + nameOfMethod);
							}
							comment = "";
							currentComments.clear();
							inMethod = false;
						}
					}if (!line.contains("(") && !line.contains(")") && line.contains(";") && inMethod == false) {
						comment = "";
					}
				} else {
					if (line.contains("/*") || line.contains("/**")) {
						multiLineComment = true;
					}
					if (multiLineComment) {
						if (line.contains("*/")) {
							multiLineComment = false;
						}
						comment += line + "\n";
					} else if (line.contains("//")) {
						comment += line + "\n";
					} else if ((line.contains("{") || line.contains("}")) && !line.contains("class")) {
						if (line.contains("{")) {
							countBrackets++;
						} else if (line.contains("}")) {
							countBrackets--;
						}
						if (countBrackets == 0) {
							// fonksiyon bitti demektir
							currentComments.add(comment);
							for (String content : currentComments) {
								comments.add(content + "\nFonksiyon : " + nameOfMethod);
							}
							comment = "";
							currentComments.clear();
							inMethod = false;
						}
					}
				}
			}
		}
		return comments;
	}

	public String getMethodName(String content) {
		String methodName = "";
		Pattern pattern = Pattern.compile("\\b\\w+\\s+(\\w+)\\s*\\(");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			methodName = matcher.group(1);
		}
		return methodName;
	}

	public void print(List<String> comments) {
		// Her metod için yorum sayısını hesapla ve ekrana yazdır
		for (String comment : comments) {
			System.out.println("	Fonksiyon : " + this.getMethodName(comment));
			System.out.println("        	 Tek Satır Yorum Sayısı :    " + this.countSingleLineComments(comment));
			System.out.println("       		 Çok Satırlı Yorum Sayısı  : " + this.countMultiLineComments(comment));
			System.out.println("        	 Javadoc Yorum Sayısı  :     " + this.countJavadocComments(comment));

			System.out.println("------------------------------------------------");
		}
	}

// Class adını bulan ve döndüren bir metod
	public String getClassName(String filePath) {
		String fileContent=this.getContent(filePath);
		Pattern pattern = Pattern.compile("class\\s+(\\w+)\\s*\\{");
		Matcher matcher = pattern.matcher(fileContent);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	public boolean isInClass(String fileContent) {
		Pattern pattern = Pattern.compile("class\\s+(\\w+)\\s*\\{");
		Matcher matcher = pattern.matcher(fileContent);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

// Bir metoddaki javadoc yorumlarının sayısını hesaplayan ve döndüren bir metod
	public int countJavadocComments(String method) {
		Pattern pattern = Pattern.compile("/\\*\\*.*?\\*/", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(method);

		int count = 0;
		while (matcher.find()) {

			count++;
		}
		return count;
	}

// Bir metoddaki tek satırlı yorumların sayısını hesaplayan ve döndüren bir metod
	public int countSingleLineComments(String method) {

		Pattern pattern = Pattern.compile("//.*?(\\r?\\n|$)");
		Matcher matcher = pattern.matcher(method);

		int count = 0;
		while (matcher.find()) {
			String match = matcher.group();
			if (!match.matches("//\\s*$")) { // Eğer sadece "//" ve ardından sadece boşluk karakterleri varsa, bu bir
												// yorum satırı olarak kabul edilmez
				count++;
			}
		}
		return count;
	}

// Bir metoddaki çok satırlı yorumların sayısını hesaplayan ve döndüren bir metod
	public int countMultiLineComments(String method) {

		Pattern pattern = Pattern.compile("/\\*(?!\\*).*?\\*/", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(method);

		int count = 0;
		while (matcher.find()) {
			count++;
		}

		return count;
	}

	public void saveFileJavadoc(List<String> contents) throws IOException {
		FileWriter fileWriter = new FileWriter("javadoc.txt");
		Pattern pattern = Pattern.compile("/\\*\\*(?:.|\\n)*?\\*/");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (String content : contents) {
			Matcher matcher = pattern.matcher(content);
			fileWriter.write("Fonksiyon : " + this.getMethodName(content) + "\n");
			fileWriter.write("\n");
			while (matcher.find()) {
				fileWriter.write(matcher.group(0) + "\n");
			}
			fileWriter.write("------------------------------\n");

		}
		fileWriter.close();
		bufferedWriter.close();
		System.out.println("javadoc.txt dosyasına yazdırma başarılı.");
	}

	public void saveFileMultiLine(List<String> contents) throws IOException {
		FileWriter fileWriter = new FileWriter("coksatir.txt");
		Pattern pattern = Pattern.compile("\\/\\*[^*][\\s\\S]*?\\*\\/");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (String content : contents) {
			Matcher matcher = pattern.matcher(content);
			fileWriter.write("Fonksiyon : " + this.getMethodName(content) + "\n");
			fileWriter.write("\n");
			while (matcher.find()) {
				fileWriter.write(matcher.group(0) + "\n");
			}
			fileWriter.write("------------------------------\n");

		}
		fileWriter.close();
		bufferedWriter.close();
		System.out.println("coksatir.txt dosyasına yazdırma başarılı.");

	}

	public void saveFileSingleLine(List<String> contents) throws IOException {
		FileWriter fileWriter = new FileWriter("teksatir.txt");
		Pattern pattern = Pattern.compile("//.*");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for (String content : contents) {
			Matcher matcher = pattern.matcher(content);
			fileWriter.write("Fonksiyon : " + this.getMethodName(content) + "\n");
			fileWriter.write("\n");
			while (matcher.find()) {
				fileWriter.write(matcher.group(0));
				fileWriter.write("\n");
			}
			fileWriter.write("------------------------------\n");
		}
		fileWriter.close();
		bufferedWriter.close();
		System.out.println("teksatir.txt dosyasına yazdırma başarılı.");

	}

	public boolean isJavadoc(String fileContent) {
		Pattern pattern = Pattern.compile("/\\*\\*(?:.|\\n)*?\\*/");
		Matcher matcher = pattern.matcher(fileContent);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isInMethod(String fileContent) {
		Pattern pattern = Pattern.compile("\\b\\w+\\s+(\\w+)\\s*\\(");
		Matcher matcher = pattern.matcher(fileContent);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

}

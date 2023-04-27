
/**
*
* @author Dilara Yavuz - dilara.yavuz@ogr.sakarya.edu.tr
* @since 15.04.2023
* <p>
* Main programi. 
* </p>
*/
import java.io.IOException;

public class Uygulama {
	public static void main(String[] args) throws IOException {

		if (args.length == 0) {
			System.out.println("Usage: java -jar Program.java <filePath>");
			System.exit(1);
		}
		String filePath = args[0];

		CommentCounter commentCounter = new CommentCounter();

		System.out.println("Sınıf : " + commentCounter.getClassName(filePath) + "\n");
		commentCounter.print(commentCounter.methodsAndComments(filePath));
		commentCounter.saveFileJavadoc(commentCounter.methodsAndComments(filePath));
		commentCounter.saveFileMultiLine(commentCounter.methodsAndComments(filePath));
		commentCounter.saveFileSingleLine(commentCounter.methodsAndComments(filePath));

	}
}

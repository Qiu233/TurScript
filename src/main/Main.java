package main;

import java.io.File;
import java.io.FileReader;

import parse.Parser;
import vm.TurVM;
import lex.Lexxer;

public class Main {
	public static void print(Object s) {
		System.out.print(s);
	}
	public static void println(Object s)
	{
		System.out.println(s);
	}
	public static double test(double a){
		return a+20;
	}

	public static void main(String args[]) throws Exception {
		/*
		 * System.out.println("请输入文件路径");
		 * 
		 * @SuppressWarnings("resource") String path = new
		 * Scanner(System.in).next();
		 */
		String path = "G:/a.txt";
		File file = new File(path);
		FileReader fr = new FileReader(file);
		char c[] = new char[(int) file.length()];
		fr.read(c);
		fr.close();

		String str = new String(c);
		Parser p = new Parser(new Lexxer(str));
		p.registerFunc("print", Main.class.getMethod("print", Object.class));
		p.registerFunc("println", Main.class.getMethod("println", Object.class));
		p.registerFunc("test", Main.class.getMethod("test",new Class[]{double.class}));
		TurVM vm = new TurVM(p);
		vm.printInfo();
		vm.exec();
	}
}

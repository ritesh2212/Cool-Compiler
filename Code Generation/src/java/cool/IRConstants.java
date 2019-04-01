package cool;

public class IRConstants {

	public static String TARGET_DATALAYOUT = "\"e-m:e-i64:64-f80:128-n8:16:32:64-S128\"";
	public static String TARGET_TRIPLE = "\"x86_64-unknown-linux-gnu\"";
	
	
	public static String SOURCE_FILENAME = "source_filename";
	public static String TARGET = "target";
	public static String DATALAYOUT  = "datalayout";
	public static String TRIPLE = "triple";
	public static String TYPE = "type";
	public static String PRIVATE = "private";
	public static String UNNAMED_ADDR = "unnamed_addr";
	public static String CONSTANT = "constant";
	public static String ALIGN  = "align";
	public static String DEFINE  = "define";
	
	public static String SPACE = " ";
	public static String EQUALTO = " = ";
	public static String INT_I32 = "i32";
	public static String SRTING_I8 = "i8*";
	public static String BOOL_I8 = "i8";
	public static String NEW_LINE = "\n";
	public static String SCOPE_CLASS = "CLASS";
	public static String SCOPE_METHOD = "METHOD";
	
	public static String PRINTF = "\ndeclare dso_local i32 @printf(i8*, ...)";
	public static String SCANF = "declare dso_local i32 @scanf(i8*, ...)";
	public static String STRCAT = "declare dso_local i8* @strcat(i8*, i8*)";
	public static String STRLEN = "declare dso_local i64 @strlen(i8*)";
	public static String EXIT = "declare void @exit(i32)";
	public static String MALLOC = "declare noalias i8* @malloc(i64)";
	
}

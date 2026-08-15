package nitis.gravillaso.annotations.processors;

import arc.util.*;
import com.squareup.javapoet.*;
import com.sun.tools.javac.model.*;
import com.sun.tools.javac.processing.*;
import mindustry.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
public abstract class BaseProcessor extends AbstractProcessor{

	public JavacElements elements;
	public JavacTypes types;
	public JavacFiler filer;

	protected int round;
	protected int rounds = 1;

	public static String modName = "gr";
	public static String classPrefix = "Gravillaso";
	public static String packageName = "nitis.gravillaso.gen";

	static{
		Vars.loadLogger();
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv){
		super.init(processingEnv);

		JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment)processingEnv;

		elements = javacProcessingEnv.getElementUtils();
		types = javacProcessingEnv.getTypeUtils();
		filer = javacProcessingEnv.getFiler();

		Map<String, String> options = processingEnv.getOptions();
		modName = options.getOrDefault("modName", modName);
		classPrefix = options.getOrDefault("classPrefix", classPrefix);
		packageName = options.getOrDefault("genPackage", packageName);
	}

	@Override
	public Set<String> getSupportedOptions(){
		return new HashSet<>(Arrays.asList("modName", "classPrefix", "genPackage"));
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
		if(round++ >= rounds) return false;

		try{
			process(roundEnv);
		}catch(Exception e){
			Throwable finalCause = Strings.getFinalCause(e);

			Log.err(finalCause);
			throw new RuntimeException(finalCause);
		}

		return true;
	}

	public abstract void process(RoundEnvironment roundEnv) throws Exception;

	public void write(TypeSpec spec) throws Exception{
		try{
			JavaFile.builder(packageName, spec)
			.indent("    ")
			.skipJavaLangImports(true)
			.build()
			.writeTo(filer);
		}catch(FilerException e){
			throw new Exception("Misbehaving files prevent annotation processing from being done. Try running `gradlew clean`");
		}
	}

	public static TypeName tName(Class<?> type){
		return ClassName.get(type).box();
	}

	public static ClassName cName(Class<?> type){
		return ClassName.get(type);
	}

	public static ClassName cName(String canonical){
		Matcher matcher = Pattern.compile("\\.[A-Z]").matcher(canonical);
		boolean find = matcher.find();
		int offset = find ? matcher.start() : 0;

		String pkgName = canonical.substring(0, offset);
		List<String> simpleNames = new ArrayList<>(Arrays.asList(canonical.substring(offset + 1).split("\\.")));
		Collections.reverse(simpleNames);
		String simpleName = simpleNames.remove(simpleNames.size() - 1);
		Collections.reverse(simpleNames);

		return ClassName.get(pkgName.isEmpty() ? packageName : pkgName, simpleName, simpleNames.toArray(new String[0]));
	}

	public static ClassName cName(Element e){
		return cName(stripTV(e.asType().toString()));
	}

	public static String stripTV(String canonical){
		return canonical.replaceAll("<[A-Z]+>", "");
	}

	@Override
	public SourceVersion getSupportedSourceVersion(){
		return SourceVersion.RELEASE_17;
	}
}
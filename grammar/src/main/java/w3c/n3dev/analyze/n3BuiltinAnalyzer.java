package w3c.n3dev.analyze;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import w3c.n3dev.analyze.n3BuiltinVisitor.PrefixEntry;
import w3c.n3dev.parser.Grammar;
import w3c.n3dev.parser.Grammar.Grammars;
import w3c.n3dev.test.RdfTestManifest;

public class n3BuiltinAnalyzer {

	public static void main(String[] args) throws Exception {
		n3BuiltinAnalyzer analyzer = new n3BuiltinAnalyzer(Grammars.n3, "tests/N3Tests/");

//		analyzer.analyzeFile("07test/grawlgen.n3");
//		analyzer.analyzeFolder("01etc/");
		analyzer.analyzeAll();
	}

	private Grammars type;
	private String mainFolder;

	private n3BuiltinVisitor visitor = new n3BuiltinVisitor();

	public n3BuiltinAnalyzer(Grammars type, String mainFolder) {
		this.type = type;
		this.mainFolder = mainFolder;
	}

	public void analyzeAll() throws Exception {
		analyzeFolder(null);
	}

	public void analyzeFolder(String folder) throws Exception {
		RdfTestManifest manifest = new RdfTestManifest(mainFolder, new File(mainFolder, "manifest.ttl"));

		manifest.forEachTest(true, "n3", (test) -> {
			if (folder == null || test.startsWith(folder))
				runTest(test);
		});

		print();
	}

	public void analyzeFile(String name) {
		runTest(name);

		print();
	}

	private void runTest(String test) {
//		System.out.println("- " + test);

		File file = new File(mainFolder, test);
		Grammar grammar = new Grammar();
		try {
			ParserRuleContext ctx = grammar.parse(file, type);
			visitor.visit(ctx);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void print() {
		System.out.println("\n\nfound built-ins:");

		System.out.println("\n> individual uris:\n");
		List<Map.Entry<String, Integer>> uris = new ArrayList<>(visitor.getUriOcc().entrySet());
		uris.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

		uris.stream().forEach(u -> {
			System.out.println(u.getKey() + "; count=" + u.getValue());
		});

		System.out.println("\n\n\n> ordered by prefix:\n");

		List<Map.Entry<String, PrefixEntry>> prefixes = new ArrayList<>(visitor.getPrefixOcc().entrySet());
		prefixes.sort((e1, e2) -> Integer.valueOf(e2.getValue().getTotalCnt()).compareTo(e1.getValue().getTotalCnt()));

		prefixes.stream().forEach(p -> {
			System.out.println("- " + p.getKey() + "; count=" + p.getValue().getTotalCnt());

			List<Map.Entry<String, Integer>> prefixUris = new ArrayList<>(p.getValue().getUriOcc().entrySet());
			prefixUris.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

			prefixUris.stream().forEach(u -> {
				System.out.println(u.getKey() + "; count=" + u.getValue());
			});
			System.out.println();
		});
	}
}
package img2graph;

import img2graph.server.StartServer;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;

@QuarkusMain
@CommandLine.Command(name = "Img2Graph", description = "Convert an image to a graph or start a webserver providing a UI for conversion.", subcommands = {
	ImageToGraph.class, StartServer.class })
public class Application {
	public static void main(String[] args) {
		System.exit(new CommandLine(new Application()).execute(args));
	}
}

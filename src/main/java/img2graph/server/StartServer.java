package img2graph.server;

import io.quarkus.runtime.Quarkus;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "start-server", description = "Starts a simple webserver allowing to convert images via a Web-UI.")
public class StartServer implements Callable<Integer> {

	@CommandLine.Unmatched String[] remainingOptions;

	@Override
	public Integer call() {
		Quarkus.run(remainingOptions);
		return 0;
	}
}

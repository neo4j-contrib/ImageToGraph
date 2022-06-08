module img2graph.cli {
    requires img2graph.core;
    requires info.picocli;

    opens img2graph.cli to
            info.picocli;
}

package image2graph.web;

import img2graph.core.Arguments;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public final class UploadFormData {
    private static final String ON = "on";

    @RestForm public FileUpload file;

    @RestForm("arrows-redirect")
    public String redirectToArrows;

    @RestForm("nodes-in-bg")
    public String nodesInBackground;

    @RestForm("simple-color")
    public String useSimpleColors;

    @RestForm("outline")
    public String outline;

    @RestForm("node-max")
    public Integer nodeMax;

    @RestForm("node-min")
    public Integer nodeMin;

    @RestForm("node-padding")
    public Integer nodePadding;

    @RestForm("supernodes")
    public Integer superNodes;

    @RestForm("color-depth")
    public Integer colorDepth;

    @RestForm("rel-avg")
    public Integer relAvg;

    @RestForm("rel-max-length")
    public Integer relLength;

    public Arguments asArguments() {
        return new Arguments(
                ON.equalsIgnoreCase(nodesInBackground),
                Math.min(nodeMax, nodeMin),
                Math.max(nodeMax, nodeMin),
                nodePadding,
                superNodes,
                relLength,
                relAvg,
                Arguments.DEFAULT_ARGUMENTS.targetResolution(),
                colorDepth,
                ON.equalsIgnoreCase(useSimpleColors),
                Arguments.DEFAULT_ARGUMENTS.transparentBg(),
                ON.equalsIgnoreCase(outline));
    }

    public boolean shouldRedirectToArrows() {
        return ON.equalsIgnoreCase(redirectToArrows);
    }
}

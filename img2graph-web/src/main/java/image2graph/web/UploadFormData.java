package image2graph.web;

import img2graph.core.Arguments;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public final class UploadFormData {

    @RestForm public FileUpload file;

    @RestForm("simple-color")
    public String useSimpleColors;

    @RestForm public String outline;

    @RestForm("node-max")
    public Integer nodeMax;

    @RestForm("node-min")
    public Integer nodeMin;

    @RestForm("node-padding")
    public Integer nodePadding;

    @RestForm("color-depth")
    public Integer colorDepth;

    @RestForm("rel-avg")
    public Integer relAvg;

    @RestForm("rel-max-length")
    public Integer relLength;

    public Arguments asArguments() {
        return new Arguments(
                false,
                Math.min(nodeMax, nodeMin),
                Math.max(nodeMax, nodeMin),
                nodePadding,
                relLength,
                relAvg,
                Arguments.DEFAULT_ARGUMENTS.targetResolution(),
                colorDepth,
                "on".equalsIgnoreCase(useSimpleColors),
                Arguments.DEFAULT_ARGUMENTS.transparentBg(),
                "on".equalsIgnoreCase(outline));
    }
}

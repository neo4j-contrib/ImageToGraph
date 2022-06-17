package img2graph.core;

public record Arguments(
        boolean keepBackground,
        int nodeMinRad,
        int nodeMaxRad,
        int nodePadding,
        int numSuperNodes,
        int relMaxDist,
        int relsPerNode,
        int targetResolution,
        int colorDepth,
        boolean simplifiedColors,
        boolean transparentBg,
        boolean outline) {

    Arguments() {
        this(false, 3, 10, 2, 0, 20, 2, 1024, 4, false, false, false);
    }

    public static final Arguments DEFAULT_ARGUMENTS = new Arguments();

    public Arguments withTargetResolution(int newTargetResolution) {
        return new Arguments(
                keepBackground,
                nodeMinRad,
                nodeMaxRad,
                nodePadding,
                numSuperNodes,
                relMaxDist,
                relsPerNode,
                newTargetResolution,
                colorDepth,
                simplifiedColors,
                transparentBg,
                outline);
    }
}

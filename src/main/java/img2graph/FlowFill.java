package img2graph;

import img2graph.ImageReader.Color;
import img2graph.ImageReader.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

class FlowFill {
    private final Image img;
    private final boolean[][] visited;

    FlowFill(Image img) {
        this.img = img;
        this.visited = new boolean[img.width][img.height];
    }

    List<Segment> getSegments() {
        List<Segment> segments = new ArrayList<>();
        for (int x = 0; x < img.width; x++) {
            for (int y = 0; y < img.height; y++) {
                if (!visited[x][y]) {
                    Segment segment = new Segment(img.colors[x][y]);
                    segments.add(segment);
                    visit(x, y, segment);
                }
            }
        }
        return segments;
    }

    private void visit(int x, int y, Segment segment) {
        visitShallow(x, y, segment);
        Queue<Coordinate> toVisit = new LinkedList<>();
        toVisit.add(new Coordinate(x, y));

        while (!toVisit.isEmpty()) {
            Coordinate coordinate = toVisit.poll();
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (!(i == 0 & j == 0)) {
                        Coordinate shouldVisit = visitShallow(coordinate.x + i, coordinate.y + j, segment);
                        if (shouldVisit != null) {
                            toVisit.add(shouldVisit);
                        }
                    }
                }
            }
        }
    }

    private Coordinate visitShallow(int x, int y, Segment segment) {
        if (x < 0 || x >= img.width || y < 0 || y >= img.height) {
            return null;
        }
        if (visited[x][y]) {
            return null;
        }
        if (segment.color.raw() != img.colors[x][y].raw()) {
            return null;
        }
        visited[x][y] = true;
        Coordinate coordinate = new Coordinate(x, y);
        segment.pixels.add(coordinate);
        return coordinate;
    }

    record Coordinate(int x, int y) {
        int distSq(Coordinate o) {
            int dX = o.x - x;
            int dY = o.y - y;
            return dX * dX + dY * dY;
        }

        @Override
        public String toString() {
            return x + "," + y;
        }
    }

    static class Segment {
        final Color color;
        final Set<Coordinate> pixels = new HashSet<>();

        public Segment(Color color) {
            this.color = color;
        }
    }
}

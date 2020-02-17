import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class Main {

    public static final int endX = 11128; // Eindhoven
    public static final int endY = 2313;

    public static final int startX = 16015; // Mt Everest
    public static final int startY = 3720;

    public static PriorityQueue<SearchNode> queue;

    public static boolean[][] used;

    public static final String GPX_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
            "\n" +
            "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" creator=\"Oregon 400t\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">\n" +
            "  <metadata>\n" +
            "    <link href=\"http://www.garmin.com\">\n" +
            "      <text>Garmin International</text>\n" +
            "    </link>\n" +
            "    <time>2009-10-17T22:58:43Z</time>\n" +
            "  </metadata>\n" +
            "  <trk>\n" +
            "    <name>Example GPX Document</name>\n" +
            "    <trkseg>";

    public static final String GPX_POSTFIX = "</trkseg>\n" +
            "  </trk>\n" +
            "</gpx>";


    public static void main(String[] args) throws Exception {
        File imageFile = new File("images/heightmap.png");
        BufferedImage image = ImageIO.read(imageFile);

        used = new boolean[image.getWidth()][image.getHeight()];

        queue = new PriorityQueue<SearchNode>(new Comparator<SearchNode>() {
            public int compare(SearchNode o1, SearchNode o2) {
                return (int)(o1.calc - o2.calc);
            }
        });

        SearchNode initial = new SearchNode(startX, startY, 255, 0,null);

        queue.add(initial);

        SearchNode next = null;

        int calc = 0;

        Raster raster = image.getRaster();

        while ((next = queue.poll()) != null) {
            if (used[next.x][next.y])
                continue;

            used[next.x][next.y] = true;

            if (calc % 10000 == 0)
                System.out.println(queue.size() + " " + next.x + " " + next.y + " " + next.value + " " + next.calc + " " + next.distance + " " + next.remaining + " " + next.elevation);

            if (next.remaining < 10) {
                System.out.println("DONE");
                System.out.println(queue.size() + " " + next.x + " " + next.y + " " + next.value + " " + next.calc + " " + next.distance + " " + next.remaining + " " + next.elevation);
                break;
            }

            for (int x = -1; x<=1; x++) {
                for (int y = -1; y<=1; y++) {
                    int nextX = next.x + x;
                    int nextY = next.y + y;

                    if (nextX>=0 && nextY>=0 && nextX < image.getWidth() && nextY < image.getHeight() && !used[nextX][nextY]) {
                        int rgb = raster.getSample(nextX, nextY, 0);
                        int value = rgb & 0xFF;

                        SearchNode node = new SearchNode(nextX, nextY, value, rgb,  next);
                        //if (value != 0) {
                            queue.add(node);
                        //}
                    }
                }
            }
            calc+=1;
            //if (calc==2000) return;
        }

        SearchNode finalNode = next;

        SearchNode prevNode = next;
        while (!queue.isEmpty()) {
            SearchNode node = queue.poll();
            if (queue.size() % 1000 == 0)
                System.out.println(queue.size());

            int rgb = (255-node.value) | ((255-node.value) << 8) | ((255-node.value) << 16) | (255 << 24);
            int size = 1;
            for (int x = -size; x<=size; x++) {
                for (int y = -size; y <= size; y++) {
                    if (node.x + x>=0 && node.y + y>=0 && node.x + x < image.getWidth() && node.y + y < image.getHeight()) {
                        image.setRGB(node.x + x, node.y + y, rgb);
                    }
                }
            }

            prevNode = node;
            while (prevNode!=null) {
                rgb = (255-prevNode.value) | ((255-prevNode.value) << 8) | ((255-prevNode.value) << 16) | (255 << 24);
                image.setRGB(prevNode.x, prevNode.y, rgb);
                prevNode = prevNode.previous;
            }
        }

        BufferedWriter writer  = new BufferedWriter(new FileWriter("images/result.gpx"));

        writer.write(GPX_PREFIX);

        prevNode = finalNode;
        while (prevNode!=null) {
            int rgb = (255-prevNode.value) | ((255-prevNode.value) << 8) | ((255-prevNode.value) << 16) | (255 << 24);

            int size = 2;
            if (prevNode.previous != null) {
                if (prevNode.previous.value < prevNode.value)
                    size += (prevNode.value - prevNode.previous.value) * 5;
            }

            for (int x = -size; x<=size; x++) {
                for (int y = -size; y <= size; y++) {
                    if (prevNode.x + x>=0 && prevNode.y + y>=0 && prevNode.x + x < image.getWidth() && prevNode.y + y < image.getHeight()) {
                        image.setRGB(prevNode.x + x, prevNode.y + y, rgb);
                    }
                }
            }

            writer.write(String.format("<trkpt lon=\"%.04f\" lat=\"%.04f\">\n" +
                    "        <ele>%.04f</ele>\n" +
                    "      </trkpt>", ((double)prevNode.x - 10800)/60, -((double)prevNode.y - 5400)/60, (double)prevNode.value * 25));
            prevNode = prevNode.previous;
        }

        writer.write(GPX_POSTFIX);
        writer.close();

        System.out.println("SAVING");
        File outputfile = new File("images/result.png");
        ImageIO.write(image, "png", outputfile);
        System.out.println("SAVED");
    }

    static class SearchNode {
        public SearchNode previous;
        public int x;
        public int y;
        public int value;
        public int rgb;

        public double elevation;
        public double distance;
        public double remaining;
        public double calc;

        public void setValue() {
            if (previous == null) {
                distance = 0;
                elevation = 0;
            } else {
                distance = previous.distance + Math.sqrt((x-previous.x) * (x-previous.x) + (y-previous.y) * (y-previous.y));
                elevation = previous.elevation;
                if (previous.value < value) {
                    int increment = (value - previous.value);

                    elevation += Math.pow(Math.pow(increment,increment),increment);
                    // all slope okay: increment
                    // avoid major slopes: Math.pow(increment,increment)
                    // avoid small slopes: Math.pow(Math.pow(increment,increment),increment)
                }
            }

            remaining = Math.sqrt((x-endX) * (x-endX) + (y-endY) * (y-endY));

            calc = distance + remaining + elevation * 1000000000;
        }

        public SearchNode(int x, int y, int value, int rgb, SearchNode previous) {
            this.x = x;
            this.y = y;
            this.value = value;
            this.previous = previous;
            this.rgb = rgb;
            setValue();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SearchNode that = (SearchNode) o;
            return x == that.x &&
                    y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}

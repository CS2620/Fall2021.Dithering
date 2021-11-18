import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    public Main(){
        //doListFormats();
        // doTestFileFormats();
        // doBitSlicing();
        // doBrightening();
        // doApplyCurve();
        doCustomFormat("horse", "png");
        doCalculateCompressionRatio("horse", "custom");
        
    }

    private static String[] fileFormats = null;

    public static String[] getFileFormats() {
        if(fileFormats != null){
            return fileFormats;
        }

        //Get the list of supported file formats
        var names = ImageIO.getWriterFormatNames();

        //Use a set to remove endings that differ only by case
        Set<String> toKeep = new HashSet<>();
        
        //Remove redundant file endings or ones we don't want
        Collection<String> ignore = Arrays.asList(new String[] { "tiff", "jpeg", "wbmp" });
        for (int i = 0; i < names.length; ++i) {
            String name = names[i].toLowerCase();
            if (ignore.contains(name))
                continue;
            toKeep.add(name);
        }
        toKeep.add("ppm");

        fileFormats = toKeep.toArray(new String[0]);

        return fileFormats;
    }


    public void doCustomFormat(String filename, String ending){
        var start = new Processor("./in/" + filename + "." + ending);
        start.saveCurrentLayer("./out/" + filename + ".custom");
        var end = new Processor("./out/" + filename + ".custom");
        
        System.out.println("Are the images the same? " + end.compareTo(start));

    }


    public void doListFormats() {
        System.out.println("The following are the file formats supported by your version of Java:");
        String[] endings = getFileFormats();
        for (var ending : endings) {
            System.out.println(ending);
        }
        System.out.println();
    }

    public void doCalculateCompressionRatio(String filename, String extension) {

        var standardFileFormats = getFileFormats();
        var start = new Processor("./in/" + filename + "." + extension);
        for (var ending : standardFileFormats) {
            start.saveCurrentLayer("./out/horse." + ending);
        }

        var compareSize = new File("./out/" + filename + "." + extension).length();

        // Get the sizes of the files
        for (var ending : standardFileFormats) {
            var size = new File("./out/horse." + ending).length();
            var ratio = compareSize / (double) size;
            System.out.println("Compression ratio compared to " + ending + ": " + ratio);
        }

    }

    public void doTestFileFormats() {
        var start = new Processor("./in/horse.png");
        start.saveCurrentLayer("./out/horse.ppm");
        var end = new Processor("./out/horse.ppm");
        var equal = end.compareTo(start);
        System.out.println(equal);
    }

    public void doBitSlicing() {
        var start = new Processor("./in/horse.png");

        for (int i = 0; i < 8; i++) {
            final int j = i;
            start.addLayer(image -> image.bitSlice(j));
            start.saveLayers(new int[] { 1 }, "./out/sliced-" + i + ".png");
            start.popLayer();
        }

        for (int i = 0; i < 8; i++) {
            start = new Processor("./in/horse.jpg").grayscale();
            final int j = i;
            start.addLayer(image -> image.bitSlice(0, j));

            start.saveCurrentLayer("./out/sliced-combined-" + 0 + "-" + i + ".png");

        }

        for (int i = 0; i < 8; i++) {
            start = new Processor("./in/horse.jpg").grayscale();
            final int j = i;
            start.addLayer(image -> image.bitSlice(j, 7));

            start.saveCurrentLayer("./out/sliced-combined2-" + i + "-" + 7 + ".png");

        }
    }

    public void doBrightening() {
        var start = new Processor("./in/horse.png");

        for (var i = 1; i < 10; i++) {
            start.push().brighten(i * 10).addLayer(image -> image.histogram(100))
                    .saveLayers(new int[] { 1, 2 }, "./out/brighten" + i * 10 + ".png").popLayer().popLayer();
        }
    }

    public void doApplyCurve() {
        var start = new Processor("./in/horse.png");

        IPixelFunction ipf = new IPixelFunction() {

            @Override
            public float run(float input) {
                return input;
                // return (float)Math.pow(input, .3);
                // return 1-input;
                // return input < .5 ? 0 : 1;

            }

        };

        start.applyCurve(ipf).addLayer(Processor.ImageFromFunction(ipf)).saveLayers(new int[] { 0, 1 },
                "./out/pixel-function.png");
    }
}

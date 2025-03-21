package santannaf.demo.brc.demo1brcjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping(path = {"/files"})
class FilesController {
    private static final String FILE = "./measurements.txt";
    private static final int MAX_CITY_NAME_LENGTH = 100;

    // Segment in the file that will be processed in parallel.
    private record Segment(long start, int size) {
    }

    // Holding the current result for a single city.
    private static class Result {
        int max;
        int min;
        long sum;
        int count;
        byte[] name;

        public String toString() {
            return round(((double) min) / 10.0) + "/" + round((((double) sum) / 10.0) / count) + "/" + round(((double) max) / 10.0);
        }

        private double round(double value) {
            return Math.round(value * 10.0) / 10.0;
        }

        // Accumulate another result into this one.
        private void add(Result other) {
            min = Math.min(min, other.min);
            max = Math.max(max, other.max);
            sum += other.sum;
            count += other.count;
        }
    }

    @GetMapping(path = {"/read"})
    void readFile() {
        // Calculate input segments.
        List<Segment> segments = getSegments();

        // Parallel processing of segments.
        List<HashMap<String, Result>> allResults = segments.stream().map(s -> {
            HashMap<String, Result> cities = new HashMap<>();
            byte[] name = new byte[MAX_CITY_NAME_LENGTH];
            Result[] results = new Result[1 << 18];

            try (FileChannel ch = (FileChannel) java.nio.file.Files.newByteChannel(Paths.get(FILE), StandardOpenOption.READ)) {
                ByteBuffer bf = ch.map(FileChannel.MapMode.READ_ONLY, s.start(), s.size());
                parseLoop(bf, name, results, cities);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return cities;
        }).parallel().toList();

        // Accumulate results sequentially.
        HashMap<String, Result> result = allResults.getFirst();
        for (int i = 1; i < allResults.size(); ++i) {
            for (Map.Entry<String, Result> r : allResults.get(i).entrySet()) {
                Result current = result.get(r.getKey());
                if (current != null) {
                    current.add(r.getValue());
                } else {
                    result.put(r.getKey(), r.getValue());
                }
            }
        }

        // Final output.
        System.out.println(new TreeMap<>(result));
    }

    private static void parseLoop(ByteBuffer bf, byte[] name, Result[] results, HashMap<String, Result> cities) {
        int pos = 0;
        byte b;
        while (pos < bf.limit()) {
            int hash = 0;
            int nameIndex = 0;
            while ((b = bf.get(pos++)) != ';') {
                hash += b;
                hash += hash << 10;
                hash ^= hash >> 6;
                name[nameIndex++] = b;
            }
            hash = hash & (results.length - 1);

            int number;
            byte sign = bf.get(pos++);
            boolean isMinus = false;
            if (sign == '-') {
                isMinus = true;
                number = bf.get(pos++) - '0';
            } else {
                number = sign - '0';
            }
            while ((b = bf.get(pos++)) != '.') {
                number = number * 10 + b - '0';
            }
            number = number * 10 + bf.get(pos++) - '0';
            if (isMinus) {
                number = -number;
            }

            while (true) {
                Result existingResult = results[hash];
                if (existingResult == null) {
                    Result r = new Result();
                    r.name = new byte[nameIndex];
                    r.max = number;
                    r.min = number;
                    r.count = 1;
                    r.sum = number;
                    System.arraycopy(name, 0, r.name, 0, nameIndex);
                    cities.put(new String(r.name), r);
                    results[hash] = r;
                    break;
                } else {
                    if (Arrays.equals(existingResult.name, 0, nameIndex, name, 0, nameIndex)) {
                        existingResult.count++;
                        existingResult.max = Math.max(existingResult.max, number);
                        existingResult.min = Math.min(existingResult.min, number);
                        existingResult.sum += number;
                        break;
                    } else {
                        // Collision error, try next.
                        hash = (hash + 1) & (results.length - 1);
                    }
                }
            }

            // Skip new line.
            pos++;
        }
    }

    private static List<Segment> getSegments() {
        try (RandomAccessFile file = new RandomAccessFile(FILE, "r")) {
            long totalSize = file.length();
            int cores = Runtime.getRuntime().availableProcessors();
            int segmentSize = ((int) (totalSize / cores));
            List<Segment> segments = new ArrayList<>();
            long filePos = 0;
            while (filePos < totalSize - segmentSize) {
                file.seek(filePos + segmentSize);
                while (file.read() != '\n') ;
                segments.add(new Segment(filePos, (int) (file.getFilePointer() - filePos)));
                filePos = file.getFilePointer();
            }
            segments.add(new Segment(filePos, (int) (totalSize - filePos)));
            return segments;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

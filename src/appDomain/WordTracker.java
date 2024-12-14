package appDomain;

import java.io.*;
import java.util.*;
import implementations.BSTree;
import utilities.Iterator;

public class WordTracker {
    private static final String REPOSITORY_FILE = "res/repository.ser";

    // A map to store metadata for each word
    private static Map<String, WordMetadata> wordMetadataMap;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar WordTracker.jar res/<input.txt> -pf/-pl/-po [-f res/<output.txt>]");
            return;
        }

        String inputFile = args[0];
        String mode = args[1];
        String outputFile = null;

        if (args.length > 2 && args[2].equals("-f") && args.length > 3) {
            outputFile = args[3];
        }

        // Load or create the BSTree and metadata map
        BSTree<String> wordTree = loadTree();

        try {
            processFile(wordTree, inputFile);
            generateReport(wordTree, mode, outputFile);
            saveTree(wordTree);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    private static BSTree<String> loadTree() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(REPOSITORY_FILE))) {
            @SuppressWarnings("unchecked")
            BSTree<String> tree = (BSTree<String>) ois.readObject();
            wordMetadataMap = (Map<String, WordMetadata>) ois.readObject();
            return tree;
        } catch (IOException | ClassNotFoundException e) {
            wordMetadataMap = new HashMap<>(); // Initialize new metadata map
            return new BSTree<>();
        }
    }

    private static void saveTree(BSTree<String> tree) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(REPOSITORY_FILE))) {
            oos.writeObject(tree);
            oos.writeObject(wordMetadataMap); // Save metadata map with the tree
        } catch (IOException e) {
            System.err.println("Error saving tree: " + e.getMessage());
        }
    }

    private static void processFile(BSTree<String> tree, String inputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                // Match words with letters, numbers, and apostrophes
                String[] words = line.split("[^\\w']+");
                for (String word : words) {
                    if (word == null || word.isEmpty()) {
                        continue; // Skip invalid or empty words
                    }

                    String normalizedWord = word.toLowerCase();

                    // Add the word to the tree if not already present
                    if (!tree.contains(normalizedWord)) {
                        tree.add(normalizedWord);
                        wordMetadataMap.putIfAbsent(normalizedWord, new WordMetadata());
                    }

                    // Update metadata
                    WordMetadata metadata = wordMetadataMap.get(normalizedWord);
                    metadata.addOccurrence(inputFile, lineNumber);
                }
                lineNumber++;
            }
        }
    }

    private static void generateReport(BSTree<String> tree, String mode, String outputFile) throws IOException {
        StringBuilder report = new StringBuilder();

        // Add the header for the output
        if (mode.equals("-pl")) {
            report.append("Writing pl format\n\n");
        } else if (mode.equals("-po")) {
            report.append("Writing po format\n\n");
        } else {
            report.append("Writing pf format\n\n");
        }

        // Collect all words into a list for custom sorting
        List<String> words = new ArrayList<>();
        Iterator<String> iterator = tree.inorderIterator();
        while (iterator.hasNext()) {
            words.add(iterator.next());
        }

        // Sort words alphabetically, ignoring case
        words.sort(String.CASE_INSENSITIVE_ORDER);

        // Generate report for each word
        for (String normalizedWord : words) {
            WordMetadata metadata = wordMetadataMap.get(normalizedWord);

            // Format the key
            report.append("Key : ===").append(normalizedWord).append("=== ");

            if (mode.equals("-po")) {
                // Include frequency in the Key line for -po format
                report.append("number of entries: ").append(metadata.getFrequency()).append(" ");
            }

            // Combine all file and line data into a single line
            metadata.getFileLineMap().forEach((fileName, lines) -> {
                report.append("found in file: ").append(new File(fileName).getName()).append(" on lines: ");
                report.append(formatLineNumbers(lines)).append(", ");
            });

            // Remove trailing comma and space
            if (report.charAt(report.length() - 2) == ',') {
                report.setLength(report.length() - 2);
            }

            report.append("\n");
        }

        // If the output file is specified, write to it
        if (outputFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                writer.write(report.toString());
                // Add the message about exporting the file
                System.out.println("Exporting file to: " + outputFile);
            }
        } else {
            System.out.println(report);
            System.out.println("Not exporting file");
        }
    }

    private static String formatLineNumbers(List<Integer> lineNumbers) {
        StringBuilder formatted = new StringBuilder();
        for (Integer lineNumber : lineNumbers) {
            formatted.append(lineNumber).append(",");
        }
        if (formatted.length() > 0) {
            formatted.setLength(formatted.length() - 1); // Remove trailing comma
        }
        return formatted.toString();
    }

    private static class WordMetadata implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<String, List<Integer>> fileLineMap;
        private int frequency;

        public WordMetadata() {
            this.fileLineMap = new HashMap<>();
            this.frequency = 0;
        }

        public void addOccurrence(String fileName, int lineNumber) {
            fileLineMap.computeIfAbsent(fileName, k -> new ArrayList<>()).add(lineNumber);
            frequency++;
        }

        public Map<String, List<Integer>> getFileLineMap() {
            return fileLineMap;
        }

        public int getFrequency() {
            return frequency;
        }
    }
}

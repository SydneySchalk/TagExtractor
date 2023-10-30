import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class TagExtractor {
    private static File selectedFile;
    private static File stopWordsFile;
    private static JFrame frame;

    public static void main(String[] args) {
        frame = new JFrame("Word Frequencies");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        JTextArea textArea = new JTextArea(10, 40);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton selectFileButton = new JButton("Select File");
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });

        JButton selectStopWordsButton = new JButton("Select Stop Words File");
        selectStopWordsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseStopWordsFile();
            }
        });

        JButton processButton = new JButton("Process Text");
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processText(textArea);
            }
        });

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveWordListToFile(textArea.getText());
            }
        });

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectFileButton);
        buttonPanel.add(selectStopWordsButton);
        buttonPanel.add(processButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(quitButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            frame.setTitle("Word Frequencies - " + selectedFile.getName());
        }
    }

    private static void chooseStopWordsFile() {
        JFileChooser stopWordsChooser = new JFileChooser();
        if (stopWordsChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            stopWordsFile = stopWordsChooser.getSelectedFile();
        }
    }

    private static void processText(JTextArea textArea) {
        if (selectedFile == null || stopWordsFile == null) {
            JOptionPane.showMessageDialog(null, "Please select both the text file and stop words file before processing.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(selectedFile.toPath());

            Set<String> stopWords = readStopWords();

            Map<String, Integer> wordFrequency = new HashMap<>();

            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase();
                word = word.replaceAll("[^a-zA-Z]", "");

                if (!stopWords.contains(word)) {
                    wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                }
            }

            // Sort the results alphabetically
            Map<String, Integer> sortedWordFrequency = wordFrequency.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            for (Map.Entry<String, Integer> entry : sortedWordFrequency.entrySet()) {
                textArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "File not found!!!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static Set<String> readStopWords() throws IOException {
        return Files.lines(stopWordsFile.toPath())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private static void saveWordListToFile(String content) {
        JFileChooser saveChooser = new JFileChooser();
        if (saveChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File saveFile = saveChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
                writer.write(content);
                JOptionPane.showMessageDialog(null, "Word frequencies saved to " + saveFile.getAbsolutePath(), "Save Confirmation", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

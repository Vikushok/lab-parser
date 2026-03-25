import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class PdfReaderTest {

    public static void main(String[] args) {
        File pdfFolder = new File("pdfs");
        if (!pdfFolder.exists() || !pdfFolder.isDirectory()) {
            System.err.println("Папка 'pdfs' не найдена!");
            return;
        }

        File[] pdfFiles = pdfFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) {
            System.err.println("В папке 'pdfs' нет PDF-файлов!");
            return;
        }

        for (File pdfFile : pdfFiles) {
            System.out.println("Обработка: " + pdfFile.getName());
            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String rawText = stripper.getText(document);

                // Применяем полную очистку и структурирование
                String cleanedText = cleanAndStructureText(rawText);

                // Сохраняем результат
                String outFileName = "cleaned_" + pdfFile.getName().replace(".pdf", ".txt");
                try (PrintWriter writer = new PrintWriter(new FileWriter(outFileName))) {
                    writer.print(cleanedText);
                }
                System.out.println("Результат сохранён в: " + outFileName);

                // Показываем первые 20 строк очищенного текста
                System.out.println("--- ПЕРВЫЕ 20 СТРОК ОЧИЩЕННОГО ТЕКСТА ---");
                String[] lines = cleanedText.split("\n");
                for (int i = 0; i < Math.min(20, lines.length); i++) {
                    System.out.println(lines[i]);
                }
                System.out.println("...\n");

            } catch (IOException e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    /**
     * Основной метод очистки и структурирования текста.
     */
    private static String cleanAndStructureText(String text) {
        // 1. Исправляем степени (10^9, 10^12)
        text = fixExponents(text);

        // 2. Объединяем строки, разорванные точками или переносами
        text = mergeLines(text);

        // 3. Удаляем строки-разделители из точек и дефисов
        text = removeDottedLines(text);

        // 4. Удаляем точки в начале строк (остатки после удаления строк с точками)
        text = removeLeadingDots(text);

        // 5. Добавляем пробелы между числами и единицами измерения
        text = fixSpacing(text);

        // 6. Удаляем повторяющиеся пустые строки
        text = text.replaceAll("(?m)^\\s*$\\n", "").replaceAll("\\n{2,}", "\n");

        return text;
    }

    private static String fixExponents(String text) {
        text = text.replaceAll("10<sup>9</sup>", "10^9");
        text = text.replaceAll("10<sup>12</sup>", "10^12");
        text = text.replaceAll("10\\^9", "10^9");
        text = text.replaceAll("10\\^12", "10^12");
        text = text.replaceAll("(\\d+),(\\d{2})(1012)", "$1,$2 10^12");
        text = text.replaceAll("(\\d+),(\\d{2})(109)", "$1,$2 10^9");
        text = text.replaceAll("(\\d{3})(109)", "$1 10^9");
        return text;
    }

    private static String mergeLines(String text) {
        String[] lines = text.split("\n");
        StringBuilder merged = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String current = lines[i].trim();
            if (current.isEmpty()) continue;

            // Удаляем строки-разделители из точек
            if (current.matches("^[.\\-–—]{5,}$")) continue;

            // Проверяем, нужно ли объединить с следующей строкой
            if (i + 1 < lines.length) {
                String next = lines[i + 1].trim();

                // Условия для объединения:
                boolean shouldMerge = false;

                // 1. Следующая строка начинается с маленькой буквы или цифры
                if (next.matches("^[а-яa-z0-9].*")) shouldMerge = true;

                // 2. Следующая строка — единица измерения
                if (next.matches("^(ммоль/литр|мкмоль/литр|г/л|%|фемтолитр|пикограмм|г/дл|мм/час|мМЕ/литр|пмоль/литр|нг/мл|Е/литр|индекс поз).*"))
                    shouldMerge = true;

                // 3. Текущая строка заканчивается на дефис или открывающую скобку
                if (current.endsWith("-") || current.endsWith("(")) shouldMerge = true;

                // 4. Специальный случай: HDL-C (разрыв после (ЛПВП))
                if (current.contains("(ЛПВП)") && next.contains("(HDL-C)")) shouldMerge = true;

                // 5. Специальный случай: LDL-C (разрыв после (ЛПНП))
                if (current.contains("(ЛПНП)") && next.contains("(LDL-")) shouldMerge = true;

                if (shouldMerge) {
                    merged.append(current).append(" ").append(next).append("\n");
                    i++; // пропускаем следующую строку
                    continue;
                }
            }

            merged.append(current).append("\n");
        }

        return merged.toString();
    }

    private static String removeDottedLines(String text) {
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            // Удаляем строки, которые полностью состоят из точек, дефисов, пробелов
            if (!line.trim().matches("^[.\\-–—\\s]+$")) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Удаляем точки в начале строк (остатки после удаления строк с точками)
     */
    private static String removeLeadingDots(String text) {
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            // Удаляем точки в начале строки
            String cleaned = line.replaceFirst("^[.\\s]+", "");
            if (!cleaned.isEmpty()) {
                result.append(cleaned).append("\n");
            }
        }
        return result.toString();
    }

    private static String fixSpacing(String text) {
        text = text.replaceAll("(\\d+)([а-яА-Яa-zA-Z/])", "$1 $2");
        text = text.replaceAll("(\\d+,\\d+)([а-яА-Яa-zA-Z/])", "$1 $2");
        text = text.replaceAll("(\\d+)(%)", "$1 $2");
        text = text.replaceAll("(\\d+,\\d+)(%)", "$1 $2");
        return text;
    }
}
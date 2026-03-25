import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PdfReaderTest {
    public static void main(String[] args) {
        File pdfFolder = new File("pdfs");

        if (!pdfFolder.exists()) {
            System.out.println("❌ Папка 'pdfs' не найдена!");
            return;
        }

        File[] pdfFiles = pdfFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (pdfFiles == null || pdfFiles.length == 0) {
            System.out.println("❌ В папке 'pdfs' нет PDF-файлов!");
            return;
        }

        System.out.println("📁 Найдено PDF-файлов: " + pdfFiles.length);

        for (File pdfFile : pdfFiles) {
            try {
                System.out.println("\n📄 Обработка: " + pdfFile.getName());

                try (PDDocument document = PDDocument.load(pdfFile)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);

                    // Исправляем степени
                    text = fixExponents(text);

                    // Объединяем строки с единицами измерения
                    text = joinUnits(text);

                    // Сохраняем в файл
                    String outputFileName = "extracted_text_" + pdfFile.getName().replace(".pdf", ".txt");
                    try (PrintWriter writer = new PrintWriter(new FileWriter(outputFileName))) {
                        writer.print(text);
                        System.out.println("💾 Текст сохранён в: " + outputFileName);
                    }

                    // Показываем результаты
                    System.out.println("\n--- ИСПРАВЛЕННЫЕ ПОКАЗАТЕЛИ ---");
                    String[] lines = text.split("\n");
                    for (String line : lines) {
                        if ((line.contains("г/л") ||
                                line.contains("10^") ||
                                line.contains("%") ||
                                line.contains("фемтолитр") ||
                                line.contains("пикограмм") ||
                                line.contains("грамм") ||
                                line.contains("ммоль")) &&
                                !line.trim().isEmpty()) {
                            System.out.println(line.trim());
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("❌ Ошибка: " + e.getMessage());
            }
        }
    }

    /**
     * Объединение строк, где единица измерения находится на следующей строке
     */
    private static String joinUnits(String text) {
        String[] lines = text.split("\n");
        List<String> result = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i].trim();

            // Если текущая строка пустая, пропускаем
            if (currentLine.isEmpty()) {
                continue;
            }

            // Проверяем, начинается ли следующая строка с единицы измерения
            if (i + 1 < lines.length) {
                String nextLine = lines[i + 1].trim();

                // Единицы измерения на следующей строке
                if (nextLine.matches("^(грамм/литр|г/л|грамм/дл|г/дл|ммоль/литр|ммоль/л|фемтолитр|пикограмм|нг/мл|мг/мл|Е/литр|мкмоль/литр|%|10\\^\\d+/.*)$") ||
                        nextLine.equals("грамм/литр") ||
                        nextLine.equals("г/л") ||
                        nextLine.equals("ммоль/литр") ||
                        nextLine.equals("фемтолитр") ||
                        nextLine.equals("пикограмм") ||
                        nextLine.equals("%")) {

                    // Объединяем текущую строку с единицей измерения
                    result.add(currentLine + " " + nextLine);
                    i++; // Пропускаем следующую строку
                    continue;
                }
            }

            // Обычная строка
            result.add(currentLine);
        }

        return String.join("\n", result);
    }

    /**
     * Исправление степеней: 1012 -> 10^12, 109 -> 10^9
     */
    private static String fixExponents(String text) {

        // ===== 1. ИСПРАВЛЯЕМ 10^12 (1012) =====

        // 4,321012/литр -> 4,32 10^12/литр
        text = text.replaceAll("(\\d+),(\\d{2})(1012)(/.*)", "$1,$2 10^12$4");

        // 4,321012 -> 4,32 10^12
        text = text.replaceAll("(\\d+),(\\d{2})(1012)(\\s|$)", "$1,$2 10^12$4");

        // 1012/литр -> 10^12/литр
        text = text.replaceAll("(\\s|^)(1012)(/.*)", "$1 10^12$3");

        // ===== 2. ИСПРАВЛЯЕМ 10^9 (109) =====

        // 269109/литр -> 269 10^9/литр
        text = text.replaceAll("(\\d{3})(109)(/.*)", "$1 10^9$3");

        // 6,15109/литр -> 6,15 10^9/литр
        text = text.replaceAll("(\\d+),(\\d{2})(109)(/.*)", "$1,$2 10^9$4");

        // 109/литр -> 10^9/литр
        text = text.replaceAll("(\\s|^)(109)(/.*)", "$1 10^9$3");

        // ===== 3. ДОБАВЛЯЕМ ПРОБЕЛЫ МЕЖДУ ЧИСЛОМ И ЕДИНИЦЕЙ =====

        // 137грамм/литр -> 137 грамм/литр
        text = text.replaceAll("(\\d+)([а-яА-Яa-zA-Z/])", "$1 $2");

        // 95,8фемтолитр -> 95,8 фемтолитр
        text = text.replaceAll("(\\d+,\\d+)([а-яА-Яa-zA-Z/])", "$1 $2");

        // 31,7пикограмм -> 31,7 пикограмм
        text = text.replaceAll("(\\d+,\\d+)([а-яА-Яa-zA-Z/])", "$1 $2");

        // ===== 4. ДОБАВЛЯЕМ ПРОБЕЛ МЕЖДУ ЧИСЛОМ И ПРОЦЕНТАМИ =====

        // 41,4% -> 41,4 %
        text = text.replaceAll("(\\d+,\\d+)(%)", "$1 $2");
        text = text.replaceAll("(\\d+)(%)", "$1 $2");

        // ===== 5. УДАЛЯЕМ ЛИШНИЕ ПРОБЕЛЫ =====

        // Убираем множественные пробелы
        text = text.replaceAll("\\s+", " ");

        // Убираем пробелы перед запятой
        text = text.replaceAll("\\s+,", ",");

        return text;
    }
}
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class LabReportParser {

    // Словарь показателей: точное название в тексте -> (русское название, английский код, единица)
    private static final Map<String, MetricInfo> METRIC_MAP = new LinkedHashMap<>();

    static {
        // ===== КЛИНИКА КРОВИ =====
        addMetric("Гемоглобин (HGB)", "Гемоглобин", "HGB", "г/л");
        addMetric("Гематокрит (HCT)", "Гематокрит", "HCT", "%");
        addMetric("Эритроциты (RBC)", "Эритроциты", "RBC", "10^12/л");
        addMetric("Средний объем эритроцитов (MCV)", "Средний объем эритроцитов", "MCV", "фл");
        addMetric("Среднее содержание гемоглобина в эритроците (MCH)", "Среднее содержание гемоглобина", "MCH", "пг");
        addMetric("Средняя концентрация гемоглобина в эритроците (MCHC)", "Средняя концентрация гемоглобина", "MCHC", "г/дл");
        addMetric("Ширина распределения эритроцитов по объёму (RDW-SD)", "RDW-SD", "RDW-SD", "фл");
        addMetric("Ширина распределения эритроцитов по объёму (RDW-CV)", "RDW-CV", "RDW-CV", "%");
        addMetric("Тромбоциты (PLT)", "Тромбоциты", "PLT", "10^9/л");
        addMetric("Тромбокрит (PCT)", "Тромбокрит", "PCT", "%");
        addMetric("Средний объем тромбоцита (MPV)", "Средний объем тромбоцита", "MPV", "фл");
        addMetric("Ширина распределения тромбоцитов (PDW)", "Ширина распределения тромбоцитов", "PDW", "фл");
        addMetric("Содержание крупных тромбоцитов (P-LCR)", "Содержание крупных тромбоцитов", "P-LCR", "%");
        addMetric("Лейкоциты (WBC)", "Лейкоциты", "WBC", "10^9/л");
        addMetric("Нейтрофилы (NEUT#)", "Нейтрофилы", "NEUT#", "10^9/л");
        addMetric("Нейтрофилы (NEUT%)", "Нейтрофилы %", "NEUT%", "%");
        addMetric("Эозинофилы (EO#)", "Эозинофилы", "EO#", "10^9/л");
        addMetric("Эозинофилы (EO%)", "Эозинофилы %", "EO%", "%");
        addMetric("Базофилы (BASO#)", "Базофилы", "BASO#", "10^9/л");
        addMetric("Базофилы (BASO%)", "Базофилы %", "BASO%", "%");
        addMetric("Лимфоциты (LYMPH#)", "Лимфоциты", "LYMPH#", "10^9/л");
        addMetric("Лимфоциты (LYMPH%)", "Лимфоциты %", "LYMPH%", "%");
        addMetric("Моноциты (MONO#)", "Моноциты", "MONO#", "10^9/л");
        addMetric("Моноциты (MONO%)", "Моноциты %", "MONO%", "%");
        addMetric("Скорость оседания эритроцитов (СОЭ) (ESR) венозной крови", "СОЭ", "ESR", "мм/час");

        // ===== БИОХИМИЯ =====
        addMetric("Глюкоза (Glucose)", "Глюкоза", "Glucose", "ммоль/л");
        addMetric("Билирубин общий (Bilirubin total)", "Билирубин общий", "Bilirubin total", "мкмоль/л");
        addMetric("Билирубин прямой/связанный (Direct bilirubin)", "Билирубин прямой", "Direct bilirubin", "мкмоль/л");
        addMetric("Билирубин непрямой/свободный (Indirect bilirubin)", "Билирубин непрямой", "Indirect bilirubin", "мкмоль/л");
        addMetric("Креатинин (Creatinine)", "Креатинин", "Creatinine", "мкмоль/л");
        addMetric("Мочевина (Urea)", "Мочевина", "Urea", "ммоль/л");
        addMetric("Мочевая кислота (Uric acid)", "Мочевая кислота", "Uric acid", "мкмоль/л");
        addMetric("Белок общий (Protein total)", "Белок общий", "Protein total", "г/л");
        addMetric("Альбумин (Albumin)", "Альбумин", "Albumin", "г/л");
        addMetric("Гомоцистеин (Homocysteine)", "Гомоцистеин", "Homocysteine", "мкмоль/л");
        addMetric("Триглицериды (Triglycerides)", "Триглицериды", "Triglycerides", "ммоль/л");
        addMetric("Холестерин общий (Cholesterol total)", "Холестерин общий", "Cholesterol total", "ммоль/л");

        // ЛИПИДНЫЙ ПРОФИЛЬ (с учётом разорванных строк)
        addMetric("Холестерин липопротеинов высокой плотности (ЛПВП) (HDL-C)", "Холестерин ЛПВП", "HDL-C", "ммоль/л");
        addMetric("Холестерин липопротеинов низкой плотности (ЛПНП) (LDL- C)", "Холестерин ЛПНП", "LDL-C", "ммоль/л");
        addMetric("Холестерин липопротеинов очень низкой плотности (ЛПОНП) (VLDL-C)", "Холестерин ЛПОНП", "VLDL-C", "ммоль/л");
        addMetric("Индекс атерогенности (Atherogenic index)", "Индекс атерогенности", "Atherogenic index", "");
        addMetric("Холестерин не-ЛПВП (non-HDL-C)", "Холестерин не-ЛПВП", "non-HDL-C", "ммоль/л");
        addMetric("Холестерин ремнантный (Cholesterol remnant)", "Холестерин ремнантный", "Cholesterol remnant", "ммоль/л");

        // ===== ФЕРМЕНТЫ =====
        addMetric("Аспартатаминотрансфераза (АсАТ) (Aspartate aminotransferase, AST)", "АсАТ", "AST", "Е/л");
        addMetric("Аланинаминотрансфераза (АлАТ) (Alanine aminotransferase, ALT)", "АлАТ", "ALT", "Е/л");
        addMetric("Лактат (Lactate)", "Лактат", "Lactate", "ммоль/л");

        // ===== МИКРОЭЛЕМЕНТЫ =====
        addMetric("Железо (Ferrum, Fe) в крови", "Железо", "Fe", "мкмоль/л");
        addMetric("Магний (Magnesium, Mg) в крови", "Магний", "Mg", "ммоль/л");
        addMetric("Кальций (Calcium, Сa) в крови", "Кальций", "Ca", "ммоль/л");
        addMetric("Фосфор (Phosphorus, P) в крови", "Фосфор", "P", "ммоль/л");
        addMetric("Калий (Potassium, К+) в крови", "Калий", "K", "ммоль/л");
        addMetric("Натрий (Sodium, Na+) в крови", "Натрий", "Na", "ммоль/л");
        addMetric("Хлор (Chloride, Сl-) в крови", "Хлор", "Cl", "ммоль/л");

        // ===== ГОРМОНЫ (с точным поиском) =====
        addMetric("Паратиреоидный гормон (ПТГ) (Parathyroid hormone)", "Паратиреоидный гормон", "PTH", "пмоль/л");
        addMetric("Тиреотропный гормон (ТТГ) (Thyroid-stimulating hormone)", "ТТГ", "TSH", "мМЕ/л");
        addMetric("Трийодтиронин свободный (Triiodthyronine free, FT3)", "Т3 свободный", "FT3", "пмоль/л");
        addMetric("Тироксин свободный (Thyroxine free, FT4)", "Т4 свободный", "FT4", "пмоль/л");

        // ===== СЕРОЛОГИЯ (гельминтозы) =====
        addMetric("Антитела к эхинококку (Echinococcus) IgG [отрицательный]", "Антитела к эхинококку IgG", "Echinococcus IgG", "индекс поз.");
        addMetric("Антитела к описторхесу (Opistorhis) IgG [отрицательный]", "Антитела к описторхису IgG", "Opistorhis IgG", "индекс поз.");
        addMetric("Антитела к токсокаре (Toxocara) IgG [отрицательный]", "Антитела к токсокаре IgG", "Toxocara IgG", "индекс поз.");
        addMetric("Антитела к трихинелле (Trichinella) IgG [отрицательный]", "Антитела к трихинелле IgG", "Trichinella IgG", "индекс поз.");
        addMetric("Антитела к лямблиям (Lamblia) lgG, lgM, lgA [отрицательный]", "Антитела к лямблиям IgG/IgM/IgA", "Lamblia IgG/IgM/IgA", "индекс поз.");
        addMetric("Антитела к аскаридам (Ascaris) lgG [отрицательный]", "Антитела к аскаридам IgG", "Ascaris IgG", "индекс поз.");

        // ===== СЕРОЛОГИЯ (инфекции) =====
        addMetric("Антитела к кандида (Candida) IgG [отрицательный]", "Антитела к Candida IgG", "Candida IgG", "индекс поз.");
        addMetric("Антитела к кандида (Candida) IgM [отрицательный]", "Антитела к Candida IgM", "Candida IgM", "индекс поз.");

        // ===== ОНКОМАРКЕРЫ =====
        addMetric("Антиген плоскоклеточной карциномы (Squamous cell carcinoma, SCC)", "SCC", "SCC", "нг/мл");
    }

    private static void addMetric(String searchKey, String nameRu, String nameEn, String unit) {
        METRIC_MAP.put(searchKey, new MetricInfo(nameRu, nameEn, unit));
    }

    public static void main(String[] args) {
        String inputFile = "cleaned_010524frolovavv.txt";
        String outputFile = "parsed_metrics.json";

        try {
            String text = readFile(inputFile);
            List<LabValue> values = parseMetrics(text);

            System.out.println("========================================");
            System.out.println("ИЗВЛЕЧЁННЫЕ ПОКАЗАТЕЛИ:");
            System.out.println("========================================\n");

            for (LabValue v : values) {
                System.out.println(v);
            }

            System.out.println("\n========================================");
            System.out.println("Всего найдено: " + values.size() + " показателей");
            System.out.println("========================================");

            saveToJson(values, outputFile);

        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static List<LabValue> parseMetrics(String text) {
        List<LabValue> results = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            for (Map.Entry<String, MetricInfo> entry : METRIC_MAP.entrySet()) {
                String searchKey = entry.getKey();
                MetricInfo info = entry.getValue();

                if (line.contains(searchKey)) {
                    Double value = extractNumber(line);
                    String unit = extractUnit(line);

                    // Если единица не найдена, используем из словаря
                    if (unit.equals("не указана") && !info.unit.isEmpty()) {
                        unit = info.unit;
                    }

                    if (value != null) {
                        LabValue v = new LabValue();
                        v.nameRu = info.nameRu;
                        v.nameEn = info.nameEn;
                        v.value = value;
                        v.unit = unit;
                        results.add(v);
                        break;
                    }
                }
            }
        }

        return results;
    }

    private static Double extractNumber(String line) {
        Pattern pattern = Pattern.compile("(\\d+[,.]?\\d*)\\s*(?:10\\^\\d+)?");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String numStr = matcher.group(1).replace(',', '.');
            try {
                return Double.parseDouble(numStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String extractUnit(String line) {
        Pattern pattern = Pattern.compile("(г/л|%|10\\^\\d+/литр|ммоль/литр|мкмоль/литр|фемтолитр|пикограмм|г/дл|мм/час|мМЕ/литр|пмоль/литр|нг/мл|Е/литр|индекс поз\\.)");
        Matcher matcher = pattern.matcher(line);
        return matcher.find() ? matcher.group(1) : "не указана";
    }

    private static void saveToJson(List<LabValue> values, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("{");
            writer.println("  \"analysis_date\": \"01.05.2024\",");
            writer.println("  \"patient\": \"Фролова Виктория Вадимовна\",");
            writer.println("  \"metrics\": [");

            for (int i = 0; i < values.size(); i++) {
                LabValue v = values.get(i);
                writer.print("    {");
                writer.print("\"name_ru\": \"" + v.nameRu + "\", ");
                writer.print("\"name_en\": \"" + v.nameEn + "\", ");
                writer.print("\"value\": " + v.value + ", ");
                writer.print("\"unit\": \"" + v.unit + "\"");
                writer.print("}");
                if (i < values.size() - 1) writer.println(",");
                else writer.println();
            }
            writer.println("  ]");
            writer.println("}");
        }
        System.out.println("\n💾 Результат сохранён в: " + filename);
    }

    static class MetricInfo {
        String nameRu;
        String nameEn;
        String unit;
        MetricInfo(String nameRu, String nameEn, String unit) {
            this.nameRu = nameRu;
            this.nameEn = nameEn;
            this.unit = unit;
        }
    }

    static class LabValue {
        String nameRu;
        String nameEn;
        double value;
        String unit;

        @Override
        public String toString() {
            return String.format("%s (%s): %.2f %s", nameRu, nameEn, value, unit);
        }
    }
}
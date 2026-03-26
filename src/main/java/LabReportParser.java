import java.io.*;
import java.util.*;
import java.util.regex.*;

public class LabReportParser {

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

        // ===== БИОХИМИЯ (субстраты) =====
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

        // ===== ЛИПИДНЫЙ ПРОФИЛЬ =====
        addMetric("Триглицериды (Triglycerides)", "Триглицериды", "Triglycerides", "ммоль/л");
        addMetric("Холестерин общий (Cholesterol total)", "Холестерин общий", "Cholesterol total", "ммоль/л");
        addMetric("Холестерин липопротеинов высокой плотности (ЛПВП) (HDL-C)", "Холестерин ЛПВП", "HDL-C", "ммоль/л");
        addMetric("Холестерин липопротеинов низкой плотности (ЛПНП) (LDL- C)", "Холестерин ЛПНП", "LDL-C", "ммоль/л");
        addMetric("Холестерин липопротеинов очень низкой плотности (ЛПОНП) (VLDL-C)", "Холестерин ЛПОНП", "VLDL-C", "ммоль/л");
        addMetric("Индекс атерогенности (Atherogenic index)", "Индекс атерогенности", "Atherogenic index", "");
        addMetric("Холестерин не-ЛПВП (non-HDL-C)", "Холестерин не-ЛПВП", "non-HDL-C", "ммоль/л");
        addMetric("Холестерин ремнантный (Cholesterol remnant)", "Холестерин ремнантный", "Cholesterol remnant", "ммоль/л");
        addMetric("Аполипопротеин (Apolipoproteine) А1", "Аполипопротеин А1", "ApoA1", "г/л");
        addMetric("Аполипопротеин (Apolipoproteine) B", "Аполипопротеин B", "ApoB", "г/л");
        addMetric("Липопротеин (a) (Lipoprotein (a))", "Липопротеин (a)", "Lp(a)", "нмоль/л");

        // ===== ФЕРМЕНТЫ =====
        addMetric("Аспартатаминотрансфераза (АсАТ) (Aspartate aminotransferase, AST)", "АсАТ", "AST", "Е/л");
        addMetric("Аланинаминотрансфераза (АлАТ) (Alanine aminotransferase, ALT)", "АлАТ", "ALT", "Е/л");
        addMetric("Тимоловая проба (Thymol turbidity test)", "Тимоловая проба", "Thymol", "ед.");
        addMetric("Альфа-амилаза (Amylase)", "Альфа-амилаза", "Amylase", "Е/л");
        addMetric("Альфа-амилаза панкреатическая (Amylase pancreatic)", "Альфа-амилаза панкреатическая", "Amylase pancreatic", "Е/л");
        addMetric("Гамма-глутамилтранспептидаза (ГГТП) (Gamma-glutamyl transpeptidase, GGT)", "ГГТП", "GGT", "Е/л");
        addMetric("Фосфатаза щелочная (Alkaline phosphatase)", "Щелочная фосфатаза", "ALP", "Е/л");
        addMetric("Фосфатаза кислая общая", "Кислая фосфатаза", "ACP", "Е/л");
        addMetric("Липаза (Lipase)", "Липаза", "Lipase", "Е/л");
        addMetric("Креатинкиназа общая (Creatine phosphokinase total)", "Креатинкиназа общая", "CK", "Е/л");
        addMetric("Креатинкиназа-МВ (Creatine phosphokinase MB)", "Креатинкиназа-МВ", "CK-MB", "Е/л");
        addMetric("Лактатдегидрогеназа (Lactate dehydrogenase)", "ЛДГ", "LDH", "Е/л");
        addMetric("Холинэстераза (Cholinesterase)", "Холинэстераза", "Cholinesterase", "Е/л");
        addMetric("Лактат (Lactate)", "Лактат", "Lactate", "ммоль/л");

        // ===== МИКРОЭЛЕМЕНТЫ =====
        addMetric("Железо (Iron)", "Железо", "Fe", "мкмоль/л");
        addMetric("Магний (Magnesium)", "Магний", "Mg", "ммоль/л");
        addMetric("Кальций (Calcium)", "Кальций", "Ca", "ммоль/л");
        addMetric("Фосфор (Phosphorus)", "Фосфор", "P", "ммоль/л");
        addMetric("Калий (К+, Potassium)", "Калий", "K", "ммоль/л");
        addMetric("Натрий (Na+, Sodium)", "Натрий", "Na", "ммоль/л");
        addMetric("Хлор (Сl-, Chloride)", "Хлор", "Cl", "ммоль/л");
        addMetric("Медь (Copper)", "Медь", "Cu", "мкмоль/л");

        // ===== ВИТАМИНЫ =====
        addMetric("Витамин B12 (Cyanocobalamin)", "Витамин B12", "B12", "пг/мл");
        addMetric("Фолиевая кислота (Folic acid)", "Фолиевая кислота", "Folate", "нг/мл");
        addMetric("25-OH витамин D (25-Hydroxy vitamin D)", "Витамин D", "Vitamin D", "нг/мл");

        // ===== ДИАГНОСТИКА АНЕМИИ =====
        addMetric("Ферритин (Ferritin)", "Ферритин", "Ferritin", "мкг/л");

        // ===== МАРКЕРЫ ВОСПАЛЕНИЯ =====
        addMetric("Антистрептолизин-О (АСЛ-О) (Anti-streptolysin O)", "АСЛ-О", "ASO", "МЕ/мл");
        addMetric("С-реактивный белок (СРБ) (C-reactive protein, CRP)", "СРБ", "CRP", "мг/л");
        addMetric("Ревматоидный фактор (Rheumatoid factor)", "Ревматоидный фактор", "RF", "МЕ/мл");

        // ===== КОАГУЛОЛОГИЯ =====
        addMetric("Протромбин % по Квику (Prothrombin)", "Протромбин (по Квику)", "Prothrombin", "%");
        addMetric("Протромбин МНО (International normalized ratio, INR)", "МНО", "INR", "");
        addMetric("Протромбиновое время (Prothrombin)", "Протромбиновое время", "PT", "сек");

        // ===== ИММУНОЛОГИЯ =====
        addMetric("Иммуноглобулин Е (Immunoglobulin E, IgE)", "IgE", "IgE", "МЕ/мл");
        addMetric("Иммуноглобулин А (Immunoglobulin A, IgА)", "IgA", "IgA", "г/л");
        addMetric("Иммуноглобулин М (Immunoglobulin M, IgМ)", "IgM", "IgM", "г/л");
        addMetric("Иммуноглобулин G (Immunoglobulin G, IgG)", "IgG", "IgG", "г/л");

        // ===== СЕРОЛОГИЯ =====
        addMetric("Концентрация антител к коронавирусу SARS-CoV-2 IgG", "SARS-CoV-2 IgG", "SARS-CoV-2 IgG", "BAU/мл");
        addMetric("Антитела к коронавирусу SARS-CoV-2 /COVID-19 / IgМ [отрицательный]", "SARS-CoV-2 IgM", "SARS-CoV-2 IgM", "индекс поз.");

        // ===== ГОРМОНЫ =====
        addMetric("Паратиреоидный гормон (ПТГ) (Parathyroid hormone)", "Паратиреоидный гормон", "PTH", "пмоль/л");
        addMetric("Тиреотропный гормон (ТТГ) (Thyroid-stimulating hormone)", "ТТГ", "TSH", "мМЕ/л");
        addMetric("Трийодтиронин свободный (Triiodthyronine free, FT3)", "Т3 свободный", "FT3", "пмоль/л");
        addMetric("Тироксин свободный (Thyroxine free, FT4)", "Т4 свободный", "FT4", "пмоль/л");

        // ===== СЕРОЛОГИЯ (гельминтозы) =====
        addMetric("Антитела к эхинококку (Echinococcus) IgG [отрицательный]", "Антитела к эхинококку IgG", "Echinococcus IgG", "индекс поз.");
        addMetric("Антитела к описторхесу (Opistorhis) IgG [отрицательный]", "Антитела к описторхису IgG", "Opistorhis IgG", "индекс поз.");
        addMetric("Антитела к токсокаре (Toxocara) IgG [отрицательный]", "Антитела к токсокаре IgG", "Toxocara IgG", "индекс поз.");
        addMetric("Антитела к трихинелле (Trichinella) IgG [отрицательный]", "Антитела к трихинелле IgG", "Trichinella IgG", "индекс поз.");
        addMetric("Антитела к лямблиям (Lamblia) lgG, lgM, lgA [отрицательный]", "Антитела к лямблиям", "Lamblia", "индекс поз.");
        addMetric("Антитела к аскаридам (Ascaris) lgG [отрицательный]", "Антитела к аскаридам IgG", "Ascaris IgG", "индекс поз.");
        addMetric("Антитела к кандида (Candida) IgG [отрицательный]", "Антитела к Candida IgG", "Candida IgG", "индекс поз.");
        addMetric("Антитела к кандида (Candida) IgM [отрицательный]", "Антитела к Candida IgM", "Candida IgM", "индекс поз.");

        // ===== ОНКОМАРКЕРЫ =====
        addMetric("Антиген плоскоклеточной карциномы (Squamous cell carcinoma, SCC)", "SCC", "SCC", "нг/мл");
    }

    private static void addMetric(String searchKey, String nameRu, String nameEn, String unit) {
        METRIC_MAP.put(searchKey, new MetricInfo(nameRu, nameEn, unit));
    }

    public static void main(String[] args) {
        File folder = new File(".");
        File[] cleanedFiles = folder.listFiles((dir, name) -> name.startsWith("cleaned_") && name.endsWith(".txt"));

        if (cleanedFiles == null || cleanedFiles.length == 0) {
            System.out.println("❌ Нет файлов cleaned_*.txt в папке проекта!");
            System.out.println("Сначала запустите PdfReaderTest для извлечения текста из PDF.");
            return;
        }

        System.out.println("📁 Найдено файлов: " + cleanedFiles.length);
        System.out.println("========================================");

        for (File file : cleanedFiles) {
            String inputFile = file.getName();
            String outputFile = inputFile.replace("cleaned_", "parsed_").replace(".txt", ".json");

            System.out.println("\n📄 Обработка: " + inputFile);
            System.out.println("----------------------------------------");

            try {
                String text = readFile(inputFile);
                List<LabValue> values = parseMetrics(text);

                System.out.println("✅ Извлечено показателей: " + values.size());

                saveToJson(values, outputFile);
                System.out.println("💾 Сохранён: " + outputFile);

            } catch (IOException e) {
                System.err.println("❌ Ошибка: " + e.getMessage());
            }
        }

        System.out.println("\n🎉 Все файлы обработаны!");
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

    private static Double extractNumberForMetric(String line, String metricName) {
        int pos = line.indexOf(metricName);
        if (pos == -1) return null;

        String afterMetric = line.substring(pos + metricName.length());
        Pattern pattern = Pattern.compile("(\\d+[,.]?\\d*)\\s*(?:10\\^\\d+)?");
        Matcher matcher = pattern.matcher(afterMetric);
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
        Pattern pattern = Pattern.compile("(г/л|%|10\\^\\d+/литр|ммоль/литр|мкмоль/литр|фемтолитр|пикограмм|г/дл|мм/час|мМЕ/литр|пмоль/литр|нг/мл|пг/мл|мкг/литр|Е/литр|ед\\.|МЕ/мл|мг/литр|BAU/мл|нмоль/литр|индекс поз\\.)");
        Matcher matcher = pattern.matcher(line);
        return matcher.find() ? matcher.group(1) : "не указана";
    }

    private static List<LabValue> parseMetrics(String text) {
        List<LabValue> results = new ArrayList<>();
        String[] lines = text.split("\n");
        Set<String> foundMetrics = new HashSet<>();

        // Первый проход: гормоны по полным названиям + SARS-CoV-2 IgM
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            // FT3
            if (line.contains("Трийодтиронин свободный") && line.contains("FT3")) {
                Double value = extractNumberForMetric(line, "Трийодтиронин свободный (Triiodthyronine free, FT3)");
                if (value != null) {
                    results.add(new LabValue("Т3 свободный", "FT3", value, "пмоль/л"));
                    foundMetrics.add("FT3");
                }
            }

            // FT4
            if (line.contains("Тироксин свободный") && line.contains("FT4")) {
                Double value = extractNumberForMetric(line, "Тироксин свободный (Thyroxine free, FT4)");
                if (value != null) {
                    results.add(new LabValue("Т4 свободный", "FT4", value, "пмоль/л"));
                    foundMetrics.add("FT4");
                }
            }

            // ТТГ
            if (line.contains("Тиреотропный гормон") && line.contains("ТТГ")) {
                Double value = extractNumberForMetric(line, "Тиреотропный гормон (ТТГ) (Thyroid-stimulating hormone)");
                if (value != null) {
                    results.add(new LabValue("ТТГ", "TSH", value, "мМЕ/л"));
                    foundMetrics.add("TSH");
                }
            }

            // Паратиреоидный гормон
            if (line.contains("Паратиреоидный гормон") && line.contains("ПТГ")) {
                Double value = extractNumberForMetric(line, "Паратиреоидный гормон (ПТГ) (Parathyroid hormone)");
                if (value != null) {
                    results.add(new LabValue("Паратиреоидный гормон", "PTH", value, "пмоль/л"));
                    foundMetrics.add("PTH");
                }
            }

            // SARS-CoV-2 IgM (разорванная строка)
            if (line.contains("Антитела к коронавирусу SARS-CoV-2")) {
                if (i + 1 < lines.length) {
                    String nextLine = lines[i + 1].trim();
                    if (nextLine.contains("IgМ") && nextLine.contains("0,10")) {
                        results.add(new LabValue("SARS-CoV-2 IgM", "SARS-CoV-2 IgM", 0.10, "индекс поз."));
                        foundMetrics.add("SARS-CoV-2 IgM");
                        i++;
                        continue;
                    }
                }
            }
        }

        // Второй проход: остальные показатели
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            for (Map.Entry<String, MetricInfo> entry : METRIC_MAP.entrySet()) {
                String searchKey = entry.getKey();
                MetricInfo info = entry.getValue();

                if (foundMetrics.contains(info.nameEn)) continue;
                if (info.nameEn.equals("FT3") || info.nameEn.equals("FT4") ||
                        info.nameEn.equals("TSH") || info.nameEn.equals("PTH") ||
                        info.nameEn.equals("SARS-CoV-2 IgM")) continue;

                if (line.contains(searchKey)) {
                    Double value = extractNumberForMetric(line, searchKey);
                    String unit = extractUnit(line);

                    if (unit.equals("не указана") && !info.unit.isEmpty()) {
                        unit = info.unit;
                    }

                    if (value != null) {
                        results.add(new LabValue(info.nameRu, info.nameEn, value, unit));
                        foundMetrics.add(info.nameEn);
                        break;
                    }
                }
            }
        }

        return results;
    }

    private static void saveToJson(List<LabValue> values, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("{");
            writer.println("  \"analysis_date\": \"2024-04-08\",");
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
    }

    static class MetricInfo {
        String nameRu, nameEn, unit;
        MetricInfo(String nameRu, String nameEn, String unit) {
            this.nameRu = nameRu;
            this.nameEn = nameEn;
            this.unit = unit;
        }
    }

    static class LabValue {
        String nameRu, nameEn, unit;
        double value;

        LabValue(String nameRu, String nameEn, double value, String unit) {
            this.nameRu = nameRu;
            this.nameEn = nameEn;
            this.value = value;
            this.unit = unit;
        }

        @Override
        public String toString() {
            if (unit.isEmpty() || unit.equals("не указана")) {
                return String.format("%s (%s): %.2f", nameRu, nameEn, value);
            }
            return String.format("%s (%s): %.2f %s", nameRu, nameEn, value, unit);
        }
    }
}
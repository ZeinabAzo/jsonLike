import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOTE:
 * This class grew very large during experimentation and learning.
 * It currently mixes multiple responsibilities:
 *  - Schema definition
 *  - Record storage
 *  - Validation logic
 *  - Query/filter handling
 *  - Console rendering
 *
 * A proper refactor would split this into:
 *  - Schema
 *  - Field / Record
 *  - Validator
 *  - QueryEngine
 *  - ConsoleRenderer
 *
 * This implementation is kept as-is for learning and archival purposes.
 */

public class Json {
    private int insertedJsons = -1;
    private String name; // to store the json name
    private ArrayList<FieldProperties> fieldList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> jsonData = new ArrayList<>();
    private HashMap<String, ArrayList<Object>> uniqueFieldData = new HashMap<>();

    public class consoleColors {

        // Dim/Faint style
        public static final String DIM = "\u001B[2m";

        // Reset
        public static final String RESET = "\u001B[0m";

        // Regular Colors
        public static final String BLACK = "\u001B[30m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[34m";
        public static final String PURPLE = "\u001B[35m";
        public static final String CYAN = "\u001B[36m";
        public static final String WHITE = "\u001B[37m";

        // Bold Colors
        public static final String BRIGHT_BLACK = "\u001B[90m";
        public static final String BRIGHT_RED = "\u001B[91m";
        public static final String BRIGHT_GREEN = "\u001B[92m";
        public static final String BRIGHT_YELLOW = "\u001B[93m";
        public static final String BRIGHT_BLUE = "\u001B[94m";
        public static final String BRIGHT_PURPLE = "\u001B[95m";
        public static final String BRIGHT_CYAN = "\u001B[96m";
        public static final String BRIGHT_WHITE = "\u001B[97m";

        // Backgrounds
        public static final String BG_LIGHT_GRAY = "\u001B[48;5;235m"; // Light gray background
        public static final String BG_DARK_GRAY = "\u001B[48;5;234m"; // Darker gray background
        public static final String BG_BLACK = "\u001B[40m";
        public static final String BG_RED = "\u001B[41m";
        public static final String BG_GREEN = "\u001B[42m";
        public static final String BG_YELLOW = "\u001B[43m";
        public static final String BG_BLUE = "\u001B[44m";
        public static final String BG_PURPLE = "\u001B[45m";
        public static final String BG_CYAN = "\u001B[46m";
        public static final String BG_WHITE = "\u001B[47m";
    }

    public Json(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<FieldProperties> getFields() {
        return new ArrayList<>(fieldList);
    }

    public static boolean isCreateContentJsonLike(String input) {
        // Match each "field": { ... } pair
        String fieldPattern = "\"(\\w+)\"\\s*:\\s*\\{[^{}]*}";
        Pattern fieldRegex = Pattern.compile(fieldPattern);
        Matcher fieldMatcher = fieldRegex.matcher(input);

        // Pattern to validate content of each field object
        String contentPattern = "\\{\\s*\"type\"\\s*:\\s*\"(\\w+)\"\\s*" +
                "(?:,\\s*\"required\"\\s*:\\s*(true|false)\\s*)?" +
                "(?:,\\s*\"unique\"\\s*:\\s*(true|false)\\s*)?\\s*}";
        Pattern fieldContentRegex = Pattern.compile(contentPattern);

        int totalFields = 0;
        int validFields = 0;

        while (fieldMatcher.find()) {
            totalFields++;
            String fullField = fieldMatcher.group(); // e.g. "id": {...}
            String valuePart = fullField.substring(fullField.indexOf(":") + 1).trim();

            Matcher valueMatcher = fieldContentRegex.matcher(valuePart);
            if (valueMatcher.matches()) {
                validFields++;
            }
        }

        if (totalFields == validFields) {
            return true;
        } else {
            return false;
        }
    }

    public Object parseValue(String type, String value) {

        type = type.toLowerCase();

        switch (type) {
            case "int" -> {
                return Integer.parseInt(value);
            }
            case "dbl" -> {
                return Double.parseDouble(value);
            }
            case "bool" -> {
                return Boolean.parseBoolean(value);
            }
            default -> {
                return value;
            }
        }
    }

    private ArrayList<ArrayList<String>> extractFields(String input) {// for insert
        String regex = "(?:\"(\\w+)\"\\s*:\\s*(true|false|\\d*.\\d+|\"?\\w+\\s*\\w*\"?|\\d+|\\{[^\\}]*\\}|\\[[^\\]]*\\]))";
        Matcher matcher = Pattern.compile(regex).matcher(input);
        ArrayList<ArrayList<String>> fieldRecord = new ArrayList<>();

        while (matcher.find()) {
            if (!matcher.group(2).trim().startsWith("{")) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                ArrayList<String> temp = new ArrayList<>();
                temp.add(key);
                temp.add(value);
                fieldRecord.add(temp);

            } else {
                // handle nested
            }

        }
        return fieldRecord;

    }

    public static void printSuccess(String message) {
        System.out.println(consoleColors.GREEN + "[SUCCESS] " + message + consoleColors.RESET);
    }

    public static void printError(String message) {
        System.out.println(consoleColors.RED + "[ERROR] " + message + consoleColors.RESET);
    }

    public static void printINf(String message) {
        System.out.println(consoleColors.BRIGHT_PURPLE + "[Announcement] " + message + consoleColors.RESET);
    }

    public boolean createJson(String input) {
        if (!input.trim().startsWith("{") || !input.trim().endsWith("}")) {
            printError("JSON must start with { and end with }");
            return false;
        }

        String content = input.trim().substring(1, input.length() - 1).trim();
        if (content.isEmpty()) {
            printError("Empty JSON definition");
            return false;
        }

        if (!isCreateContentJsonLike(content)) {
            printError("input is not json-like");
            return false;
        }

        // regex for field definitions
        String fieldRegex = "\"(\\w+)\"\\s*:\\s*\\{\\s*\"type\"\\s*:\\s*\"(\\w+)\"\\s*(?:,\\s*\"required\"\\s*:\\s*(true|false)\\s*)?(?:,\\s*\"unique\"\\s*:\\s*(true|false)\\s*)?\\}";

        Pattern pattern = Pattern.compile(fieldRegex);
        Matcher matcher = pattern.matcher(content);

        boolean hasFields = false;
        while (matcher.find()) {
            hasFields = true;
            String fieldName = matcher.group(1);
            String fieldType = matcher.group(2);
            boolean required = matcher.group(3) != null && Boolean.parseBoolean(matcher.group(3));
            boolean unique = matcher.group(4) != null && Boolean.parseBoolean(matcher.group(4));

            if (!isValidType(fieldType)) {
                printError("Invalid type '" + fieldType + "' for field '" + fieldName + "'");
                return false;
            }

            for (FieldProperties name : fieldList) {
                if (name.getName().equals(fieldName)) {
                    printError("Duplicate field name: " + fieldName);
                    return false;
                }
            }

            AddField(fieldName, fieldType, required, unique);
        }

        if (!hasFields) {
            printError("No valid fields found in JSON definition");
            return false;
        }

        return true;
    }

    private boolean isValidType(String type) {
        return type.matches("string|int|dbl|bool|list|time");
    }

    public void AddField(String name, String type, Boolean required, Boolean unique) {// for create
        FieldProperties field = new FieldProperties(name, type, required, unique);
        fieldList.add(field);
    }

    public boolean insertJson(String input, String objName) {
        if (!input.trim().startsWith("{") || !input.trim().endsWith("}")) {
            printError("JSON must start with { and end with }");
            return false;
        }
        HashMap<String, String> tempUniqueValues = new HashMap<>();

        String content = input.trim().substring(1, input.length() - 1).trim();
        ArrayList<ArrayList<String>> extractedFields = extractFields(content);

        ArrayList<String> currentlyAddedFields = new ArrayList<>();
        HashMap<String, Object> newEntry = new HashMap<>();

        for (ArrayList<String> entry : extractedFields) {
            String fieldName = entry.get(0);
            String fieldValue = entry.get(1);

            FieldProperties field = null;
            for (FieldProperties f : getFields()) {
                if (f.getName().equalsIgnoreCase(fieldName)) {
                    field = f;
                    break;
                }
            }

            if (field == null) {
                printError("Field '" + fieldName + "' does not exist in " + objName + " type.");
                return false;
            }

            String fieldNameLower = fieldName.toLowerCase();
            if (currentlyAddedFields.contains(fieldNameLower)) {
                printError("The field '" + fieldName + "' has duplicate entries in this JSON type.");
                return false;
            }

            currentlyAddedFields.add(fieldNameLower);

            String type = field.getType();
            boolean isFieldUnique = field.isUnique();
    

            // Check type:
            if (type.equalsIgnoreCase("string")) {
                if (!(fieldValue.startsWith("\"") || fieldValue.endsWith("\""))) {
                    printError("The value for field '" + fieldName + "' must be of type '" + type + "'");
                    return false;
                }
            } else if (type.equalsIgnoreCase("int")) {
                if (!fieldValue.matches("\\d+")) {
                    printError("The value for field '" + fieldName + "' must be of type '" + type + "'");
                    return false;
                }
            } else if (type.equalsIgnoreCase("bool")) {
                if (!fieldValue.matches("true|false")) {
                    printError("The value for field '" + fieldName + "' must be of type '" + type + "'");
                    return false;
                }
            } else if (type.equalsIgnoreCase("dbl")) {
                if (!fieldValue.matches("\\d*.\\d+")) {
                    printError("The value for field '" + fieldName + "' must be of type '" + type + "'");
                    return false;
                }
            }

            Object parsedValue = parseValue(type, fieldValue);

            // Unique field check
            if (isFieldUnique) {
                if (uniqueFieldData.containsKey(fieldNameLower) &&
                        uniqueFieldData.get(fieldNameLower).contains(fieldValue.toLowerCase())) {
                    printError("Duplicate value found for unique field '" + fieldName + "': " + fieldValue);
                    return false;
                }

                tempUniqueValues.put(fieldNameLower, fieldValue.toLowerCase());
            }

            newEntry.put(field.getName(), parsedValue);
        }

        for (FieldProperties field : getFields()) {
            if (field.isRequired() && !currentlyAddedFields.contains(field.getName().toLowerCase())) {
                printError("The required field '" + field.getName() + "' was not added.");
                return false;
            }
        }

        for (FieldProperties field : fieldList) {
            if (!currentlyAddedFields.contains(field.getName().toLowerCase())) {
                newEntry.put(field.getName(), setDefaultValue(field.getName()));
            }
        }

        for (Map.Entry<String, String> entry : tempUniqueValues.entrySet()) {
            uniqueFieldData.putIfAbsent(entry.getKey(), new ArrayList<>());
            uniqueFieldData.get(entry.getKey()).add(entry.getValue());
        }
        jsonData.add(newEntry);
        insertedJsons++;
        ArrayList<Integer> indexeOfEntry = new ArrayList<>();
        indexeOfEntry.add(insertedJsons);
        printINf("Inserted data for type "+ objName +" :");
        printFancyTable(indexeOfEntry);
        printSuccess("Data successfully inserted into the '" + objName + "' JSON type.");
        return true;
    }

    private Object setDefaultValue(String fieldName) {

        String type = "";

        for (FieldProperties f : fieldList) {
            if (f.getName().equals(fieldName)) {
                type = f.getType();
            }
        }
        switch (type) {
            case "int" -> {
                return 0;
            }
            case "dbl" -> {
                return 0.0;
            }
            case "bool" -> {
                return false;
            }
            case "string" -> {
                return "";
            }
            default -> {
                return fieldName;
            }
        }
    }

    public ArrayList<Integer> parseFilter(String filter, String objName) {

        Pattern filterPattern = Pattern.compile("(?:\\s*(\\d*\\.\\d+|\"?\\w+\"?)\\s*(>|<|=)\\s*(\"?\\w+\\s*\\w*\"?)\\s*)",
                Pattern.CASE_INSENSITIVE);
        Matcher filterMatcher = filterPattern.matcher(filter);

        ArrayList<HashMap<String, Object>> fieldsToBeChanged = new ArrayList<>();
        for (HashMap<String, Object> row : jsonData) {
            fieldsToBeChanged.add(row);
        }
        ArrayList<Integer> a = new ArrayList<>();
        while (filterMatcher.find()) {
            String condition1 = filterMatcher.group(1).trim();
            String operator = filterMatcher.group(2).trim();
            String condition2 = filterMatcher.group(3).trim();

            if (condition1.matches("\\d*.?\\d+") && condition2.matches("\\w+")) {// check for when two nums are added
                a = numVarCase(Double.parseDouble(condition1), condition2, operator,
                        fieldsToBeChanged);
                return a;
            } else if (condition2.matches("\\d*.?\\d+") && condition1.matches("\\w+")) {
                a = varNumCase(Double.parseDouble(condition2), condition1, operator,
                        fieldsToBeChanged);
                return a;
            } else if (condition1.startsWith("\"") && !condition2.startsWith("\"")) {// incase we have one string and
                // one field
                a = varStrCase(condition1, operator, condition2, fieldsToBeChanged);
                return a;
            } else if (condition2.startsWith("\"") && !condition1.startsWith("\"")) {
                a = varStrCase(condition2, operator, condition1, fieldsToBeChanged);
                return a;
            } else if (condition1.matches("true|false") && condition2.matches("\\w+")) {
                a = varBoolCase(condition1, operator, condition2, fieldsToBeChanged);
                return a;
            } else if (condition2.matches("true|false") && condition1.matches("\\w+")) {
                a = varBoolCase(condition1, operator, condition2, fieldsToBeChanged);
                return a;
            }
        }
        return a;
    }

    private ArrayList<Integer> varStrCase(String str, String operator, String var,
            ArrayList<HashMap<String, Object>> fieldsToBeChanged) {
        ArrayList<HashMap<String, Object>> filteredFields = new ArrayList<>();
        for (HashMap<String, Object> map : jsonData) {
            filteredFields.add(new HashMap<>(map)); // shallow copy of each map
        }

        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < filteredFields.size(); i++) {
            indexes.add(i);
        }
        if (filteredFields.isEmpty()) {
            printError("No fields found that match the specified filter : ' " + str + ">" + var + " ' ");
            return null;
        }

        str = str.replaceAll("\"", "");

        switch (operator) {
            case "=" -> {
                Iterator<HashMap<String, Object>> iterator = filteredFields.iterator();
                int indexCount = 0;

                while (iterator.hasNext()) {
                    HashMap<String, Object> set = iterator.next();
                    Object value = set.get(var.trim());

                    if (value instanceof String) {
                        String fieldValue = value.toString().replaceAll("\"", "");
                        if (!(fieldValue.equalsIgnoreCase(str))) {
                            iterator.remove();
                            indexes.remove(Integer.valueOf(indexCount));
                        }

                    } else if (value == null) {
                        printError("the field mentioned in filter : " + var + " does not exist");
                        return null;
                    } else {
                        printError("Invalid comparison for: " + str + " and " + var);
                        return null;
                    }
                    indexCount++;
                }
            }
            default -> {
                printError("Invalid operator format");
                return null;
            }
        }

        return indexes;
    }

    private ArrayList<Integer> varBoolCase(String var, String operator, String bool,
            ArrayList<HashMap<String, Object>> fieldsToBeChanged) {
        ArrayList<HashMap<String, Object>> filteredFields = new ArrayList<>();
        for (HashMap<String, Object> map : jsonData) {
            filteredFields.add(new HashMap<>(map)); // shallow copy of each map
        }
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < filteredFields.size(); i++) {
            indexes.add(i);
        }

        if (filteredFields.isEmpty()) {
            printError("No fields found that match the specified filter : ' " + bool + ">" + var + " ' ");
            return null;
        }

        switch (operator) {
            case "=": {
                Iterator<HashMap<String, Object>> iterator = filteredFields.iterator();
                boolean strBool = Boolean.parseBoolean(bool.toLowerCase().trim());
                int indexCount = 0;

                while (iterator.hasNext()) {
                    HashMap<String, Object> set = iterator.next();
                    Object value = null;
                    for (String key : set.keySet()) {
                        if (key.equalsIgnoreCase(var.trim())) {
                            value = set.get(key);
                            break;
                        }
                    }

                    if (value instanceof Boolean) {
                        boolean fieldValue = (Boolean) value;
                        if (!(fieldValue == strBool)) {
                            iterator.remove();
                            indexes.remove(Integer.valueOf(indexCount));
                        }
                    } else if (value == null) {
                        printError("the field mentioned in filter : " + var + " does not exist");
                        return null;
                    } else {
                        printError("Invalid comparison for: " + bool + " and " + var);
                        return null;
                    }
                    indexCount++;
                }
                break;
            }
            default: {
                printError("Invalid operator format");
                break;
            }
        }
        return indexes;
    }

    private ArrayList<Integer> varNumCase(double num, String var, String operator,
            ArrayList<HashMap<String, Object>> fieldsToBeChanged) {
        ArrayList<HashMap<String, Object>> filteredFields = new ArrayList<>();
        for (HashMap<String, Object> map : jsonData) {
            filteredFields.add(new HashMap<>(map)); // shallow copy of each map
        }
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < filteredFields.size(); i++) {
            indexes.add(i);
        }

        if (filteredFields.isEmpty()) {
            printError("No fields found that match the specified filter : ' " + num + ">" + var + " ' ");
            return null;
        }

        switch (operator) {
            case "<" -> {
                Iterator<HashMap<String, Object>> iterator = filteredFields.iterator();
                int indexCount = 0;

                while (iterator.hasNext()) {
                    HashMap<String, Object> set = iterator.next();
                    Object value = set.get(var.trim());

                    if (value instanceof Integer || value instanceof Double) {
                        double fieldValue = value instanceof Integer
                                ? ((Integer) value).doubleValue()
                                : (Double) value;

                        if (!(fieldValue < num)) {
                            iterator.remove();
                            indexes.remove(Integer.valueOf(indexCount));
                        }
                    } else if (value == null) {
                        printError("the field mentioned in filter : " + var + " does not exist");
                        return null;
                    } else {
                        printError("Invalid comparison for: " + num + " and " + var);
                        return null;
                    }
                    indexCount++;
                }
            }
            case ">" -> {
                Iterator<HashMap<String, Object>> iterator = filteredFields.iterator();
                int indexCount = 0;

                while (iterator.hasNext()) {
                    HashMap<String, Object> set = iterator.next();
                    Object value = set.get(var.trim());

                    if (value instanceof Integer || value instanceof Double) {
                        double fieldValue = value instanceof Integer
                                ? ((Integer) value).doubleValue()
                                : (Double) value;

                        if (!(fieldValue > num)) {
                            iterator.remove();
                            indexes.remove(Integer.valueOf(indexCount));
                        }
                    } else if (value == null) {
                        printError("the field mentioned in filter : " + var + " does not exist");
                        return null;
                    } else {
                        printError("Invalid comparison for: " + num + " and " + var);
                        return null;
                    }
                    indexCount++;
                }
            }
            case "=" -> {
                Iterator<HashMap<String, Object>> iterator = filteredFields.iterator();
                int indexCount = 0;

                while (iterator.hasNext()) {
                    HashMap<String, Object> set = iterator.next();
                    Object value = set.get(var.trim());

                    if (value instanceof Integer || value instanceof Double) {
                        double fieldValue = value instanceof Integer
                                ? ((Integer) value).doubleValue()
                                : (Double) value;

                        if (!(fieldValue == num)) {
                            iterator.remove();
                            indexes.remove(Integer.valueOf(indexCount));
                        }
                    } else if (value == null) {
                        printError("the field mentioned in filter : " + var + " does not exist");
                        return null;
                    } else {
                        printError("Invalid comparison for: " + num + " and " + var);
                        return null;
                    }
                    indexCount++;
                }
            }
            default -> {
                printError("Invalid operator format");
                return null;
            }
        }
        return indexes;
    }

    private ArrayList<Integer> numVarCase(double num, String var, String operator,
            ArrayList<HashMap<String, Object>> fieldsToBeChanged) {
        ArrayList<HashMap<String, Object>> filteredFields = new ArrayList<>();
        for (HashMap<String, Object> map : jsonData) {
            filteredFields.add(new HashMap<>(map)); // shallow copy of each map
        }
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < filteredFields.size(); i++) {
            indexes.add(i);
        }

        if (filteredFields.isEmpty()) {
            printError("No fields found that match the specified filter : ' " + num + ">" + var + " ' ");
            return null;
        }

        switch (operator) {
            case ">" -> {
                Iterator<HashMap<String, Object>> iterator = filteredFields.iterator();
                int indexCount = 0;
                while (iterator.hasNext()) {
                    HashMap<String, Object> set = iterator.next();
                    Object value = set.get(var.trim());

                    if (value instanceof Integer || value instanceof Double) {
                        double fieldValue = value instanceof Integer
                                ? ((Integer) value).doubleValue()
                                : (Double) value;

                        if (!(fieldValue < num)) {
                            iterator.remove();
                            indexes.remove(Integer.valueOf(indexCount));
                        }
                    } else if (value == null) {
                        printError("the field mentioned in filter : " + var + " does not exist");
                        return null;
                    } else {
                        printError("Invalid comparison for: " + num + " and " + var);
                        return null;
                    }
                    indexCount++;
                }

            }
            case "<" -> {
                Iterator<HashMap<String, Object>> iterator = filteredFields.iterator();
                int indexCount = 0;
                while (iterator.hasNext()) {
                    HashMap<String, Object> set = iterator.next();
                    Object value = set.get(var.trim());

                    if (value instanceof Integer || value instanceof Double) {
                        double fieldValue = value instanceof Integer
                                ? ((Integer) value).doubleValue()
                                : (Double) value;
                                

                        if (!(fieldValue < num)) {
                            iterator.remove();
                            indexes.remove(Integer.valueOf(indexCount));
                        }
                    } else if (value == null) {
                        printError("the field mentioned in filter : " + var + " does not exist");
                        return null;
                    } else {
                        printError("Invalid comparison for: " + num + " and " + var);
                        return null;
                    }
                    indexCount++;
                }

            }
            case "=" -> {
                Iterator<HashMap<String, Object>> iterator = filteredFields.iterator();
                int indexCount = 0;
                while (iterator.hasNext()) {
                    HashMap<String, Object> set = iterator.next();
                    Object value = set.get(var.trim());

                    if (value instanceof Integer || value instanceof Double) {
                        double fieldValue = value instanceof Integer
                                ? ((Integer) value).doubleValue()
                                : (Double) value;

                        if (!(fieldValue == num)) {
                            iterator.remove();
                            indexes.remove(Integer.valueOf(indexCount));
                        }
                    } else if (value == null) {
                        printError("the field mentioned in filter : " + var + " does not exist");
                        return null;
                    } else {
                        printError("Invalid comparison for: " + num + " and " + var);
                        return null;
                    }
                    indexCount++;
                }

            }
            default -> {
                printError("Invalid operator format");
                return null;
            }
        }
        return indexes;
    }

    // private ArrayList<HashMap<String, Object>>
    // filterFields(ArrayList<HashMap<String, Object>> fields, String var) {
    // for (HashMap<String, Object> set : fields) {
    // if (!set.containsKey(var)) {
    // fields.remove(set);
    // }
    // }
    // return fields;
    // }

    public boolean updateJson(String input, String objName) {
        Pattern pattern = Pattern.compile("(?:\\(\\s*([^\\)]+)\\s*\\))?\\s*\\{\\s*([^\\}]+)\\s*\\}",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        if (!matcher.find()) {
            printError("Invalid UPDATE command format");
            return false;
        }

        ArrayList<ArrayList<String>> extractedFields = new ArrayList<>();
        ArrayList<Integer> filteredIndexes = new ArrayList<>();
        String conditionField = matcher.group(2).trim();
        extractedFields = extractFields(conditionField);

        String group1 = matcher.group(1);
        if (group1 != null && !group1.trim().isEmpty()) {
            String filter = group1.trim();

            Pattern filterPattern = Pattern.compile(
                    "(?:\\s*(\\w+)\\s*(>|<|=)\\s*\"?(\\w+)\"?\\s*)", // fixed the regex
                    Pattern.CASE_INSENSITIVE);
            Matcher filterMatcher = filterPattern.matcher(filter);

            if (filterMatcher.find()) {
                filteredIndexes = parseFilter(filter, objName);
                if (filteredIndexes==null ||  filteredIndexes.isEmpty()) {
                    printError("NO field was found matching this filter");
                    return true;
                }
            } else {
                printError("Invalid filter input");
                return false;
            }
        } else {
            for (int i = 0; i < jsonData.size(); i++) {
                filteredIndexes.add(i);
            }
        }

        for (ArrayList<String> fieldToUpdate : extractedFields) {
            String fieldName = fieldToUpdate.get(0);
            String fieldValue = fieldToUpdate.get(1);
            String fieldType = "";

            boolean found = false;
            for (FieldProperties f : fieldList) {
                if (f.getName().equals(fieldName)) {
                    found = true;
                }
            }
            if (!found) {
                printError("The mentioned field : " + fieldName + " does not exist in Json type named : " + objName);
                return false;
            }

            String matchedUniqueKey = getMatchingKey(uniqueFieldData.keySet(), fieldName);


            for (Integer index : filteredIndexes) {
                HashMap<String, Object> entry = jsonData.get(index);
                String actualKey = getMatchingKey(entry.keySet(), fieldName);
                if (actualKey != null) { // added a parsevalue thingy thing here
                    for (FieldProperties field : fieldList) {
                        if (field.getName().equals(fieldName)) {
                            fieldType = field.getType();
                            break;
                        }
                    }

                    if (fieldType.equalsIgnoreCase("string")) {
                        if (!(fieldValue.startsWith("\"") && fieldValue.endsWith("\""))) {
                            printError("The value for field '" + fieldName + "' must be of type '" + fieldType + "'");
                            return false;
                        }
                    } else if (fieldType.equalsIgnoreCase("int")) {
                        if (!fieldValue.matches("\\d+")) {
                            printError("The value for field '" + fieldName + "' must be of type '" + fieldType + "'");
                            return false;
                        }
                    } else if (fieldType.equalsIgnoreCase("bool")) {
                        if (!fieldValue.matches("true|false")) {
                            printError("The value for field '" + fieldName + "' must be of type '" + fieldType + "'");
                            return false;
                        }
                    } else if (fieldType.equalsIgnoreCase("dbl")) {
                        if (!fieldValue.matches("\\d*.\\d+")) {
                            printError("The value for field '" + fieldName + "' must be of type '" + fieldType + "'");
                            return false;
                        }
                    }

                    Object parsedFieldValue = parseValue(fieldType, fieldValue);
                    if (matchedUniqueKey != null) {
                        int valuesCount = countKeyValueMatches(matchedUniqueKey, fieldValue);
                        if (filteredIndexes.size() > 1 || (filteredIndexes.size() == 1 && valuesCount > 0)) {
                            printError("can't update 2 or more unique fields containing same value");
                            return false;
                        }
                    }
                    entry.replace(actualKey, parsedFieldValue);
                } else {
                    printError("The field : '" + fieldName + "' was not found in object.");
                    return false;
                }
            }
        }

        return true;
    }

    private int countKeyValueMatches(String key, Object value) {
        int count = 0;
        String valueAsString = String.valueOf(value).toLowerCase();

        for (HashMap<String, Object> row : jsonData) {
            String actualKey = getMatchingKey(row.keySet(), key);
            if (actualKey != null) {
                Object currentValue = row.get(actualKey);
                if (currentValue != null) {
                    String currentValueAsString = String.valueOf(currentValue).toLowerCase();
                    if (currentValueAsString.equals(valueAsString)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    private String getMatchingKey(Set<String> keys, String targetKey) {
        for (String key : keys) {
            if (key.equalsIgnoreCase(targetKey)) {
                return key;
            }
        }
        return null;
    }

    public void printFancyTable(ArrayList<Integer> indexes) {

        // incase indexes list is null or empty print all rows
        if (indexes == null || indexes.isEmpty()) {
            indexes = new ArrayList<>();
            for (int i = 0; i < jsonData.size(); i++) {
                indexes.add(i);
            }
        }

        Set<String> keySet = jsonData.get(0).keySet();// just get the first row to get our fields
        List<String> headers = new ArrayList<>(keySet);// for fieldnames at the top

        // measure column max-widths to have same lenght for each column
        Map<String, Integer> colWidths = new HashMap<>();
        for (String key : headers) {
            int maxWidth = key.length();
            for (int i = 0; i <= insertedJsons; i++) {
                Object value = jsonData.get(i).get(key);
                maxWidth = Math.max(maxWidth, String.valueOf(value).length());

            }
            colWidths.put(key, maxWidth + 2); // adding 2: space on both sides
        }

        // fancy borders
        String top = "╔";
        String mid = "╠";
        String bottom = "╚";
        for (int i = 0; i < headers.size(); i++) {
            String key = headers.get(i);
            int width = colWidths.get(key);
            top += "═".repeat(width) + (i == headers.size() - 1 ? "╗" : "╦");
            mid += "═".repeat(width) + (i == headers.size() - 1 ? "╣" : "╬");
            bottom += "═".repeat(width) + (i == headers.size() - 1 ? "╝" : "╩");
        }

        // first row -> header
        System.out.println(top);
        StringBuilder headerRow = new StringBuilder("║");
        for (String key : headers) {
            int width = colWidths.get(key);
            String cell = centerText(key, width);
            String colored = consoleColors.BRIGHT_BLUE + cell + consoleColors.RESET;
            headerRow.append(colored).append("║");
        }

        System.out.println(headerRow);
        System.out.println(mid);

        for (int i = 0; i < jsonData.size(); i++) {
            if (indexes.contains(i)) {
                Map<String, Object> row = jsonData.get(i);
                boolean isEvenRow = i % 2 == 0;

                // zebra background
                String bgColor = isEvenRow ? consoleColors.BG_DARK_GRAY
                        : consoleColors.DIM + consoleColors.BG_LIGHT_GRAY;
                String textColor = consoleColors.BLACK;

                // the last border of the row
                System.out.print(consoleColors.RESET + "║");

                for (String key : headers) {
                    String value = String.valueOf(row.getOrDefault(key, ""));

                    String padded = centerText(value, colWidths.get(key));
                    System.out.print(bgColor + textColor + padded + consoleColors.RESET + "║");
                }

                System.out.println();
            }
        }
        System.out.println(bottom);
    }

    // helps to centerize?! (T_T) text in column--? doesnt work
    private String centerText(String text, int width) {
        int padding = width - text.length();
        int padStart = padding / 2;
        int padEnd = padding - padStart;
        return " ".repeat(padStart) + text + " ".repeat(padEnd);
    }

    public boolean searchJson(String filter, String objName) {

        if (jsonData.isEmpty()) {
            printError("No data found for json-type named : " + objName);
            return true;
        }

        // made a copy of our json's indexes
        ArrayList<Integer> searchIndexes = new ArrayList<>();

        if (!(filter == null)) {
            Pattern filterPattern = Pattern.compile(
                    "(?:\\(\\s*([^\\)]+)\\s*\\))?\\s*$",
                    Pattern.CASE_INSENSITIVE);
            Matcher filterMatcher = filterPattern.matcher(filter);

            if (filterMatcher.find() && !filterMatcher.group(0).trim().isEmpty()) {
                searchIndexes = parseFilter(filter, objName);
                if (searchIndexes== null || searchIndexes.isEmpty()) {
                    printError("NO field was found matching this filter");
                    return true;
                }
            } else if (filterMatcher.group(0).trim().isEmpty()) {
                printError("Invalid search command");
                return false;
            } else {
                printError("Invalid filter command");
                return false;
            }
        }
        // print json(filtered)
        printFancyTable(searchIndexes);
        return true;
    }

    public boolean deleteJson(String filter, String objName) {

        // made a copy of our json's indexes
        ArrayList<Integer> deleteIndexes = new ArrayList<>();

        if (!(filter == null)) {
            Pattern filterPattern = Pattern.compile(
                    "(?:\\(\\s*([^\\)]+)\\s*\\))?\\s*$",
                    Pattern.CASE_INSENSITIVE);
            Matcher filterMatcher = filterPattern.matcher(filter);

            if (filterMatcher.find() && !filterMatcher.group(0).trim().isEmpty()) {
                // found search indexes
                deleteIndexes = parseFilter(filter, objName);
                if (deleteIndexes==null || deleteIndexes.isEmpty()) {
                    printError("NO field was found matching this filter");
                    return true;
                }
            } else if (filterMatcher.group(0).trim().isEmpty()) {
                printError("Invalid delete command");
                return false;
            } else {
                printError("Invalid filter command");
                return false;
            }
        } else {
            for (int i = 0; i < jsonData.size(); i++) {
                deleteIndexes.add(i);
            }
        }

        // Remove the items at the specified indexes
        for (int i = deleteIndexes.size() - 1; i >= 0; i--) {
            Integer index = deleteIndexes.get(i);
            if (index >= 0 && index < jsonData.size()) {
                // now we gotta check unique fields:
                for (Map.Entry<String, ArrayList<Object>> entry : uniqueFieldData.entrySet()) {
                    String uniqueFieldName = entry.getKey();
                    ArrayList<Object> fieldData = entry.getValue();
                    for (Map.Entry<String, Object> entry2 : jsonData.get((int) index).entrySet()) {
                        Object object = entry2.getValue();
                        if (entry2.getKey().equalsIgnoreCase(uniqueFieldName)) {
                            if (fieldData.contains(object.toString())) {
                                fieldData.remove(object.toString());// does it remove only one of fields?
                                uniqueFieldData.replace(uniqueFieldName, fieldData);
                            }

                        }
                    }
                }
                jsonData.remove((int) index); // safely removed

            }
        }
        insertedJsons = insertedJsons - deleteIndexes.size();
        return true;

    }
}

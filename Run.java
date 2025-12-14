import java.util.*;


public class Run {
    private final ArrayList<Json> jsonNames = new ArrayList<>();

    public static void main(final String[] args) {
        final Proj1 p = new Proj1();
        final Scanner scanner = new Scanner(System.in);
        Json.printINf("JSON Data Management System - Ready for commands");

        while (true) {
            System.out.print("> ");
            final String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                p.handleCommands(input);
            } catch (final Exception e) {
                System.out.println("Error processing command: " + e.getMessage());
            }
        }

        scanner.close();
    }

    public void handleCommands(final String input) {
        final String[] parsedCommand = input.trim().split("\\s+", 2);


        if (parsedCommand.length < 2) {
            Json.printError("Invalid command format");
            return;
        }

        boolean success = false;

        switch (parsedCommand[0].toLowerCase()) {
            case "create":
                final String[] jsonInf = parsedCommand[1].split("\\{", 2);
                if (jsonInf.length < 2) {
                    Json.printError("Invalid JSON body format. Ensure it starts with '{' and ends with '}'");
                    return;
                }

                final String jsonName = jsonInf[0].trim();
                final String jsonBody = "{" + jsonInf[1].trim();
                for (final Json json2 : jsonNames) {
                    if (json2.getName().equalsIgnoreCase(jsonName)) {
                        Json.printError("This Json already exists");
                        Json.printError("Failed to create JSON type '" + jsonName + "'");
                        return;
                    }
                }

                final Json json = new Json(jsonName);
                success = json.createJson(jsonBody);

                if (success) {
                    jsonNames.add(json);
                    Json.printSuccess("JSON type '" + jsonName + "' created successfully");
                } else {
                    Json.printError("Failed to create JSON type '" + jsonName + "'");
                }
                break;

            case "insert":
                final String[] jsonInfo = parsedCommand[1].split("\\{", 2);
                if (jsonInfo.length < 2) {
                    Json.printError("Invalid JSON body format. Ensure it starts with '{' and ends with '}'");
                    return;
                }


                final String jsonsName = jsonInfo[0].trim();
                final String jsonsBody = "{" + jsonInfo[1].trim();
                Json jsonInstance = new Json(" ");
                for (final Json json2 : jsonNames) {
                    if (json2.getName().equalsIgnoreCase(jsonsName)) {
                        jsonInstance = json2;
                    }
                }
                if (jsonInstance.getName().equals(" ")) {
                    Json.printError("This JSON type : " + jsonsName + " does not exist. ");
                    break;
                }
                success = jsonInstance.insertJson(jsonsBody, jsonsName);
                if (success) {
                    Json.printSuccess("Data inserted into JSON type '" + jsonsName + "' successfully.");
                } else {
                    Json.printError("Failed to insert data into JSON type '" + jsonsName + "'");
                }
                break;
            case "update":
                final String[] jsonInfor = parsedCommand[1].split("\\s+", 2);
                if (jsonInfor.length < 2) {
                    Json.printError("Invalid JSON body format. Ensure it starts with '{' and ends with '}'");
                    return;
                }

                final String jsonToUpName = jsonInfor[0].trim();
                final String jsonToUpBody = jsonInfor[1].trim();
                Json jsonIns = new Json(" ");
                for (final Json json2 : jsonNames) {
                    if (json2.getName().equalsIgnoreCase(jsonToUpName)) {
                        jsonIns = json2;
                    }
                }
                if (jsonIns.getName().equals(" ")) {
                    Json.printError("This JSON type : " + jsonToUpName + " does not exist. ");
                    break;
                }
                success = jsonIns.updateJson(jsonToUpBody, jsonToUpName);
                if (success) {
                    Json.printSuccess("Data updated successfuly");
                } else {
                    Json.printError("Data did not update successfuly");
                }
                break;

            case "search":
            //initialize
            String Name="";
            String filter=null;
                if(parsedCommand[1].trim().matches("\\w+")){
                    //no filters available.only the name
                    Name=parsedCommand[1].trim();
                }else{
                    final String[] jsonIn = parsedCommand[1].split("\\(", 2);
                    Name=jsonIn[0].trim();
                    filter="("+jsonIn[1].trim();
                }
                //detect json
                Json jsontemp = new Json(" ");
                for (final Json json2 : jsonNames) {
                    if (json2.getName().equalsIgnoreCase(Name)) {
                        jsontemp = json2;
                    }
                }
                if (jsontemp.getName().equals(" ")) {
                    Json.printError("This JSON type : " + Name + " does not exist. ");
                    break;
                }
                //search process type -> bool
                success = jsontemp.searchJson(filter, Name);//if filter=null, update all indexes
                if (success) {
                    Json.printSuccess("searched successfuly");
                } else {
                    Json.printError("Failed to search");
                }
                break;
            
            case "delete":
            String NameD="";
            String filterD=null;
                if(parsedCommand[1].trim().matches("\\w+")){
                    //no filters available.only the name
                    NameD=parsedCommand[1].trim();
                }else{
                    final String[] jsonInstt = parsedCommand[1].split("\\(", 2);
                    NameD=jsonInstt[0].trim();
                    filterD="("+jsonInstt[1].trim();
                }
                //detect json
                Json jsontempp = new Json(" ");
                for (final Json json2 : jsonNames) {
                    if (json2.getName().equalsIgnoreCase(NameD)) {
                        jsontempp = json2;
                    }
                }
                if (jsontempp.getName().equals(" ")) {
                    Json.printError("This JSON type : " + NameD + " does not exist. ");
                    break;
                }
                //search process type -> bool
                success = jsontempp.deleteJson(filterD, NameD);//if filter=null, update all indexes
                if (success) {
                    Json.printSuccess("deleted successfuly");
                } else {
                    Json.printError("Failed to delete");
                }
                break;
            default:
                Json.printError("Unhandled command: " + parsedCommand[0]);
        }
    }

}

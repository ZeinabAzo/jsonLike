
public class FieldProperties {
    private String name;
    private String type;
    private boolean required;
    private boolean unique;
    
    //constructor
    public FieldProperties(String name,String type, Boolean required, Boolean unique) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.unique = unique;
    }
    
    //getter
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isRequired() { return required; }
    public boolean isUnique() { return unique; }
    @Override
    public String toString() {
        return name + " " + type + " " + required + " " + unique;
    }

}

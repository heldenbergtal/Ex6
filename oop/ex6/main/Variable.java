package oop.ex6.main;

public class Variable {
    private final String type;
    private Boolean isAssigned;
    private final Boolean isFinal;

    public Variable(String type, Boolean isAssigned, Boolean isFinal) {
        this.type = type;
        this.isAssigned = isAssigned;
        this.isFinal = isFinal;
    }

    public String getType() {return type;}
    public Boolean getIsAssigned() {return isAssigned;}
    public Boolean getIsFinal() {return isFinal;}
    public void setIsAssigned(Boolean isAssigned) {
        this.isAssigned = isAssigned;
    }
}

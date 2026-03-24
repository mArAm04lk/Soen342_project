package model;

public class Collaborator {
    private String name;
    private String category;

    public Collaborator(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
package fr.ladybug.team.mapping;

import org.bson.types.ObjectId;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Id;

/**
 * Mapping class Name for mongodb
 * Represents name of the person which is stored in the phonebook
 */
@Entity("names")
public class Name {
    @Id
    private ObjectId id;
    private String name;

    /**
     * Empty constructor for mapping purpose
     */
    public Name() {

    }

    /**
     * Simple field-wise constructor
     * @param name the name which is to be stored
     */
    public Name(String name) {
        this.name = name;
    }

    /**
     * Field-wise constructor with manual id assignment
     * @param name the name which is to be stored
     * @param id the id which is to be assigned to this record
     */
    public Name(String name, ObjectId id) {
        this.name = name;
        this.id = id;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

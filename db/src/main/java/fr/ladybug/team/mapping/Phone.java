package fr.ladybug.team.mapping;

import org.bson.types.ObjectId;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Id;

/**
 * Mapping class Phone for mongodb
 * Represent phone of the person which is stored in the phonebook
 */
@Entity("phones")
public class Phone {
    @Id
    private ObjectId id;
    private String phone;

    /**
     * Empty constuctor for mapping purpose
     */
    public Phone() {

    }

    /**
     * Simple field-wise constructor
     * @param phone the phone which is to be stored
     */
    public Phone(String phone) {
        this.phone = phone;
    }

    /**
     * Field-wise constructor with manual id assignment
     * @param phone the phone which is to be stored
     * @param id the is which is to be assignment to this record
     */
    public Phone(String phone, ObjectId id) {
        this.phone = phone;
        this.id = id;
    }

    public ObjectId getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }
}

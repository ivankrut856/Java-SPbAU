package fr.ladybug.team.mapping;

import org.bson.types.ObjectId;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Id;

/**
 * Mapping class NameToPhone for mongodb
 * Represent many-to-many relation between name and phone in phonebook
 */
@Entity("name_to_phone")
public class NameToPhone {
    @Id
    private ObjectId id;

    private ObjectId nameId;
    private ObjectId phoneId;

    /**
     * Empty constructor for mapping purpose
     */
    public NameToPhone() {

    }

    /**
     * Simple field-wise constructor
     * @param nameId the id of the Name which is linked by the relation
     * @param phoneId the id of the Phone which is linked by the relation
     */
    public NameToPhone(ObjectId nameId, ObjectId phoneId) {
        this.nameId = nameId;
        this.phoneId = phoneId;
    }
    /**
     * Field-wise constructor with manual id assignment
     * @param nameId the id of the Name which is linked by the relation
     * @param phoneId the id of the Phone which is linked by the relation
     * @param id the id to be assigned to this record
     */
    public NameToPhone(ObjectId nameId, ObjectId phoneId, ObjectId id) {
        this.nameId = nameId;
        this.phoneId = phoneId;
        this.id = id;
    }


    public ObjectId getNameId() {
        return nameId;
    }

    public ObjectId getPhoneId() {
        return phoneId;
    }

    public ObjectId getId() {
        return id;
    }
}

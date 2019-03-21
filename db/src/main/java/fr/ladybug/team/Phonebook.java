package fr.ladybug.team;

import com.mongodb.MongoClient;
import fr.ladybug.team.mapping.Name;
import fr.ladybug.team.mapping.NameToPhone;
import fr.ladybug.team.mapping.Phone;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import xyz.morphia.Datastore;
import xyz.morphia.Morphia;
import xyz.morphia.aggregation.Projection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Phonebook stores names and phones in database.
 * Every name can be associated with multiple phones
 * Every phone can be associated with multiple names
 * Each name-phone pair cannot be store in more than one instance
 * Class hides all DB queries
 */
public class Phonebook {
    private Morphia morphia;
    private Datastore datastore;

    /**
     * Creates instance associated to database with specified name
     * @param dbName the name to which phonebook is to be associated
     */
    public Phonebook(@NotNull String dbName) {
        Logger mongoLogger = Logger.getLogger("xyz.morphia.logging");
        mongoLogger.setLevel(Level.OFF);
        Logger morphiaLogger = Logger.getLogger("org.mongodb.driver");
        morphiaLogger.setLevel(Level.OFF);

        morphia = new Morphia();
        morphia.mapPackage("fr.ladybug.team.mapping");
        datastore = morphia.createDatastore(new MongoClient("localhost", 27017), dbName);
        datastore.ensureIndexes();
    }

    /**
     * Gets internal database representation which can be used for manual queries
     * @return datastore object associated with phonebook
     */
    public @NotNull Datastore getDatastore() {
        return datastore;
    }

    /**
     * Drops all related collection from the database
     */
    public void clearDB() {
        datastore.getCollection(Name.class).drop();
        datastore.getCollection(Phone.class).drop();
        datastore.getCollection(NameToPhone.class).drop();
    }

    /**
     * Gets an iterator on all phones associated in database with specified name
     * @param name the name which is to be associated with the phones
     * @return an iterator on all phones associated in database with specified name
     */
    public @NotNull Iterator<Phone> getPhonesByName(@NotNull String name) {
        return datastore.createAggregation(NameToPhone.class)
                .lookup("names", "nameId", "_id", "mansName")
                .unwind("mansName")
                .match(datastore.createQuery(Object.class).disableValidation().field("mansName.name").equal(name))
                .lookup("phones", "phoneId", "_id", "mansPhone")
                .unwind("mansPhone")
                .project(Projection.projection("_id").suppress(), Projection.projection("phone", "mansPhone.phone"))
                .aggregate(Phone.class);
    }

    /**
     * Gets an iterator on all names associated in database with specified phone
     * @param phone the phone which is to be associated with the names
     * @return an iterator on all names associated in database with specified phone
     */
    public @NotNull Iterator<Name> getNamesByPhone(@NotNull String phone) {
        return datastore.createAggregation(NameToPhone.class)
                .lookup("phones", "phoneId", "_id", "mansPhone")
                .unwind("mansPhone")
                .match(datastore.createQuery(Object.class).disableValidation().field("mansPhone.phone").equal(phone))
                .lookup("names", "nameId", "_id", "mansName")
                .unwind("mansName")
                .project(Projection.projection("_id").suppress(), Projection.projection("name", "mansName.name"))
                .aggregate(Name.class);
    }

    /**
     * Gets all phonebook records.
     * @return an iterator on all records as name-phone pairs
     */
    public @NotNull Iterator<NamePhonePair> getAllPairs() {
        return datastore.createAggregation(NameToPhone.class)
                .lookup("phones", "phoneId", "_id", "mansPhone")
                .unwind("mansPhone")
                .lookup("names", "nameId", "_id", "mansName")
                .unwind("mansName")
                .project(
                        Projection.projection("_id").suppress(),
                        Projection.projection("name.name", "mansName.name"),
                        Projection.projection("phone.phone", "mansPhone.phone"))
                .aggregate(NamePhonePair.class);
    }

    /**
     * Gets instance of Name from database.
     * If there are no such element, it creates new instance or does nothing (depends on policy)
     * @param name the name by which instance of Name will be found
     * @param policy the policy on which depends whether new instance will be created or not
     * @return an instance of Name with specified name
     * @throws AlreadyExistsException if the name already exists and NO_DUPLICATES policy is set
     */
    public Name getOrCreateName(@NotNull String name, ElementsPolicy policy) throws AlreadyExistsException {
        Name candidateName = datastore.find(Name.class).field("name").equal(name).get();

        if (policy == ElementsPolicy.NO_DUPLICATES && candidateName != null)
            throw new AlreadyExistsException();

        if (candidateName == null) {
            if (policy == ElementsPolicy.NOTHING)
                return null;

            candidateName = new Name(name, new ObjectId());
            datastore.save(candidateName);
        }
        return candidateName;
    }

    /**
     * Gets instance of Phone from database.
     * If there are no such element, it creates new instance or does nothing (depends on policy)
     * @param phone the phone by which instance of Phone will be found
     * @param policy the policy on which depends whether new instance will be created or not
     * @return an instance of Phone with specified name
     * @throws AlreadyExistsException if the phone already exists and NO_DUPLICATES policy is set
     */
    public Phone getOrCreatePhone(@NotNull String phone, ElementsPolicy policy) throws AlreadyExistsException {
        Phone candidatePhone = datastore.find(Phone.class).field("phone").equal(phone).get();

        if (policy == ElementsPolicy.NO_DUPLICATES && candidatePhone != null)
            throw new AlreadyExistsException();

        if (candidatePhone == null) {
            if (policy == ElementsPolicy.NOTHING)
                return null;

            candidatePhone = new Phone(phone, new ObjectId());
            datastore.save(candidatePhone);
        }
        return candidatePhone;
    }

    /**
     * Gets instance of NameToPhone from database.
     * If there are no such element, it creates new instance or does nothing (depends on policy)
     * @param personId the id of name by which instance of NameToPhone will be found
     * @param phoneId the id of phone by which instance of NameToPhone will be found
     * @param policy the policy on which depends whether new instance will be created or not
     * @return an instance of NameToPhone with specified name and phone
     * @throws AlreadyExistsException if the relation already exists and NO_DUPLICATES policy is set
     */
    public NameToPhone getOrCreateRelation(@NotNull ObjectId personId, @NotNull ObjectId phoneId, ElementsPolicy policy)
            throws AlreadyExistsException {
        NameToPhone candidateRelation = datastore.find(NameToPhone.class)
                .field("nameId").equal(personId)
                .field("phoneId").equal(phoneId).get();

        if (policy == ElementsPolicy.NO_DUPLICATES && candidateRelation != null)
            throw new AlreadyExistsException();

        if (candidateRelation == null) {
            if (policy == ElementsPolicy.NOTHING)
                return null;

            candidateRelation = new NameToPhone(personId, phoneId, new ObjectId());
            datastore.save(candidateRelation);
        }
        return candidateRelation;
    }

    /**
     * Adds name-phone pair to the phonebook
     * The phonebook does not allow duplicate name-phone pairs
     * If an attempt of such addition will be made, no addition will be performed
     * @param name the name which is to be added as a part of the pair with phone to the phonebook
     * @param phone the phone which is to be added as a paro of the pair with name to the phonebook
     * @throws AlreadyExistsException if given name-phone pair already exists in the phonebook
     */
    public void addPair(@NotNull String name, @NotNull String phone) throws AlreadyExistsException {
        ObjectId personId = Objects.requireNonNull(getOrCreateName(name, ElementsPolicy.CREATE)).getId();
        ObjectId phoneId = Objects.requireNonNull(getOrCreatePhone(phone, ElementsPolicy.CREATE)).getId();
        getOrCreateRelation(personId, phoneId, ElementsPolicy.NO_DUPLICATES);
    }

    /**
     * Removes name-phone pair from the phonebook
     * If no such pair exists, throws NoSuchElementException
     * @param name the name pair of which and specified phone will be removed from the phonebook
     * @param phone the phone pair of which and specified name will be removed from the phonebook
     * @throws NoSuchElementException if no such pair exists
     */
    public void removePair(@NotNull String name, @NotNull String phone)
            throws NoSuchElementException {
        Name personData = null;
        Phone phoneData = null;
        try {
            personData = getOrCreateName(name, ElementsPolicy.NOTHING);
            phoneData = getOrCreatePhone(phone, ElementsPolicy.NOTHING);
        } catch (AlreadyExistsException ignore) {
            //cannot be thrown
        }
        if (personData == null || phoneData == null) {
            throw new NoSuchElementException();
        }

        NameToPhone relation = null;
        try {
            relation = getOrCreateRelation(personData.getId(), phoneData.getId(), ElementsPolicy.NOTHING);
        } catch (AlreadyExistsException ignore) {
            //cannot be thrown
        }
        if (relation == null) {
            throw new NoSuchElementException();
        }

        datastore.delete(relation);
    }

    /**
     * Changes relation between name and phone
     * Performs by removing old pair and adding new one
     * @param name the old name connected by relation
     * @param phone the old phone connected by relation
     * @param newName the new name which is to be connected by relation
     * @param newPhone the new phone which is to be connected by relation
     * @throws NoSuchElementException if no old pair exists
     * @throws AlreadyExistsException if new pair already exists
     */
    public void changePair(@NotNull String name, @NotNull String phone, @NotNull String newName, @NotNull String newPhone)
            throws NoSuchElementException, AlreadyExistsException {
        removePair(name, phone);
        addPair(newName, newPhone);
    }

    /**
     * Pair class representing name-phone record in database
     */
    static public class NamePhonePair {
        private Name name;
        public Name name() {
            return name;
        }

        private Phone phone;
        public Phone phone() {
            return phone;
        }
    }

    /**
     * Represent policy options for GetOrCreate* methods
     * NO_DUPLICATES -- AlreadyExistsException will be thrown if such element exists
     * CREATE -- Object will be created in case of non-existence
     * NOTHING -- Nothing will be performed in case of non-existence
     */
    private enum ElementsPolicy {
        NO_DUPLICATES,
        CREATE,
        NOTHING
    }
}

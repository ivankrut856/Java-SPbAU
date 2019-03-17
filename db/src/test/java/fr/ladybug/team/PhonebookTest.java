package fr.ladybug.team;

import fr.ladybug.team.mapping.Name;
import fr.ladybug.team.mapping.NameToPhone;
import fr.ladybug.team.mapping.Phone;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PhonebookTest {

    private Phonebook phonebook;

    @BeforeEach
    void fillTestData() {
        phonebook = new Phonebook("phonebook_test_db");
        phonebook.clearDB();

        Name man1 = new Name("peter", new ObjectId());
        Name man2 = new Name("devil", new ObjectId());
        Phone phone1 = new Phone("2539", new ObjectId());
        Phone phone2 = new Phone("2539-000", new ObjectId());
        Phone phone3 = new Phone("666-devil", new ObjectId());

        NameToPhone relation = new NameToPhone(man1.getId(), phone1.getId(), new ObjectId());
        NameToPhone relation2 = new NameToPhone(man1.getId(), phone2.getId(), new ObjectId());

        NameToPhone relation3 = new NameToPhone(man2.getId(), phone2.getId(), new ObjectId());
        NameToPhone relation4 = new NameToPhone(man2.getId(), phone3.getId(), new ObjectId());

        phonebook.getDatastore().save(man1);
        phonebook.getDatastore().save(man2);
        phonebook.getDatastore().save(phone1);
        phonebook.getDatastore().save(phone2);
        phonebook.getDatastore().save(phone3);

        phonebook.getDatastore().save(relation);
        phonebook.getDatastore().save(relation2);
        phonebook.getDatastore().save(relation3);
        phonebook.getDatastore().save(relation4);
    }

    @Test
    void initializeDBNoExcept() {

    }

    @Test
    void addNewPairTest() {
        List<String> actualList = new ArrayList<>();
        phonebook.getPhonesByName("drunker").forEachRemaining(x-> actualList.add(x.getPhone()));
        assertEquals(actualList, Collections.emptyList());

        actualList.clear();
        phonebook.getNamesByPhone("8800").forEachRemaining(x-> actualList.add(x.getName()));
        assertEquals(actualList, Collections.emptyList());

        phonebook.addPair("drunker", "8800");

        actualList.clear();
        phonebook.getPhonesByName("drunker").forEachRemaining(x-> actualList.add(x.getPhone()));
        assertEquals(actualList, Arrays.asList("8800"));

        actualList.clear();
        phonebook.getNamesByPhone("8800").forEachRemaining(x-> actualList.add(x.getName()));
        assertEquals(actualList, Arrays.asList("drunker"));
    }

    @Test
    void removePairTest() {
        List<String> actualList = new ArrayList<>();
        phonebook.getPhonesByName("devil").forEachRemaining(x-> actualList.add(x.getPhone()));
        assertEquals(actualList, Arrays.asList("2539-000", "666-devil"));

        assertDoesNotThrow(() -> phonebook.removePair("devil", "2539-000"));

        actualList.clear();
        phonebook.getPhonesByName("devil").forEachRemaining(x-> actualList.add(x.getPhone()));
        assertEquals(actualList, Arrays.asList("666-devil"));
    }

    @Test
    void getAllPairsTest() {
        List<Phonebook.NamePhonePair> actualList = new ArrayList<>();
        phonebook.getAllPairs().forEachRemaining(actualList::add);
        List<String> expectedListNames = Arrays.asList("peter", "peter", "devil", "devil");
        List<String> expectedListPhones = Arrays.asList("2539", "2539-000", "2539-000", "666-devil");

        assertEquals(expectedListNames.size(), actualList.size());
        for (int i = 0; i < expectedListNames.size(); i++) {
            assertNotNull(actualList.get(i).name());
            assertEquals(expectedListNames.get(i), actualList.get(i).name().getName());

            assertNotNull(actualList.get(i).phone());
            assertEquals(expectedListPhones.get(i), actualList.get(i).phone().getPhone());
        }
    }

    @Test
    void badRemoveTest() {
        assertThrows(NoSuchElementException.class, ()-> phonebook.removePair("devil", "2539"));
        assertThrows(NoSuchElementException.class, ()-> phonebook.removePair("peter", "666-devil"));
        assertThrows(NoSuchElementException.class, ()-> phonebook.removePair("drunker", "8800"));
    }

    @Test
    void changePairTest() {
        List<String> actualList = new ArrayList<>();
        phonebook.getNamesByPhone("777-god").forEachRemaining(x-> actualList.add(x.getName()));
        assertEquals(actualList, Collections.emptyList());

        phonebook.changePair("devil", "666-devil", "devil", "777-god");

        actualList.clear();
        phonebook.getNamesByPhone("777-god").forEachRemaining(x-> actualList.add(x.getName()));
        assertEquals(actualList, Arrays.asList("devil"));
    }

}
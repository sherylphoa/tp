package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_PLUMBING;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private static final Person ALICE_WITH_PLUMBING_TAG = new PersonBuilder(ALICE).withTags(VALID_TAG_PLUMBING).build();
    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(Collections.emptyList(), addressBook.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesTags() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData.getTagList(), addressBook.getTagList());
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_PLUMBING)
                .build();
        List<Person> newPersons = Arrays.asList(ALICE, editedAlice);
        List<Tag> newTags = Collections.singletonList(new Tag(VALID_TAG_PLUMBING));
        AddressBookStub newData = new AddressBookStub(newPersons, newTags);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_PLUMBING)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void hasTag_nullTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasTag(null));
    }

    @Test
    public void hasTag_tagInAddressBook_returnsTrue() {
        Tag plumbingTag = new Tag(VALID_TAG_PLUMBING);
        addressBook.addTag(plumbingTag);
        assertTrue(addressBook.hasTag(plumbingTag));
    }

    @Test
    public void addPerson_personWithNewTags_addsTagsToGlobalList() {
        Tag newTag = new Tag(VALID_TAG_PLUMBING);

        addressBook.addPerson(ALICE_WITH_PLUMBING_TAG);

        assertTrue(addressBook.hasTag(newTag));
        assertTrue(addressBook.getTagList().contains(newTag));
    }

    @Test
    public void addPerson_tagsStandardizedToMasterList_success() {
        Tag masterTag = new Tag("Urgent");
        addressBook.addTag(masterTag);

        Person dirtyPerson = new PersonBuilder(ALICE).withTags("URGENT").build();
        addressBook.addPerson(dirtyPerson);

        Person savedPerson = addressBook.getPersonList().get(0);
        Tag savedTag = savedPerson.getTags().iterator().next();
        assertEquals("Urgent", savedTag.tagName);
    }

    @Test
    public void addPerson_multipleTagsWithMixedCasing_resolvedCorrectly() {
        // Setup master list
        addressBook.addTag(new Tag("Hardware"));
        addressBook.addTag(new Tag("Software"));

        // Add person with mixed casing
        Person p = new PersonBuilder(ALICE).withTags("hardware", "SOFTWARE").build();
        addressBook.addPerson(p);

        Set<Tag> resolvedTags = addressBook.getPersonList().get(0).getTags();

        assertEquals(2, resolvedTags.size());
        assertTrue(resolvedTags.contains(new Tag("Hardware")));
        assertTrue(resolvedTags.contains(new Tag("Software")));

        List<String> tagNames = resolvedTags.stream().map(t -> t.tagName).collect(Collectors.toList());
        assertTrue(tagNames.contains("Hardware"));
        assertTrue(tagNames.contains("Software"));
    }

    @Test
    public void setTag_existingTagRenamed_updatesAllPersonsWithThatTag() {
        Tag oldTag = new Tag("OldTag");
        Tag newTag = new Tag("NewTag");

        Person alice = new PersonBuilder(ALICE).withTags("OldTag").build();
        Person bob = new PersonBuilder(BOB).withTags("OldTag", "Other").build();

        addressBook.addPerson(alice);
        addressBook.addPerson(bob);

        // Execute the global rename
        addressBook.setTag(oldTag, newTag);

        // Check global list
        assertFalse(addressBook.hasTag(oldTag));
        assertTrue(addressBook.hasTag(newTag));

        // Check individual persons for the update
        assertTrue(addressBook.getPersonList().get(0).getTags().contains(newTag));
        assertFalse(addressBook.getPersonList().get(0).getTags().contains(oldTag));

        assertTrue(addressBook.getPersonList().get(1).getTags().contains(newTag));
        assertTrue(addressBook.getPersonList().get(1).getTags().contains(new Tag("Other")));
    }

    @Test
    public void setTag_changeCapitalization_updatesAllPersons() {
        Tag oldTag = new Tag("plumbing");
        Tag newTag = new Tag("Plumbing");
        Person alice = new PersonBuilder(ALICE).withTags("plumbing").build();
        addressBook.addPerson(alice);

        addressBook.setTag(oldTag, newTag);

        // Verify tag in tag list is updated
        assertTrue(addressBook.getTagList().contains(newTag));
        assertEquals("Plumbing", addressBook.getTagList().get(0).tagName);

        // Verify tag in Person is updated
        Person updatedAlice = addressBook.getPersonList().get(0);
        assertTrue(updatedAlice.getTags().contains(newTag));
        // Check specific casing
        assertEquals("Plumbing", updatedAlice.getTags().iterator().next().tagName);
    }

    @Test
    public void removePerson_lastPersonWithTag_tagRemoved() {
        addressBook.addPerson(ALICE_WITH_PLUMBING_TAG);
        assertTrue(addressBook.hasTag(new Tag("Plumbing")));

        addressBook.removePerson(ALICE_WITH_PLUMBING_TAG);
        assertFalse(addressBook.hasTag(new Tag("Plumbing")));
    }

    @Test
    public void removePerson_tagStillUsedByOthers_tagNotRemoved() {
        Person bobWithPlumbingTag = new PersonBuilder(BOB).withTags(VALID_TAG_PLUMBING).build();
        addressBook.addPerson(ALICE_WITH_PLUMBING_TAG);
        addressBook.addPerson(bobWithPlumbingTag);

        addressBook.removePerson(ALICE_WITH_PLUMBING_TAG);
        assertTrue(addressBook.hasTag(new Tag(VALID_TAG_PLUMBING)));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList()
                + ", tags=" + addressBook.getTagList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<Tag> tags = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons, Collection<Tag> tags) {
            this.persons.setAll(persons);
            this.tags.setAll(tags);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Tag> getTagList() { // Implement this
            return tags;
        }
    }

}

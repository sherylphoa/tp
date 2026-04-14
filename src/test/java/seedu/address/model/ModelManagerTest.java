package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_AC_SERVICE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_PLUMBING;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;
import seedu.address.model.person.predicates.NameContainsKeywordsPredicate;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void setUserPrefs_nullUserPrefs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setUserPrefs(null));
    }

    @Test
    public void setUserPrefs_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setAddressBookFilePath(Paths.get("address/book/file/path"));
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager.setUserPrefs(userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setAddressBookFilePath(Paths.get("new/address/book/file/path"));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void setAddressBookFilePath_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setAddressBookFilePath(null));
    }

    @Test
    public void setAddressBookFilePath_validPath_setsAddressBookFilePath() {
        Path path = Paths.get("address/book/file/path");
        modelManager.setAddressBookFilePath(path);
        assertEquals(path, modelManager.getAddressBookFilePath());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasTag_nullTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasTag(null));
    }

    @Test
    public void hasTag_tagNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasTag(new Tag(VALID_TAG_PLUMBING)));
    }

    @Test
    public void hasTag_tagInAddressBook_returnsTrue() {
        modelManager.addPerson(new PersonBuilder(ALICE).withTags(VALID_TAG_PLUMBING).build());
        assertTrue(modelManager.hasTag(new Tag(VALID_TAG_PLUMBING)));
    }

    @Test
    public void setTag_validTag_updatesAddressBook() {
        Tag oldTag = new Tag(VALID_TAG_PLUMBING);
        Tag newTag = new Tag(VALID_TAG_AC_SERVICE);
        modelManager.addPerson(new PersonBuilder().withName("Alice").withTags(VALID_TAG_PLUMBING).build());

        modelManager.setTag(oldTag, newTag);

        // Verify the tag is renamed in the global list
        assertFalse(modelManager.hasTag(oldTag));
        assertTrue(modelManager.hasTag(newTag));

        // Verify the person in the filtered list reflects the change
        Person alice = modelManager.getFilteredPersonList().get(0);
        assertTrue(alice.getTags().contains(newTag));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void detelePerson_selectedPerson() {
        Person personToDelete = ALICE;
        modelManager.setSelectedPerson(personToDelete);
        assertEquals(ALICE, modelManager.getSelectedPerson().getValue());
    }

    @Test
    public void deleteTag_selectedPersonAffected_refreshesSelectedPerson() {
        Tag tagToDelete = new Tag("ToDelete");
        Person personWithTag = new PersonBuilder(ALICE).withTags("ToDelete").build();

        modelManager.addPerson(personWithTag);
        modelManager.setSelectedPerson(personWithTag);

        // Ensure the person is currently selected
        assertEquals(personWithTag, modelManager.getSelectedPerson().getValue());

        // Execute delete
        modelManager.deleteTag(tagToDelete);

        // The selected person should no longer be the exact same object (it was nulled and replaced)
        assertNotSame(personWithTag, modelManager.getSelectedPerson().getValue());

        // The new selected person should NOT have the deleted tag
        assertFalse(modelManager.getSelectedPerson().getValue().getTags().contains(tagToDelete));

        // The identity (name/phone) should remain the same
        assertTrue(modelManager.getSelectedPerson().getValue().isSamePerson(personWithTag));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.singlePredicateFilteredPersonList(new NameContainsKeywordsPredicate(Arrays.asList(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.resetPredicatesFilteredPersonList();

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setAddressBookFilePath(Paths.get("differentFilePath"));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void setPerson_editedPersonMatchesFilter_selectedPersonUpdated() {
        // Add persons to model
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);

        // Select ALICE
        modelManager.setSelectedPerson(ALICE);
        assertEquals(ALICE, modelManager.getSelectedPerson().getValue());

        // Apply filter that matches ALICE
        modelManager.singlePredicateFilteredPersonList(p -> p.getName().equals(ALICE.getName()));

        // Edit ALICE to something still matching filter
        Person editedAlice = new PersonBuilder(ALICE).withName(ALICE.getName().fullName).build();
        modelManager.setPerson(ALICE, editedAlice);

        // Selected person should be updated to edited version
        assertEquals(editedAlice, modelManager.getSelectedPerson().getValue());
    }

    @Test
    public void setPerson_editedPersonNoLongerMatchesFilter_selectedPersonCleared() {
        // Add persons to model
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);

        // Select ALICE
        modelManager.setSelectedPerson(ALICE);
        assertEquals(ALICE, modelManager.getSelectedPerson().getValue());

        // Apply filter that matches only ALICE (by name)
        modelManager.singlePredicateFilteredPersonList(p -> p.getName().equals(ALICE.getName()));

        // Edit ALICE to something that no longer matches filter
        Person editedAlice = new PersonBuilder(ALICE).withName("Different Name").build();
        modelManager.setPerson(ALICE, editedAlice);

        // Selected person should be cleared
        assertNull(modelManager.getSelectedPerson().getValue());
    }
}

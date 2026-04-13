package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SimpleObjectProperty<Person> selectedPerson =
            new SimpleObjectProperty<>();
    private List<Predicate<Person>> activePredicates;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        this.filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        this.activePredicates = new ArrayList<>(List.of(PREDICATE_SHOW_ALL_PERSONS));
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
        selectedPerson.set(null);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    // Person Operations
    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);

        if (target.equals(selectedPerson.get())) {
            selectedPerson.set(null);
        }
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        resetPredicatesFilteredPersonList();
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);

        if (target.equals(selectedPerson.get())) {
            // Check if edited person still matches current filter
            if (filteredPersons.getPredicate() == null || filteredPersons.getPredicate().test(editedPerson)) {
                selectedPerson.set(editedPerson);
            } else {
                selectedPerson.set(null);
            }
        }
    }

    // Tag Operations
    @Override
    public boolean hasTag(Tag tag) {
        requireNonNull(tag);
        return addressBook.hasTag(tag);
    }

    @Override
    public void setTag(Tag target, Tag editedTag) {
        requireAllNonNull(target, editedTag);

        addressBook.setTag(target, editedTag);

        resetPredicatesFilteredPersonList();
        refreshSelectedPersonIfTagAffected(target);
    }

    @Override
    public void deleteTag(Tag target) {
        requireNonNull(target);

        addressBook.removeTag(target);

        resetPredicatesFilteredPersonList();
        refreshSelectedPersonIfTagAffected(target);
    }

    /**
     * Refreshes the {@code selectedPerson} if they are currently assigned the given {@code target} tag.
     * Ensures the Details View reflects changes made to tags (e.g., renaming or deleting).
     */
    private void refreshSelectedPersonIfTagAffected(Tag target) {
        Person currentlySelected = selectedPerson.getValue();
        if (currentlySelected != null && currentlySelected.getTags().stream().anyMatch(t -> t.isSameTag(target))) {
            selectedPerson.setValue(null);
            getFilteredPersonList().stream()
                    .filter(p -> p.isSamePerson(currentlySelected))
                    .findFirst()
                    .ifPresent(selectedPerson::setValue);
        }
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void resetPredicatesFilteredPersonList() {
        activePredicates.clear();
        addPredicateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        assert activePredicates.size() == 1;
    }

    @Override
    public void singlePredicateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        activePredicates.clear();
        resetPredicatesFilteredPersonList();
        addPredicateFilteredPersonList(predicate);
        assert activePredicates.size() == 2;
    }

    @Override
    public void addPredicateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        activePredicates.add(predicate);
        //Solution below inspired by
        //https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html#and-java.util.function.Predicate-
        Predicate<Person> uberPredicate = activePredicates.stream().reduce(p -> true, Predicate::and);
        filteredPersons.setPredicate(uberPredicate);
    }

    @Override
    public ObservableValue<Person> getSelectedPerson() {
        return selectedPerson;
    }

    @Override
    public void setSelectedPerson(Person person) {
        selectedPerson.set(person);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager)) {
            return false;
        }

        ModelManager otherModelManager = (ModelManager) other;
        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons);
    }
}

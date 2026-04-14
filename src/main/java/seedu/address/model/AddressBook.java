package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;

/**
 * Wraps all data at the address-book level
 * Duplicates are not allowed (by .isSamePerson comparison)
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons;
    private final UniqueTagList tags;

    /*
     * The 'unusual' code block below is a non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        persons = new UniquePersonList();
        tags = new UniqueTagList();
    }

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
        rebuildTagList();
    }

    /**
     * Replaces the contents of the tag list with {@code tags}.
     * {@code tags} must not contain duplicate tags.
     */
    public void setTags(List<Tag> tags) {
        this.tags.setTags(tags);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        rebuildTagList();
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);

        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
        rebuildTagList();
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
        rebuildTagList();
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
        rebuildTagList();
    }

    /**
     * Updates the internal UniqueTagList when a person is added, edited or removed to ensure that the internal
     * UniqueTagList contains all tags present in a person.
     */
    private void rebuildTagList() {
        Set<Tag> tagsInUse = persons.asUnmodifiableObservableList().stream()
                .flatMap(person -> person.getTags().stream())
                .collect(Collectors.toSet());

        tags.setTags(new ArrayList<>(tagsInUse));
    }

    //// tag-level operations

    /**
     * Returns true if a tag equal to {@code tag} exists in the address book.
     */
    public boolean hasTag(Tag tag) {
        requireNonNull(tag);

        return tags.contains(tag);
    }

    /**
     * Adds {@code t} for storage
     */
    public void addTag(Tag t) {
        if (!tags.contains(t)) {
            tags.add(t);
        }
    }

    /**
     * Updates the tag {@code target} to {@code editedTag} globally.
     * Every person currently holding {@code target} will be updated to hold {@code editedTag}.
     */
    public void setTag(Tag target, Tag editedTag) {
        requireAllNonNull(target, editedTag);

        tags.setTag(target, editedTag);

        updatePersonsWithTag(target, tagsSet -> {
            tagsSet.remove(target);
            tagsSet.add(editedTag);
            return tagsSet;
        });
    }

    /**
     * Removes {@code key} from this {@code AddressBook}. Any persons with this tag will have this tag removed.
     * {@code key} must exist in the address book.
     */
    public void removeTag(Tag key) {
        requireNonNull(key);
        tags.remove(key);

        updatePersonsWithTag(key, tagsSet -> {
            tagsSet.remove(key);
            return tagsSet;
        });
    }

    /**
     * Helper method to create a new Person instance for setTag and removeTag.
     */
    private void updatePersonsWithTag(Tag target, UnaryOperator<Set<Tag>> tagTransformer) {
        List<Person> updatedPersonList = persons.asUnmodifiableObservableList().stream()
                .map(person -> {
                    if (!person.getTags().contains(target)) {
                        return person;
                    }
                    Set<Tag> updatedTags = new HashSet<>(person.getTags());
                    // tagTransformer decides if we rename or remove the tag
                    tagTransformer.apply(updatedTags);

                    return new Person(
                            person.getName(), person.getPhone(), person.getEmail(),
                            person.getAddress(), person.getNotes(), person.getLogHistory(), updatedTags
                    );
                })
                .collect(Collectors.toList());

        persons.setPersons(updatedPersonList);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("tags", tags)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Tag> getTagList() {
        return tags.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook)) {
            return false;
        }

        AddressBook otherAddressBook = (AddressBook) other;
        return persons.equals(otherAddressBook.persons)
                && tags.equals(otherAddressBook.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, tags);
    }
}

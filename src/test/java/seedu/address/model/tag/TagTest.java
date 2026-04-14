package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName("")); // empty string (0 characters)
        assertFalse(Tag.isValidTagName(" ")); // spaces only
        assertFalse(Tag.isValidTagName("    ")); // multiple spaces
        assertFalse(Tag.isValidTagName("a".repeat(51))); // 51 characters (Exceeds 50 limit)
        assertFalse(Tag.isValidTagName("王".repeat(51))); // 51 Unicode characters

        // valid tag names - ASCII
        assertTrue(Tag.isValidTagName("a")); // exactly 1 character
        assertTrue(Tag.isValidTagName("abcde12345")); // alphanumeric
        assertTrue(Tag.isValidTagName("12345")); // numeric only
        assertTrue(Tag.isValidTagName("Best-Friend!")); // symbols now allowed
        assertTrue(Tag.isValidTagName("2nd floor")); // spaces now allowed
        assertTrue(Tag.isValidTagName("#urgent")); // leading symbols allowed
        assertTrue(Tag.isValidTagName("a".repeat(50))); // exactly 50 characters

        // valid tag names - Unicode (non-Latin scripts)
        assertTrue(Tag.isValidTagName("王")); // single character Unicode
        assertTrue(Tag.isValidTagName("空调")); // Chinese
        assertTrue(Tag.isValidTagName("에어컨")); // Korean
        assertTrue(Tag.isValidTagName("エアコン")); // Japanese
        assertTrue(Tag.isValidTagName("王".repeat(50))); // exactly 50 characters Unicode

        // valid tag names - mixed scripts and symbols
        assertTrue(Tag.isValidTagName("Aircon 空调"));
        assertTrue(Tag.isValidTagName("#电工"));
    }

    @Test
    public void equals_caseInsensitive() {
        Tag tag = new Tag("Plumbing");

        // same object -> returns true
        assertTrue(tag.equals(tag));

        // same values, same case -> returns true
        assertTrue(tag.equals(new Tag("Plumbing")));

        // same values, different case -> returns true
        assertTrue(tag.equals(new Tag("plumbing")));
        assertTrue(tag.equals(new Tag("PLUMBING")));
        assertTrue(tag.equals(new Tag("pLuMbInG")));

        // different types -> returns false
        assertFalse(tag.equals(1));

        // null -> returns false
        assertFalse(tag.equals(null));

        // different values -> returns false
        assertFalse(tag.equals(new Tag("Electrical")));
    }

    @Test
    public void hashCode_caseInsensitive() {
        Tag tag1 = new Tag("Plumbing");
        Tag tag2 = new Tag("plumbing");
        Tag tag3 = new Tag("PLUMBING");

        // All should have the same hash code regardless of capitalisatoin
        assertEquals(tag1.hashCode(), tag2.hashCode());
        assertEquals(tag2.hashCode(), tag3.hashCode());

        // Different tags should have different hash codes
        assertNotEquals(tag1.hashCode(), new Tag("Electrical").hashCode());
    }
}

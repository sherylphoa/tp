package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_AC_SERVICE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_PLUMBING;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.exceptions.DuplicateTagException;
import seedu.address.model.tag.exceptions.TagNotFoundException;

public class UniqueTagListTest {
    private final UniqueTagList uniqueTagList = new UniqueTagList();
    private final Tag plumbingTag = new Tag(VALID_TAG_PLUMBING);
    private final Tag electricalTag = new Tag(VALID_TAG_AC_SERVICE);
    private final Tag plumbingTagCaps = new Tag("PLUMBING");

    @Test
    public void contains_nullTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueTagList.contains(null));
    }

    @Test
    public void contains_tagNotInList_returnsFalse() {
        assertFalse(uniqueTagList.contains(plumbingTag));
    }

    @Test
    public void contains_tagInList_returnsTrue() {
        uniqueTagList.add(plumbingTag);
        assertTrue(uniqueTagList.contains(plumbingTag));
    }

    @Test
    public void add_nullTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueTagList.add(null));
    }

    @Test
    public void add_duplicateTag_throwsDuplicateTagException() {
        uniqueTagList.add(plumbingTag);
        assertThrows(DuplicateTagException.class, () -> uniqueTagList.add(plumbingTag));
    }

    @Test
    public void setTag_nullTargetTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueTagList.setTag(null, plumbingTag));
    }

    @Test
    public void setTag_nullEditedTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueTagList.setTag(plumbingTag, null));
    }

    @Test
    public void setTag_targetTagNotInList_throwsTagNotFoundException() {
        assertThrows(TagNotFoundException.class, () -> uniqueTagList.setTag(plumbingTag, plumbingTag));
    }

    @Test
    public void setTag_editedTagIsSameTag_success() {
        uniqueTagList.add(plumbingTag);
        uniqueTagList.setTag(plumbingTag, plumbingTag);
        UniqueTagList expectedUniqueTagList = new UniqueTagList();
        expectedUniqueTagList.add(plumbingTag);
        assertEquals(expectedUniqueTagList, uniqueTagList);
    }

    @Test
    public void setTag_editedTagHasDifferentName_success() {
        uniqueTagList.add(plumbingTag);
        uniqueTagList.setTag(plumbingTag, electricalTag);
        UniqueTagList expectedUniqueTagList = new UniqueTagList();
        expectedUniqueTagList.add(electricalTag);
        assertEquals(expectedUniqueTagList, uniqueTagList);
    }

    @Test
    public void setTag_editedTagHasNonUniqueName_throwsDuplicateTagException() {
        uniqueTagList.add(plumbingTag);
        uniqueTagList.add(electricalTag);
        assertThrows(DuplicateTagException.class, () -> uniqueTagList.setTag(plumbingTag, electricalTag));
    }

    @Test
    public void setTags_nullUniqueTagList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueTagList.setTags((UniqueTagList) null));
    }

    @Test
    public void setTags_uniqueTagList_replacesOwnListWithProvidedUniqueTagList() {
        uniqueTagList.add(electricalTag);
        UniqueTagList expectedUniqueTagList = new UniqueTagList();
        expectedUniqueTagList.add(plumbingTag);
        uniqueTagList.setTags(expectedUniqueTagList);
        assertEquals(expectedUniqueTagList, uniqueTagList);
    }

    @Test
    public void setTags_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueTagList.setTags((List<Tag>) null));
    }

    @Test
    public void setTags_list_replacesOwnListWithProvidedList() {
        uniqueTagList.add(plumbingTag);
        List<Tag> tagList = Collections.singletonList(electricalTag);
        uniqueTagList.setTags(tagList);
        UniqueTagList expectedUniqueTagList = new UniqueTagList();
        expectedUniqueTagList.add(electricalTag);
        assertEquals(expectedUniqueTagList, uniqueTagList);
    }

    @Test
    public void setTags_listWithDuplicateTags_throwsDuplicateTagException() {
        List<Tag> listWithDuplicateTags = Arrays.asList(plumbingTag, plumbingTag);
        assertThrows(DuplicateTagException.class, () -> uniqueTagList.setTags(listWithDuplicateTags));
    }

    @Test
    public void setTags_listWithCaseInsensitiveDuplicateTags_throwsDuplicateTagException() {
        List<Tag> listWithDuplicateTags = Arrays.asList(plumbingTag, plumbingTagCaps);
        assertThrows(DuplicateTagException.class, () -> uniqueTagList.setTags(listWithDuplicateTags));
    }

    @Test
    public void add_duplicateCaseInsensitive_throwsDuplicateTagException() {
        uniqueTagList.add(plumbingTag);
        // Adding the same name with different case
        assertThrows(DuplicateTagException.class, () -> uniqueTagList.add(plumbingTagCaps));
    }

    @Test
    public void remove_caseInsensitive_success() {
        uniqueTagList.add(plumbingTag);
        // Removing using a different case
        uniqueTagList.remove(plumbingTagCaps);
        assertEquals(0, uniqueTagList.asUnmodifiableObservableList().size());
    }

    @Test
    public void remove_tagDoesNotExist_throwsTagNotFoundException() {
        UniqueTagList uniqueTagList = new UniqueTagList();
        assertThrows(TagNotFoundException.class, () -> uniqueTagList.remove(new Tag("NonExistent")));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
                -> uniqueTagList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniqueTagList.asUnmodifiableObservableList().toString(), uniqueTagList.toString());
    }
}

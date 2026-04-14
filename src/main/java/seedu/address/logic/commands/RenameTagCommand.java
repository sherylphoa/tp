package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.tag.Tag;

/**
 * Renames an existing tag globally in the address book.
 */
public class RenameTagCommand extends Command {

    public static final String COMMAND_WORD = "renametag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Renames an existing tag globally. Tags are case-insensitive. "
            + "Exactly two tags must be provided as parameters.\n"
            + "Parameters: --tag=OLD_TAG --tag=NEW_TAG\n"
            + "Example: " + COMMAND_WORD + " --tag=Plumbing --tag=Pipe-Repair";

    public static final String MESSAGE_SUCCESS = "Renamed Tag: %1$s to %2$s";
    public static final String MESSAGE_TAG_NOT_FOUND = "The tag '%1$s' does not exist in Linkline.";
    public static final String MESSAGE_DUPLICATE_TAG = "The tag '%1$s' already exists.";

    private final Tag oldTag;
    private final Tag newTag;

    /**
     * @param oldTag The tag to be renamed.
     * @param newTag The new name for the tag.
     */
    public RenameTagCommand(Tag oldTag, Tag newTag) {
        requireNonNull(oldTag);
        requireNonNull(newTag);
        this.oldTag = oldTag;
        this.newTag = newTag;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (!model.hasTag(oldTag)) {
            throw new CommandException(String.format(MESSAGE_TAG_NOT_FOUND, oldTag.tagName));
        }

        if (model.hasTag(newTag)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_TAG, newTag.tagName));
        }

        model.setTag(oldTag, newTag);

        return new CommandResult(String.format(MESSAGE_SUCCESS, oldTag.tagName, newTag.tagName)).withSaveRequired();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof RenameTagCommand)) {
            return false;
        }

        RenameTagCommand otherCommand = (RenameTagCommand) other;
        return oldTag.equals(otherCommand.oldTag)
                && newTag.equals(otherCommand.newTag);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("oldTag", oldTag)
                .add("newTag", newTag)
                .toString();
    }
}

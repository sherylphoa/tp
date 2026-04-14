---
layout: default.md
title: "User Guide"
pageNav: 3
---

# Linkline User Guide

Linkline is a **desktop app for solo residential service technicians who want to manage client records quickly from the
keyboard**. It keeps contact details, service addresses, notes, tags, and timestamped service logs in one place, so you
can prepare for repeat jobs without digging through chat history or paper notes.

Linkline is optimized for a Command Line Interface (CLI), but still gives you the convenience of a Graphical User
Interface (GUI). If you can type quickly, Linkline can help you manage client records faster than a mouse-heavy app.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure you have Java `17` or above installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version
   prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2526S2-CS2103-F09-4/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for Linkline.

1. Open a command terminal, `cd` into the folder you put the jar file in, and run:

   ```text
   java -jar linkline.jar
   ```

   If Linkline does not find an existing data file, it starts with sample data.<br>
   A GUI similar to the one below should appear in a few seconds.<br>
   ![Linkline main window](images/Ui.png)

1. In the main window shown above:

   * The command box is at the top.
   * The result display is directly below the command box.
   * The left panel shows the current client list with each client's index, name, and phone number.
   * The right panel shows the selected client's full details, including notes and service logs.

1. Type a command in the command box and press Enter to execute it. Try these commands first:

   * `list`
   * `add --name=John Doe --phone=98765432 --email=johnd@example.com --address=John street, block 123, #01-01`
   * `view 1`
   * `find --tag=AC-Service`
   * `copyaddr 1`
   * `help`

1. Refer to the [Features](#features) below for the full command list, description, and examples.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are values you supply.<br>
  Example: in `add --name=NAME`, replace `NAME` with an actual name such as `Alex Yeoh`.

* Items in square brackets are optional.<br>
  Example: `--name=NAME [--tag=TAG]` can be used as `--name=Alex Yeoh --tag=AC-Service` or `--name=Alex Yeoh`.

* Items followed by `...` can be repeated.<br>
  Example: `[--tag=TAG]...` means you can use zero or more `--tag=` fields, such as no `--tag=` fields at all,
  `--tag=AC-Service`, or `--tag=AC-Service --tag=Plumbing`.

* When a command format shows a named field as `--field=[VALUE]`, that field may be given a value or left empty if that
  command gives the empty value a special meaning.<br>
  Example: `find --tag=[TAG]` allows both `find --tag=Plumbing` and `find --tag=`.

* When a command uses named fields such as `--name=` and `--phone=`, those fields can usually appear in any order unless
  stated otherwise.

* In prefix-based commands, a field value must not contain a space immediately followed by another field marker used by
  the same command.<br>
  Example: `--notes=Call before arriving --tag=Urgent` is interpreted as a notes value followed by a tag field, not as
  one literal notes value.

* Commands that do not accept arguments reject extra input.<br>
  Example: `help 123` and `list now` are invalid.

* Commands and prefixes are case-sensitive.<br>
  Example: `list` is a valid command, but `List` or `LIST` will be rejected. Similarly, `--name=` is recognized, but `--Name=` or `--NAME=` will not work as expected.

* If you are using a PDF version of this document, be careful when copying commands that span multiple lines. Some PDF
  viewers may remove spaces around line breaks.
</box>

<box type="info" seamless>

**Field constraints:**<br>

The following constraints apply whenever these field values are entered in commands.

* `NAME`: 1 to 100 characters. Must not be blank.
* `PHONE_NUMBER`: Must contain 3 to 15 digits in total. Spaces and hyphens are allowed only between digit groups.
* `EMAIL`: Must be a valid `local-part@domain` email address. Must not exceed 320 characters. The local-part must be
  between 1 and 64 characters. The local-part should only contain alphanumeric characters and these special characters,
  excluding the parentheses, (!#$%&'*+/=?^_`{|}~.-). The local part may not start or end with special characters and
  special characters cannot appear consecutively. The domain is made up of domain labels separated by periods and must
  not exceed 255 characters. The domain name must end with a domain label at least 2 characters long. Each domain label
  must start and end with alphanumeric characters, not exceed 63 characters, and consist of alphanumeric characters
  separated only by hyphens, if any.
* `ADDRESS`: Must not be blank.
* `TAG`: 1 to 50 characters. Must not be blank.
* `NOTES`: 0 to 500 characters.
* `LOG_MESSAGE`: 1 to 1000 characters. Must contain at least one non-space character.

</box>

<box type="tip" seamless>

**Note on character counting:** For free-text fields such as `NOTES` and `LOG_MESSAGE`, character limits are measured internally using Unicode code points. In most cases, this matches what users would visually perceive and naturally count as characters, though some combined symbols may count differently.
</box>

<box type="tip" seamless>

**Tip:** Linkline keeps the displayed client list sorted by name, then by phone number (by numeric digits only – spaces
and hyphens are ignored). Whenever a command changes which clients are shown, whether by modifying data, resetting the
list, or narrowing it, the displayed client list remains sorted in that order.
</box>

### Viewing help: `help`

Opens the help window.

![help message](images/helpMessage.png)

Format:

```text
help
```

* This command does not accept any arguments.

### Adding a client: `add`

Adds a new client to Linkline.

Format:

```text
add --name=NAME --phone=PHONE_NUMBER --email=EMAIL --address=ADDRESS [--notes=NOTES] [--tag=TAG]...
```

* `--name=`, `--phone=`, `--email=`, and `--address=` are required.
* `--tag=` can be repeated. Other named fields can appear at most once.
* Linkline rejects duplicates. Two clients are considered duplicates if they share the same email address
(case-insensitive) or the same phone number after ignoring spaces and hyphens.
* After a successful `add`, Linkline shows the full client list again.

<box type="tip" seamless>

**Note:** Only one primary phone number is supported per client. If you need to store additional numbers (e.g., home,
office), you may add them in the `notes` field. However, please note that numbers stored in `notes` will **not** be
included in search results when using the `find` command.
</box>

<box type="tip" seamless>

**Note on country codes:** Country codes (e.g., `+65`) are not officially supported. As a workaround, you can prefix the
country code with a hyphen (e.g., `65-91234567`). Be aware that duplicate detection ignores hyphens and spaces, so
`65-91234567`, `65-9123-4567`, and `6591234567` will be treated as the same number.
</box>

Examples:

* `add --name=John Tan --phone=9123 4567 --email=johntan@example.com --address=123 Clementi Rd, #04-05`
* `add --name=Alex Yeoh --phone=98765432 --email=alexyeoh1234@gmail.com --address=123 Clementi Street --notes=Strict visitor screening --tag=Electrical Wiring`

Example result after an `add` command:
![add command result](images/addCommandResult.png)

### Editing a client: `edit`

Edits an existing client in Linkline.

Format:

```text
edit INDEX [--name=NAME] [--phone=PHONE_NUMBER] [--email=EMAIL] [--address=ADDRESS] [--notes=NOTES] [--tag=TAG]...
```

* The index refers to the index number shown in the current displayed client list.
* The index must be a positive integer.
* At least one field must be provided.
* Except for `--tag=`, each field can appear at most once.
* Any field you provide replaces the client's current value for that field.
* Editing tags is not cumulative. If you provide `--tag=`, Linkline replaces the client's entire tag list with the tags
  you supplied.
* Use `--tag=` with no value to clear all tags. In this case, it cannot be combined with other `--tag=` fields in the
  same command.
* Use `--notes=` with no value to clear notes.
* Linkline rejects edits that would make the client duplicate another existing client.

<box type="tip" seamless>

**Note on editing clients on filtered lists:** If you edit a client while viewing a filtered list (e.g., after using
`find` or `filtertag`), the client may disappear from the displayed client list and details panel if the edited details
no longer match the current filter criteria. Use `list` to see all clients again.
</box>

Examples:

* `edit 1 --phone=91234567 --email=johndoe@example.com`
* `edit 2 --name=Betsy Crower --tag=`
* `edit 3 --notes=Client requested weekday morning visits`

### Deleting a client: `delete`

Deletes the specified client from Linkline with confirmation.

Format:

```text
delete INDEX
```

* The index refers to the index number shown in the current displayed client list.
* The index must be a positive integer.
* This command uses two-step confirmation:
    * The first `delete INDEX` only shows a confirmation message.
    * The second matching `delete INDEX` completes the deletion.
* Any other command, including an invalid command, provided after the first `delete` command cancels the pending
  deletion.

<box type="tip" seamless>

**Tip:** After the first `delete 1`, commands such as `delete 1` and `delete 01` both confirm the deletion because
Linkline compares the parsed index value. Leading/trailing spaces and spaces between the command word and index are
ignored. Numbers with leading zeros (e.g., '01', '001') also confirm the deletion.
</box>

Examples:

* `delete 1` followed by `find --name=Bernice`
    * The pending deletion is canceled by the `find` command.
* `delete 1`
  * Shows confirmation message for deleting the client at index 1.
  * If you enter `delete 1` again, the client at index 1 is deleted.

Example result of `delete` command with confirmation (first attempt and confirmed deletion):
![pending delete command result](images/pendingDeleteCommandResult.png)

![confirmed delete command result](images/confirmedDeleteCommandResult.png)

### Clearing all entries: `clear`

Clears all data in Linkline with confirmation.

Format:

```text
clear
```

* This command does not accept any arguments.
* This command uses two-step confirmation:
    * The first `clear` only shows a confirmation message.
    * The second `clear` clears all entries.
* Any other command, including an invalid command, provided after the first `clear` command cancels the pending action.

Example result after confirming `clear`:
![clear command result](images/clearCommandResult.png)

### Listing all clients: `list`

Shows all clients.

Format:

```text
list
```

* This command resets any previous `find` or `filtertag`.
* This command does not accept any arguments.

Example result after a `list` command:
![list command result](images/listCommandResult.png)

### Viewing client details: `view`

Shows the specified client's full details in the right-hand panel (including full client name, tags, phone number, email address...).

Format:

```text
view INDEX
```

* The index refers to the index number shown in the current displayed client list.
* The index must be a positive integer such as `1`, `2`, or `3`.

Example result after a `view` command:
![view command result](images/viewCommandResult.png)

<box type="tip" seamless>

**Tip:** Use this command before `logdelete` if you need to check the log numbers shown in the UI to locate the index of
the log you would like to delete.
</box>

Examples:

* `view 1`
* `find --name=Alex` followed by `view 1`

**Note:** Clicking on client in list will only change the highlight position, to see details of a client or before making any information modification, please use `view` command.

### Finding clients by details: `find`

Searches the currently displayed client list for clients whose name, phone number, email address, physical address, or tag
matches at least one supplied query. Uses `OR` matching across all supplied queries and fields.

`find` will only search based on the clients currently in the displayed client list. \
Both `find` and `filtertag` commands can be used to narrow down the current list.

Use `list` when you want to search from the full client list again.

Format:

```text
find [--name=SUBNAME]... [--phone=SUBNUMBER]... [--email=SUBEMAIL]... [--address=SUBADDRESS]... [--tag=[SUBTAG]]...
```

* Provide at least one search field. Any text before the first field is invalid.
* `--name=`, `--phone=`, `--email=`, and `--address=` must have a value.
* `--tag=` may be left empty. `find --tag=` matches clients with no tags.
* Repeat a field when you want to supply multiple queries for that field.<br>
  Example: `find --name=Alex --name=Tan`
* Within a single `find` command, Linkline uses `OR` matching across all supplied queries and fields.<br>
  Example: `find --name=Alex --tag=Plumbing` returns clients whose name matches `Alex` **or** whose tag matches
  `Plumbing`.
* For the `phone` field specifically, queries will match clients regardless of whether hyphens and spaces are present
  or omitted in their actual phone field.
  Example: `find --phone=91278492` will match clients with phone numbers `91278492`, `9127-8492`, `9127 8492`.
* All matching is case-insensitive substring matching, including tags.<br>
  Example: `--tag=Electrical` matches the tag `Electrical Wiring`.
* `find --name=Alice Bob` searches for the single substring `Alice Bob`. To search for `Alice` and `Bob` separately,
  use `find --name=Alice --name=Bob`.

Examples:

* `find --phone=9927`
* `find --email=@example.com`
* `find --address=Tampines`
* `find --tag=Electrical`
* `find --tag=`
* `find --name=Alex` followed by `filtertag --tag=AC-Service`
* `find --name=alex --name=bernice --name=oliveiro --name=david --name=irfan --name=roy`

Example result after a `find` command:
![find command result](images/findCommandResult.png)

### Filtering clients by tag: `filtertag`

Shows only clients in the currently displayed client list whose tags satisfy the supplied tag filter. uses `AND` matching
across all supplied queries and fields

`filtertag` will only search based on the clients currently in the displayed client list. \
Both `find` and `filtertag` commands can be used to narrow down the current list.

Use `list` when you want to search from the full client list again.

Format:

```text
filtertag --tag=[TAG] [--tag=[MORE_TAGS]]...
```

* Provide at least one `--tag=` field. Any text before the first `--tag=` is invalid.
* Each non-empty `--tag=` value is treated as one whole tag name, even if it contains spaces.
* Non-empty tag values use case-insensitive exact matching.
  Example: `--tag=Electrical` does not match the tag `Electrical Wiring`.
* If you provide multiple non-empty tags, a client must contain **all** of them to be shown.
  Example: `filtertag --tag=Plumbing --tag=Electrical Wiring` only shows clients tagged with both `Plumbing` and
  `Electrical Wiring`.
* `filtertag --tag=` is a special case that matches only clients with no tags.
* You cannot mix blank and non-blank tag values in the same command.<br>
  Example: `filtertag --tag=Plumbing --tag=` is invalid.

Examples:

* `filtertag --tag=AC-Service`
* `filtertag --tag=`
* `find --address=Tampines` followed by `filtertag --tag=Electrical Wiring`
* `filtertag --tag=Plumbing --tag=Electrical Wiring`

Example result after a `filtertag` command:
![filtertag command result](images/filtertagCommandResult.png)

### Copying a client's address: `copyaddr`

Copies the specified client's address to your system clipboard.

Format:

```text
copyaddr INDEX
```

* The index refers to the index number shown in the current displayed client list.
* The index must be a positive integer.
* If Linkline cannot access the clipboard, it shows an error instead of copying anything.

Examples:

* `copyaddr 1`
* `find --tag=Plumbing` followed by `copyaddr 2`

<box type="warning" seamless>

**Warning:** The `copyaddr` command copies the address based on the current **displayed index**. The copied address may
become outdated if:

- The client list changes (e.g., using `list` or `find`), causing the index to point to a different client.
- The client's address is edited after copying.
  </box>

### Copying an edit command template: `copyedit`

Copies a ready-to-edit `edit` command for the specified client to your system clipboard.

Format:

```text
copyedit INDEX
```

* The index refers to the index number shown in the current displayed client list.
* The index must be a positive integer.
* The copied command uses the client's current displayed index.
* The copied command includes the client's name, phone, email, address, and all current tags.
* If the client has notes, the copied command also includes `--notes=...`.
* This is useful when you want to change a field with long or multiple values (e.g., tags, notes, ...) without retyping
  the rest.
* If Linkline cannot access the clipboard, it shows an error instead of copying anything.

Examples:

* `copyedit 1`
* `find --name=Bernice` followed by `copyedit 1`

<box type="tip" seamless>

**Tip:** A common workflow for editing is `copyedit INDEX`, paste the copied command into the command box, change only
the field you want, and then press Enter.
</box>

<box type="warning" seamless>

**Warning:** The `copyedit` command copies the current **displayed index**, not the client's identity.

- If you change the displayed client list (e.g., using `list` or `find`) before running the pasted command, the index
  in the copied command may now refer to a different client.
- If the client's details (e.g., name, phone, email) have been edited since copying, the copied command may contain
  outdated information.
</box>

### Adding a client log: `logadd`

Adds a timestamped log entry to the specified client.

Format:

```text
logadd INDEX LOG_MESSAGE
```

* The index refers to the index number shown in the current displayed client list.
* The index must be a positive integer.
* Linkline automatically records the current date and time for the new log entry.
* Custom or backdated log timestamps are not currently supported.
* Use `view INDEX` if you want to inspect the updated logs in the right-hand panel.

Examples:

* `logadd 1 Observed leakage beneath sink during site visit.`
* `logadd 2 Client requested follow-up call next Wednesday at 2pm.`
* `logadd 3 Fixed plumbing issue for toilet in master bedroom. Recommended plumbing services for other toilets.`

Example result after a `logadd` command:
![logadd command result](images/logaddCommandResult.png)

### Deleting a client log: `logdelete`

Deletes one log entry from the specified client with confirmation.

Format:

```text
logdelete CLIENT_INDEX LOG_INDEX
```

* `CLIENT_INDEX` refers to the index number shown in the current displayed client list.
* `LOG_INDEX` refers to the log number shown in the UI for that client.
* Both indices must be positive integers.
* If the client has no logs, the command fails.
* If `LOG_INDEX` does not exist for that client, the command fails.
* This command uses two-step confirmation:
    * The first `logdelete CLIENT_INDEX LOG_INDEX` only shows a confirmation message.
    * The second matching `logdelete CLIENT_INDEX LOG_INDEX` deletes the log entry.
* Any other command, including an invalid command, cancels the pending log deletion.
* In the right-hand panel, logs are shown newest first, but numbered oldest to newest.<br>
  Example: if a client has 5 logs, the topmost and latest entry is labeled `Log 5`.

Examples:

* `logdelete 3 1` followed by `find --name=Bernice`
    * The pending deletion is canceled by the `find` command.
* Continuing from the earlier `logadd 3 ...` example: `logdelete 3 1`
    * Shows confirmation message for deleting log `1` of client `3`.
    * If you enter `logdelete 3 1` again, the corresponding log is deleted.

Example result after confirming `logdelete`:
![confirmed logdelete command result](images/logdeleteCommandResult.png)

### Renaming a tag: `renametag`

Renames an existing tag across all clients in Linkline.

Format:

```text
renametag --tag=OLD_TAG --tag=NEW_TAG
```

* Provide exactly two `--tag=` fields, in this order: `OLD_TAG`, then `NEW_TAG`.
* The `OLD_TAG` must already exist in Linkline.
* The `NEW_TAG` must be valid and must not already exist.
* Tag names are case-insensitive and capitalisation cannot be changed with `renametag`.<br>
  Example: `PLUMBING` and `plumbing` are treated as the same tag.
* After a successful rename, Linkline shows the full client list again.

Examples:

* `renametag --tag=Electrical Wiring --tag=Electrical`
* `renametag --tag=AC-Service --tag=AC-Repair`

Example result after a `renametag` command:
![renametag command result](images/renametagCommandResult.png)

### Deleting a tag: `deletetag`

Deletes a tag from Linkline and removes it from all clients that currently use it.

Format:

```text
deletetag TAG_NAME
```

* `TAG_NAME` must already exist in Linkline.
* This command uses two-step confirmation:
    * The first `deletetag TAG_NAME` only shows a confirmation message.
    * The second matching `deletetag TAG_NAME` deletes the tag globally.
* Any other command, including an invalid command, cancels the pending tag deletion.
* After a successful deletion, Linkline shows the full client list again.

Examples:

* `deletetag Electrical Wiring` followed by `find --name=Bernice`
    * The pending deletion is canceled by the `find` command.
* `deletetag ac-repair`
    * Shows confirmation message for deleting tag `ac-repair`.
    * If you enter `deletetag ac-repair` again, the tag `ac-repair` is deleted.

Example result after confirming `deletetag`:
![confirmed deletetag command result](images/deletetagCommandResult.png)

### Exiting Linkline: `exit`

Closes the application.

Format:

```text
exit
```

* This command does not accept any arguments.

### Saving the data

Linkline saves address book data to `data/linkline.json` automatically after each successful command that changes
persisted data. Read-only commands such as `list`, `find`, `view`, `help`, and clipboard commands do not update this
file.

If an address book save fails after a data-changing command, Linkline reports the error immediately. If those unsaved
changes are still in memory when Linkline closes normally, it retries the save once. There is no manual save command.

<box type="tip" seamless>

**Tip:** If Linkline reports a save error after a data-changing command, try to fix that storage problem before
exiting. The shutdown retry is best-effort only, so if it also fails, the unsaved address book changes in memory are
lost when the app closes.
</box>

Window preferences such as size and position are saved separately to `preferences.json` when Linkline closes normally.

### Editing the data file

Linkline stores data in `[JAR file location]/data/linkline.json`. Advanced users can edit this file directly.

<box type="warning" seamless>

**Caution:**<br>
If the data file contains invalid JSON, missing required fields, or invalid field values that violate Linkline's
constraints, Linkline starts with an empty address book on the next run. If possible, Linkline also creates a
timestamped backup of the corrupted file in the same folder before continuing.<br>
Not every manual edit mistake will trigger this recovery path. For example, missing optional fields such as `notes`,
`logs`, or `tags` may still be defaulted during loading instead of causing a backup to be created. Even though Linkline
attempts to create a backup when loading fails, it is still best to make your own backup before editing the file
manually.
</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install Linkline on the other computer and replace the new `data/linkline.json` file with the one from your
previous Linkline home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen and later switch to using only the
   primary screen, the GUI may open off-screen. Delete `preferences.json` before starting Linkline again.
2. **If you minimize the Help Window** and then run `help` again, the original Help Window remains minimized and no new
   Help Window appears. Restore the minimized Help Window manually.
3. **Selection highlight is cosmetic and has no function**. The highlight is a remnant of the original AB3 codebase and
   does not control the details panel. The details panel updates based on commands like `view`, `add`, `edit`, `delete`,
   etc. Clicking on the displayed client list has no effect. The highlight may shift unexpectedly after commands like
   `edit`, `add`, or `list`, but this is harmless and does not affect functionality. For example, after editing a client
   out of a filtered list, the highlight may move to another client, but this does not mean the highlighted client
   should now be shown in the details panel. Future versions may remove or repurpose this highlight.
4. **Tag capitalisation cannot be updated globally**. Tags are case-insensitive for duplicate detection and identity,
   but there is currently no way to change the capitalisation of an existing tag across all clients. For example, a tag
   created as `plumbing` cannot be updated to `Plumbing` without deleting and recreating it for each affected client.

--------------------------------------------------------------------------------------------------------------------

## Command summary

| Action                | Format, Examples                                                                                                                                                                                                                         |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Add**               | `add --name=NAME --phone=PHONE_NUMBER --email=EMAIL --address=ADDRESS [--notes=NOTES] [--tag=TAG]...`<br>Example: `add --name=John Tan --phone=9123 4567 --email=johntan@example.com --address=123 Clementi Rd, #04-05 --tag=AC-Service` |
| **Clear**             | `clear`                                                                                                                                                                                                                                  |
| **Copy Address**      | `copyaddr INDEX`<br>Example: `copyaddr 1`                                                                                                                                                                                                |
| **Copy Edit Command** | `copyedit INDEX`<br>Example: `copyedit 1`                                                                                                                                                                                                |
| **Delete**            | `delete INDEX`<br>Example: `delete 2`                                                                                                                                                                                                    |
| **Delete Tag**        | `deletetag TAG_NAME`<br>Example: `deletetag plumbing`                                                                                                                                                                                    |
| **Edit**              | `edit INDEX [--name=NAME] [--phone=PHONE_NUMBER] [--email=EMAIL] [--address=ADDRESS] [--notes=NOTES] [--tag=TAG]...`<br>Example: `edit 2 --phone=91234567 --notes=Client requested morning slot`                                         |
| **Exit**              | `exit`                                                                                                                                                                                                                                   |
| **Filter Tag**        | `filtertag --tag=[TAG] [--tag=[MORE_TAGS]]...`<br>Example: `filtertag --tag=Plumbing --tag=Electrical Wiring`                                                                                                                            |
| **Find**              | `find [--name=SUBNAME]... [--phone=SUBNUMBER]... [--email=SUBEMAIL]... [--address=SUBADDRESS]... [--tag=[TAG]]...`<br>Example: `find --name=Alex --tag=AC-Service`                                                                       |
| **Help**              | `help`                                                                                                                                                                                                                                   |
| **List**              | `list`                                                                                                                                                                                                                                   |
| **Log Add**           | `logadd INDEX LOG_MESSAGE`<br>Example: `logadd 1 Completed AC servicing and replaced filter.`                                                                                                                                            |
| **Log Delete**        | `logdelete CLIENT_INDEX LOG_INDEX`<br>Example: `logdelete 2 1`                                                                                                                                                                           |
| **Rename Tag**        | `renametag --tag=OLD_TAG --tag=NEW_TAG`<br>Example: `renametag --tag=AC-Service --tag=AC-Repair`                                                                                                                                         |
| **View**              | `view INDEX`<br>Example: `view 1`                                                                                                                                                                                                        |

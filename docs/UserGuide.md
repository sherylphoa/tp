---
  layout: default.md
    title: "User Guide"
    pageNav: 3
---

# Linkline User Guide

Linkline is a **desktop app for solo technicians to manage client details, optimized for use via a Command Line
Interface** (CLI) while still having the benefits of a Graphical User Interface (GUI). If you can type fast, Linkline
can get your contact management tasks done faster than traditional GUI apps.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure you have Java `17` or above installed in your Computer.<br>
   **Mac users:** Ensure you have the precise JDK version
   prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2526S2-CS2103-F09-4/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for Linkline.

1. Open a command terminal, `cd` into the folder you put the jar file in, and use the `java -jar linkline.jar` command
   to run the application.<br>
   A GUI similar to the below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type the command in the command box and press Enter to execute it. e.g. typing **`help`** and pressing Enter will
   open the help window.<br>
   Some example commands you can try:

    * `list` : Lists all contacts.

    * `add --name=John Doe --phone=98765432 --email=johnd@example.com --address=John street, block 123, #01-01` : Adds a
      contact named `John Doe` to the Address Book.

    * `delete 3` : Deletes the 3rd contact shown in the current list.

    * `clear` : Deletes all contacts.

    * `exit` : Exits the app.

1. Refer to the [Features](#features) below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  e.g. in `add --name=NAME`, `NAME` is a parameter which can be used as `add --name=John Doe`.

* Items in square brackets are optional.<br>
  e.g `--name=NAME [--tag=TAG]` can be used as `--name=John Doe --tag=friend` or as `--name=John Doe`.

* Items with `…`​ after them can be used multiple times including zero times.<br>
  e.g. `[--tag=TAG]…​` can be used as ` ` (i.e. 0 times), `--tag=friend`, `--tag=friend --tag=family` etc.

* Parameters can be in any order except for `renametag`.<br>
  e.g. if the command specifies `--name=NAME --phone=PHONE_NUMBER`, `--phone=PHONE_NUMBER --name=NAME` is also
  acceptable.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines
  as space characters surrounding line-breaks may be omitted when copied over to the application.
  </box>

### Viewing help : `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`

* This command does not accept any arguments. 
* Entering `help` with additional parameters (e.g., `help 123`) will return an error message.

### Adding a person: `add`

Adds a person to the address book. List is automatically sorted lexicographically by `NAME`, followed by `PHONE_NUMBER`

* Person with the same email or phone number as an existing person in the address book cannot be added as they are
  considered duplicated persons.

Format: `add --name=NAME --phone=PHONE_NUMBER --email=EMAIL --address=ADDRESS [--tag=TAG]…​ [--notes=NOTES]`

<box type="tip" seamless>

**Tip:** A person can have any number of tags (including 0) and 1 or 0 notes.
</box>

Examples:

* `add --name=John Doe --phone=9876-5432 --email=johnd@example.com --address=John street, block 123, #01-01`
* `add --name=Betsy Crowe --tag=AC service --email=betsycrowe@example.com --address=123 Clementi Rd #04-05 --phone=9123 4567 --notes=Gate code 1234, beware of large dog`

### Listing all persons : `list`

Shows a sorted list of all persons in the address book.

Format: `list`
* This command does not accept any arguments.
* Entering `list` with additional parameters (e.g., `list 123`) will return an error message.

### Editing a person : `edit`

Edits an existing person in the address book.

Format:
`edit INDEX [--name=NAME] [--phone=PHONE_NUMBER] [--email=EMAIL] [--address=ADDRESS] [--tag=TAG]…​ [--notes=NOTES]`

* Edits the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, the existing tags of the person will be removed i.e adding of tags is not cumulative.
* You can remove all the person’s tags or notes by typing `--tag=` or `--notes=` respectively without specifying any
  tags after it.

Examples:

* `edit 1 --phone=91234567 --email=johndoe@example.com` Edits the phone number and email address of the 1st person to be
  `91234567` and `johndoe@example.com` respectively.
* `edit 2 --name=Betsy Crower --tag=` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing
  tags.

### Locating persons by name: `find`

Finds persons whose names and phone numbers contain any of the given keywords. If there is more than one person, the
list returned is sorted.

Format: `find [--name=NAME_KEYWORD…​] [--phone=PHONE_KEYWORD…​]`

* At least one of the optional fields must be provided.
* Multiple keywords can be provided, separated by spaces
* The search is case-insensitive. e.g `hans` will match `Hans`
* The order of the keywords does not matter. e.g. `Hans Bo` will match `Bo Hans`
* Only name and phone number is searched.
* Persons matching at least one keyword will be returned (i.e. `OR` search). This also applies to phone number.
  e.g. `Hans Bo` will return `Hans Gruber`, `Bo Yang`

Examples:

* `find --name=John` returns `john` and `John Doe`
* `find --name=Alex --phone=1234` returns persons named `Alex` and persons with `1234` in their phone number
* `find --name=alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex bernice'](images/findAlexBerniceResult.png)

### Deleting a person : `delete`

Deletes the specified person from the address book with confirmation.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​
* **Two-step confirmation**: You will be prompted to confirm the deletion by typing the same command again.
* Any other command typed after the first `delete` will cancel the pending deletion.

Examples:

* `list` followed by `delete 2`
    * Shows confimation message with the person's details.
    * Typing `delete 2` again confirms and deletes the 2nd person.
* `find Betsy` followed by `delete 1`
    * Shows confimation message for the 1st person in the search results.
    * Typing `delete 1` again deletes that person.
* `delete 1` followed by `list`
    * The pending deletion is cancelled. The list command executes normally.

### Copying a person's address: `copyaddr`

Copies the specified person's address from the address book.

Format: `copyaddr INDEX`

* Copies the person's address at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​

### Viewing client details: `view`

Shows the specified person's full details.

Format: `view INDEX`

* Shows the person's full details at the specified `INDEX` in the right-hand panel.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​

### Filtering clients by tags: `filter`

Finds persons whose tags contain all the given keywords. If there is more than one person, the list returned is sorted.

Format: `filter --tag=TAG_KEYWORD [--tag=MORE_KEYWORDS]…​`

* Multiple keywords can be provided, separated by spaces
* The filter is case-insensitive. e.g `Plumbing` will match `plumbing`
* Only filters by tags.
* Only persons matching all one keyword will be returned (i.e. `AND` search).

### Renaming a tag: `renametag`

Renames an existing tag to a new name across the entire address book. All clients with the old tag will be updated with the new tag name.

Format: `renametag --tag=OLD_TAG --tag=NEW_TAG`

* Tags have to be in this specific order and only 2 parameters are accepted.
* The `OLD_TAG` must exist in the address book.
* The `NEW_TAG` must be a valid tag name and should not already exist in the address book.
* Tag names are case-insensitive. e.g., `PLUMBING` and `plumbing` are considered the same tag.

Examples:

* `renametag --tag=AC-Service --tag=Aircon-Repair` renames all instances of `AC-Service` to `Aircon-Repair`.
* `renametag --tag=plumbing --tag=General-Maintenance` renames the `plumbing` tag to `General-Maintenance`.

### Deleting a tag: `deletetag`

Deletes a specific tag and removes it from all clients currently having it.

Format: `deletetag TAG_NAME`

* The `TAG_NAME` must exist in the address book. 
* This operation cannot be undone.

### Clearing all entries : `clear`

Clears all entries from the address book.

Format: `clear`
* This command does not accept any arguments.
* Entering `clear` with additional parameters (e.g., `clear 123`) will return an error message.

### Exiting the program : `exit`

Exits the program.

Format: `exit`
* This command does not accept any arguments.
* Entering `exit` with additional parameters (e.g., `exit 123`) will return an error message.

### Saving the data

LinkLine data are saved in the hard disk automatically after any command that changes the data. There is no need to save
manually.

### Editing the data file

LinkLine data are saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are
welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes to the data file makes its format invalid, LinkLine will discard all data and start with an empty data
file at the next run. Hence, it is recommended to take a backup of the file before editing it.<br>
Furthermore, certain edits can cause the LinkLine to behave in unexpected ways (e.g., if a value entered is outside the
acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another Computer?<br>
**A**: Install the app in the other computer and overwrite the empty data file it creates with the file that contains
the data of your previous LinkLine home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only
   the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the
   application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut
   `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to
   manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

 Action           | Format, Examples                                                                                                                                                                                                                                                                      
------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 **Add**          | `add --name=NAME --phone=PHONE_NUMBER --email=EMAIL --address=ADDRESS [--tag=TAG]…​ [--notes=NOTES]` <br> e.g., `add --name=James Ho --phone=22224444 --email=jamesho@example.com --address=123, Clementi Rd, 1234665 --tag=AC-Service --tag=Plumbing --notes=Prefers morning visits` 
 **Clear**        | `clear`                                                                                                                                                                                                                                                                               
 **Copy Address** | `copyaddr INDEX`<br> e.g., `copyaddr 1`                                                                                                                                                                                                                                               
 **Delete**       | `delete INDEX`<br> e.g., `delete 3`                                                                                                                                                                                                                                                   
 **Delete Tag**   | `deletetag TAG_NAME`<br> e.g., `deletetag plumbing`                                                                                                                                                                                                                                  
 **Edit**         | `edit INDEX [--name=NAME] [--phone=PHONE_NUMBER] [--email=EMAIL] [--address=ADDRESS] [--tag=TAG]…​ [--notes=NOTES]`<br> e.g.,`edit 2 --name=James Lee --email=jameslee@example.com`                                                                                                   
 **Filter**       | `filter --tag=TAG_KEYWORD [--tag=MORE_KEYWORDS]…​`<br> e.g., `filter --tag=Plumbing`                                                                                                                                                                                                     
 **Find**         | `find [--name=KEYWORD [MORE_KEYWORDS]] [--phone=NUMBER [MORE_NUMBERS]]`<br> e.g., `find --name=James Jake`                                                                                                                                                                                   
 **Help**         | `help`                                                                                                                                                                                                                                                                                
 **List**         | `list`                                                                                                                                                                                                                                                                                
 **Rename Tag**   | `renametag --tag=OLD_TAG --tag=NEW_TAG`<br> e.g., `renametag --tag=AC-Service --tag=Aircon-Repair`                                                                                                                                                                                     
 **View**         | `view INDEX`<br> e.g., `view 1`                                                                                                                                                                                                                                                       

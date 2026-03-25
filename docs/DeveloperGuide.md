---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# LinkLine Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

This project is based on the [AddressBook-Level 3](https://se-education.org/addressbook-level3/) project created by the [SE-EDU initiative](https://se-education.org).

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

Given below is a quick overview of main components and how they interact with each other.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* implements its functionality using a concrete `{Component Name}Manager` class (which follows the corresponding API `interface` mentioned in the previous point.

For example, the `Logic` component defines its API in the `Logic.java` interface and implements its functionality using the `LogicManager.java` class which follows the `Logic` interface. Other components interact with a given component through its interface rather than the concrete class (reason: to prevent outside component's being coupled to the implementation of a component), as illustrated in the (partial) class diagram below.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` that is made up of parts e.g.`CommandBox`, `ResultDisplay`, `PersonListPanel`, `StatusBarFooter` etc. All these, including the `MainWindow`, inherit from the abstract `UiPart` class which captures the commonalities between classes that represent parts of the visible GUI.

The `UI` component uses the JavaFx UI framework. The layout of these UI parts are defined in matching `.fxml` files that are in the `src/main/resources/view` folder. For example, the layout of the [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java) is specified in [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml)

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component, as it displays `Person` object residing in the `Model`.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, it is passed to an `AddressBookParser` object which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above (for simplicity), in the code it can take several interactions (between the command object and the `Model`) to achieve.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned back from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name e.g., `AddCommandParser`) which uses the other classes shown above to parse the user command and create a `XYZCommand` object (e.g., `AddCommand`) which the `AddressBookParser` returns back as a `Command` object.
* All `XYZCommandParser` classes (e.g., `AddCommandParser`, `DeleteCommandParser`, ...) inherit from the `Parser` interface so that they can be treated similarly where possible e.g, during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` and `Tag` objects (which are contained in `UniquePersonList` and a `UniqueTagList` objects respectively).
* stores each `Person`'s `LogHistory`, which contains zero or more `LogEntry` records.
* stores the currently 'selected' `Person` objects (e.g., results of a search query) as a separate _filtered_ list which is exposed to outsiders as an unmodifiable `ObservableList<Person>` that can be 'observed' e.g. the UI can be bound to this list so that the UI automatically updates when the data in the list change.
* stores a `UserPref` object that represents the user’s preferences. This is exposed to the outside as a `ReadOnlyUserPref` objects.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* inherits from both `AddressBookStorage` and `UserPrefStorage`, which means it can be treated as either one (if only the functionality of only one is needed).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Field validation constraints

#### Implementation

Field-level validation is enforced in the model layer (`Name`, `Phone`, `Email`, `Address`, `Tag`, `Notes`) and reused by parsers via `ParserUtil`.
All user-provided values are trimmed in `ParserUtil` before validation.

| Field | Constraint summary                                                                                                                            |
| --- |-----------------------------------------------------------------------------------------------------------------------------------------------|
| `Name` | 1 to 100 printable characters, and cannot be blank (`Name#VALIDATION_REGEX`).                                                                 |
| `Phone` | Must contain 3 to 15 digits in total; spaces and hyphens are allowed only between digit groups (`Phone#VALIDATION_REGEX`).                    |
| `Email` | Enforces a stricter `local-part@domain` format where local-part and domain labels follow explicit character rules (`Email#VALIDATION_REGEX`). |
| `Address` | Must not be blank (first non-whitespace character required).                                                                                  |
| `Tag` | 1 to 50 printable characters, and cannot be blank (`Tag#VALIDATION_REGEX`).                                                                   |
| `Notes` | Optional free text with max length 200 characters (`Notes#MAX_LENGTH`).                                                                       |

This keeps validation centralized and consistent for both command execution and JSON deserialization.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, configuration, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [Configuration guide](Configuration.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a solo (one-person) residential on-site service technician (e.g., aircon cleaning / handyman / appliance servicing)
* manages repeat customers and needs fast recall of client contact + exact service-location details
* often works from a laptop/desktop between jobs and prefers a fast, typing-first workflow
* can type quickly and prefers keyboard shortcuts / CLI-style commands over mouse-heavy interactions
* needs to store service-location context to avoid mistakes on-site

**Value proposition**: A fast, typing-first address book that helps solo residential service technicians avoid mistakes and wasted time by keeping client contact details tightly coupled with service-location context, searchable in seconds.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​                             | I want to …​                                                                 | So that I can…​                                                                 |
|----------|--------------------------------------|------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| `* * *`  | solo residential service technician   | add a new client with name, phone, email, and service address details        | record the exact contact details and service-location details                  |
| `* * *`  | solo residential service technician   | delete a client record                                                       | remove one-off customers who are not likely to become repeat customers         |
| `* * *`  | solo residential service technician   | find a client by name                                                        | locate the correct client quickly                                              |
| `* * *`  | solo residential service technician   | view a list of clients stored                                                | get an overview of my business                                                 |
| `* * *`  | solo residential service technician   | keep data in a human-editable local file that the app can load               | access client information between sessions                                     |
| `* *`    | solo residential service technician   | update a client's contact details                                            | keep client records accurate when details change                               |
| `* *`    | solo residential service technician   | have the list view sorted by name (lexicographic order)                      | quickly skim the list to find certain clients                                  |
| `* *`    | solo residential service technician   | find a specific client's details quickly (e.g., by typing part of an address)| retrieve the correct client even if I don't remember their name                |
| `* *`    | solo residential service technician   | add special requirements or precautions specified by each client             | avoid mistakes and prepare properly before a visit                              |
| `* *`    | solo residential service technician   | see a compact view when listing clients and view full details only when I select a client | scan my list quickly without losing access to details                    |
| `* *`    | solo residential service technician   | copy a client's service address to the clipboard                             | paste it quickly into maps or other apps                                       |
| `* *`    | solo residential service technician   | append a timestamped service note to a client's record as a visit log        | track what was done previously and follow up correctly                         |
| `* *`    | solo residential service technician   | sort the contact list by most recent interaction                             | prioritize clients I worked with recently                                      |
| `* *`    | solo residential service technician   | attach tags to a client                                                      | recognize customer types or service types at a glance                           |
| `* *`    | solo residential service technician   | filter clients by tag                                                        | narrow down to the relevant subset                                             |
| `* *`    | solo residential service technician   | see a "Today's Visits" list when I tag clients with a date and remove them from the list when done | manage my daily visits quickly                                   |
| `*`      | new user                              | start with sample data on first launch                                       | understand how the app is supposed to look without entering everything first   |
| `*`      | solo residential service technician   | be prompted by the app when I want to delete a client with service history   | avoid accidentally losing important past records                               |
| `*`      | solo residential service technician   | group tags into larger categories                                            | categorize clients and services more systematically                             |
| `*`      | solo residential service technician   | update or rename a tag globally (cascading)                                  | keep my tagging consistent when I change my conventions                        |
| `*`      | solo residential service technician   | delete a tag globally (cascading)                                            | keep my tagging consistent when I change my conventions                        |
| `*`      | solo residential service technician   | pin frequent clients to a "Favorites" quick-access list                      | view my regular clients with a single command                                  |
| `*`      | solo residential service technician   | store multiple service locations under one client                             | handle clients who have more than one address (e.g., home + property)          |
| `*`      | solo residential service technician   | set one service location as the default                                      | quickly use the most common address without extra steps                        |
| `*`      | solo residential service technician   | record the brand and model of an appliance for a client                      | prepare the right tools or parts before visiting                               |
| `*`      | solo residential service technician   | see the list of clients visited within a certain time period                 | manage clients based on loyalty or recency                                     |
| `*`      | solo residential service technician   | set a warranty end date for a specific repair                                | know whether a follow-up is still under warranty                               |
| `*`      | solo residential service technician   | archive or hide inactive clients                                             | reduce clutter while keeping records for reference                             |
| `*`      | solo residential service technician   | clear all data                                                               | reset the app completely when needed (e.g., for demo or testing)               |
| `*`      | solo residential service technician   | be warned when adding a client that looks like a duplicate                   | avoid creating repeated records accidentally                                   |
| `*`      | solo residential service technician   | merge two client records                                                     | combine duplicates into a single correct record                                |
| `*`      | solo residential service technician   | import clients from other files into my existing list                        | bring over contacts from older tools or files quickly                          |
| `*`      | solo residential service technician   | undo or redo recent actions                                                  | recover from mistakes quickly                                                  |

*{More to be added}*

### Use cases

(For all use cases below, `Linkline` is the system, and the `user` is the sole Actor, unless specified otherwise)

#### Use Case: UC01 - Add new client
**System:** `Linkline`
**Actor:** `user`
**Guarantees:**
* The number of clients is either unchanged (unsuccessful) or incremented (successful).

**MSS**
1. `user` requests to add a client.
2. `user` fills in the details (name, phone, email, address, with optional tag and notes field) of the person on the CLI.
3. `Linkline` creates a new client, inserts them into the list in lexicographical order by name and displays the details on the GUI.
4. Use case ends.

**Extensions**
* 2a. Name provided is invalid by criteria given in feature specification.
    * `Linkline` returns error message informing `user` what criteria the name must meet.
    * Use case ends.
* 2b. Phone number provided is invalid by criteria given in feature specification.
    * `Linkline` returns error message informing `user` what criteria the phone number must meet.
    * Use case ends.
* 2c. Email address provided is invalid by criteria given in feature specification.
    * `Linkline` returns error message informing `user` what criteria the email address must meet.
    * Use case ends.
* 2d. Address provided is invalid by criteria given in feature specification.
    * `Linkline` returns error message informing `user` what criteria the address must meet.
    * Use case ends.
* 2e. Tag provided is invalid by criteria given in feature specification.
    * `Linkline` returns error message informing `user` what criteria the tag must meet.
    * Use case ends.
* 2f. Notes provided is invalid by criteria given in feature specification.
    * `Linkline` returns error message informing `user` what criteria the notes must meet.
    * Use case ends.

#### Use Case: UC02 - Search for a client
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` enters one or multiple words as a search query.
2. `Linkline` uses the query provided to filter and list (lexicographically) the clients whose name match the query.
3. Use case ends.

**Extensions**
* 1a. The search query does not match against any of the clients' names.
    * `Linkline` returns an empty page that informs `user` no matching clients were found.
    * Use case ends.
* 1b. No search query was provided.
    * `Linkline` returns error message informing `user` that at least one word must be provided as a search query.
    * Use case ends.

#### Use Case: UC03 - Navigate to client address.
**System:** `Linkline`
**Actor:** `user`, User's operating system `os`, and mapping software `map`
**Preconditions:** A client address must be a valid address that `map` can parse.

**MSS**
1. `user` copies client address into `os` via `Linkline` command by specifying index of client.
2. `user` pastes the client address into `map`.
3. `user` follows the instructions given by `map` to go to the client address.
4. Use case ends.

**Extensions**
* 1a. The index given is invalid, and does not point to a client.
    * `Linkline` returns an error that informs `user` the index given is invalid.
    * Use case ends.
* 2a. `Linkline` fails to copy client address into `os` clipboard, for whatever reason.
    * `Linkline` returns message informing `user` to copy the client address manually.
    * `user` <ins>searches for the client (UC02)</ins>.
    * `user` highlights client address and copies it.
    * Use case resumes from step 2.

#### Use Case: UC04 - Change client phone number
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` edits a client's detail via `Linkline` command by specifying their index and new phone number, and re-sorts the list to maintain lexicographical order.
2. `Linkline` displays new fields of the updated client.
3. Use case ends.

**Extensions**
* 1a. The index given is invalid, and does not point to a client.
    * `Linkline` returns an error that informs `user` the index given is invalid.
    * Use case ends.
* 2a. Provided phone number is same as phone number of another client.
    * `Linkline` returns error message informing `user` that the phone number is already in use.
    * Use case ends.

#### Use Case: UC05 - Delete a client
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` enters delete command with the index of the target client.
2. `Linkline` displays the client's details and ask for confirmation.
3. `user` enters the same delete command again.
4. `Linkline` deletes the client and confirms the deletion.
5. Use case ends.

**Extensions**
* 1a. The index given is invalid (not a positive integer or out of range).
    * `Linkline` returns an error message showing that the input index is invalid to the `user`.
    * Use case ends.
* 2a. `user` enters any other command instead of confirming.
    * `Linkline` cancels the pending deletion and executes the new command normally.
    * Use case ends.

#### Use Case: UC06 - List Clients
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` requests to view all client added in `Linkline` via command.
2. `Linkeline` shows list with all clients sorted lexicographically by name.
3. Use case ends.

**Extensions**
* 1a. There are no client added yet.
    * `Linkline` returns an error message to remind `user` to add at least one client before listing them.
    * Use case ends.

#### Use Case: UC07 - View client detail
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` confirms personal detail of a specific client via `Linkline` by specifying the index of target client.
2. `Linkline` shows full details of the client including name, phone, email, full address and precautions.
3. Use case ends.

**Extensions**
* 1a. The index given is invalid (not a positive integer or out of range).
    * `Linkline` returns an error message showing that the input index is invalid to the `user`.
    * Use case ends.

#### Use Case: UC08 - Edit existing client details
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` edit client detail by adding new tag(s) or/and notes via `Linkline` by specifying client index, tag content or/and notes content.
2. `Linkline` displays new fields of the updated client.
3. Use case ends.

**Extensions**
* 1a. The index given is invalid (not a positive integer or out of range).
    * `Linkline` returns an error message showing that the input index is invalid to the `user`.
    * Use case ends.
* 1a. No field is provided.
    * `Linkline` returns an error message to inform `user` adding at least one field.
    * Use case ends.

#### Use Case: UC09 - Filter clients by tags
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` enters one or multiple keywords as a filter query.
2. `Linkline` uses the query provided to filter and list (lexicographically) the clients who have the specified tags that match the query.
3. Use case ends.

**Extensions**
* 1a. The filter query does not match against any of the clients' tags.
    * `Linkline` returns an empty page that informs `user` no matching clients were found.
    * Use case ends.
* 1b. No filter query was provided.
    * `Linkline` returns error message informing `user` that at least one word must be provided as a filter query.
    * Use case ends.
* 1c. Filter query provided is invalid by criteria given in feature specification.
    * `Linkline` returns error message informing `user` what criteria the query must meet.
    * Use case ends.

#### Use Case: UC10 - Rename a tag
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` requests to rename an existing tag to a new name.
2. `Linkline` renames the tag in the global tag list.
3. `Linkline` updates all clients currently having the old tag to reflect the new tag name.
4. Use case ends.

**Extensions**
* 1a. The target tag name does not exist in the address book.
    * `Linkline` returns an error message informing the `user` that the tag was not found.
    * Use case ends.
* 1b. The new tag name already exists in the address book.
    * `Linkline` returns an error message informing the `user` that the new tag name is a duplicate.
    * Use case ends.

#### Use Case: UC11 - Delete a tag
**System:** `Linkline`
**Actor:** `user`

**MSS**
1. `user` requests to delete a specific tag by name.
2. `Linkline` removes the tag from the global tag list.
3. `Linkline` removes the tag from all clients with that tag.

**Extensions**
* 1a. The tag name provided does not exist. 
    * `Linkline` returns an error message informing the `user` that the tag was not found. 
    * Use case ends.

   *{More to be added}*

### Non-Functional Requirements

1. Portability: Should work on any _mainstream OS_ as long as it has Java `17` or above installed.
2. Capacity & Performance: Should be able to hold up to 1000 persons without a noticeable sluggishness in performance for typical usage.
3. Usability (Efficiency): A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4. Data Integrity (Duplicate Prevention): The system must prevent duplicate client entries by enforcing uniqueness on normalized phone number and normalized email address. An attempt to add or edit a client resulting in a duplicate must be rejected with a clear error message.
5. Data Persistence (Safety): All data-modifying commands (`add`, `delete`, `edit`) must trigger an automatic save to the local JSON file. The save mechanism must use a safe write strategy (e.g., write to temp file then rename) to prevent data corruption in case of a system crash during write.
6. Fault Tolerance (Load Failure): If the data file is missing, corrupted, or in an invalid format on startup, the application must not crash.
7. Performance (Command Response): Every user command that does not modify data (e.g., find, list, view) should display the result within 500 milliseconds for a database of up to 1000 clients.
8. Performance (Startup Latency): The application should be ready for user input within 3 seconds on a standard hardware configuration (e.g., a laptop from the last 5 years) with a dataset of up to 500 clients.
9. Usability (Learnability): A first-time user who has never used a command-line interface before should be able to understand the basic workflow (add, list, find) within 10 minutes, aided by sample data and a comprehensive help command.
10. Consistency (User Experience): The application must strictly adhere to the command format, parameter rules, and error messages defined in the functional specification to ensure a predictable and reliable user experience.
11. Constraint (Scope): The system is designed exclusively for a solo technician. Features requiring multi-user access, cloud synchronization, or network communication are explicitly out of scope for the MVP.
12. Constraint (Storage Format): Data persistence is limited to a human-editable JSON file. No external database systems (e.g., MySQL, PostgreSQL) shall be used.
13. Accessibility (Error Clarity): All error messages must be user-friendly and actionable, specifying exactly what went wrong and how to correct it, rather than displaying technical stack traces or cryptic codes.

### Glossary

* **Client / Person**: The primary entity in the address book. Mandatory fields include Name, Phone, Email, and Address. Optional fields include Tags and Notes.
* **CLI (Command Line Interface)**: A text-based interface where the user interacts with the application by typing commands rather than using a mouse.
* **Compact View**: A display mode in the GUI where client records are shown with limited information (name and phone only), allowing the user to scan many records at once without excessive scrolling.
* **Duplicate Client**: Two clients are considered duplicates if they share the same normalized phone number or normalized email address.
* **Full View**: A details panel (or an expanded display mode) where all information for a specific client (name, phone, email, full address, tags and notes) is visible to the user.
* **JSON (JavaScript Object Notation)**: The lightweight, text-based data format used by the Storage component to persist data to the hard disk.
* **Mainstream OS**: Widely used operating systems like Windows, Linux, Unix, MacOS
* **Service-Location Context**: Precise physical details about a client's address (e.g., precise address, access instructions, precautions, or special requirements) critical for an on-site technician.
* **Solo Technician**: The target user of the app.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</box>

### Launch and shutdown

1. Initial launch

   1. Download the jar file and copy into an empty folder

   1. Double-click the jar file Expected: Shows the GUI with a set of sample contacts. The window size may not be optimum.

1. Saving window preferences

   1. Resize the window to an optimum size. Move the window to a different location. Close the window.

   1. Re-launch the app by double-clicking the jar file.<br>
       Expected: The most recent window size and location is retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command. Multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: Expected: No person is deleted. Confirmation message with the person's details is shown. Status bar remains the same. Pending deletion state is set for index 1.

   1. Test case: `delete 1` (immediately after the above) <br>
      Expected: First contact is deleted from the list. Details of the deleted contact shown in the status message. Timestamp in the status bar is updated.

   1. Test case: `delete 1` then `list` then `delete 1` <br>
      Expected: First `delete 1` shows confirmation. `list` cancels the pending deletion. Second `delete 1` shows confirmation again (not auto-deleted).
   
   1. Test case: `delete 0`<br>
      Expected: No person is deleted. Error details shown in the status message. Status bar remains the same.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{explain how to simulate a missing/corrupted file, and the expected behavior}_

1. _{ more test cases …​ }_

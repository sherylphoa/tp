---
layout: default.md
title: "Developer Guide"
pageNav: 3
---

# Linkline Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

This project is based on the [AddressBook-Level 3](https://se-education.org/addressbook-level3/) project created by
the [SE-EDU initiative](https://se-education.org).

### AI Assistance
* [Shim Jaejun](AboutUs.html#shim-jaejun) used Codex in a limited assistive role for parts of the Notes
  and Logs features. This included exploring JavaFX implementation approaches for UI components that display notes and
  logs, and drafting initial versions of Notes- and Logs-related test cases from manually specified scenarios. All
  suggestions were reviewed, adapted, and refined manually before inclusion in the project.
* [Tan Kin Ru](AboutUs.html#tan-kin-ru) used DeepSeek in a limited assistive role for parts of the test cases for some features implemented, including `copyaddr`, `copyedit`, `delete`, `edit`, and pending action confirmation flows. This included identifying edge cases and suggesting test scenarios to increase coverage. All suggestions were reviewed, adapted, and refined manually before inclusion in the project.
* [Sheryl Phoa](AboutUs.html#sheryl-phoa) used Gemini in a limited assistive role for parts of the development and test
  cases of the `filtertag`, `renametag` and `deletetag` features. This included suggesting test scenarios to increase
  test coverage. All suggestions were reviewed, adapted and refined manually before inclusion in the project.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** above shows Linkline's high-level structure.

Given below is a quick overview of main components and how they interact with each other.

**Main components of the architecture**

**`Main`** (consisting of classes `Main` and `MainApp`) is in charge of the app launch and shutdown.

* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shutdown, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the app.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the app in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues
the command `add --name=Amy Tan --phone=91234567 --email=amy@example.com --address=123 Clementi Rd`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

This example shows the typical flow of a data-modifying command at component level: `UI` forwards the raw command text
to `Logic`, `Logic` updates `Model`, and the updated state is then persisted through `Storage`.
Feature-specific flows such as confirmation-based commands, chained filtering, and log operations are described in the
[Implementation](#implementation) section.

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* implements its functionality using a concrete `{Component Name}Manager` class (which follows the corresponding API
  `interface` mentioned in the previous point.

For example, the `Logic` component defines its API in the `Logic.java` interface and implements its functionality using
the `LogicManager.java` class which follows the `Logic` interface. Other components interact with a given component
through its interface rather than the concrete class (reason: to prevent outside components being coupled to the
implementation of a component), as illustrated in the (partial) class diagram below.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in 
[`Ui.java`](https://github.com/AY2526S2-CS2103-F09-4/tp/blob/master/src/main/java/seedu/address/ui/Ui.java).

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` made up of smaller parts such as `CommandBox`, `ResultDisplay`, `PersonListPanel`,
`PersonDetailPanel`, and `StatusBarFooter`. All these, including `MainWindow`, inherit from the abstract `UiPart`
class, which captures the common behavior of visible GUI parts.

Linkline's UI is organized around a split-pane workflow:

* `PersonListPanel` renders the current filtered client list in a compact form through `PersonCard`, showing only the
  displayed index, name, and phone number.
* `PersonDetailPanel` renders the currently selected client through `PersonDetailCard`, showing the full contact
  details, notes, tags, and service logs.
* When no client is selected, the details panel shows a placeholder instead of a detail card.

The UI uses the JavaFX framework. The layout of each UI part is defined in a matching `.fxml` file under
`src/main/resources/view`. For example, `MainWindow` is backed by `MainWindow.fxml`.

The `UI` component,

* executes user commands using the `Logic` component.
* observes the filtered client list exposed by `Logic`, so the left pane updates automatically when commands such as
  `list`, `find`, and `filtertag` change the displayed set of clients.
* observes the selected client exposed by `Logic`, so the right pane updates automatically when commands such as `view`
  change which client is currently in focus.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it renders `Person` and `LogEntry` objects residing in the `Model`.

### Logic component

The **API** of this component is specified in
[`Logic.java`](https://github.com/AY2526S2-CS2103-F09-4/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking the first execution of
`execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the first `delete 1` command"/>

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser`, `DeleteCommand` and `CommandResult` should end at the destroy marker
(X) but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, `LogicManager` passes the raw command text to
   `AddressBookParser`.
1. `AddressBookParser` extracts the command word, chooses a matching parser (e.g., `DeleteCommandParser`), and uses it
   to parse the remaining arguments.
1. This produces a `Command` object (more precisely, an object of one of its subclasses such as `DeleteCommand`) which
   is then executed by `LogicManager`.
1. The command may communicate with the `Model` during execution to read or modify in-memory data.
1. The result of the command execution is encapsulated as a `CommandResult` object and returned back from `Logic`.
1. If the `CommandResult` carries a `PendingAction`, `LogicManager` stores it and waits for a matching follow-up
   command instead of completing the action immediately.
1. If the `CommandResult` requires an address book save, `LogicManager` persists the updated address book through
   `Storage` before returning the result to the UI.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:

* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a
  placeholder for the specific command name e.g., `AddCommandParser`) which uses the other classes shown above to parse
  the user command and create a `XYZCommand` object (e.g., `AddCommand`) which the `AddressBookParser` returns back as a
  `Command` object.
* All `XYZCommandParser` classes (e.g., `AddCommandParser`, `DeleteCommandParser`, ...) inherit from the `Parser`
  interface so that they can be treated similarly where possible e.g, during testing.
* Prefix-based commands such as `add`, `edit`, `find`, `filtertag`, and `renametag` rely on `ArgumentTokenizer` and
  `CliSyntax` to parse Linkline's `--field=` format.
* Positional commands such as `view`, `delete`, `copyaddr`, `copyedit`, `logadd`, `logdelete`, and `deletetag`
  parse their arguments directly from the remaining input string.

### Model component

The **API** of this component is specified in
[`Model.java`](https://github.com/AY2526S2-CS2103-F09-4/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores Linkline's client data inside `AddressBook`, which owns a `UniquePersonList` and a `UniqueTagList`.
* stores each `Person`'s `Notes`, `LogHistory`, and tags as part of the domain model.
* stores the currently displayed subset of clients as a separate *filtered* list, which is exposed to outsiders as an
  unmodifiable `ObservableList<Person>`. This allows the client list panel in the UI to update automatically when
  commands such as `list`, `find`, and `filtertag` change the displayed set of clients.
* stores the currently selected `Person` separately from the filtered list. This is exposed to outsiders as
  `ObservableValue<Person>`, which allows the details panel in the UI to update automatically when the selected client
  changes.
* enforces client uniqueness using normalized phone number or case-insensitive email comparison.
* maintains a global tag list derived from the tags currently attached to stored clients.
* stores a `UserPrefs` object that represents the user's preferences. This is exposed to the outside as
  `ReadOnlyUserPrefs`.

The `Model` is still intended to be largely independent of the other main components. However, the current
implementation contains one localized coupling: `SearchPredicate` accepts `ArgumentMultimap` from the `Logic` parsing
layer. This is an implementation shortcut for the current search feature, not an intended architectural dependency.

### Storage component

The **API** of this component is specified in
[`Storage.java`](https://github.com/AY2526S2-CS2103-F09-4/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,

* can save both address book data and user preference data in JSON format, and read them back into corresponding
  objects.
* stores each client's notes, tags, and log history as part of the JSON representation of that client record.
* inherits from both `AddressBookStorage` and `UserPrefsStorage`, which means it can be treated as either one (if only
  the functionality of only one is needed).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects
  that belong to the `Model`).

If address book data is malformed or violates model constraints, the storage layer reports the load failure through `DataLoadingException`. Recovery from such startup failures is handled by `MainApp`, which also attempts to create a backup of the corrupted file when possible.
During deserialization, missing non-required fields such as `notes`, `logs`, and `tags` are defaulted when possible. However, if those fields are present but contain invalid values, they are still treated as malformed data and can cause loading to fail.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### CLI parsing strategy

Linkline currently uses two command-parsing styles:

* Prefix-based commands such as `add`, `edit`, `find`, `filtertag`, and `renametag`
* Positional commands such as `view`, `delete`, `copyaddr`, `copyedit`, `logadd`, `logdelete`, and `deletetag`

`AddressBookParser` first separates the command word from the remaining argument string, then delegates to a dedicated parser for that command. Prefix-based parsers typically rely on `ArgumentTokenizer`, `ArgumentMultimap`, and `CliSyntax` to parse Linkline's `--field=` format.

The current tokenizer has several important characteristics:

* A prefix is only recognized when the tokenizer sees whitespace immediately before that prefix.
* Repeated prefixes are accumulated in insertion order.
* Argument values are trimmed before being stored in `ArgumentMultimap`.
* Empty values are still preserved, which allows commands such as `edit 1 --tag=`, `edit 1 --notes=`, `find --tag=`,
  and `filtertag --tag=` to give command-specific meaning to an intentionally empty field.

In the documentation, command formats use `--field=[VALUE]` when a command accepts such an intentionally empty value.

This design keeps most prefix-based commands consistent, but it also explains several current limitations:

* a field value cannot safely include a space followed by another recognized field marker for the same command
* leading and trailing whitespace in user input cannot be preserved
* the parser does not currently support quoted arguments or escape sequences

This trade-off is intentional. Compared with shorter prefix styles such as `n/` or `p/`, Linkline's current
`--field=` syntax is less likely to collide with normal user-entered text while still remaining manageable to type
frequently. Quoted and escaped input remains a planned enhancement because it would remove several of the current
parsing edge cases and could also make more compact command syntax variants practical in the future.

### Confirmation-based commands with `PendingAction`

Some destructive commands in Linkline require confirmation before they take effect. This behavior is implemented using `PendingAction` and handled centrally by `LogicManager`.

Commands currently using this mechanism include:

* `delete`
* `clear`
* `deletetag`
* `logdelete`

The first execution of such a command does not immediately mutate the model. Instead:

1. The command validates the request and creates a concrete `PendingAction` object.
1. The command returns a `CommandResult` carrying both the confirmation message and the pending action.
1. `LogicManager` stores that pending action instead of attempting any address book save at that stage.

When the next user command arrives, `LogicManager` checks whether it matches the stored pending action:

* If it matches, `LogicManager` calls `PendingAction#complete(model)`, clears the pending action, and then applies the
  normal save policy to the returned `CommandResult`.
* If it does not match, `LogicManager` discards the pending action and executes the new command normally.
* If parsing or command execution fails, `LogicManager` also clears the pending action.

This approach keeps confirmation behavior out of `LogicManager`'s command-dispatch logic while allowing multiple
commands to share the same workflow. The sequence diagram in the Logic component section illustrates the first
`delete 1`, which creates and stores a `DeletePendingAction`.

### Chained `find` and `filtertag`

Linkline treats `find` and `filtertag` as incremental navigation commands. Instead of restarting from the full address
book every time, each command further narrows the currently displayed client list.

This is implemented in `ModelManager` using:

* a `FilteredList<Person>` named `filteredPersons`
* a list of active predicates named `activePredicates`

When `find` or `filtertag` is executed, the command constructs a predicate and appends it through
`addPredicateFilteredPersonList(...)`. When `list` is executed, `ModelManager` clears the accumulated predicates
through `resetPredicatesFilteredPersonList()` and restores the full client list.

This leads to two levels of combination:

* across commands, repeated `find` and `filtertag` operations are combined using logical `AND`
* within one `find`, `SearchPredicate` combines the supplied fields using logical `OR`
* within one `filtertag`, `TagsMatchAllKeywordsPredicate` requires all supplied non-empty tags using logical `AND`

There are also command-specific tag cases:

* in `find`, tag queries use case-insensitive substring matching, while `find --tag=` matches only clients with no tags
* in `filtertag`, `filtertag --tag=` is a special case that matches only clients with no tags
* `FilterTagCommandParser` rejects mixed blank and non-blank tag values in the same `filtertag` command

This behavior is intentional. `find` and `filtertag` are meant to preserve the user's current context while narrowing
results step by step.

Both `FindCommand` and `FilterTagCommand` also clear the selected client if that client is no longer present in the
newly filtered list. This prevents the details panel from showing a client that is no longer visible in the list pane.

### Global tag operations

Linkline treats tags as address-book-level data rather than as isolated strings attached independently to each client. This is why `renametag` and `deletetag` operate globally.

The main logic lives in
`AddressBook`:

* `rebuildTagList()` derives the global `UniqueTagList` from the tags currently attached to all stored clients
* `setTag(...)` renames a tag globally by updating the tag list and recreating every affected `Person` with the new tag
* `removeTag(...)` deletes a tag globally by removing it from the tag list and recreating every affected `Person`
  without that tag

`ModelManager` adds two extra responsibilities on top of that:

* it resets the filtered list after tag rename or delete so the left pane shows all possibly affected clients
* it refreshes the selected client if that client was affected by the tag change, so the details panel shows the
  updated tags immediately

This design centralizes tag-update logic in the model layer and avoids duplicating the same cascading behavior across multiple commands.

### Log history and display indices

Each client stores a `LogHistory`, which is implemented as an immutable, newest-first list of `LogEntry` objects.

This has two important consequences:

* `logadd` creates a new `LogEntry`, produces an updated `LogHistory`, constructs a new `Person`, and replaces the
  original client in the model
* `logdelete` also works by creating a new `LogHistory` and then replacing the original client with an updated `Person`

There is also a deliberate distinction between list order and UI numbering:

* `LogHistory` stores entries newest first
* the details panel displays the newest entry at the top
* however, the UI labels logs as `Log 1`, `Log 2`, ... from oldest to newest through `LogEntryDisplayIndex`

This is intentional. Linkline shows the most recent interaction first, but it keeps log numbers aligned with insertion
order: `Log 1` is the earliest recorded log, and the largest log number is the most recently added one.

This means `logdelete CLIENT_INDEX LOG_INDEX` cannot directly use the displayed log number as the internal list index.
Instead, `LogDeleteCommand` converts the displayed log number into the internal zero-based list index using:

`internal index = total logs - display index`

This conversion ensures that the command deletes the same log entry that the user sees labeled in the UI.

### Persistence policy

Linkline uses separate persistence policies for address book data and user preferences.

#### Address book data

* Successful commands that modify persisted address book data mark their `CommandResult` as save-required.
* `LogicManager` saves `data/linkline.json` only for save-required results. Read-only commands, failed commands, and
  first-stage confirmation results (i.e., the `PendingAction`s) do not trigger an address book save.
* If such a save fails after the in-memory model has already changed, `LogicManager` records that unsaved address book
  changes are still present.
* A later successful address book save clears that unsaved state.
* On graceful shutdown, `MainApp.stop()` makes one best-effort retry only if that unsaved state is still present. Failing of which, it logs the failure and exits without further retries (i.e., the file is not saved).

#### User preferences

* GUI preferences such as window size and position are saved to `preferences.json` on graceful shutdown instead of
  after every command.
* This is acceptable because preferences are session-level state for the next run, while `linkline.json` stores the
  primary user data that should be saved immediately after successful data-changing commands.

### Field validation constraints

Field-level validation is enforced in the model layer (`Name`, `Phone`, `Email`, `Address`, `Tag`, `Notes`,
`LogMessage`) and reused by parsers through `ParserUtil`.
For command input, `ParserUtil` trims leading and trailing whitespace from field values before validation. This means
the length-based limits below apply after boundary whitespace has been removed. `LogAddCommandParser` first separates
the client index from the free-text log message, then passes the message through `ParserUtil.parseLogMessage`, so log
messages follow the same trimming rule as other parser-handled fields.

| Field        | Constraint summary                                                                                                                           |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| `Name`       | 1 to 100 Unicode code points, and cannot be blank (`Name#VALIDATION_REGEX`).                                                       |
| `Phone`      | Must contain 3 to 15 digits in total; spaces and hyphens are allowed only between digit groups (`Phone#VALIDATION_REGEX`).                   |
| `Email`      | Enforces a stricter `local-part@domain` format where local-part and domain labels follow explicit character rules (`Email#VALIDATION_REGEX`). |
| `Address`    | Must not be blank (first non-whitespace character required).                                                                                 |
| `Tag`        | 1 to 50 Unicode code points, and cannot be blank (`Tag#VALIDATION_REGEX`).                                                                   |
| `Notes`      | Optional free text with max length 500 Unicode code points (`Notes#MAX_LENGTH`).                                                                      |
| `LogMessage` | 1 to 1000 Unicode code points (`LogMessage#MIN_LENGTH`, `LogMessage#MAX_LENGTH`).                                                            |

This keeps validation centralized and consistent for both command execution and JSON deserialization.

These constraints are not perfect. For example:
* **Name validation** uses a 1-100 character limit. The minimum prevents blank names, while the maximum accommodates
real-world names (most are under 50 characters). The limit is measured in Unicode code points to support multi-language
names.
* **Phone validation** accepts unrealistic formats like `1 2 2-2` because
it prioritizes flexibility (supporting international formats and readable spacing) over strictness. Country code support
remains a planned enhancement. However, these constraints are sufficient for Linkline's target use case, and invalid
entries can always be corrected later using the `edit` command.

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

* is a solo residential on-site service technician (e.g. aircon servicing, handyman work, appliance repair)
* manages repeat clients and needs each client's contact details, address, notes, and service history in one place
* often works from a laptop or desktop between jobs and prefers a fast keyboard-first workflow
* can type quickly and would rather use commands than navigate mouse-heavy forms
* needs to review past client context quickly before revisits to avoid on-site mistakes

**Value proposition**: Linkline is a keyboard-first desktop address book for solo residential service technicians. It keeps client contact details, service-location notes, tags, and timestamped service logs together so the user can find and work with the right client context quickly between jobs.

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a ...                            | I want to ...                                                                          | So that I can ...                                                |
|----------|-------------------------------------|----------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `* * *`  | solo residential service technician | add a new client with name, phone, email, and service address details                 | record the exact contact details and service-location details   |
| `* * *`  | solo residential service technician | delete a client record                                                                 | remove one-off customers who are not likely to become repeat customers |
| `* * *`  | solo residential service technician | find a client by name or another known detail                                          | locate the correct client quickly                               |
| `* * *`  | solo residential service technician | view a list of stored clients                                                          | get an overview of my business                                  |
| `* * *`  | solo residential service technician | keep data in a human-editable local file that the app can load                         | access client information between sessions                      |
| `* *`    | solo residential service technician | update a client's contact details                                                      | keep client records accurate when details change                |
| `* *`    | solo residential service technician | have the list view sorted by name                                                      | quickly skim the list to find certain clients                   |
| `* *`    | solo residential service technician | find a specific client's details quickly, for example by typing part of an address     | retrieve the correct client even if I do not remember the name  |
| `* *`    | solo residential service technician | add special requirements or precautions specified by each client                       | avoid mistakes and prepare properly before a visit              |
| `* *`    | solo residential service technician | scan a compact client list and view full details only when needed                      | move quickly without losing access to important details         |
| `* *`    | solo residential service technician | copy a client's service address to the clipboard                                       | paste it quickly into maps or other apps                        |
| `* *`    | solo residential service technician | copy a ready-made edit command for an existing client                                  | update a client record with fewer typing mistakes               |
| `* *`    | solo residential service technician | append a timestamped service note to a client's record as a visit log                  | track what was done previously and follow up correctly          |
| `* *`    | solo residential service technician | delete an incorrect log entry                                                          | keep a client's service history accurate                        |
| `* *`    | solo residential service technician | attach tags to a client                                                                | recognize customer types or service types at a glance           |
| `* *`    | solo residential service technician | filter clients by tag                                                                  | narrow down to the relevant subset                              |
| `* *`    | solo residential service technician | see a "Today's Visits" list when I tag clients with a date and remove them when done   | manage my daily visits quickly                                  |
| `*`      | new user                            | start with sample data on first launch                                                 | understand how the app is supposed to look without entering everything first |
| `*`      | solo residential service technician | sort the contact list by most recent interaction                                       | prioritize clients I worked with recently                       |
| `*`      | solo residential service technician | be prompted before deleting a client record                                            | avoid accidentally losing important past records                |
| `*`      | solo residential service technician | group tags into larger categories                                                      | categorize clients and services more systematically             |
| `*`      | solo residential service technician | update or rename a tag globally                                                        | keep my tagging consistent when I change my conventions         |
| `*`      | solo residential service technician | delete a tag globally                                                                  | keep my tagging consistent when I change my conventions         |
| `*`      | solo residential service technician | pin frequent clients to a quick-access list                                            | view my regular clients with a single command                   |
| `*`      | solo residential service technician | store multiple service locations under one client                                      | handle clients who have more than one address                   |
| `*`      | solo residential service technician | set one service location as the default                                                | quickly use the most common address without extra steps         |
| `*`      | solo residential service technician | record the brand and model of an appliance for a client                                | prepare the right tools or parts before visiting                |
| `*`      | solo residential service technician | see the list of clients visited within a certain time period                           | manage clients based on loyalty or recency                      |
| `*`      | solo residential service technician | set a warranty end date for a specific repair                                          | know whether a follow-up is still under warranty                |
| `*`      | solo residential service technician | archive or hide inactive clients                                                       | reduce clutter while keeping records for reference              |
| `*`      | solo residential service technician | clear all data                                                                         | reset the app completely when needed, for example for a demo or testing |
| `*`      | solo residential service technician | be warned when adding a client that looks like a duplicate                             | avoid creating repeated records accidentally                    |
| `*`      | solo residential service technician | merge two client records                                                               | combine duplicates into a single correct record                 |
| `*`      | solo residential service technician | import clients from other files into my existing list                                  | bring over contacts from older tools or files quickly           |
| `*`      | solo residential service technician | undo or redo recent actions                                                            | recover from mistakes quickly                                   |

### Use cases

(For all use cases below, `Linkline` is the system, and the `user` is the sole Actor, unless specified otherwise)

#### Use Case: UC01 - Add new client

**System:** `Linkline`<br>
**Actor:** `user`<br>
**Guarantees:** The number of clients is either unchanged (unsuccessful) or incremented (successful).

**MSS**

1. `user` requests to add a client, providing the client's name, phone number, email address, physical address, and any optional tags or notes.
2. `Linkline` validates the provided details.
3. `Linkline` adds the client to the client list and saves the updated data.<br>
Use case ends.

**Extensions**

* 1a. A field provided is invalid according to the feature specification.
    * `Linkline` returns an error message describing the violated field constraints.<br>
    Use case ends.
* 2a. A duplicate client (client with the same normalized phone number or case-insensitive email address) already exists.
    * `Linkline` returns an error message indicating that the client would be a duplicate.<br>
    Use case ends.

#### Use Case: UC02 - Search for a client

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` specifies one or more search fields and queries.
2. `Linkline` searches the currently displayed client list for clients that match at least one supplied query in the
   specified fields.
3. `Linkline` shows the matching clients.<br>
  Use case ends.

**Extensions**

* 1a. The search command format is invalid.
    * `Linkline` returns the command usage message.<br>
    Use case ends.
* 2a. No clients match the search query.
    * `Linkline` shows an empty client list.<br>
    Use case ends.

#### Use Case: UC03 - Copy client address to clipboard

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` locates the target client in the displayed client list.
2. `user` requests to copy the client's address by specifying the client's index.
3. `Linkline` copies the client's address to the system clipboard and shows success.
4. `user` pastes the address into another application.<br>
  Use case ends.

**Extensions**

* 2a. The index given is invalid (not a positive integer or out of range).
    * `Linkline` returns an error message informing the user that the index is invalid.<br>
    Use case ends.
* 3a. `Linkline` fails to access the system clipboard.
    * `Linkline` returns an error message asking the user to copy the address manually from the displayed client details.<br>
    Use case ends.

#### Use Case: UC04 - Change client phone number

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` locates the target client in the displayed client list.
2. `user` requests to change the client's phone number by specifying the client's index and the new phone number.
3. `Linkline` validates the new phone number and duplicate rules.
4. `Linkline` updates the client's phone number, sorts the list, and saves the updated data.<br>
  Use case ends.

**Extensions**

* 2a. The index given is invalid.
    * `Linkline` returns an error message informing the user that the index is invalid.<br>
    Use case ends.
* 2b. The phone number provided is invalid according to the feature specification.
    * `Linkline` returns an error message describing the phone number constraints.<br>
    Use case ends.
* 3a. The new phone number would duplicate another client's details.
    * `Linkline` returns an error message indicating that the edit would create a duplicate client.<br>
    Use case ends.

#### Use Case: UC05 - Delete a client

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` locates the target client in the displayed client list.
2. `user` requests to delete that client by index.
3. `Linkline` shows a confirmation message identifying the client to be deleted.
4. `user` repeats the same delete command.
5. `Linkline` deletes the client and saves the updated data.<br>
  Use case ends.

**Extensions**

* 2a. The index given is invalid (not a positive integer or out of range).
    * `Linkline` returns an error message informing the user that the index is invalid.<br>
    Use case ends.
* 4a. `user` enters any other command instead of confirming.
    * `Linkline` cancels the pending deletion and executes the new command normally.<br>
    Use case ends.

#### Use Case: UC06 - List clients

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` requests to list all stored clients.
2. `Linkline` shows all clients in sorted order.<br>
  Use case ends.

**Extensions**

* 2a. No clients are stored.
    * `Linkline` shows an empty client list.<br>
    Use case ends.

#### Use Case: UC07 - View client detail

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` locates the target client in the displayed client list.
2. `user` requests to view the client's details by specifying the client's index.
3. `Linkline` shows the full details of the client.<br>
  Use case ends.

**Extensions**

* 2a. The index given is invalid (not a positive integer or out of range).
    * `Linkline` returns an error message informing the user that the index is invalid.<br>
    Use case ends.

#### Use Case: UC08 - Edit client notes or tags

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` locates the target client in the displayed client list.
2. `user` requests to edit the client's tags and/or notes by specifying the client's index and the updated values.
3. `Linkline` validates the updated values.
4. `Linkline` updates the client's record and saves the updated data.<br>
  Use case ends.

**Extensions**

* 2a. The index given is invalid (not a positive integer or out of range).
    * `Linkline` returns an error message informing the user that the index is invalid.<br>
    Use case ends.
* 2b. No editable field is provided.
    * `Linkline` returns an error message indicating that at least one field must be edited.<br>
    Use case ends.
* 2c. A tag provided is invalid according to the feature specification.
    * `Linkline` returns an error message describing the tag constraints.<br>
    Use case ends.
* 2d. Notes provided are invalid according to the feature specification.
    * `Linkline` returns an error message describing the notes constraints.<br>
    Use case ends.

#### Use Case: UC09 - Filter clients by tags

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` enters one or more tags as a filter query.
2. `Linkline` filters the currently displayed client list to clients that match all specified tags.
3. `Linkline` shows the matching clients.<br>
  Use case ends.

**Extensions**

* 1a. The filter command format is invalid.
    * `Linkline` returns the command usage message.<br>
    Use case ends.
* 1b. A provided tag is invalid according to the feature specification.
    * `Linkline` returns an error message describing the tag constraints.<br>
    Use case ends.
* 2a. No clients match the filter query.
    * `Linkline` shows an empty client list.<br>
    Use case ends.

#### Use Case: UC10 - Rename a tag

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` requests to rename an existing tag to a new name.
2. `Linkline` validates the request.
3. `Linkline` renames the tag in the global tag list and updates all affected clients.
4. `Linkline` saves the updated data.<br>
  Use case ends.

**Extensions**

* 1a. The command format is invalid.
    * `Linkline` returns the command usage message.<br>
    Use case ends.
* 2a. The target tag does not exist in `Linkline`.
    * `Linkline` returns an error message informing the user that the tag was not found.<br>
    Use case ends.
* 2b. The new tag name is invalid or already exists in `Linkline`.
    * `Linkline` returns an error message indicating why the rename cannot be applied.<br>
    Use case ends.

#### Use Case: UC11 - Delete a tag

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` requests to delete an existing tag by name.
2. `Linkline` validates that the tag exists and shows a confirmation message identifying the tag to be deleted.
3. `user` repeats the same delete tag command.
4. `Linkline` removes the tag from the global tag list and from all affected clients.
5. `Linkline` saves the updated data.<br>
  Use case ends.

**Extensions**

* 1a. The command format is invalid.
    * `Linkline` returns the command usage message.<br>
    Use case ends.
* 2a. The tag name provided does not exist.
    * `Linkline` returns an error message informing the user that the tag was not found.<br>
    Use case ends.
* 3a. `user` enters any other command instead of confirming.
    * `Linkline` cancels the pending tag deletion and executes the new command normally.<br>
    Use case ends.

#### Use Case: UC12 - Add a service log

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` locates the target client in the displayed client list.
2. `user` requests to add a log entry to that client by specifying the client's index and providing the log message.
3. `Linkline` timestamps the log entry, adds it to the client's log history, and saves the updated data.<br>
  Use case ends.

**Extensions**

* 2a. The index given is invalid and does not point to a client in the displayed client list.
    * `Linkline` returns an error message informing the user that the index is invalid.<br>
    Use case ends.
* 2b. The log message is missing or invalid according to the feature specification.
    * `Linkline` returns an error message describing the log message constraints.<br>
    Use case ends.

#### Use Case: UC13 - Delete a service log

**System:** `Linkline`<br>
**Actor:** `user`<br>

**MSS**

1. `user` locates the target client in the displayed client list.
2. `user` requests to delete a log entry by client index and displayed log number.
3. `Linkline` shows a confirmation message identifying the targeted log entry.
4. `user` repeats the same log deletion command.
5. `Linkline` deletes the log entry and saves the updated data.<br>
  Use case ends.

**Extensions**

* 2a. The client index given is invalid.
    * `Linkline` returns an error message informing the user that the client index is invalid.<br>
    Use case ends.
* 2b. The selected client has no log entries.
    * `Linkline` returns an error message indicating that there are no logs to delete.<br>
    Use case ends.
* 2c. The displayed log number is invalid for the selected client.
    * `Linkline` returns an error message indicating that the log number is invalid.<br>
    Use case ends.
* 4a. `user` enters any other command instead of confirming.
    * `Linkline` cancels the pending log deletion and executes the new command normally.<br>
    Use case ends.

### Non-Functional Requirements

1. Should work on any mainstream OS with Java `17` or above installed.
2. Should be able to hold up to 1000 clients without noticeable sluggishness during typical operations such as `list`, `find`, `filtertag`, `view`, `add`, `edit`, `logadd`, and `logdelete`.
3. A user who can type at an average speed for regular English text should be able to complete frequent tasks faster using commands than using a mouse-driven workflow.
4. A first-time user should be able to learn the basic workflow (`add`, `list`, `find`, `view`, `edit`) within about 10 minutes using sample data and the help command.
5. Client identity must remain unique based on normalized phone number and case-insensitive email address.
6. Client data must remain available across application restarts using a local human-editable JSON file.
7. The application should remain usable and should not crash on startup even if the main data file is missing, malformed, or otherwise unreadable, while preserving existing user data where possible.
8. The application is intended for a single local user. Multi-user collaboration, cloud synchronization, and networked storage are out of scope.
9. Data persistence is limited to a local human-editable JSON file. External database systems are out of scope.
10. Error messages should be actionable and should describe either the violated field constraints or the correct command usage, rather than exposing technical stack traces.
11. Command behavior should remain consistent with the documented command format and index conventions.
12. The application should remain usable without network access because technicians may need it between jobs or in locations with unreliable connectivity.

### Glossary

* **Client / Person**: The primary entity in Linkline. Mandatory fields include Name, Phone, Email, and Address. Optional fields include Tags, Notes, and service logs.
* **CLI (Command Line Interface)**: A text-based interface where the user interacts with the application by typing commands rather than using a mouse.
* **Compact view**: The left-panel summary view that shows a concise list of clients for quick scanning.
* **Details panel**: The right-panel in the main window that displays the full details of a selected client (name, phone, email, address, notes, tags and logs).
* **Displayed client list**: The subset of clients currently shown after commands such as `list`, `find`, or `filtertag`.
* **Duplicate client**: Two clients considered the same because they share the same normalized phone number or case-insensitive email address.
* **Full view**: The detailed view that shows all available information for the currently selected client.
* **JSON (JavaScript Object Notation)**: The text-based format used by the Storage component to persist data on disk.
* **Log entry**: A timestamped service note attached to a client record.
* **Display log number**: The log number shown in the UI. It counts from oldest to newest even though log entries are displayed newest first.
* **Mainstream OS**: Widely used operating systems such as Windows, macOS, and mainstream Linux distributions.
* **Pending action**: A temporary confirmation state stored after the first execution of a destructive command such as `delete`, `clear`, `deletetag`, or `logdelete`.
* **Selected client**: The client whose full details are currently shown in the details pane.
* **Service-location context**: Physical details about a client's service address, access instructions, precautions, or special requirements that matter during an on-site visit.
* **Solo technician**: The primary target user of Linkline.

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</box>

The test cases below assume Linkline is launched in a fresh folder so that the built-in sample data is loaded. Some
test cases modify stored data. If a later test case assumes the original sample data, restart with a fresh app folder
or restore `data/linkline.json` first.

### Launch and window placement

1. Initial launch

    1. Download the jar file and copy it into an empty folder.

    1. Open a terminal in that folder and run `java -jar linkline.jar`.<br>
       Expected: The GUI opens with the built-in sample clients. The initial window size may not be optimal.

1. Recovering from oversized and off-screen saved window preferences

    1. Prerequisites: Launch the app once and close it so that `preferences.json` exists.

    1. Open `preferences.json` and change the saved `guiSettings` to values that would not fit on the current screen,
       for example a very large (e.g., 100000) `windowWidth` and `windowHeight`, together with off-screen `windowCoordinates`
       such as `x = -400` and `y = -200`.

    1. Re-launch the app with `java -jar linkline.jar`.<br>
       Expected: Linkline readjusts the saved size and position so that the window fits within the visible screen area
       instead of opening oversized or partially hidden.

1. Recovering from a corrupted data file

    1. Prerequisites: Launch the app once, execute any successful data-changing command such as
       `add --name=Temp User --phone=91234567 --email=temp@example.com --address=123 Test Road`, and close the app so
       that `data/linkline.json` exists.

    1. Test case: Invalid JSON format.<br>
       Open `data/linkline.json` and break the JSON syntax, for example by removing a closing brace or quote.

    1. Re-launch the app.<br>
       Expected: Linkline starts with an empty client list, shows a startup warning about invalid JSON or malformed
       Linkline data, and creates a backup file named `linkline.json.corrupted-<timestamp>.bak` in the same folder
       when permissions allow.

    1. Test case: Valid JSON format but invalid Linkline data schema.<br>
       Restore a valid `data/linkline.json` first, then rename a required key such as `"name"` to `"names"` while
       keeping the file as valid JSON.

    1. Re-launch the app.<br>
       Expected: Linkline starts with an empty client list, shows a startup warning about invalid JSON or malformed
       Linkline data, and creates a backup file named `linkline.json.corrupted-<timestamp>.bak` in the same folder
       when permissions allow.
   
<box type="info" seamless>

Removing or renaming optional fields such as `notes`, `logs`, or `tags` may still allow the file to load because Linkline defaults missing optional fields during deserialization. To reproduce startup recovery reliably, use invalid JSON syntax, corrupt a required field, or insert an invalid field value.

</box>

### Navigating and narrowing the displayed client list

1. Finding and filtering clients

    1. Prerequisites: Fresh launch with the built-in sample data.

    1. Test case: `find --name=Alex`<br>
       Expected: Only `Alex Yeoh` remains in the displayed client list.

    1. Test case: `list` followed by `find --tag=Plumb`<br>
       Expected: Only `Bernice Yu` and `Charlotte Oliveiro` remain in the displayed client list.

    1. Test case: `list` followed by `find --name=Alex --tag=Plumbing`<br>
       Expected: `Alex Yeoh`, `Bernice Yu`, and `Charlotte Oliveiro` are shown because a single `find` command
       matches any supplied field.

    1. Test case: `list` followed by `find --tag=`<br>
       Expected: The displayed client list becomes empty because the built-in sample data has no untagged clients.

    1. Test case: `list` followed by `filtertag --tag=Plumbing --tag=Electrical Wiring`<br>
       Expected: Only `Bernice Yu` is shown because `filtertag` requires all specified tags to be present.

    1. Test case: `list` followed by `edit 3 --tag=` followed by `filtertag --tag=`<br>
       Expected: Only `Charlotte Oliveiro` is shown because `filtertag --tag=` keeps only clients with no tags.

    1. Test case: `list` followed by `filtertag --tag=Plumbing --tag=`<br>
       Expected: Error message shown because `filtertag` does not allow blank and non-blank tag values in the same command.

1. Viewing client details

    1. Prerequisites: `list`

    1. Test case: `view 1`<br>
       Expected: The first displayed client's full details appear in the right-hand panel.

    1. Test case: `find --name=Alex` followed by `view 1` followed by `filtertag --tag=Plumbing`<br>
       Expected: After `view 1`, `Alex Yeoh` is shown in the right-hand panel. After
       `filtertag --tag=Plumbing`, the displayed client list becomes empty and the right-hand panel returns to its placeholder
       state because the previously selected client is no longer shown.

### Adding, copying, and editing clients

1. Adding a client

    1. Test case:
       `add --name=Ethan Lim --phone=97861234 --email=ethanlim@example.com --address=Blk 123 Tampines Street 11, #08-12 --notes=Gate code 2048 --tag=Plumbing`<br>
       Expected: Success message shown. `Ethan Lim` appears in the displayed client list in sorted order.

    1. Test case:
       `add --name=Alex Clone --phone=8743 8807 --email=alex.clone@example.com --address=Blk 9 Test Road`<br>
       Expected: Duplicate-client error shown because phone numbers are compared ignoring formatting.

1. Copying a client's address

    1. Prerequisites: `list`

    1. Test case: `copyaddr 1`<br>
       Expected: The address of the first displayed client is copied to the system clipboard.

    1. Test case: `copyaddr 0`<br>
       Expected: Error message shown because `0` is not a valid client index.

1. Copying an edit command template

    1. Prerequisites: `list`

    1. Test case: `copyedit 1`<br>
       Expected: A ready-to-edit `edit 1 ...` command for the first displayed client is copied to the system
       clipboard.

    1. Paste the copied command into the command box, change one field such as `--phone=81112222`, and execute it.<br>
       Expected: The first client's details are updated successfully.

1. Editing a client to a duplicate identity

    1. Prerequisites: Use a fresh app folder or restore the original sample data. Execute `list`.

    1. Test case: `edit 2 --phone=87438807`<br>
       Expected: Duplicate-client error shown because that phone number already belongs to `Alex Yeoh`.

### Confirmation-based deletion and clearing

1. Deleting a client

    1. Prerequisites: `list`

    1. Test case: `delete 1`<br>
       Expected: No client is deleted yet. A confirmation message identifying the first displayed client is shown.

    1. Test case: `delete 1` immediately again<br>
       Expected: The first displayed client is deleted from the list.

    1. Test case: `delete 1` then `list` then `delete 1`<br>
       Expected: The first `delete 1` shows confirmation. `list` cancels the pending deletion. The second `delete 1`
       asks for confirmation again instead of deleting immediately.

1. Clearing all clients

    1. Prerequisites: Use a fresh app folder or restore the original sample data.

    1. Test case: `clear` then `help`<br>
       Expected: `help` cancels the pending clear action and opens the help window.

    1. Test case: `clear` then `clear`<br>
       Expected: All clients are removed and the displayed client list becomes empty.

### Working with service logs

1. Adding a log

    1. Prerequisites: Use a fresh app folder or restore the original sample data. Execute `view 1` so that
       `Alex Yeoh` is selected in the right-hand panel.

    1. Test case: `logadd 1 Observed condensation near living room unit.`<br>
       Expected: Success message shown. The right-hand panel refreshes and shows a new latest log at the top, labeled
       `Log 3`.

1. Deleting a log

    1. Prerequisites: Use a fresh app folder or restore the original sample data. Execute `view 1` so that
       `Alex Yeoh` is selected in the right-hand panel.

    1. Test case: `logdelete 1 2`<br>
       Expected: No log is deleted yet. A confirmation message identifies the latest log of `Alex Yeoh`.

    1. Test case: `logdelete 1 2` immediately again<br>
       Expected: The latest log is deleted. The right-hand panel returns to showing one log, labeled `Log 1`.

    1. Test case: `logdelete 3 1`<br>
       Expected: Error message shown because the third sample client (`Charlotte Oliveiro`) has no logs.

### Global tag operations

1. Renaming a tag

    1. Prerequisites: Use a fresh app folder or restore the original sample data. Execute `list`.

    1. Test case: `renametag --tag=AC-Service --tag=Aircon-Service`<br>
       Expected: All clients that previously had `AC-Service` now show `Aircon-Service`, and the full client list
       remains displayed.

    1. Test case: `renametag --tag=Plumbing --tag=Electrical Wiring`<br>
       Expected: Error shown because the new tag already exists.

1. Deleting a tag

    1. Test case: `deletetag Plumbing`<br>
       Expected: No tag is deleted yet. A confirmation message is shown.

    1. Test case: `deletetag Plumbing` immediately again<br>
       Expected: `Plumbing` is removed from all clients and the full client list is shown.

    1. Test case: `deletetag Electrical Wiring` then `find --name=Bernice`<br>
       Expected: The `find` command cancels the pending tag deletion. `Electrical Wiring` is not removed.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Effort**
This project extends the AddressBook-Level 3 (AB3) codebase into Linkline, a client management system tailored for solo service technicians. While AB3 serves as a simple contact manager, Linkline introduces domain-specific features such as service logs, confirmation flows and corrupted file handling.

### Challenges

| **Challenge** | **Description**                                                                                                                                                               |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Multiple entity types** | AB3 manages only `Person` and `Tag`. Linkline adds `LogHistory`, `LogEntry`, and `LogMessage` as first-class entities with their own validation, storage, and UI representation. |
| **Complex CLI syntax** | Migrated from `n/` prefixes to Linux-style `--name=` format, requiring updates across all parsers, commands, and tests.                                                       |
| **Two-step confirmation** | Implemented generic `PendingAction` framework for delete, clear, and log-delete commands without polluting `LogicManager` with command-specific logic.                        |
| **Selective persistence** | Reworked save behavior so only commands that modify `linkline.json` trigger address book writes, while failed writes leave a tracked dirty state for one best-effort retry on graceful exit. |
| **Duplicate detection** | Enhanced `edit` command to prevent duplicate phone/email across different clients while allowing self-edits.                                                                  |
| **Corrupted file handling** | Added detection and user-friendly error messaging for corrupted `linkline.json` without auto-overwriting.                                                                     |
| **UI improvements** | Redesigned the interface with a split-pane layout featuring a compact list view (showing name and phone) and a full details panel (showing all client information when selected via `view`).                                                                                                         |

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Planned Enhancements**

Team size: 5

1. **Support quoted and escaped CLI field values**: Linkline currently treats a space followed by another recognized field
   marker as the start of a new argument, and it also trims away boundary whitespace. We plan to support quoted input
   such as `--notes="Call before arriving -- bring ladder"` together with escapes such as `\"`, `\\`, and `\n` so
   free-text fields can preserve literal special text more reliably. This would also make more compact syntax variants (e.g., `-n=...`, `n/...`) easier to consider in the future if needed.
2. **Support user-specified service timestamps for logs**: `logadd` currently records only the current system date and
   time at the moment the command is entered. We plan to support storing the actual service datetime separately from
   the log creation time so users can backdate missed job notes without losing an audit trail.
3. **Add `logedit` for correcting existing log entries**: Linkline currently supports `logadd` and `logdelete`, but
   fixing a mistake in a log entry requires deleting the old log and adding a replacement. We plan to add a
   `logedit` command so users can update an existing log entry directly while keeping the rest of the client's log
   history intact.
4. **Extend `find` to search notes and log history**: The current `find` command can search names, phone numbers,
   emails, addresses, and tags, but it cannot search service notes or past log content. We plan to extend `find`
   with fields such as `--notes=...` and `--log=...` so users can locate clients using site instructions or previous
   job records.
5. **Enhance phone number handling with country codes**: Linkline currently stores a single phone number per client with 
   limited country code support. We plan to enhance this by allowing optional `+<country_code>` prefixes
   (e.g., `+65 91234567`) and normalizing numbers using `country_code + local_digits` for duplicate detection.
   This ensures `+65 9999 9999` and `9999 9999` are treated as duplicates, while `+66 9999 9999` remains distinct.
   We also plan to support a default country code (e.g., `setcountrycode 65`) so users can enter local numbers without
   typing `+65` every time.
6. **Support multiple phone numbers per client**: Linkline currently stores only one phone number per client. We plan to
   extend phone number storage to support multiple numbers (e.g., mobile, home, office). As an interim workaround, users
   can store secondary numbers in the `notes` field.
7. **Improve duplicate error messages**: The current duplicate error message does not specify which field caused the
   duplicate or which client is affected. We plan to enhance it to show the duplicate field (phone or email), the name
   and index of the existing client, and a suggestion to use `list` if the duplicate is not visible in the current
   filtered view.
8. **Repurpose the selection highlight**: The current highlight is a cosmetic leftover with no function. We plan to
   repurpose it to provide useful feedback (e.g., indicating the most recently viewed client).
9. **Consolidate search and filter logic into a unified `find` command:** Currently, `find` and `filtertag` exist as
   separate commands. `filtertag` was originally implemented as a distinct component to reflect the architectural
   decision to treat `Tags` as a first-class entity for a more OOP model. However, this separation now creates a command
   overlap where users must switch between `find` (for broad OR-matching across general fields) and `filtertag` (for
   specific tag-set intersection). We plan to unify these into a single `find` command. This update will include a
   `--matchall=` flag to allow users to toggle between partial and exact tag matching, providing a more intuitive and
   streamlined CLI experience while maintaining the underlying `Tag` architecture.
10. **Improve adaptive sizing for the Notes and Logs sections:** Linkline currently keeps the client detail panel usable
   through wrapping and scrolling, but longer `notes` and `logs` content can still require more internal scrolling than
   necessary on larger windows. We plan to let these sections use available vertical space more effectively as the app
   window grows, while preserving usability and access to the full content on smaller windows.

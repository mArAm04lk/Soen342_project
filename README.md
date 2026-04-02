# Soen342_project

## Course
SOEN 342 - Software Requirements and Deployment (Winter 2026)

## Team Members
- Maram Loukil [40299252] - username: mArAm04lk
- Keshini Shummoogum [40296473] - username: Keshini-sh
- Vinuya Sivakolunthu [40278252] - username: Vinuya04

## Project Overview
This repository contains the Proof_of_Concept Java application for managing tasks, projects, and collaborators from a CSV file.

The current implementation includes:
- CSV import of tasks, projects, and collaborators
- Search by keyword, status, priority, project, collaborator, and due date
- CSV export for all tasks or selected task subsets
- iCal export using `ical4j`

## Current Project Location
The active Java project is in:
- [Proof_of_Concept](Proof_of_Concept)

## Compile and Run
As of now, the project is built with Maven.

Prerequisite:
- Install Maven if it is not already available on your system

Compile the project:
```bash
cd Proof_of_Concept
mvn compile
```

Run the application:
```bash
cd Proof_of_Concept
mvn exec:java -Dexec.mainClass=Main
```

If you want to compile and run in one step:
```bash
cd Proof_of_Concept
mvn compile exec:java -Dexec.mainClass=Main
```

## How the Application Works
1. Enter the path to a CSV file when prompted.
2. Review the imported tasks.
3. Choose a search option if you want to filter tasks.
4. Choose an export scope:
	- filtered search results
	- a single task
	- all tasks in a project
	- all tasks
5. Choose an export format:
	- CSV
	- iCal (`.ics`)

## Input and Output Files
- Sample input CSV: [Proof_of_Concept/tasks.csv](Proof_of_Concept/tasks.csv)
- CSV exports are written to the file path you enter at runtime
- iCal exports are written to the `.ics` path you enter at runtime

## Notes
- The project currently uses the folder structure already present in `Proof_of_Concept`.
- Project-based exports now work because imported tasks are linked back to their project.
- iCal exports only include tasks with due dates.
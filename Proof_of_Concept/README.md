# Proof of Concept

## Seed the H2 database

To reset and repopulate the local H2 database from `tasks.csv`, run:

```powershell
Set-Location c:\Soen342_project\Proof_of_Concept
powershell -ExecutionPolicy Bypass -File .\seed-db.ps1
```

You can also double-click or run `seed-db.bat` from the project folder on Windows.

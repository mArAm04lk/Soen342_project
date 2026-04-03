param(
    [string]$CsvPath = (Join-Path $PSScriptRoot "tasks.csv")
)

$projectRoot = $PSScriptRoot
$h2Jar = Get-ChildItem -Path "$env:USERPROFILE\.m2\repository\com\h2database\h2" -Recurse -Filter "h2-*.jar" -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $h2Jar) {
    throw "Could not find the H2 JAR under $env:USERPROFILE\.m2\repository\com\h2database\h2. Install the H2 dependency first."
}

if (-not (Test-Path $CsvPath)) {
    throw "CSV file not found: $CsvPath"
}

$dbFiles = @(
    (Join-Path $projectRoot "task_planner_db.mv.db"),
    (Join-Path $projectRoot "task_planner_db.trace.db")
)

foreach ($dbFile in $dbFiles) {
    if (Test-Path $dbFile) {
        Remove-Item $dbFile -Force
    }
}

$buildDir = Join-Path $env:TEMP "soen342_seed_build"
if (Test-Path $buildDir) {
    Remove-Item $buildDir -Recurse -Force
}

New-Item -ItemType Directory -Path $buildDir | Out-Null

Set-Location $projectRoot

javac -cp $h2Jar -d $buildDir model\*.java service\DatabaseService.java

$seedSource = @'
import service.DatabaseService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SeedDatabase {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("CSV path is required");
        }

        DatabaseService.initializeSchema();
        DatabaseService.replaceAllData(DatabaseService.readRecordsFromCsv(args[0]));

        try (Connection connection = DriverManager.getConnection("jdbc:h2:./task_planner_db", "sa", "")) {
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT (SELECT COUNT(*) FROM tasks) AS tasks_count, (SELECT COUNT(*) FROM projects) AS projects_count, (SELECT COUNT(*) FROM collaborators) AS collaborators_count");
                resultSet.next();
                System.out.println("tasks=" + resultSet.getInt("tasks_count"));
                System.out.println("projects=" + resultSet.getInt("projects_count"));
                System.out.println("collaborators=" + resultSet.getInt("collaborators_count"));
            }
        }
    }
}
'@

$seedFile = Join-Path $buildDir "SeedDatabase.java"
Set-Content -Path $seedFile -Value $seedSource -Encoding ASCII

javac -cp "$buildDir;$h2Jar" -d $buildDir $seedFile
java -cp "$buildDir;$h2Jar" SeedDatabase $CsvPath

Write-Host "Database reset and seeded from $CsvPath"
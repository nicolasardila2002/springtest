# Ejecuta la colección de Postman con Newman y genera reportes HTML y JSON
# Requisitos: Node.js + npm instalados, luego: npm install -g newman newman-reporter-html

$collection = "$(Resolve-Path .\postman\logitrack_collection.json)"
$environment = "$(Resolve-Path .\postman\logitrack_environment.json)"
$timestamp = (Get-Date -Format "yyyyMMdd-HHmmss")
$reportDir = "$(Resolve-Path .)\postman\results\$timestamp"
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null

Write-Host "Ejecutando colección: $collection"
Write-Host "Environment: $environment"

# Ejecutar newman (salida JSON y HTML)
$newmanCmd = "newman run `"$collection`" -e `"$environment`" -r cli,json,html --reporter-json-export `"$reportDir\report.json`" --reporter-html-export `"$reportDir\report.html`""
Write-Host $newmanCmd
Invoke-Expression $newmanCmd

Write-Host "Reportes generados en: $reportDir"

# 백엔드 실행 스크립트 (dev 환경)
# 사용법: PowerShell에서 .\run-dev.ps1

# 이미 실행 중인 인스턴스 종료
$existing = Get-WmiObject Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -like "*peoplejob-backend*" }
if ($existing) {
    $existing | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
    Write-Host "기존 인스턴스 종료됨"
    Start-Sleep -Milliseconds 500
}

# 빌드
Write-Host "빌드 중..."
& .\mvnw.cmd package "-Dmaven.test.skip=true" -q
if ($LASTEXITCODE -ne 0) { Write-Host "빌드 실패"; exit 1 }

# 실행
Write-Host "서버 시작 중 (포트 8090)..."
java -Xms512m -Xmx1g -XX:+UseG1GC -jar "target\peoplejob-backend-0.0.1-SNAPSHOT.jar" --spring.profiles.active=dev

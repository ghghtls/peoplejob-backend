# 백엔드 실행 스크립트 (dev 환경)
# 사용법: PowerShell에서 .\run-dev.ps1

$PORT = 8090

# 포트를 점유 중인 프로세스 종료 (방법 1: netstat 기반 — 가장 확실)
$netstatLines = netstat -ano | Select-String ":$PORT\s"
$pids = $netstatLines |
    ForEach-Object { ($_ -split '\s+')[-1] } |
    Where-Object { $_ -match '^\d+$' } |
    Select-Object -Unique

if ($pids) {
    foreach ($pid in $pids) {
        try {
            Stop-Process -Id $pid -Force -ErrorAction Stop
            Write-Host "포트 $PORT 점유 프로세스(PID $pid) 종료됨"
        } catch {
            Write-Host "PID $pid 종료 실패 (이미 종료됐거나 권한 부족): $_"
        }
    }
    # 소켓이 TIME_WAIT 상태에서 해제될 때까지 잠시 대기
    Start-Sleep -Milliseconds 800
} else {
    Write-Host "포트 $PORT 사용 중인 프로세스 없음"
}

# 빌드
Write-Host "빌드 중..."
& .\mvnw.cmd package "-Dmaven.test.skip=true" -q
if ($LASTEXITCODE -ne 0) { Write-Host "빌드 실패"; exit 1 }

# 실행
Write-Host "서버 시작 중 (포트 $PORT)..."
java -Xms512m -Xmx1g -XX:+UseG1GC -jar "target\peoplejob-backend-0.0.1-SNAPSHOT.jar" --spring.profiles.active=dev

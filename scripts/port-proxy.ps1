# 1. Alte Regeln aufräumen
netsh interface portproxy reset

# 2. Die aktuelle WSL-Host-IP herausfinden
$wslIp = (bash.exe -c "ip route show | grep default").Split(" ")[2]

if ($wslIp) {
    Write-Host "WSL IP gefunden: $wslIp" -ForegroundColor Green

    # 3. Neue Regel setzen (0.0.0.0 lauscht auf allen IPs, auch der neuen WSL-IP)
    netsh interface portproxy add v4tov4 listenaddress=0.0.0.0 listenport=5560 connectaddress=127.0.0.1 connectport=5554

    Write-Host "Brücke auf Port 5560 aktiv. Nutze in WSL: telnet $wslIp 5560" -ForegroundColor Cyan
} else {
    Write-Host "WSL scheint nicht zu laufen." -ForegroundColor Red
}
pause

@echo off
setlocal enabledelayedexpansion

:: =============================================================================
:: MT-MX PROJECT STARTUP SCRIPT (Windows)
:: =============================================================================
:: Bu script projeyi tek komutla başlatır ve test verileri ile hazır hale getirir
:: Usage: start.bat [mode]
::   mode: production (default), dev, tools
:: =============================================================================

set MODE=production
set CLEAN=false
set STATUS_ONLY=false
set STOP_ONLY=false

:: Colors for output (Windows ANSI support)
set RED=[91m
set GREEN=[92m
set YELLOW=[93m
set BLUE=[94m
set NC=[0m

:: Parse arguments
:parse_args
if "%~1"=="" goto :start_main
if /i "%~1"=="production" set MODE=production
if /i "%~1"=="prod" set MODE=production
if /i "%~1"=="dev" set MODE=dev
if /i "%~1"=="development" set MODE=dev
if /i "%~1"=="tools" set MODE=tools
if /i "%~1"=="-h" goto :show_help
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-c" set CLEAN=true
if /i "%~1"=="--clean" set CLEAN=true
if /i "%~1"=="-s" set STATUS_ONLY=true
if /i "%~1"=="--status" set STATUS_ONLY=true
if /i "%~1"=="--stop" set STOP_ONLY=true
shift
goto :parse_args

:start_main
echo %BLUE%[INFO]%NC% 🚀 MT-MX Project Startup
echo ========================

if "%STOP_ONLY%"=="true" goto :stop_services
if "%STATUS_ONLY%"=="true" goto :show_status

:: Check Docker
call :check_docker
if errorlevel 1 exit /b 1

:: Check Docker Compose
call :check_docker_compose
if errorlevel 1 exit /b 1

:: Check and kill ports
call :check_and_kill_ports

if "%CLEAN%"=="true" call :cleanup

:: Start services
call :start_services %MODE%
if errorlevel 1 exit /b 1

:: Wait for services
call :wait_for_services %MODE%

:: Show status
call :show_status %MODE%

echo %GREEN%[SUCCESS]%NC% 🎉 MT-MX projesi başarıyla başlatıldı!
echo %BLUE%[INFO]%NC% Servisleri durdurmak için: start.bat --stop
goto :end

:: =============================================================================
:: FUNCTIONS
:: =============================================================================

:check_docker
echo %BLUE%[INFO]%NC% Docker durumu kontrol ediliyor...
docker info >nul 2>&1
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Docker çalışmıyor! Lütfen Docker Desktop'ı başlatın.
    exit /b 1
)
echo %GREEN%[SUCCESS]%NC% Docker çalışıyor ✓
exit /b 0

:check_docker_compose
echo %BLUE%[INFO]%NC% Docker Compose durumu kontrol ediliyor...
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Docker Compose bulunamadı! Lütfen Docker Desktop'ı güncelleyin.
    exit /b 1
)
echo %GREEN%[SUCCESS]%NC% Docker Compose mevcut ✓
exit /b 0

:check_and_kill_ports
echo %BLUE%[INFO]%NC% Port kullanımları kontrol ediliyor...

:: Check ports that might be in use
call :check_port 5432 "PostgreSQL"
call :check_port 8081 "Backend API"
call :check_port 3000 "Frontend"
call :check_port 5173 "Vite Dev"
call :check_port 8082 "Backend Dev"
call :check_port 5050 "pgAdmin"

echo %GREEN%[SUCCESS]%NC% Port kontrolü tamamlandı ✓
exit /b 0

:check_port
set port=%1
set service_name=%2
set service_name=%service_name:"=%

:: Find process using the port (Windows specific)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%port% " ^| findstr "LISTENING"') do (
    set pid=%%a
    if defined pid (
        echo %YELLOW%[WARNING]%NC% Port %port% (%service_name%) kullanımda
        
        :: Get process name
        for /f "tokens=1" %%b in ('tasklist /fi "pid eq !pid!" /fo csv /nh 2^>nul') do (
            set process_name=%%b
            set process_name=!process_name:"=!
        )
        
        if defined process_name (
            echo %BLUE%[INFO]%NC% Port %port%'u kullanan process: !process_name! (PID: !pid!)
        ) else (
            echo %BLUE%[INFO]%NC% Port %port%'u kullanan process PID: !pid!
        )
        
        echo %BLUE%[INFO]%NC% Process durduruluyor...
        
        :: Try to kill the process gracefully first
        taskkill /pid !pid! >nul 2>&1
        if not errorlevel 1 (
            echo %GREEN%[SUCCESS]%NC% Port %port% serbest bırakıldı ✓
        ) else (
            :: Force kill if graceful kill failed
            taskkill /pid !pid! /f >nul 2>&1
            if not errorlevel 1 (
                echo %GREEN%[SUCCESS]%NC% Port %port% zorla serbest bırakıldı ✓
            ) else (
                echo %YELLOW%[WARNING]%NC% Process durdurulamadı, devam ediliyor...
            )
        )
        goto :eof
    )
)
exit /b 0

:cleanup
echo %BLUE%[INFO]%NC% Eski containerlar temizleniyor...
:: Stop containers first
docker-compose down >nul 2>&1
:: Remove volumes and orphans
docker-compose down --volumes --remove-orphans >nul 2>&1
:: Clean up orphaned containers
docker container prune -f >nul 2>&1
echo %GREEN%[SUCCESS]%NC% Temizlik tamamlandı ✓
exit /b 0

:start_services
echo %BLUE%[INFO]%NC% MT-MX projesi başlatılıyor (%~1 mode)...

if /i "%~1"=="production" (
    echo %BLUE%[INFO]%NC% Production mode'da başlatılıyor...
    docker-compose up -d db backend frontend
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Production servisleri başlatılamadı!
        exit /b 1
    )
) else if /i "%~1"=="dev" (
    echo %BLUE%[INFO]%NC% Development mode'da başlatılıyor...
    :: Dev modunda sadece veritabanını başlat
    docker-compose up -d db
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Veritabanı servisi başlatılamadı!
        exit /b 1
    )
    echo %BLUE%[INFO]%NC% Development mode: Sadece veritabanı Docker'da çalışıyor
    echo %BLUE%[INFO]%NC% Backend ve Frontend'i manuel olarak başlatın:
    echo %BLUE%[INFO]%NC%   Backend: cd mt-mx-be ^&^& mvn spring-boot:run
    echo %BLUE%[INFO]%NC%   Frontend: cd mt-mx-fe ^&^& npm run dev
) else if /i "%~1"=="tools" (
    echo %BLUE%[INFO]%NC% Tools mode'da başlatılıyor (pgAdmin dahil)...
    docker-compose --profile tools up -d db backend frontend pgadmin
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Tools servisleri başlatılamadı!
        exit /b 1
    )
) else (
    echo %RED%[ERROR]%NC% Geçersiz mode: %~1
    echo %BLUE%[INFO]%NC% Kullanılabilir modlar: production, dev, tools
    exit /b 1
)
exit /b 0

:wait_for_services
echo %BLUE%[INFO]%NC% Servisler başlatılıyor, lütfen bekleyin...

:: Wait for database
echo %BLUE%[INFO]%NC% Veritabanı bekleniyor...
set timeout=60
:wait_db_loop
if %timeout% leq 0 (
    echo %RED%[ERROR]%NC% Veritabanı başlatılamadı!
    exit /b 1
)
docker-compose exec -T db pg_isready -U user -d mtmxdb >nul 2>&1
if not errorlevel 1 (
    echo %GREEN%[SUCCESS]%NC% Veritabanı hazır ✓
    goto :wait_services_mode_check
)
timeout /t 2 /nobreak >nul
set /a timeout-=2
goto :wait_db_loop

:wait_services_mode_check
:: Skip backend/frontend checks in dev mode
if /i "%~1"=="dev" (
    echo %BLUE%[INFO]%NC% Development mode: Backend ve Frontend kontrolleri atlanıyor
    goto :eof
)

:wait_backend
:: Wait for backend (only in production/tools mode)
echo %BLUE%[INFO]%NC% Backend servis bekleniyor...
set timeout=120
:wait_backend_loop
if %timeout% leq 0 (
    echo %YELLOW%[WARNING]%NC% Backend servis başlatılamadı, manuel kontrol edin.
    goto :wait_frontend
)
curl -s http://localhost:8081/actuator/health >nul 2>&1
if not errorlevel 1 (
    echo %GREEN%[SUCCESS]%NC% Backend hazır ✓
    goto :wait_frontend
)
timeout /t 3 /nobreak >nul
set /a timeout-=3
goto :wait_backend_loop

:wait_frontend
:: Wait for frontend (only in production/tools mode)
echo %BLUE%[INFO]%NC% Frontend servis bekleniyor...
set timeout=60
:wait_frontend_loop
if %timeout% leq 0 (
    echo %YELLOW%[WARNING]%NC% Frontend servis başlatılamadı, manuel kontrol edin.
    goto :eof
)
curl -s http://localhost:3000 >nul 2>&1
if not errorlevel 1 (
    echo %GREEN%[SUCCESS]%NC% Frontend hazır ✓
    goto :eof
)
timeout /t 2 /nobreak >nul
set /a timeout-=2
goto :wait_frontend_loop

:show_status
echo %BLUE%[INFO]%NC% Servis durumları:
echo.

:: Show Docker container status
docker-compose ps 2>nul
if errorlevel 1 echo Docker Compose bilgisi alınamadı

echo.
echo %BLUE%[INFO]%NC% Erişim URL'leri:

if /i "%~1"=="dev" (
    echo 🗄️  Veritabanı:   %GREEN%localhost:5432%NC% (mtmxdb/user/password)
    echo 🔧 Backend:      %YELLOW%http://localhost:8081%NC% (Manuel başlatın: cd mt-mx-be ^&^& mvn spring-boot:run)
    echo 🌐 Frontend:     %YELLOW%http://localhost:3000%NC% (Manuel başlatın: cd mt-mx-fe ^&^& npm run dev)
) else (
    echo 🌐 Frontend:     %GREEN%http://localhost:3000%NC%
    echo 🔧 Backend API:  %GREEN%http://localhost:8081%NC%
    echo 📚 Swagger UI:   %GREEN%http://localhost:8081/swagger-ui.html%NC%
    echo 🔍 Health Check: %GREEN%http://localhost:8081/actuator/health%NC%
)

:: Check if pgAdmin is running
docker-compose ps 2>nul | findstr pgadmin >nul
if not errorlevel 1 (
    echo 🗄️  pgAdmin:     %GREEN%http://localhost:5050%NC% (admin@mtmx.com / admin123)
)

echo.

if not /i "%~1"=="dev" (
    echo %BLUE%[INFO]%NC% Test verileri otomatik olarak yüklendi:
    echo   • MT103: 4 örnek mesaj
    echo   • MT102: 3 örnek mesaj
    echo   • MT202: 4 örnek mesaj
    echo   • MT202COV: 3 örnek mesaj
    echo   • MT203: 4 örnek mesaj
    echo.
)
exit /b 0

:show_help
echo MT-MX Project Startup Script (Windows)
echo.
echo Kullanım:
echo   %~nx0 [mode] [options]
echo.
echo Modlar:
echo   production, prod    Production mode (tüm servisler Docker'da)
echo   dev, development    Development mode (sadece DB Docker'da)
echo   tools              Production + pgAdmin
echo.
echo Seçenekler:
echo   -h, --help         Bu yardım mesajını göster
echo   -c, --clean        Başlamadan önce temizle
echo   -s, --status       Sadece durum göster
echo   --stop             Servisleri durdur
echo.
echo Örnekler:
echo   %~nx0                 # Production mode'da başlat
echo   %~nx0 dev             # Development mode'da başlat (sadece DB)
echo   %~nx0 tools --clean   # Tools mode'da temizleyerek başlat
echo   %~nx0 --status        # Durum göster
echo   %~nx0 --stop          # Servisleri durdur
exit /b 0

:stop_services
echo %BLUE%[INFO]%NC% Servisler durduruluyor...
docker-compose down --remove-orphans
echo %GREEN%[SUCCESS]%NC% Tüm servisler durduruldu ✓
exit /b 0

:end
endlocal 
#!/bin/bash

# =============================================================================
# MT-MX PROJECT STARTUP SCRIPT
# =============================================================================
# Bu script projeyi tek komutla başlatır ve test verileri ile hazır hale getirir
# Usage: ./start.sh [mode]
#   mode: production (default), dev, tools
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Docker is running
check_docker() {
    print_status "Docker durumu kontrol ediliyor..."
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker çalışmıyor! Lütfen Docker'ı başlatın."
        exit 1
    fi
    print_success "Docker çalışıyor ✓"
}

# Function to check if Docker Compose is available
check_docker_compose() {
    print_status "Docker Compose durumu kontrol ediliyor..."
    if ! command -v docker-compose > /dev/null 2>&1; then
        print_error "Docker Compose bulunamadı! Lütfen Docker Compose'u yükleyin."
        exit 1
    fi
    print_success "Docker Compose mevcut ✓"
}

# Function to check and kill processes using required ports (more robust)
check_and_kill_ports() {
    local ports=("5432" "8081" "3000" "5173" "8082" "5050")
    local port_names=("PostgreSQL" "Backend API" "Frontend" "Vite Dev" "Backend Dev" "pgAdmin")
    
    print_status "Port kullanımları kontrol ediliyor..."
    
    for i in "${!ports[@]}"; do
        local port="${ports[$i]}"
        local name="${port_names[$i]}"
        
        # Check if port is in use - more robust check
        if netstat -tuln 2>/dev/null | grep -q ":$port " || lsof -ti:$port > /dev/null 2>&1; then
            print_warning "Port $port ($name) kullanımda"
            
            # Try to get process info
            local pid=""
            if command -v lsof > /dev/null 2>&1; then
                pid=$(lsof -ti:$port 2>/dev/null | head -1)
            fi
            
            if [ -n "$pid" ]; then
                local process_name=$(ps -p $pid -o comm= 2>/dev/null || echo "bilinmeyen")
                print_status "Port $port'u kullanan process: $process_name (PID: $pid)"
                print_status "Process durduruluyor..."
                
                # First try SIGTERM (graceful shutdown)
                if kill $pid 2>/dev/null; then
                    sleep 3
                    
                    # Check if process is still running
                    if kill -0 $pid 2>/dev/null; then
                        print_warning "Process hala çalışıyor, zorla durduruluyor..."
                        kill -9 $pid 2>/dev/null || true
                    fi
                    
                    print_success "Port $port serbest bırakıldı ✓"
                else
                    print_warning "Process durdurulamadı, devam ediliyor..."
                fi
            else
                print_warning "Port $port kullanımda ama process bulunamadı"
            fi
        fi
    done
    
    print_success "Port kontrolü tamamlandı ✓"
}

# Function to cleanup existing containers (safer approach)
cleanup() {
    print_status "Eski containerlar temizleniyor..."
    
    # Stop containers first
    docker-compose down > /dev/null 2>&1 || true
    
    # Remove volumes if they exist
    docker-compose down --volumes --remove-orphans > /dev/null 2>&1 || true
    
    # Clean up any orphaned containers
    docker container prune -f > /dev/null 2>&1 || true
    
    print_success "Temizlik tamamlandı ✓"
}

# Function to start services (fixed dev mode)
start_services() {
    local mode=${1:-production}
    
    print_status "MT-MX projesi başlatılıyor ($mode mode)..."
    
    case $mode in
        "production"|"prod")
            print_status "Production mode'da başlatılıyor..."
            if ! docker-compose up -d db backend frontend; then
                print_error "Production servisleri başlatılamadı!"
                return 1
            fi
            ;;
        "dev"|"development")
            print_status "Development mode'da başlatılıyor..."
            # Dev modunda sadece veritabanını başlat, backend ve frontend local olarak çalışacak
            if ! docker-compose up -d db; then
                print_error "Veritabanı servisi başlatılamadı!"
                return 1
            fi
            print_status "Development mode: Sadece veritabanı Docker'da çalışıyor"
            print_status "Backend ve Frontend'i manuel olarak başlatın:"
            print_status "  Backend: cd mt-mx-be && mvn spring-boot:run"
            print_status "  Frontend: cd mt-mx-fe && npm run dev"
            ;;
        "tools")
            print_status "Tools mode'da başlatılıyor (pgAdmin dahil)..."
            if ! docker-compose --profile tools up -d db backend frontend pgadmin; then
                print_error "Tools servisleri başlatılamadı!"
                return 1
            fi
            ;;
        *)
            print_error "Geçersiz mode: $mode"
            print_status "Kullanılabilir modlar: production, dev, tools"
            return 1
            ;;
    esac
    
    return 0
}

# Function to wait for services (improved with better timeouts)
wait_for_services() {
    local mode=${1:-production}
    
    print_status "Servisler başlatılıyor, lütfen bekleyin..."
    
    # Wait for database
    print_status "Veritabanı bekleniyor..."
    local timeout=60
    while [ $timeout -gt 0 ]; do
        if docker-compose exec -T db pg_isready -U user -d mtmxdb > /dev/null 2>&1; then
            print_success "Veritabanı hazır ✓"
            break
        fi
        sleep 2
        timeout=$((timeout-2))
    done
    
    if [ $timeout -le 0 ]; then
        print_error "Veritabanı başlatılamadı!"
        return 1
    fi
    
    # Dev modunda backend ve frontend kontrol etme
    if [ "$mode" = "dev" ] || [ "$mode" = "development" ]; then
        print_status "Development mode: Backend ve Frontend kontrolleri atlanıyor"
        return 0
    fi
    
    # Wait for backend (only in production/tools mode)
    print_status "Backend servis bekleniyor..."
    timeout=120
    while [ $timeout -gt 0 ]; do
        if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
            print_success "Backend hazır ✓"
            break
        fi
        sleep 3
        timeout=$((timeout-3))
    done
    
    if [ $timeout -le 0 ]; then
        print_warning "Backend servis başlatılamadı, manuel kontrol edin."
    fi
    
    # Wait for frontend (only in production/tools mode)
    print_status "Frontend servis bekleniyor..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if curl -s http://localhost:3000 > /dev/null 2>&1; then
            print_success "Frontend hazır ✓"
            break
        fi
        sleep 2
        timeout=$((timeout-2))
    done
    
    if [ $timeout -le 0 ]; then
        print_warning "Frontend servis başlatılamadı, manuel kontrol edin."
    fi
    
    return 0
}

# Function to show service status (improved)
show_status() {
    local mode=${1:-production}
    
    print_status "Servis durumları:"
    echo
    
    # Show Docker container status
    if command -v docker-compose > /dev/null 2>&1; then
        docker-compose ps 2>/dev/null || echo "Docker Compose bilgisi alınamadı"
    fi
    
    echo
    print_status "Erişim URL'leri:"
    
    if [ "$mode" = "dev" ] || [ "$mode" = "development" ]; then
        echo -e "🗄️  Veritabanı:   ${GREEN}localhost:5432${NC} (mtmxdb/user/password)"
        echo -e "🔧 Backend:      ${YELLOW}http://localhost:8081${NC} (Manuel başlatın: cd mt-mx-be && mvn spring-boot:run)"
        echo -e "🌐 Frontend:     ${YELLOW}http://localhost:3000${NC} (Manuel başlatın: cd mt-mx-fe && npm run dev)"
    else
        echo -e "🌐 Frontend:     ${GREEN}http://localhost:3000${NC}"
        echo -e "🔧 Backend API:  ${GREEN}http://localhost:8081${NC}"
        echo -e "📚 Swagger UI:   ${GREEN}http://localhost:8081/swagger-ui.html${NC}"
        echo -e "🔍 Health Check: ${GREEN}http://localhost:8081/actuator/health${NC}"
    fi
    
    if docker-compose ps 2>/dev/null | grep -q pgadmin; then
        echo -e "🗄️  pgAdmin:     ${GREEN}http://localhost:5050${NC} (admin@mtmx.com / admin123)"
    fi
    
    echo
    
    if [ "$mode" != "dev" ] && [ "$mode" != "development" ]; then
        print_status "Test verileri otomatik olarak yüklendi:"
        echo "  • MT103: 4 örnek mesaj"
        echo "  • MT102: 3 örnek mesaj" 
        echo "  • MT202: 4 örnek mesaj"
        echo "  • MT202COV: 3 örnek mesaj"
        echo "  • MT203: 4 örnek mesaj"
        echo
    fi
}

# Function to show help
show_help() {
    echo "MT-MX Project Startup Script"
    echo
    echo "Kullanım:"
    echo "  $0 [mode] [options]"
    echo
    echo "Modlar:"
    echo "  production, prod    Production mode (tüm servisler Docker'da)"
    echo "  dev, development    Development mode (sadece DB Docker'da)"
    echo "  tools              Production + pgAdmin"
    echo
    echo "Seçenekler:"
    echo "  -h, --help         Bu yardım mesajını göster"
    echo "  -c, --clean        Başlamadan önce temizle"
    echo "  -s, --status       Sadece durum göster"
    echo "  --stop             Servisleri durdur"
    echo
    echo "Örnekler:"
    echo "  $0                 # Production mode'da başlat"
    echo "  $0 dev             # Development mode'da başlat (sadece DB)"
    echo "  $0 tools --clean   # Tools mode'da temizleyerek başlat"
    echo "  $0 --status        # Durum göster"
    echo "  $0 --stop          # Servisleri durdur"
}

# Function to stop services
stop_services() {
    print_status "Servisler durduruluyor..."
    docker-compose down --remove-orphans
    print_success "Tüm servisler durduruldu ✓"
}

# Main execution
main() {
    local mode="production"
    local clean=false
    local status_only=false
    local stop_only=false
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -c|--clean)
                clean=true
                shift
                ;;
            -s|--status)
                status_only=true
                shift
                ;;
            --stop)
                stop_only=true
                shift
                ;;
            production|prod|dev|development|tools)
                mode=$1
                shift
                ;;
            *)
                print_error "Bilinmeyen parametre: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # Execute based on flags
    if [ "$stop_only" = true ]; then
        check_docker
        check_docker_compose
        stop_services
        exit 0
    fi
    
    if [ "$status_only" = true ]; then
        show_status "$mode"
        exit 0
    fi
    
    # Main startup sequence
    echo "🚀 MT-MX Project Startup"
    echo "========================"
    
    check_docker
    check_docker_compose
    check_and_kill_ports
    
    if [ "$clean" = true ]; then
        cleanup
    fi
    
    if start_services "$mode"; then
        if wait_for_services "$mode"; then
            show_status "$mode"
            print_success "🎉 MT-MX projesi başarıyla başlatıldı!"
            print_status "Servisleri durdurmak için: $0 --stop"
        else
            print_error "Servisler tam olarak başlatılamadı!"
            exit 1
        fi
    else
        print_error "Servisler başlatılamadı!"
        exit 1
    fi
}

# Run main function with all arguments
main "$@" 
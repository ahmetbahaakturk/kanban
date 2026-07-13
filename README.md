# Kanban

Kanban, paylaşılabilir bir `publicId` ile board oluşturmayı ve mevcut boardları açmayı sağlayan basit bir görev yönetimi uygulamasıdır. Kartlar listeler arasında sürüklenebilir, aynı liste içinde sıralanabilir, düzenlenebilir ve silinebilir.

## Teknolojiler

- Backend: Java 25, Spring Boot, Spring Data JPA ve Flyway
- Frontend: React, Vite ve dnd-kit
- Veritabanı: PostgreSQL; ortam değişkenleri verilmezse yerel geliştirmede H2
- Konteyner: Docker Compose, Nginx

## Tek komutla çalıştırma

Bilgisayarda Docker Desktop'ın çalışıyor olması yeterlidir. Varsayılan `5173`, `8080` ve `5432` portlarının kullanılabilir olduğundan emin olduktan sonra proje dizininde şu komutu çalıştırın:

```bash
docker compose up --build
```

Bu komut PostgreSQL, backend ve frontend servislerini sırasıyla hazırlar ve çalıştırır.

- Uygulama: http://localhost:5173
- Backend API: http://localhost:8080
- PostgreSQL: `localhost:5432`

Servisleri arka planda çalıştırmak için:

```bash
docker compose up --build -d
```

Logları takip etmek için:

```bash
docker compose logs -f
```

Servisleri durdurmak için:

```bash
docker compose down
```

PostgreSQL verileri `postgres_data` adlı Docker volume'unda saklanır. Servisleri durdurmak verileri silmez. Veritabanını da tamamen sıfırlamak için aşağıdaki komut kullanılabilir:

```bash
docker compose down -v
```

> Bu komut PostgreSQL içindeki tüm proje verilerini kalıcı olarak siler.

## Ayarlar

Compose varsayılan olarak aşağıdaki değerleri kullanır:

| Değişken | Varsayılan değer | Açıklama |
| --- | --- | --- |
| `POSTGRES_DB` | `kanban` | Veritabanı adı |
| `POSTGRES_USER` | `kanban` | Veritabanı kullanıcısı |
| `POSTGRES_PASSWORD` | `kanban` | Veritabanı parolası |
| `POSTGRES_PORT` | `5432` | Bilgisayardan erişilen PostgreSQL portu |
| `BACKEND_PORT` | `8080` | Backend portu |
| `FRONTEND_PORT` | `5173` | Uygulamanın tarayıcı portu |

Bu değerler proje kökünde oluşturulacak bir `.env` dosyasıyla değiştirilebilir:

```dotenv
POSTGRES_DB=kanban
POSTGRES_USER=kanban
POSTGRES_PASSWORD=guclu-bir-parola
POSTGRES_PORT=5432
BACKEND_PORT=8080
FRONTEND_PORT=5173
```

Varsayılan portlardan biri başka bir uygulama tarafından kullanılıyorsa yalnızca ilgili port değerini `.env` içinde değiştirmek yeterlidir.

## Docker olmadan yerel geliştirme

Backend için herhangi bir veritabanı ortam değişkeni verilmezse uygulama otomatik olarak bellek içi H2 veritabanını kullanır:

```powershell
.\mvnw spring-boot:run
```

Frontend ayrı bir terminalde çalıştırılabilir:

```powershell
cd frontend
npm ci
npm run dev
```

Vite geliştirme sunucusu `/api` isteklerini `http://localhost:8080` adresindeki backende yönlendirir.

## Test ve kontroller

Backend testleri:

```powershell
.\mvnw test
```

Frontend kontrolleri:

```powershell
cd frontend
npm run lint
npm run build
```

Flyway, backend her başladığında gerekli veritabanı şemasını otomatik olarak oluşturur veya günceller.

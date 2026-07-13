# Kanban

Paylaşılabilir board isimleriyle çalışan, sade ve hızlı bir Kanban uygulaması.

Bu projede kullanıcı bir board adı belirleyerek yeni board oluşturabilir veya daha önce oluşturulmuş bir boardu aynı adla açabilir. Board içindeki kartlar listeler arasında sürüklenebilir, aynı liste içinde sıralanabilir, düzenlenebilir ve silinebilir.

## Özellikler

- Public board adıyla board oluşturma ve açma
- Varsayılan Kanban listeleri: Backlog, To Do, In Progress, Done
- Kart oluşturma, düzenleme ve silme
- Kartları liste içinde ve listeler arasında sürükle-bırak ile sıralama
- Son gezilen boardları tarayıcı local storage üzerinde saklama
- Flyway ile otomatik veritabanı şeması kurulumu
- Docker Compose ile frontend, backend ve PostgreSQL'i tek komutla çalıştırma
- Ortam değişkenleri yoksa backend tarafında H2 ile hızlı lokal geliştirme

## Teknolojiler

| Katman | Teknolojiler |
| --- | --- |
| Backend | Java 25, Spring Boot, Spring Data JPA, Flyway |
| Frontend | React, Vite, dnd-kit |
| Veritabanı | PostgreSQL, H2 |
| Çalıştırma | Docker Compose, Nginx |

## Hızlı Başlangıç

Docker Desktop çalışıyorsa proje kök dizininde tek komut yeterli:

```bash
docker compose up --build
```

Servisler ayağa kalktıktan sonra:

- Uygulama: http://localhost:5173
- Backend API: http://localhost:8080
- PostgreSQL: `localhost:5432`

Hazır örnek board için uygulamada `mock` boardunu açabilirsiniz.

## Docker Komutları

Arka planda çalıştırmak için:

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

Veritabanı verileri `postgres_data` Docker volume içinde saklanır. Veritabanını tamamen sıfırlamak için:

```bash
docker compose down -v
```

Bu komut PostgreSQL içindeki proje verilerini kalıcı olarak siler.

## Ortam Değişkenleri

Compose varsayılan olarak aşağıdaki değerleri kullanır:

| Değişken | Varsayılan | Açıklama |
| --- | --- | --- |
| `POSTGRES_DB` | `kanban` | PostgreSQL veritabanı adı |
| `POSTGRES_USER` | `kanban` | PostgreSQL kullanıcı adı |
| `POSTGRES_PASSWORD` | `kanban` | PostgreSQL parolası |
| `POSTGRES_PORT` | `5432` | Bilgisayardan erişilen PostgreSQL portu |
| `BACKEND_PORT` | `8080` | Backend portu |
| `FRONTEND_PORT` | `5173` | Frontend portu |

Bu değerleri değiştirmek için proje kökünde `.env` dosyası oluşturabilirsiniz:

```dotenv
POSTGRES_DB=kanban
POSTGRES_USER=kanban
POSTGRES_PASSWORD=guclu-bir-parola
POSTGRES_PORT=5432
BACKEND_PORT=8080
FRONTEND_PORT=5173
```

Örneğin `5173` portu doluysa sadece `FRONTEND_PORT` değerini değiştirmeniz yeterlidir.

## Docker Olmadan Çalıştırma

Backend, veritabanı ortam değişkenleri verilmezse otomatik olarak H2 kullanır.

Backend:

```powershell
.\mvnw spring-boot:run
```

Frontend:

```powershell
cd frontend
npm ci
npm run dev
```

Vite geliştirme sunucusu `/api` isteklerini `http://localhost:8080` adresindeki backende yönlendirir.

## Faydalı Endpointler

| Metot | Endpoint | Açıklama |
| --- | --- | --- |
| `POST` | `/boards` | Yeni board oluşturur |
| `GET` | `/boards/{publicId}` | Boardu listeleri ve kartlarıyla getirir |
| `POST` | `/cards` | Bir task list içine kart oluşturur |
| `PUT` | `/cards/{cardId}` | Kart başlık, metin ve rengini günceller |
| `DELETE` | `/cards/{cardId}` | Kartı siler |
| `PUT` | `/task-lists/order` | Task listler içindeki kart sırasını günceller |

Frontend Docker ile çalışırken bu istekler `/api` prefix'i üzerinden Nginx tarafından backende iletilir.

## Test ve Kontroller

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

## Proje Yapısı

```text
.
├── compose.yaml
├── Dockerfile
├── frontend
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src
└── src
    ├── main
    │   ├── java/com/kanban
    │   └── resources/db/migration
    └── test
```

## Notlar

- Veritabanı şeması Flyway migrationları ile kurulur.
- Docker Compose akışında kalıcı veritabanı PostgreSQL'tir.
- Lokal backend geliştirme sırasında env verilmezse H2 kullanıldığı için uygulamayı yeniden başlatınca veriler sıfırlanabilir.

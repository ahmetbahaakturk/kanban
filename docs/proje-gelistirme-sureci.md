# Proje Geliştirme Süreci ve Kazanımlar

Bu doküman, Kanban projesini geliştirirken izlediğim adımları, aldığım teknik kararların nedenlerini ve proje sonunda edindiğim deneyimleri anlatır.

## İzlenen Geliştirme Adımları

1. Projenin başında SQL şemasını ve Java tarafındaki entity karşılıklarını tasarladım. Amaç, uygulamanın veri modelini en baştan netleştirmekti.

2. Sırayla `Board`, `TaskList` ve `Card` tablolarını oluşturdum. İlk tasarımda `TaskList` ile `Card` arasında bir ara tablo düşündüm. Ancak bu yapının gereksiz karmaşıklık getireceğini fark ettim. Bu nedenle kartın bağlı olduğu task list ile sıralama bilgisini doğrudan `Card` tablosunda tutmaya karar verdim. Aynı listede iki kartın aynı konuma sahip olmamasını sağlamak için `task_list_id` ve `position` alanlarına unique constraint ekledim.

3. Board oluşturulurken isim girilmezse rastgele bir `publicId` üretme fikrini denedim. Bu yapının yardımcı fonksiyonunu ve testlerini düşündüm; ancak sonraki tasarım kararımla birlikte bu yaklaşımdan vazgeçtim.

4. Rastgele `publicId` üretimi yerine board adını tamamen kullanıcının belirlemesine karar verdim. Böylece board adresleri daha anlamlı ve paylaşılabilir hale geldi.

5. Bir board oluşturulduğunda varsayılan task listlerin de otomatik olarak oluşmasını sağladım: Backlog, To Do, In Progress ve Done.

6. İlk iki gün boyunca kodu sadeleştirdim, tekrar eden yapıları azalttım ve backend akışlarını daha okunur hale getirdim.

7. Ana ekran ve board ekranını React.js ile oluşturmaya karar verdim. Frontend tarafında yapay zeka desteğinden yararlanarak hızlı prototipleme ve arayüz geliştirme deneyimi edindim.

8. Card oluşturma endpointlerini yazdım. Böylece kullanıcı, belirttiği task list içine yeni kart ekleyebildi.

9. Kart sıralama yapısını ilk başta tek kart üzerinden kurguladım: frontend sadece taşınan kartın yeni listesini ve yeni konumunu gönderecekti. Daha sonra tüm etkilenen listelerin güncel halini göndermenin bu proje için daha uygun olduğuna karar verdim. Backend, gelen sıralamayı doğrular; kartları önce geçici negatif pozisyonlara taşır; ardından yeni liste ve pozisyonlarını yazar. Bu iki aşamalı akış, unique constraint ile sıralama çakışmasını önler.

10. Vibe coding yaklaşımıyla frontend geliştirmeye devam ederek kalan arayüzü tamamladım.

11. Frontend tarafında sade, anlaşılır ve işlevsel bir tasarım oluşturmaya odaklandım. Kart oluşturma, düzenleme, silme, sürükle-bırak ve son gezilen boardları görüntüleme gibi temel akışlar eklendi.

12. Projenin kurulumu, çalıştırılması ve endpointleri için README dokümantasyonunu ekledim. Ayrıca API'leri kolay denemek için Postman collection dosyasını projeye dahil ettim.

## Projeden Anladıklarım

Bu projede backend kodunu, frontend bilgim sınırlı olsa bile yapay zeka desteğiyle işlevsel bir arayüze bağlayabildiğimi deneyimledim. API tasarımı, React bileşenleri ve kullanıcı akışlarını birlikte düşünmenin uygulamayı daha kullanılabilir hale getirdiğini gördüm.

Özellikle listeler arası kart sıralama yapısında tasarım kararının önemini öğrendim. Tek bir kartın hareket bilgisini göndermek daha küçük bir istek oluşturur; ancak sıralama kurallarını backend tarafında daha karmaşık hale getirir. Etkilenen listelerin son halini göndermek ise frontendden daha fazla veri gönderilmesini gerektirir, fakat backendin sonucu doğrulamasını ve kalıcı sıralamayı uygulamasını daha net hale getirir.

Unique constraint kullanımının veri tutarlılığı için değerli olduğunu, fakat sıralama değişikliklerinde ara durumlarda çakışma yaratabileceğini öğrendim. Bu nedenle kartları önce geçici negatif pozisyonlara alıp, daha sonra gerçek pozisyonlara taşıyan iki aşamalı kayıt yapısı uyguladım.

Genel olarak bu proje; veri modeli tasarlama, Spring Boot ile API geliştirme, Flyway migration kullanma, React ile arayüz kurma, Docker ile çalıştırma ve teknik kararların uygulamanın karmaşıklığına etkisini anlama konusunda pratik kazanmamı sağladı.

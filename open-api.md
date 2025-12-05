обновлённая документация

text
# SOPDS Java - OpenAPI

## API v1

**URL:** http://localhost:8080/api

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8080/api-docs

---

## Scanner API

### Запустить сканирование

**Endpoint:** `POST /api/scanner/scan`

Запускает сканирование библиотеки в фоновом режиме.

**Пример запроса:**

curl -X POST http://localhost:8080/api/scanner/scan
-H "Content-Type: application/json"

text

**Ответ 200 (успех):**

{
"status": "success",
"message": "Library scan started",
"timestamp": 1733356800000
}

text

**Ответ 400 (сканирование уже идёт):**

{
"status": "error",
"message": "Scan already in progress",
"timestamp": 1733356800000
}

text

---

### Статус сканирования

**Endpoint:** `GET /api/scanner/status`

Возвращает текущий статус сканирования.

**Пример запроса:**

curl -X GET http://localhost:8080/api/scanner/status

text

**Ответ 200:**

{
"isScanning": false,
"timestamp": 1733356800000
}

text

---

## OPDS API

### Скачать книгу

**Endpoint:** `GET /opds/download/{id}/{zip}`

Скачивает файл книги.

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `id` | Long | ID книги |
| `zip` | int | 0 - оригинальный файл, 1 - в ZIP-архиве |

**Пример запроса (оригинальный файл):**

curl -X GET http://localhost:8080/opds/download/485/0
-o book.fb2

text

**Пример запроса (в ZIP):**

curl -X GET http://localhost:8080/opds/download/485/1
-o book.fb2.zip

text

**Ответ 200:** Бинарный файл книги

**Ответ 404:** Книга не найдена

**Ответ 500:** Ошибка чтения файла

---

### Получить обложку

**Endpoint:** `GET /opds/cover/{id}`

Возвращает обложку книги (если есть).

**Пример запроса:**

curl -X GET http://localhost:8080/opds/cover/485
-o cover.jpg

text

**Ответ 200:** Изображение (JPEG/PNG)

**Ответ 404:** Обложка не найдена

---

## Web API (поиск)

### Поиск книг

**Endpoint:** `GET /search/books`

**Параметры:**
| Параметр | Тип | По умолчанию | Описание |
|----------|-----|--------------|----------|
| `searchtype` | String | `m` | Тип поиска: `m` - содержит, `b` - начинается с, `a` - по автору ID, `s` - по серии ID, `g` - по жанру ID, `i` - по ID книги, `d` - дубликаты |
| `searchterms` | String | `""` | Поисковый запрос |
| `page` | int | `1` | Номер страницы |

**Пример запроса:**

Поиск по названию (содержит "война")

curl "http://localhost:8080/search/books?searchtype=m&searchterms=война&page=1"
Книги автора с ID=5

curl "http://localhost:8080/search/books?searchtype=a&searchterms=5"
Книги серии с ID=10

curl "http://localhost:8080/search/books?searchtype=s&searchterms=10"
Книги жанра с ID=3

curl "http://localhost:8080/search/books?searchtype=g&searchterms=3"

text

**Ответ:** HTML-страница со списком книг

---

### Поиск авторов

**Endpoint:** `GET /search/authors`

**Параметры:**
| Параметр | Тип | По умолчанию | Описание |
|----------|-----|--------------|----------|
| `searchtype` | String | `m` | Тип поиска: `m` - содержит, `b` - начинается с, `e` - точное совпадение |
| `searchterms` | String | `""` | Поисковый запрос |
| `page` | int | `1` | Номер страницы |

**Пример запроса:**

curl "http://localhost:8080/search/authors?searchtype=m&searchterms=толстой"

text

---

### Поиск серий

**Endpoint:** `GET /search/series`

**Параметры:** аналогично поиску авторов

**Пример запроса:**

curl "http://localhost:8080/search/series?searchtype=b&searchterms=гарри"

text

---

## Reader API

### Читать книгу

**Endpoint:** `GET /read/{id}`

Открывает книгу для чтения в браузере.

**Параметры:**
| Параметр | Тип | По умолчанию | Описание |
|----------|-----|--------------|----------|
| `id` | Long | - | ID книги |
| `page` | int | `1` | Номер страницы/главы |

**Пример запроса:**

curl "http://localhost:8080/read/485?page=1"

text

**Ответ:** HTML-страница с содержимым книги

**Поддерживаемые форматы:**
- FB2 — полная поддержка с навигацией по главам
- TXT — постраничное отображение
- PDF/DJVU — встроенный просмотрщик браузера

---

## Каталоги

### Просмотр каталога

**Endpoint:** `GET /catalog`

**Параметры:**
| Параметр | Тип | По умолчанию | Описание |
|----------|-----|--------------|----------|
| `cat` | Long | `null` | ID каталога (null = корень) |
| `page` | int | `1` | Номер страницы |

**Пример запроса:**

Корневой каталог

curl "http://localhost:8080/catalog"
Подкаталог с ID=5

curl "http://localhost:8080/catalog?cat=5&page=1"

text

---

## Навигация по алфавиту

### Книги по буквам

**Endpoint:** `GET /book`

**Параметры:**
| Параметр | Тип | По умолчанию | Описание |
|----------|-----|--------------|----------|
| `lang` | int | `0` | Код языка (0=все, 1=ru, 2=en) |
| `chars` | String | `""` | Префикс названия |

**Пример:**

curl "http://localhost:8080/book?lang=1&chars=А"

text

### Авторы по буквам

**Endpoint:** `GET /author`

### Серии по буквам

**Endpoint:** `GET /series`

### Жанры

**Endpoint:** `GET /genre`

**Параметры:**
| Параметр | Тип | По умолчанию | Описание |
|----------|-----|--------------|----------|
| `section` | int | `0` | ID секции (0 = список секций) |

**Пример:**

Список секций жанров

curl "http://localhost:8080/genre"
Жанры в секции

curl "http://localhost:8080/genre?section=5"

text
undefined
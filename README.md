# KotlinKT2

Данный API был создан с целью закрыть КТ2 на 25 баллов по предмету "Кроссплатформенная разработка на языке Kotlin"

## Что нужно для работы с API
* Скачать Docker Desktop [здесь](https://www.docker.com/products/docker-desktop/)
* Скачать файл docker-compose.yml из репозитория 
* Рекомендуется скачать [postman](https://www.postman.com/downloads/) для тестирования API

## Как запустить API 

### На Windows
1. Скачайте файл docker-compose.yml 
2. В командой строке перейдите на директорию с файлом docker-compose.yml
3. Пропишите в этой директории:
```
docker compose up
```
чтобы запустить работу API

4. Чтобы выключить работу API пропишите в той же директории:
```
docker compose down
```

## Описание всех запросов

### Создать пользователя 
```
 POST /users
```
Тело запроса:
```
{
    "name": String,
    "email": String,
    "password" String
}
```
Ответ:
```
{
    "id": Intger,
    "name": String,
    "email": String
}
```

### Получить всех пользователей
```
 GET /users
```

Ответ:
```
[
  {
      "id": Intger,
      "name": String,
      "email": String
  },
  {
      "id": Intger,
      "name": String,
      "email": String
  },
  {
  ...
  }
]
```

### Получить пользователя по id

#### Способ 1 

```
 GET /users/{id}
```

Ответ:
```
{
    "id": Integer,
    "name": String,
    "email": String
}
```

#### Способ 2

```
 GET /users?username={username}
```

Ответ:
```
{
    "id": Integer,
    "name": String,
    "email": String
}
```

### Залогиниться
```
 POST /users/login
```
Тело запроса:
```
{
    "name": String,
    "password" String
}
```
Ответ:
```
{
   "token": String
}
```

### Изменить пользовательские данные

В заголовок запроса Authorization нужно передать значение: Bearer {token} (полученные из запроса users/login, без кавычек)

```
 PUT /users/{id}
```
Тело запроса:
```
{
    "name": String,
    "email": String
    "password" String
}
```
Ответ:
```
{
   "status": "updated"
}
```

### Удалить пользователя

В заголовок запроса Authorization нужно передать значение: Bearer {token} (полученные из запроса users/login, без кавычек)

```
 DELETE /users/{id}
```
Тело запроса:
Ответ:
```
{
    "status": "deleted",
    "user": "{username}"
}
```

## Дополнительно
* Пароли хранятся зашифрованными
  [тут должно быть изображение](https://i.ibb.co/8nbffrv3/image.png)
* Изменять и удалять пользовательские данные можно только свои. При попытке изменить или удалить чужие данные, будет выведен определенный текст в ответ
* Для авторизации используется JWT
* Для шифрования паролей используется BCrypt

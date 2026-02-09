# Лабораторная работа №1

---

## Требования по выполнению:
Вам дан шаблон репозитория. Необходимо __склонировать__ его и выполнить задания, описанные ниже. При этом как-то изменять/удалять уже существующие файлы ```запрещено```.  

**На каждое задание необходимо сделать отдельный kotlin-файл формата: ```./src/task<номер задания>.kt```**  
**В качестве решения приложите ссылку на PR в вашем репозитории**

---
### Инструкция по компиляции и запуску:
* Чтобы скомпилировать какой-либо из файлов, используйте команду `kotlinc <_имя исходника_> -include-runtime -d <_имя полученного jar файла_>`
* Для запуска используйте команду `java -jar <_имя jar файла_>`
---



## Задание 1
### _Напишите приложение, которое на вход через параметры командной строки получит текст и выдаст список слов, разделенных пробельными символами._
### Пример:
```bash
$ java -jar task1.jar the quick brown fox jumps over the lazy dog  
the  
quick  
brown  
fox  
jumps  
over  
the  
lazy  
dog
```

## Задание 2
### _Слова из предыдущего задания должны быть отсортированы по алфавиту_
### Пример:
```bash
$ java -jar task2.jar the quick brown fox jumps over the lazy dog  
brown  
dog  
fox  
jumps  
lazy  
over  
quick  
the  
the
```

## Задание 3
### _Слова из предыдущего задания должны быть уникальными_
### Пример:
```bash
$ java -jar task3.jar the quick brown fox jumps over the lazy dog  
brown  
dog  
fox  
jumps  
lazy  
over  
quick  
the
```

## Задание 4
### _После каждого слова выведите количество его повторений_
### Пример:
```bash
$ java -jar task4.jar the quick brown fox jumps over the lazy dog  
brown 1  
dog 1  
fox 1  
jumps 1  
lazy 1  
over 1  
quick 1  
the 2
```

## Задание 5
### _Список должен быть отсортирован сначала по количеству повторений в обратном порядке, в случае одинакового количества – по алфавиту_
### Пример:
```bash
$ java -jar task5.jar the quick brown fox jumps over the lazy dog  
the 2  
brown 1  
dog 1  
fox 1  
jumps 1  
lazy 1  
over 1  
quick 1
```


## Задание 6
### _Если вашему приложению из задания 5 не передано ни одного параметра, то считайте список слов для сортировки из стандартного потока ввода (stdin), чтобы ваше приложение дополнительно могло запускаться вот так:_ `echo "the quick brown fox jumps over the lazy dog" | java -jar yourapp.jar`
### Пример:
```bash
$ echo "the quick brown fox jumps over the lazy dog" | java -jar task6.jar  
the 2  
brown 1  
dog 1  
fox 1  
jumps 1  
lazy 1  
over 1  
quick 1
```
*Или же*
```bash
$ java -jar task6.jar the quick brown fox jumps over the lazy dog  
the 2  
brown 1  
dog 1  
fox 1  
jumps 1  
lazy 1  
over 1  
quick 1
```  
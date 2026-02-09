fun main(args: Array<String>) {
    // Если аргументы переданы – используем их, иначе читаем все слова из stdin
    val words: List<String> =
        if (args.isNotEmpty()) {
            args.toList()
        } else {
            generateSequence(::readLine)
                .flatMap { line -> line.split("\\s+".toRegex()).asSequence() }
                .filter { it.isNotEmpty() }
                .toList()
        }

    val counts = words.groupingBy { it }.eachCount()

    counts.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key }
        )
        .forEach { (word, count) ->
            println("$word $count")
        }
}



fun main(args: Array<String>) {
    val counts = args.groupingBy { it }.eachCount()

    counts.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key }
        )
        .forEach { (word, count) ->
            println("$word $count")
        }
}


